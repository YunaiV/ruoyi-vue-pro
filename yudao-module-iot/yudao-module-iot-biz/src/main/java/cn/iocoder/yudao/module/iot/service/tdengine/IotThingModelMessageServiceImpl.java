package cn.iocoder.yudao.module.iot.service.tdengine;

import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.device.IotDeviceStatusUpdateReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDataDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.FieldParser;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.TdFieldDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.TdTableDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.ThingModelMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.thinkmodelfunction.IotThinkModelFunctionDO;
import cn.iocoder.yudao.module.iot.dal.redis.deviceData.DeviceDataRedisDAO;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceStatusEnum;
import cn.iocoder.yudao.module.iot.enums.product.IotProductFunctionTypeEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.thinkmodelfunction.IotThinkModelFunctionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 物模型消息 Service 实现类
 */
@Slf4j
@Service
public class IotThingModelMessageServiceImpl implements IotThingModelMessageService {

    @Value("${spring.datasource.dynamic.datasource.tdengine.url}")
    private String url;

    @Resource
    private IotThinkModelFunctionService iotThinkModelFunctionService;
    @Resource
    private IotDeviceService iotDeviceService;
    @Resource
    private TdEngineTableService tdEngineTableService;
    @Resource
    private TdEngineSuperTableService tdEngineSuperTableService;
    @Resource
    private TdEngineDataWriterService tdEngineDataWriterService;

    @Resource
    private DeviceDataRedisDAO deviceDataRedisDAO;

    @Override
    @TenantIgnore
    public void saveThingModelMessage(IotDeviceDO device, ThingModelMessage thingModelMessage) {
        // 判断设备状态，如果为未激活状态，创建数据表
        if (IotDeviceStatusEnum.INACTIVE.getStatus().equals(device.getStatus())) {
            // 创建设备数据表
            createDeviceTable(device.getDeviceType(), device.getProductKey(), device.getDeviceName(), device.getDeviceKey());
            // 更新设备状态
            IotDeviceStatusUpdateReqVO updateReqVO = new IotDeviceStatusUpdateReqVO();
            updateReqVO.setId(device.getId());
            updateReqVO.setStatus(IotDeviceStatusEnum.ONLINE.getStatus());
            iotDeviceService.updateDeviceStatus(updateReqVO);
        }

        // 获取设备属性
        Map<String, Object> params = thingModelMessage.dataToMap();

        // 物模型校验，过滤非物模型属性
        List<IotThinkModelFunctionDO> functionList = iotThinkModelFunctionService
                .getThinkModelFunctionListByProductKey(thingModelMessage.getProductKey())
                .stream()
                .filter(function -> IotProductFunctionTypeEnum.PROPERTY.getType().equals(function.getType()))
                .toList();

        if (functionList.isEmpty()) {
            return;
        }

        // 获取属性标识符集合
        Set<String> propertyIdentifiers = functionList.stream()
                .map(IotThinkModelFunctionDO::getIdentifier)
                .collect(Collectors.toSet());

        Map<String, IotThinkModelFunctionDO> functionMap = functionList.stream()
                .collect(Collectors.toMap(IotThinkModelFunctionDO::getIdentifier, function -> function));

        // 过滤并收集有效的属性字段
        List<TdFieldDO> schemaFieldValues = new ArrayList<>();
        schemaFieldValues.add(new TdFieldDO("time", thingModelMessage.getTime()));
        params.forEach((key, val) -> {
            if (propertyIdentifiers.contains(key)) {
                schemaFieldValues.add(new TdFieldDO(key.toLowerCase(), val));
                // 缓存设备属性
                setDeviceDataCache(device, functionMap.get(key), val, thingModelMessage.getTime());
            }
        });
        if (schemaFieldValues.size() == 1) {
            return;
        }

        // 构建并保存设备属性数据
        tdEngineDataWriterService.insertData(TdTableDO.builder().build()
                .setDataBaseName(getDatabaseName())
                .setTableName(getDeviceTableName(device.getProductKey(), device.getDeviceName()))
                .setColumns(schemaFieldValues));
    }

    /**
     * 缓存设备属性
     *
     * @param device                  设备信息
     * @param iotThinkModelFunctionDO 物模型属性
     * @param val                     属性值
     * @param time                    时间
     */
    private void setDeviceDataCache(IotDeviceDO device, IotThinkModelFunctionDO iotThinkModelFunctionDO, Object val, Long time) {
        IotDeviceDataDO deviceData = IotDeviceDataDO.builder()
                .productKey(device.getProductKey())
                .deviceName(device.getDeviceName())
                .identifier(iotThinkModelFunctionDO.getIdentifier())
                .value(val != null ? val.toString() : null)
                .updateTime(DateUtil.toLocalDateTime(new Date(time)))
                .deviceId(device.getId())
                .thinkModelFunctionId(iotThinkModelFunctionDO.getId())
                .name(iotThinkModelFunctionDO.getName())
                .dataType(iotThinkModelFunctionDO.getProperty().getDataType().getType())
                .build();
        deviceDataRedisDAO.set(deviceData);
    }

    /**
     * 创建设备数据表
     *
     * @param deviceType 设备类型
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @param deviceKey  设备 Key
     */
    private void createDeviceTable(Integer deviceType, String productKey, String deviceName, String deviceKey) {
        String superTableName = getProductPropertySTableName(deviceType, productKey);
        String dataBaseName = getDatabaseName();

        List<Map<String, Object>> maps = tdEngineSuperTableService.describeSuperTable(new TdTableDO(dataBaseName, superTableName));
        List<TdFieldDO> tagsFieldValues = new ArrayList<>();

        if (maps != null) {
            List<Map<String, Object>> taggedNotesList = maps.stream()
                    .filter(map -> "TAG".equals(map.get("note")))
                    .toList();

            tagsFieldValues = FieldParser.parse(taggedNotesList.stream()
                    .map(map -> List.of(map.get("field"), map.get("type"), map.get("length")))
                    .collect(Collectors.toList()));

            for (TdFieldDO tagsFieldValue : tagsFieldValues) {
                switch (tagsFieldValue.getFieldName()) {
                    case "product_key" -> tagsFieldValue.setFieldValue(productKey);
                    case "device_key" -> tagsFieldValue.setFieldValue(deviceKey);
                    case "device_name" -> tagsFieldValue.setFieldValue(deviceName);
                    case "device_type" -> tagsFieldValue.setFieldValue(deviceType);
                }
            }
        }

        // 创建设备数据表
        String tableName = getDeviceTableName(productKey, deviceName);
        tdEngineTableService.createTable(TdTableDO.builder().build()
                .setDataBaseName(dataBaseName)
                .setSuperTableName(superTableName)
                .setTableName(tableName)
                .setTags(tagsFieldValues));
    }

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    private String getDatabaseName() {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    /**
     * 获取产品属性表名
     *
     * @param deviceType 设备类型
     * @param productKey 产品 Key
     * @return 产品属性表名
     */
    private static String getProductPropertySTableName(Integer deviceType, String productKey) {
        return switch (deviceType) {
            case 1 -> String.format("gateway_sub_%s", productKey).toLowerCase();
            case 2 -> String.format("gateway_%s", productKey).toLowerCase();
            default -> String.format("device_%s", productKey).toLowerCase();
        };
    }

    /**
     * 获取设备表名
     *
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @return 设备表名
     */
    private static String getDeviceTableName(String productKey, String deviceName) {
        return String.format("device_%s_%s", productKey.toLowerCase(), deviceName.toLowerCase());
    }
}