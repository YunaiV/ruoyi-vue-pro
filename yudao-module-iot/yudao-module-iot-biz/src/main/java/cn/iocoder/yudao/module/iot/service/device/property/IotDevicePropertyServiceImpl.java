package cn.iocoder.yudao.module.iot.service.device.property;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDevicePropertyHistoryListReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDevicePropertyRespVO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDevicePropertyDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import cn.iocoder.yudao.module.iot.dal.redis.device.DevicePropertyRedisDAO;
import cn.iocoder.yudao.module.iot.dal.redis.device.DeviceReportTimeRedisDAO;
import cn.iocoder.yudao.module.iot.dal.redis.device.DeviceServerIdRedisDAO;
import cn.iocoder.yudao.module.iot.dal.storage.IotDevicePropertyDAO;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotThingModelTypeEnum;
import cn.iocoder.yudao.module.iot.framework.storage.core.util.IotStorageFieldUtils;
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

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.getBigDecimal;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.DEVICE_PROPERTY_HISTORY_TIMES_INVALID;

/**
 * IoT 设备【属性】数据 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class IotDevicePropertyServiceImpl implements IotDevicePropertyService {

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
    private IotDevicePropertyDAO devicePropertyDAO;

    // ========== 设备属性相关操作 ==========

    @Override
    public void defineDevicePropertyData(Long productId) {
        // 1. 查询产品和物模型
        IotProductDO product = productService.validateProductExists(productId);
        List<IotThingModelDO> thingModels = filterList(thingModelService.getThingModelListByProductId(productId),
                thingModel -> IotThingModelTypeEnum.PROPERTY.getType().equals(thingModel.getType()));
        // 2. 定义存储结构：字段映射、差异比较、建表改表，由存储 DAO 实现
        devicePropertyDAO.defineProductPropertyTable(product.getId(), thingModels);
    }

    @Override
    public void deleteDevicePropertyData(Long productId) {
        // 删除存储结构：TDengine、IoTDB 的 DDL 差异，由存储 DAO 实现
        devicePropertyDAO.deleteProductPropertyTable(productId);
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
        Map<String, Object> properties = new LinkedHashMap<>();
        params.forEach((key, value) -> {
            if (!(key instanceof CharSequence)) {
                log.error("[saveDeviceProperty][消息({}) 的属性 key({}) 类型不正确]", message, key);
                return;
            }
            if (value == null) {
                log.warn("[saveDeviceProperty][消息({}) 的属性({}) 值为空，跳过]", message, key);
                return;
            }
            // 忽略大小写匹配物模型，避免设备上报的 key 与 identifier 大小写不一致导致丢失
            IotThingModelDO thingModel = CollUtil.findOne(thingModels,
                    o -> StrUtil.equalsIgnoreCase(o.getIdentifier(), (CharSequence) key));
            if (thingModel == null || thingModel.getProperty() == null) {
                log.error("[saveDeviceProperty][消息({}) 的属性({}) 不存在]", message, key);
                return;
            }
            String identifier = thingModel.getIdentifier(); // 统一以物模型 identifier 作为 key，避免大小写问题
            Object convertedValue = convertPropertyValue(message, thingModel, value);
            if (convertedValue != null) {
                properties.put(identifier, convertedValue);
            }
        });
        if (CollUtil.isEmpty(properties)) {
            log.error("[saveDeviceProperty][消息({}) 没有合法的属性]", message);
        } else {
            // 2.1 保存设备属性【数据】
            devicePropertyDAO.insert(device, properties,
                    System.currentTimeMillis(),
                    LocalDateTimeUtil.toEpochMilli(message.getReportTime()));

            // 2.2 保存设备属性【日志】
            Map<String, IotDevicePropertyDO> properties2 = convertMap(properties.entrySet(), Map.Entry::getKey, entry ->
                    IotDevicePropertyDO.builder().value(entry.getValue()).updateTime(message.getReportTime()).build());
            deviceDataRedisDAO.putAll(device.getId(), properties2);
        }

        // 2.3 提取 GeoLocation 并更新设备定位
        // 为什么 properties 为空，也要执行定位更新？因为可能上报的属性里，没有合法属性，但是包含 GeoLocation 定位属性
        extractAndUpdateDeviceLocation(device, (Map<?, ?>) message.getParams());
    }

    private Object convertPropertyValue(IotDeviceMessage message, IotThingModelDO thingModel, Object value) {
        String identifier = thingModel.getIdentifier();
        String dataType = thingModel.getProperty().getDataType();
        try {
            Object convertedValue = thingModelService.convertThingModelPropertyValue(thingModel, value);
            if (convertedValue == null) {
                log.warn("[saveDeviceProperty][消息({}) 的属性({}) 值({}) 无法转换为类型({})，跳过]",
                        message, identifier, value, dataType);
            }
            return convertedValue;
        } catch (Exception e) {
            log.error("[saveDeviceProperty][消息({}) 的属性({}) 值({}) 转换为类型({}) 异常，跳过]",
                    message, identifier, value, dataType, e);
            return null;
        }
    }

    @Override
    public Map<String, IotDevicePropertyDO> getLatestDeviceProperties(Long deviceId) {
        return deviceDataRedisDAO.get(deviceId);
    }

    @Override
    public List<IotDevicePropertyRespVO> getHistoryDevicePropertyList(IotDevicePropertyHistoryListReqVO listReqVO) {
        // 1.1 校验时间范围：必须包含开始、结束两个时间；任一为空时，无法确定范围，返回空
        LocalDateTime[] times = listReqVO.getTimes();
        if (times == null || times.length != 2) {
            throw exception(DEVICE_PROPERTY_HISTORY_TIMES_INVALID);
        }
        if (times[0] == null || times[1] == null) {
            return Collections.emptyList();
        }
        // 1.2 校验属性标识符：无法作为存储字段的标识符，不会有历史数据
        if (!IotStorageFieldUtils.isPropertyFieldName(listReqVO.getIdentifier())) {
            return Collections.emptyList();
        }
        // 1.3 校验设备：按当前租户查询，且过滤已删除的设备；不存在时，不查询历史数据
        IotDeviceDO device = deviceService.getDevice(listReqVO.getDeviceId());
        if (device == null) {
            return Collections.emptyList();
        }

        // 2. 查询历史数据
        return devicePropertyDAO.selectHistoryList(device.getProductId(), listReqVO);
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
