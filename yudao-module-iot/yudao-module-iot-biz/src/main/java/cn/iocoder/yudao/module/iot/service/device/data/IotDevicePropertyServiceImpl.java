package cn.iocoder.yudao.module.iot.service.device.data;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.data.IotDevicePropertyHistoryPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.data.IotDevicePropertyRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.model.dataType.ThingModelDateOrTextDataSpecs;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDevicePropertyDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import cn.iocoder.yudao.module.iot.dal.redis.device.DevicePropertyRedisDAO;
import cn.iocoder.yudao.module.iot.dal.redis.device.DeviceReportTimeRedisDAO;
import cn.iocoder.yudao.module.iot.dal.tdengine.IotDevicePropertyMapper;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotDataSpecsDataTypeEnum;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotThingModelTypeEnum;
import cn.iocoder.yudao.module.iot.framework.tdengine.core.TDengineTableField;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import cn.iocoder.yudao.module.iot.service.thingmodel.IotThingModelService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
     */
    private static final Map<String, String> TYPE_MAPPING = MapUtil.<String, String>builder()
            .put(IotDataSpecsDataTypeEnum.INT.getDataType(), TDengineTableField.TYPE_INT)
            .put(IotDataSpecsDataTypeEnum.FLOAT.getDataType(), TDengineTableField.TYPE_FLOAT)
            .put(IotDataSpecsDataTypeEnum.DOUBLE.getDataType(), TDengineTableField.TYPE_DOUBLE)
            .put(IotDataSpecsDataTypeEnum.ENUM.getDataType(), TDengineTableField.TYPE_TINYINT) // TODO 芋艿：为什么要映射为 TINYINT 的说明？
            .put(IotDataSpecsDataTypeEnum.BOOL.getDataType(), TDengineTableField.TYPE_TINYINT) // TODO 芋艿：为什么要映射为 TINYINT 的说明？
            .put(IotDataSpecsDataTypeEnum.TEXT.getDataType(), TDengineTableField.TYPE_NCHAR)
            .put(IotDataSpecsDataTypeEnum.DATE.getDataType(), TDengineTableField.TYPE_TIMESTAMP)
            .put(IotDataSpecsDataTypeEnum.STRUCT.getDataType(), TDengineTableField.TYPE_NCHAR) // TODO 芋艿：怎么映射！！！！
            .put(IotDataSpecsDataTypeEnum.ARRAY.getDataType(), TDengineTableField.TYPE_NCHAR) // TODO 芋艿：怎么映射！！！！
            .build();

    @Resource
    private IotDeviceService deviceService;
    @Resource
    private IotThingModelService thingModelService;
    @Resource
    private IotProductService productService;

    @Resource
    private DevicePropertyRedisDAO deviceDataRedisDAO;
    @Resource
    private DeviceReportTimeRedisDAO deviceReportTimeRedisDAO;

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
            oldFields.addAll(devicePropertyMapper.getProductPropertySTableFieldList(product.getProductKey()));
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
            devicePropertyMapper.createProductPropertySTable(product.getProductKey(), newFields);
            return;
        }
        // 2.2 情况二：如果是修改的时候，需要更新表
        devicePropertyMapper.alterProductPropertySTable(product.getProductKey(), oldFields, newFields);
    }

    private List<TDengineTableField> buildTableFieldList(List<IotThingModelDO> thingModels) {
        return convertList(thingModels, thingModel -> {
            TDengineTableField field = new TDengineTableField(
                    StrUtil.toUnderlineCase(thingModel.getIdentifier()), // TDengine 字段默认都是小写
                    TYPE_MAPPING.get(thingModel.getProperty().getDataType()));
            if (thingModel.getProperty().getDataType().equals(IotDataSpecsDataTypeEnum.TEXT.getDataType())) {
                field.setLength(((ThingModelDateOrTextDataSpecs) thingModel.getProperty().getDataSpecs()).getLength());
            }
            return field;
        });
    }

    @Override
    @TenantIgnore
    public void saveDeviceProperty(IotDeviceMessage message) {
        if (!(message.getData() instanceof Map)) {
            log.error("[saveDeviceProperty][消息内容({}) 的 data 类型不正确]", message);
            return;
        }
        // 1. 获得设备信息
        IotDeviceDO device = deviceService.getDeviceByProductKeyAndDeviceNameFromCache(message.getProductKey(), message.getDeviceName());
        if (device == null) {
            log.error("[saveDeviceProperty][消息({}) 对应的设备不存在]", message);
            return;
        }

        // 2. 根据物模型，拼接合法的属性
        // TODO @芋艿：【待定 004】赋能后，属性到底以 thingModel 为准（ik），还是 db 的表结构为准（tl）？
        List<IotThingModelDO> thingModels = thingModelService.getThingModelListByProductKeyFromCache(device.getProductKey());
        Map<String, Object> properties = new HashMap<>();
        ((Map<?, ?>) message.getData()).forEach((key, value) -> {
            if (CollUtil.findOne(thingModels, thingModel -> thingModel.getIdentifier().equals(key)) == null) {
                log.error("[saveDeviceProperty][消息({}) 的属性({}) 不存在]", message, key);
                return;
            }
            properties.put((String) key, value);
        });
        if (CollUtil.isEmpty(properties)) {
            log.error("[saveDeviceProperty][消息({}) 没有合法的属性]", message);
            return;
        }

        // 3.1 保存设备属性【数据】
        devicePropertyMapper.insert(device, properties,
                LocalDateTimeUtil.toEpochMilli(message.getReportTime()));

        // 3.2 保存设备属性【日志】
        deviceDataRedisDAO.putAll(message.getDeviceKey(), convertMap(properties.entrySet(), Map.Entry::getKey,
                entry -> IotDevicePropertyDO.builder().value(entry.getValue()).updateTime(message.getReportTime()).build()));
    }

    @Override
    public Map<String, IotDevicePropertyDO> getLatestDeviceProperties(Long deviceId) {
        // 获取设备信息
        IotDeviceDO device = deviceService.validateDeviceExists(deviceId);

        // 获得设备属性
        return deviceDataRedisDAO.get(device.getDeviceKey());
    }

    @Override
    public PageResult<IotDevicePropertyRespVO> getHistoryDevicePropertyPage(IotDevicePropertyHistoryPageReqVO pageReqVO) {
        // 获取设备信息
        IotDeviceDO device = deviceService.validateDeviceExists(pageReqVO.getDeviceId());
        pageReqVO.setDeviceKey(device.getDeviceKey());

        try {
            IPage<IotDevicePropertyRespVO> page = devicePropertyMapper.selectPageByHistory(
                    new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize()), pageReqVO);
            return new PageResult<>(page.getRecords(), page.getTotal());
        } catch (Exception exception) {
            if (exception.getMessage().contains("Table does not exist")) {
                return PageResult.empty();
            }
            throw exception;
        }
    }

    // ========== 设备时间相关操作 ==========

    @Override
    public Set<String> getDeviceKeysByReportTime(LocalDateTime maxReportTime) {
        return deviceReportTimeRedisDAO.range(maxReportTime);
    }

    @Override
    @Async
    public void updateDeviceReportTimeAsync(String deviceKey, LocalDateTime reportTime) {
        deviceReportTimeRedisDAO.update(deviceKey, reportTime);
    }

}