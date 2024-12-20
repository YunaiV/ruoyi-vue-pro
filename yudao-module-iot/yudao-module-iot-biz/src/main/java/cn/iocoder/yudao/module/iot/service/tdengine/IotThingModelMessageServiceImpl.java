package cn.iocoder.yudao.module.iot.service.tdengine;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.device.IotDeviceStatusUpdateReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDataDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.FieldParser;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.TdFieldDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.TdTableDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.ThingModelMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotProductThingModelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.redis.deviceData.DeviceDataRedisDAO;
import cn.iocoder.yudao.module.iot.dal.tdengine.TdEngineDDLMapper;
import cn.iocoder.yudao.module.iot.dal.tdengine.TdEngineDMLMapper;
import cn.iocoder.yudao.module.iot.enums.IotConstants;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceStatusEnum;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotProductThingModelTypeEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.thingmodel.IotProductThingModelService;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import cn.iocoder.yudao.module.iot.util.IotTdDatabaseUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;

/**
 * 物模型消息 Service 实现类
 */
@Slf4j
@Service
public class IotThingModelMessageServiceImpl implements IotThingModelMessageService {

    private static final String TAG_NOTE = "TAG";
    private static final String NOTE = "note";
    private static final String TIME = "time";
    private static final String DEVICE_KEY = "device_key";
    private static final String DEVICE_NAME = "device_name";
    private static final String PRODUCT_KEY = "product_key";
    private static final String DEVICE_TYPE = "device_type";

    @Value("${spring.datasource.dynamic.datasource.tdengine.url}")
    private String url;

    @Resource
    private IotProductThingModelService iotProductThingModelService;
    @Resource
    private IotDeviceService iotDeviceService;
    @Resource
    private IotProductService productService;

    @Resource
    private TdEngineDDLMapper tdEngineDDLMapper;
    @Resource
    private TdEngineDMLMapper tdEngineDMLMapper;

    @Resource
    private DeviceDataRedisDAO deviceDataRedisDAO;


    // TODO @haohao：这个方法，可以考虑加下 1. 2. 3. 更有层次感
    @Override
    @TenantIgnore
    public void saveThingModelMessage(IotDeviceDO device, ThingModelMessage thingModelMessage) {
        // 1. 判断设备状态，如果为未激活状态，创建数据表并更新设备状态
        if (IotDeviceStatusEnum.INACTIVE.getStatus().equals(device.getStatus())) {
            // 1.1 创建设备表
            createDeviceTable(device.getDeviceType(), device.getProductKey(), device.getDeviceName(), device.getDeviceKey());
            iotDeviceService.updateDeviceStatus(new IotDeviceStatusUpdateReqVO()
                    .setId(device.getId()).setStatus(IotDeviceStatusEnum.ONLINE.getStatus()));
            // 1.2 创建物模型日志设备表
            createThingModelMessageDeviceTable(device.getProductKey(), device.getDeviceName(), device.getDeviceKey());
        }

        // 2. 获取设备属性并进行物模型校验，过滤非物模型属性
        Map<String, Object> params = thingModelMessage.dataToMap();
        List<IotProductThingModelDO> thingModelList = getValidThingModelList(thingModelMessage.getProductKey());
        if (thingModelList.isEmpty()) {
            return;
        }

        // 3. 过滤并收集有效的属性字段，缓存设备属性
        List<TdFieldDO> schemaFieldValues = filterAndCollectValidFields(params, thingModelList, device, thingModelMessage.getTime());
        if (schemaFieldValues.size() == 1) { // 仅有时间字段，无需保存
            return;
        }

        // 4. 构建并保存设备属性数据
        tdEngineDMLMapper.insertData(TdTableDO.builder()
                .dataBaseName(getDatabaseName())
                .tableName(getDeviceTableName(device.getProductKey(), device.getDeviceName()))
                .columns(schemaFieldValues)
                .build());
    }

