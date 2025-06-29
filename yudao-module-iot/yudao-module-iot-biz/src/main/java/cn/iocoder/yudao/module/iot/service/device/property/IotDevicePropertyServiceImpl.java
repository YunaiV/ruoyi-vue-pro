package cn.iocoder.yudao.module.iot.service.device.property;

import cn.hutool.core.collection.CollUtil;
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
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import cn.iocoder.yudao.module.iot.service.thingmodel.IotThingModelService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;

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
    public void saveDeviceProperty(IotDeviceDO device, IotDeviceMessage message) {
        if (!(message.getParams() instanceof Map)) {
            log.error("[saveDeviceProperty][消息内容({}) 的 data 类型不正确]", message);
            return;
        }

        // 1. 根据物模型，拼接合法的属性
        // TODO @芋艿：【待定 004】赋能后，属性到底以 thingModel 为准（ik），还是 db 的表结构为准（tl）？
        List<IotThingModelDO> thingModels = thingModelService.getThingModelListByProductIdFromCache(device.getProductId());
        Map<String, Object> properties = new HashMap<>();
        ((Map<?, ?>) message.getParams()).forEach((key, value) -> {
            IotThingModelDO thingModel = CollUtil.findOne(thingModels, o -> o.getIdentifier().equals(key));
            if (thingModel == null || thingModel.getProperty() == null) {
                log.error("[saveDeviceProperty][消息({}) 的属性({}) 不存在]", message, key);
                return;
            }
            if (ObjectUtils.equalsAny(thingModel.getProperty().getDataType(),
                    IotDataSpecsDataTypeEnum.STRUCT.getDataType(), IotDataSpecsDataTypeEnum.ARRAY.getDataType())) {
                // 特殊：STRUCT 和 ARRAY 类型，在 TDengine 里，有没对应数据类型，只能通过 JSON 来存储
                properties.put((String) key, JsonUtils.toJsonString(value));
            } else {
                properties.put((String) key, value);
            }
        });
        if (CollUtil.isEmpty(properties)) {
            log.error("[saveDeviceProperty][消息({}) 没有合法的属性]", message);
            return;
        }

        // 2.1 保存设备属性【数据】
        devicePropertyMapper.insert(device, properties, LocalDateTimeUtil.toEpochMilli(message.getReportTime()));

        // 2.2 保存设备属性【日志】
        Map<String, IotDevicePropertyDO> properties2 = convertMap(properties.entrySet(), Map.Entry::getKey, entry ->
                IotDevicePropertyDO.builder().value(entry.getValue()).updateTime(message.getReportTime()).build());
        deviceDataRedisDAO.putAll(device.getId(), properties2);
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

}