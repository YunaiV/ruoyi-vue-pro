package cn.iocoder.yudao.module.iot.service.tdengine;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.IotDeviceStatusUpdateReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.FieldParser;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.TableDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.TdFieldDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.ThingModelMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.thinkmodelfunction.IotThinkModelFunctionDO;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceStatusEnum;
import cn.iocoder.yudao.module.iot.enums.product.IotProductFunctionTypeEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.thinkmodelfunction.IotThinkModelFunctionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private IotTdEngineService iotTdEngineService;

    @Override
    @TenantIgnore
    public void saveThingModelMessage(IotDeviceDO device, ThingModelMessage thingModelMessage) {
        // 判断设备状态，如果为未激活状态，创建数据表
        if (device.getStatus().equals(0)) {
            // 创建设备数据表
            createDeviceTable(device.getDeviceType(), device.getProductKey(), device.getDeviceName(), device.getDeviceKey());
            // 更新设备状态
            IotDeviceStatusUpdateReqVO updateReqVO = new IotDeviceStatusUpdateReqVO();
            updateReqVO.setId(device.getId());
            updateReqVO.setStatus(IotDeviceStatusEnum.ONLINE.getStatus());
            iotDeviceService.updateDeviceStatus(updateReqVO);
        }

        // 1. 获取设备属性
        Map<String, Object> params = thingModelMessage.dataToMap();

        // 2. 物模型校验，过滤非物模型属性
        List<IotThinkModelFunctionDO> thinkModelFunctionListByProductKey = iotThinkModelFunctionService.getThinkModelFunctionListByProductKey(thingModelMessage.getProductKey());

        // 2.1 筛选是属性 IotProductFunctionTypeEnum
        thinkModelFunctionListByProductKey.removeIf(iotThinkModelFunctionDO -> !iotThinkModelFunctionDO.getType().equals(IotProductFunctionTypeEnum.PROPERTY.getType()));
        if (thinkModelFunctionListByProductKey.isEmpty()) {
            return;
        }
        // 2.2 获取属性名称
        Map<String, String> thingModelProperties = thinkModelFunctionListByProductKey.stream().collect(Collectors.toMap(IotThinkModelFunctionDO::getIdentifier, IotThinkModelFunctionDO::getName));

        // 4. 保存属性记录
        List<TdFieldDO> schemaFieldValues = new ArrayList<>();

        // 1. 设置字段名
        schemaFieldValues.add(new TdFieldDO("time", thingModelMessage.getTime()));

        // 2. 遍历新属性
        params.forEach((key, val) -> {
            if (thingModelProperties.containsKey(key)) {
                schemaFieldValues.add(new TdFieldDO(key.toLowerCase(), val));
            }
        });

        // 3. 保存设备属性
        TableDO tableData = new TableDO();
        tableData.setDataBaseName(url.substring(url.lastIndexOf("/") + 1));
        tableData.setSuperTableName(getProductPropertySTableName(device.getDeviceType(), device.getProductKey()));
        tableData.setTableName("device_" + device.getProductKey().toLowerCase() + "_" + device.getDeviceName().toLowerCase());
        tableData.setSchemaFieldValues(schemaFieldValues);

        // 4. 保存数据
        iotTdEngineService.insertData(tableData);
    }

    private void createDeviceTable(Integer deviceType, String productKey, String deviceName, String deviceKey) {
        List<TdFieldDO> tagsFieldValues = new ArrayList<>();
        String SuperTableName = getProductPropertySTableName(deviceType, productKey);
        List<Map<String, Object>> maps = iotTdEngineService.describeSuperTable(url.substring(url.lastIndexOf("/") + 1), SuperTableName);
        if (maps != null) {
            List<Map<String, Object>> taggedNotesList = maps.stream().filter(map -> "TAG".equals(map.get("note"))).toList();
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

        // 1. 创建设备数据表
        String tableName = "device_" + productKey.toLowerCase() + "_" + deviceName.toLowerCase();
        TableDO tableDto = new TableDO();
        tableDto.setDataBaseName(url.substring(url.lastIndexOf("/") + 1));
        tableDto.setSuperTableName(SuperTableName);
        tableDto.setTableName(tableName);
        tableDto.setTagsFieldValues(tagsFieldValues);
        iotTdEngineService.createTable(tableDto);
    }

    static String getProductPropertySTableName(Integer deviceType, String productKey) {
        return switch (deviceType) {
            case 1 -> String.format("gateway_sub_%s", productKey).toLowerCase();
            case 2 -> String.format("gateway_%s", productKey).toLowerCase();
            default -> String.format("device_%s", productKey).toLowerCase();
        };
    }
}