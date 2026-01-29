package cn.iocoder.yudao.module.iot.service.device.property;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDevicePropertyHistoryListReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDevicePropertyRespVO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDevicePropertyDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.model.dataType.ThingModelDateOrTextDataSpecs;
import cn.iocoder.yudao.module.iot.dal.redis.device.DevicePropertyRedisDAO;
import cn.iocoder.yudao.module.iot.dal.redis.device.DeviceReportTimeRedisDAO;
import cn.iocoder.yudao.module.iot.dal.redis.device.DeviceServerIdRedisDAO;
import cn.iocoder.yudao.module.iot.dal.tdengine.IotDevicePropertyMapper;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotDataSpecsDataTypeEnum;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotThingModelTypeEnum;
import cn.iocoder.yudao.module.iot.framework.tdengine.core.TDengineTableField;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import cn.iocoder.yudao.module.iot.service.thingmodel.IotThingModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.getBigDecimal;

/**
 * IoT 设备【属性】数据 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class IotDevicePropertyServiceImpl implements IotDevicePropertyService {

    /**
     * 物模型的数据类型，与 TDengine 数据类型的映射关系
     *
     * @see <a href="https://docs.taosdata.com/reference/taos-sql/data-type/">TDEngine 数据类型</a>
     */
    private static final Map<String, String> TYPE_MAPPING = MapUtil.<String, String>builder()
            .put(IotDataSpecsDataTypeEnum.INT.getDataType(), TDengineTableField.TYPE_INT)
            .put(IotDataSpecsDataTypeEnum.FLOAT.getDataType(), TDengineTableField.TYPE_FLOAT)
            .put(IotDataSpecsDataTypeEnum.DOUBLE.getDataType(), TDengineTableField.TYPE_DOUBLE)
            .put(IotDataSpecsDataTypeEnum.ENUM.getDataType(), TDengineTableField.TYPE_TINYINT)
            .put(IotDataSpecsDataTypeEnum.BOOL.getDataType(), TDengineTableField.TYPE_TINYINT)
            .put(IotDataSpecsDataTypeEnum.TEXT.getDataType(), TDengineTableField.TYPE_VARCHAR)
            .put(IotDataSpecsDataTypeEnum.DATE.getDataType(), TDengineTableField.TYPE_TIMESTAMP)
            .put(IotDataSpecsDataTypeEnum.STRUCT.getDataType(), TDengineTableField.TYPE_VARCHAR)
            .put(IotDataSpecsDataTypeEnum.ARRAY.getDataType(), TDengineTableField.TYPE_VARCHAR)
            .build();

    @Resource
    private IotThingModelService thingModelService;
    @Resource
    @Lazy  // 延迟加载，解决循环依赖
    private IotProductService productService;
    @Resource
    @Lazy  // 延迟加载，解决循环依赖
    private IotDeviceService deviceService;

    @Resource
    private DevicePropertyRedisDAO deviceDataRedisDAO;
    @Resource
    private DeviceReportTimeRedisDAO deviceReportTimeRedisDAO;
    @Resource
    private DeviceServerIdRedisDAO deviceServerIdRedisDAO;

    @Resource
    private IotDevicePropertyMapper devicePropertyMapper;

    // ========== 设备属性相关操作 ==========

    @Override
    public void defineDevicePropertyData(Long productId) {
        // 1.1 查询产品和物模型
        IotProductDO product = productService.validateProductExists(productId);
        List<IotThingModelDO> thingModels = filterList(thingModelService.getThingModelListByProductId(productId),
                thingModel -> IotThingModelTypeEnum.PROPERTY.getType().equals(thingModel.getType()));
        // 1.2 解析 DB 里的字段
        List<TDengineTableField> oldFields = new ArrayList<>();
        try {
            oldFields.addAll(devicePropertyMapper.getProductPropertySTableFieldList(product.getId()));
        } catch (Exception e) {
            if (!e.getMessage().contains("Table does not exist")) {
                throw e;
            }
        }

        // 2.1 情况一：如果是新增的时候，需要创建表
        List<TDengineTableField> newFields = buildTableFieldList(thingModels);
        if (CollUtil.isEmpty(oldFields)) {
            if (CollUtil.isEmpty(newFields)) {
                log.info("[defineDevicePropertyData][productId({}) 没有需要定义的属性]", productId);
                return;
            }
            devicePropertyMapper.createProductPropertySTable(product.getId(), newFields);
            return;
        }
        // 2.2 情况二：如果是修改的时候，需要更新表
        devicePropertyMapper.alterProductPropertySTable(product.getId(), oldFields, newFields);
    }

    private List<TDengineTableField> buildTableFieldList(List<IotThingModelDO> thingModels) {
        return convertList(thingModels, thingModel -> {
            TDengineTableField field = new TDengineTableField(
                    StrUtil.toUnderlineCase(thingModel.getIdentifier()), // TDengine 字段默认都是小写
                    TYPE_MAPPING.get(thingModel.getProperty().getDataType()));
            String dataType = thingModel.getProperty().getDataType();
            if (Objects.equals(dataType, IotDataSpecsDataTypeEnum.TEXT.getDataType())) {
                field.setLength(((ThingModelDateOrTextDataSpecs) thingModel.getProperty().getDataSpecs()).getLength());
            } else if (ObjectUtils.equalsAny(dataType, IotDataSpecsDataTypeEnum.STRUCT.getDataType(),
                    IotDataSpecsDataTypeEnum.ARRAY.getDataType())) {
                field.setLength(TDengineTableField.LENGTH_VARCHAR);
            }
            return field;
        });
    }

    @Override
    @SuppressWarnings("PatternVariableCanBeUsed")
    public void saveDeviceProperty(IotDeviceDO device, IotDeviceMessage message) {
        if (!(message.getParams() instanceof Map)) {
            log.error("[saveDeviceProperty][消息内容({}) 的 data 类型不正确]", message);
            return;
        }
        Map<?, ?> params = (Map<?, ?>) message.getParams();
        if (CollUtil.isEmpty(params)) {
            log.error("[saveDeviceProperty][消息内容({}) 的 data 为空]", message);
            return;
        }

        // 1. 根据物模型，拼接合法的属性
        // TODO @芋艿：【待定 004】赋能后，属性到底以 thingModel 为准（ik），还是 db 的表结构为准（tl）？
        List<IotThingModelDO> thingModels = thingModelService.getThingModelListByProductIdFromCache(device.getProductId());
        Map<String, Object> properties = new HashMap<>();
        params.forEach((key, value) -> {
            IotThingModelDO thingModel = CollUtil.findOne(thingModels, o -> o.getIdentifier().equals(key));
            if (thingModel == null || thingModel.getProperty() == null) {
                log.error("[saveDeviceProperty][消息({}) 的属性({}) 不存在]", message, key);
                return;
            }
            String dataType = thingModel.getProperty().getDataType();
            if (ObjectUtils.equalsAny(dataType,
                    IotDataSpecsDataTypeEnum.STRUCT.getDataType(), IotDataSpecsDataTypeEnum.ARRAY.getDataType())) {
                // 特殊：STRUCT 和 ARRAY 类型，在 TDengine 里，有没对应数据类型，只能通过 JSON 来存储
                properties.put((String) key, JsonUtils.toJsonString(value));
            } else if (IotDataSpecsDataTypeEnum.INT.getDataType().equals(dataType)) {
                properties.put((String) key, Convert.toInt(value));
            } else if (IotDataSpecsDataTypeEnum.FLOAT.getDataType().equals(dataType)) {
                properties.put((String) key, Convert.toFloat(value));
            } else if (IotDataSpecsDataTypeEnum.DOUBLE.getDataType().equals(dataType)) {
                properties.put((String) key, Convert.toDouble(value));
            }  else if (IotDataSpecsDataTypeEnum.BOOL.getDataType().equals(dataType)) {
                properties.put((String) key, Convert.toByte(value));
            }  else {
                properties.put((String) key, value);
            }
        });
        if (CollUtil.isEmpty(properties)) {
            log.error("[saveDeviceProperty][消息({}) 没有合法的属性]", message);
        } else {
            // 2.1 保存设备属性【数据】
            devicePropertyMapper.insert(device, properties, LocalDateTimeUtil.toEpochMilli(message.getReportTime()));

            // 2.2 保存设备属性【日志】
            Map<String, IotDevicePropertyDO> properties2 = convertMap(properties.entrySet(), Map.Entry::getKey, entry ->
                    IotDevicePropertyDO.builder().value(entry.getValue()).updateTime(message.getReportTime()).build());
            deviceDataRedisDAO.putAll(device.getId(), properties2);
        }

        // 2.3 提取 GeoLocation 并更新设备定位
        // 为什么 properties 为空，也要执行定位更新？因为可能上报的属性里，没有合法属性，但是包含 GeoLocation 定位属性
        extractAndUpdateDeviceLocation(device, (Map<?, ?>) message.getParams());
    }

    @Override
    public Map<String, IotDevicePropertyDO> getLatestDeviceProperties(Long deviceId) {
        return deviceDataRedisDAO.get(deviceId);
    }

    @Override
    public List<IotDevicePropertyRespVO> getHistoryDevicePropertyList(IotDevicePropertyHistoryListReqVO listReqVO) {
        try {
            return devicePropertyMapper.selectListByHistory(listReqVO);
        } catch (Exception exception) {
            if (exception.getMessage().contains("Table does not exist")) {
                return Collections.emptyList();
            }
            throw exception;
        }
    }

    // ========== 设备时间相关操作 ==========

    @Override
    public Set<Long> getDeviceIdListByReportTime(LocalDateTime maxReportTime) {
        return deviceReportTimeRedisDAO.range(maxReportTime);
    }

    @Override
    @Async
    public void updateDeviceReportTimeAsync(Long id, LocalDateTime reportTime) {
        deviceReportTimeRedisDAO.update(id, reportTime);
    }

    @Override
    public void updateDeviceServerIdAsync(Long id, String serverId) {
        if (StrUtil.isEmpty(serverId)) {
            return;
        }
        deviceServerIdRedisDAO.update(id, serverId);
    }

    @Override
    public String getDeviceServerId(Long id) {
        return deviceServerIdRedisDAO.get(id);
    }

    // ========== 设备定位相关操作 ==========

    /**
     * 从属性中提取 GeoLocation 并更新设备定位
     *
     * @see <a href="https://help.aliyun.com/zh/iot/user-guide/device-geolocation">阿里云规范</a>
     * GeoLocation 结构体包含：Longitude, Latitude, Altitude, CoordinateSystem
     */
    private void extractAndUpdateDeviceLocation(IotDeviceDO device, Map<?, ?> params) {
        // 1. 解析 GeoLocation 经纬度坐标
        BigDecimal[] location = parseGeoLocation(params);
        if (location == null) {
            return;
        }

        // 2. 更新设备定位
        deviceService.updateDeviceLocation(device, location[0], location[1]);
        log.info("[extractAndUpdateGeoLocation][设备({}) 定位更新: lng={}, lat={}]",
                device.getId(), location[0], location[1]);
    }

    /**
     * 从属性参数中解析 GeoLocation，返回经纬度坐标数组 [longitude, latitude]
     *
     * @param params 属性参数
     * @return [经度, 纬度]，解析失败返回 null
     */
    @SuppressWarnings("unchecked")
    private BigDecimal[] parseGeoLocation(Map<?, ?> params) {
        if (params == null) {
            return null;
        }
        // 1. 查找 GeoLocation 属性（标识符为 GeoLocation 或 geoLocation）
        Object geoValue = params.get("GeoLocation");
        if (geoValue == null) {
            geoValue = params.get("geoLocation");
        }
        if (geoValue == null) {
            return null;
        }

        // 2. 转换为 Map
        Map<String, Object> geoLocation = null;
        if (geoValue instanceof Map) {
            geoLocation = (Map<String, Object>) geoValue;
        } else if (geoValue instanceof String) {
            geoLocation = JsonUtils.parseObject((String) geoValue, Map.class);
        }
        if (geoLocation == null) {
            return null;
        }

        // 3. 提取经纬度（支持阿里云命名规范：首字母大写）
        BigDecimal longitude = getBigDecimal(geoLocation, "Longitude");
        if (longitude == null) {
            longitude = getBigDecimal(geoLocation, "longitude");
        }
        BigDecimal latitude = getBigDecimal(geoLocation, "Latitude");
        if (latitude == null) {
            latitude = getBigDecimal(geoLocation, "latitude");
        }
        if (longitude == null || latitude == null) {
            return null;
        }
        // 校验经纬度范围：经度 -180 到 180，纬度 -90 到 90
        if (longitude.compareTo(BigDecimal.valueOf(-180)) < 0 || longitude.compareTo(BigDecimal.valueOf(180)) > 0
                || latitude.compareTo(BigDecimal.valueOf(-90)) < 0 || latitude.compareTo(BigDecimal.valueOf(90)) > 0) {
            log.warn("[parseGeoLocation][经纬度超出有效范围: lng={}, lat={}]", longitude, latitude);
            return null;
        }
        return new BigDecimal[]{longitude, latitude};
    }

}