    private List<IotProductThingModelDO> getValidThingModelList(String productKey) {
        return filterList(iotProductThingModelService.getProductThingModelListByProductKey(productKey),
                thingModel -> IotProductThingModelTypeEnum.PROPERTY.getType().equals(thingModel.getType()));
    }

    @Override
    @TenantIgnore
    public void createSuperTable(Long productId) {
        // 1. 查询产品
        IotProductDO product = productService.getProduct(productId);

        // 2. 获取超级表的名称和数据库名称
        // TODO @alwayssuper：最好 databaseName、superTableName 的处理，放到 tdThinkModelMessageMapper 里。可以考虑，弄个 default 方法
        String databaseName = IotTdDatabaseUtils.getDatabaseName(url);
        String superTableName = IotTdDatabaseUtils.getThingModelMessageSuperTableName(product.getProductKey());

        // 解析物模型，获取字段列表
        List<TdFieldDO> schemaFields = List.of(
                TdFieldDO.builder().fieldName("time").dataType("TIMESTAMP").build(),
                TdFieldDO.builder().fieldName("id").dataType("NCHAR").dataLength(64).build(),
                TdFieldDO.builder().fieldName("sys").dataType("NCHAR").dataLength(2048).build(),
                TdFieldDO.builder().fieldName("method").dataType("NCHAR").dataLength(256).build(),
                TdFieldDO.builder().fieldName("params").dataType("NCHAR").dataLength(2048).build()
        );
        // 设置超级表的标签
        List<TdFieldDO> tagsFields = List.of(
                TdFieldDO.builder().fieldName("device_key").dataType("NCHAR").dataLength(64).build()
        );
        // 3. 创建超级表
        tdEngineDDLMapper.createSuperTable(new TdTableDO(databaseName, superTableName, schemaFields, tagsFields));
    }

    private List<IotProductThingModelDO> getValidFunctionList(String productKey) {
        // TODO @puhui999：使用 convertList 会好点哈
        return iotProductThingModelService
                .getProductThingModelListByProductKey(productKey)
                .stream()
                .filter(function -> IotProductThingModelTypeEnum.PROPERTY.getType().equals(function.getType()))
                .toList();
    }

    private List<TdFieldDO> filterAndCollectValidFields(Map<String, Object> params, List<IotProductThingModelDO> thingModelList, IotDeviceDO device, Long time) {
        // 1. 获取属性标识符集合
        Set<String> propertyIdentifiers = convertSet(thingModelList, IotProductThingModelDO::getIdentifier);

        // 2. 构建属性标识符和属性的映射
        Map<String, IotProductThingModelDO> thingModelMap = convertMap(thingModelList, IotProductThingModelDO::getIdentifier);

        // 3. 过滤并收集有效的属性字段
        List<TdFieldDO> schemaFieldValues = new ArrayList<>();
        schemaFieldValues.add(new TdFieldDO(TIME, time));
        params.forEach((key, val) -> {
            if (propertyIdentifiers.contains(key)) {
                schemaFieldValues.add(new TdFieldDO(key.toLowerCase(), val));
                // 缓存设备属性
                // TODO @haohao：这个缓存的写入，可以使用的时候 cache 么？被动读
                setDeviceDataCache(device, thingModelMap.get(key), val, time);
            }
        });
        return schemaFieldValues;
    }

    /**
     * 缓存设备属性
     *
     * @param device                 设备信息
     * @param iotProductThingModelDO 物模型属性
     * @param val                    属性值
     * @param time                   时间
     */
    private void setDeviceDataCache(IotDeviceDO device, IotProductThingModelDO iotProductThingModelDO, Object val, Long time) {
        IotDeviceDataDO deviceData = IotDeviceDataDO.builder()
                .productKey(device.getProductKey())
                .deviceName(device.getDeviceName())
                .identifier(iotProductThingModelDO.getIdentifier())
                .value(val != null ? val.toString() : null)
                .updateTime(DateUtil.toLocalDateTime(new Date(time)))
                .deviceId(device.getId())
                .thingModelId(iotProductThingModelDO.getId())
                .name(iotProductThingModelDO.getName())
                .dataType(iotProductThingModelDO.getProperty().getDataType())
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
        // 1. 获取超级表名和数据库名
        String superTableName = getProductPropertySTableName(deviceType, productKey);
        String dataBaseName = getDatabaseName();

        // 2. 获取超级表的结构信息
        List<Map<String, Object>> maps = tdEngineDDLMapper.describeSuperTable(new TdTableDO(dataBaseName, superTableName));
        List<TdFieldDO> tagsFieldValues = new ArrayList<>();
        if (maps != null) {
            // 2.1 过滤出 TAG 类型的字段
            List<Map<String, Object>> taggedNotesList = CollectionUtils.filterList(maps, map -> TAG_NOTE.equals(map.get(NOTE)));

            // 2.2 解析字段信息
            tagsFieldValues = FieldParser.parse(taggedNotesList.stream()
                    .map(map -> List.of(map.get("field"), map.get("type"), map.get("length")))
                    .collect(Collectors.toList()));

            // 2.3 设置 TAG 字段的值
            for (TdFieldDO tagsFieldValue : tagsFieldValues) {
                switch (tagsFieldValue.getFieldName()) {
                    case PRODUCT_KEY -> tagsFieldValue.setFieldValue(productKey);
                    case DEVICE_KEY -> tagsFieldValue.setFieldValue(deviceKey);
                    case DEVICE_NAME -> tagsFieldValue.setFieldValue(deviceName);
                    case DEVICE_TYPE -> tagsFieldValue.setFieldValue(deviceType);
                }
            }
        }

        // 3. 创建设备数据表
        String tableName = getDeviceTableName(productKey, deviceName);
        tdEngineDDLMapper.createTable(TdTableDO.builder().build()
                .setDataBaseName(dataBaseName)
                .setSuperTableName(superTableName)
                .setTableName(tableName)
                .setTags(tagsFieldValues));
    }

    /**
     * 创建物模型日志设备数据表
     *
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @param deviceKey  设备 Key
     *
     */
    private void createThingModelMessageDeviceTable(String productKey, String deviceName, String deviceKey){

        // 1. 获取超级表的名称、数据库名称、设备日志表名称
        String databaseName = IotTdDatabaseUtils.getDatabaseName(url);
        String superTableName = IotTdDatabaseUtils.getThingModelMessageSuperTableName(productKey);
        // TODO @alwayssuper：最好 databaseName、superTableName、thinkModelMessageDeviceTableName 的处理，放到 tdThinkModelMessageMapper 里。可以考虑，弄个 default 方法
        String thinkModelMessageDeviceTableName = IotTdDatabaseUtils.getThingModelMessageDeviceTableName(productKey, deviceName);

        // 2. 创建物模型日志设备数据表
//        tdThingModelMessageMapper.createTableWithTag(ThingModelMessageDO.builder().build()
//                .setDataBaseName(databaseName)
//                .setSuperTableName(superTableName)
//                .setTableName(thinkModelMessageDeviceTableName)
//                .setDeviceKey(deviceKey));
    }

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    private String getDatabaseName() {
        return StrUtil.subAfter(url, "/", true);
    }

    /**
     * 获取产品属性表名
     *
     * @param deviceType 设备类型
     * @param productKey 产品 Key
     * @return 产品属性表名
     */
    private static String getProductPropertySTableName(Integer deviceType, String productKey) {
        // TODO @haohao：枚举下，会好点哈。
        return switch (deviceType) {
            case 1 -> String.format(IotConstants.GATEWAY_SUB_STABLE_NAME_FORMAT, productKey).toLowerCase();
            case 2 -> String.format(IotConstants.GATEWAY_STABLE_NAME_FORMAT, productKey).toLowerCase();
            default -> String.format(IotConstants.DEVICE_STABLE_NAME_FORMAT, productKey).toLowerCase();
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
        return String.format(IotConstants.DEVICE_TABLE_NAME_FORMAT, productKey.toLowerCase(), deviceName.toLowerCase());
    }

}