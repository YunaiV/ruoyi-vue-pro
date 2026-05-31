package cn.iocoder.yudao.module.iot.service.thingmodel;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.vo.IotThingModelListReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.vo.IotThingModelPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.vo.IotThingModelSaveReqVO;
import cn.iocoder.yudao.module.iot.convert.thingmodel.IotThingModelConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.model.dataType.ThingModelBoolOrEnumDataSpecs;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.model.dataType.ThingModelDataSpecs;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.model.dataType.ThingModelDateOrTextDataSpecs;
import cn.iocoder.yudao.module.iot.dal.mysql.thingmodel.IotThingModelMapper;
import cn.iocoder.yudao.module.iot.dal.redis.RedisKeyConstants;
import cn.iocoder.yudao.module.iot.enums.product.IotProductStatusEnum;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotDataSpecsDataTypeEnum;
import cn.iocoder.yudao.module.iot.framework.tdengine.core.TDengineTableField;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceModbusPointService;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * IoT 产品物模型 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class IotThingModelServiceImpl implements IotThingModelService {

    @Resource
    private IotThingModelMapper thingModelMapper;

    @Resource
    @Lazy // 延迟加载，解决循环依赖
    private IotProductService productService;
    @Resource
    @Lazy // 延迟加载，解决循环依赖
    private IotDeviceModbusPointService deviceModbusPointService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createThingModel(IotThingModelSaveReqVO createReqVO) {
        // 1.1 校验功能标识符在同一产品下是否唯一
        validateIdentifierUnique(null, createReqVO.getProductId(), createReqVO.getIdentifier());
        // 1.2 功能名称在同一产品下是否唯一
        validateNameUnique(createReqVO.getProductId(), createReqVO.getName());
        // 1.3 校验产品状态，发布状态下，不允许新增功能
        validateProductStatus(createReqVO.getProductId());

        // 2. 插入数据库
        IotThingModelDO thingModel = IotThingModelConvert.INSTANCE.convert(createReqVO);
        thingModelMapper.insert(thingModel);

        // 3. 删除缓存
        deleteThingModelListCache(createReqVO.getProductId());
        return thingModel.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateThingModel(IotThingModelSaveReqVO updateReqVO) {
        // 1.1 校验功能是否存在
        validateProductThingModelMapperExists(updateReqVO.getId());
        // 1.2 校验功能标识符是否唯一
        validateIdentifierUnique(updateReqVO.getId(), updateReqVO.getProductId(), updateReqVO.getIdentifier());
        // 1.3 校验产品状态，发布状态下，不允许操作功能
        validateProductStatus(updateReqVO.getProductId());

        // 2. 更新数据库
        IotThingModelDO thingModel = IotThingModelConvert.INSTANCE.convert(updateReqVO);
        thingModelMapper.updateById(thingModel);

        // 3. 同步更新 Modbus 点位的冗余字段（identifier、name）
        deviceModbusPointService.updateDeviceModbusPointByThingModel(
                updateReqVO.getId(), updateReqVO.getIdentifier(), updateReqVO.getName());

        // 4. 删除缓存
        deleteThingModelListCache(updateReqVO.getProductId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteThingModel(Long id) {
        // 1.1 校验功能是否存在
        IotThingModelDO thingModel = thingModelMapper.selectById(id);
        if (thingModel == null) {
            throw exception(THING_MODEL_NOT_EXISTS);
        }
        // 1.2 校验产品状态，发布状态下，不允许操作功能
        validateProductStatus(thingModel.getProductId());

        // 2. 删除功能
        thingModelMapper.deleteById(id);

        // 3. 删除缓存
        deleteThingModelListCache(thingModel.getProductId());
    }

    @Override
    public IotThingModelDO getThingModel(Long id) {
        return thingModelMapper.selectById(id);
    }

    @Override
    public List<IotThingModelDO> getThingModelListByProductId(Long productId) {
        return thingModelMapper.selectListByProductId(productId);
    }

    @Override
    public List<IotThingModelDO> getThingModelListByProductIdAndIdentifiers(Long productId, Collection<String> identifiers) {
        return thingModelMapper.selectListByProductIdAndIdentifiers(productId, identifiers);
    }

    @Override
    public List<IotThingModelDO> getThingModelListByProductIdAndType(Long productId, Integer type) {
        return thingModelMapper.selectListByProductIdAndType(productId, type);
    }

    @Override
    @Cacheable(value = RedisKeyConstants.THING_MODEL_LIST, key = "#productId")
    @TenantIgnore // 忽略租户信息
    public List<IotThingModelDO> getThingModelListByProductIdFromCache(Long productId) {
        return thingModelMapper.selectListByProductId(productId);
    }

    @Override
    public PageResult<IotThingModelDO> getProductThingModelPage(IotThingModelPageReqVO pageReqVO) {
        return thingModelMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotThingModelDO> getThingModelList(IotThingModelListReqVO reqVO) {
        return thingModelMapper.selectList(reqVO);
    }

    @Override
    public void validateThingModelListExists(Long productId, Set<String> identifiers) {
        if (CollUtil.isEmpty(identifiers)) {
            return;
        }
        List<IotThingModelDO> thingModels = thingModelMapper.selectListByProductIdAndIdentifiers(
            productId, identifiers);
        Set<String> foundIdentifiers = convertSet(thingModels, IotThingModelDO::getIdentifier);
        for (String identifier : identifiers) {
            if (!foundIdentifiers.contains(identifier)) {
                throw exception(THING_MODEL_NOT_EXISTS);
            }
        }
    }

    @Override
    public Object convertThingModelPropertyValue(IotThingModelDO thingModel, Object value) {
        if (thingModel == null || thingModel.getProperty() == null || value == null) {
            return null;
        }
        String dataType = thingModel.getProperty().getDataType();
        if (ObjectUtils.equalsAny(dataType,
                IotDataSpecsDataTypeEnum.STRUCT.getDataType(), IotDataSpecsDataTypeEnum.ARRAY.getDataType())) {
            // 特殊：STRUCT 和 ARRAY 类型，在 TDengine 里，没有对应数据类型，只能通过 JSON 来存储
            return convertToVarchar(JsonUtils.toJsonString(value), TDengineTableField.LENGTH_VARCHAR);
        }
        if (IotDataSpecsDataTypeEnum.INT.getDataType().equals(dataType)) {
            return convertToInt(value);
        }
        if (IotDataSpecsDataTypeEnum.FLOAT.getDataType().equals(dataType)) {
            return convertToFloat(value);
        }
        if (IotDataSpecsDataTypeEnum.DOUBLE.getDataType().equals(dataType)) {
            return convertToDouble(value);
        }
        if (IotDataSpecsDataTypeEnum.ENUM.getDataType().equals(dataType)) {
            return convertEnumToTinyInt(thingModel, value);
        }
        if (IotDataSpecsDataTypeEnum.BOOL.getDataType().equals(dataType)) {
            return convertBoolToTinyInt(value);
        }
        if (IotDataSpecsDataTypeEnum.TEXT.getDataType().equals(dataType)) {
            return convertToVarchar(Convert.toStr(value), getTextLength(thingModel));
        }
        if (IotDataSpecsDataTypeEnum.DATE.getDataType().equals(dataType)) {
            return convertToTimestamp(value);
        }
        return null;
    }

    private Integer getTextLength(IotThingModelDO thingModel) {
        ThingModelDataSpecs dataSpecs = thingModel.getProperty().getDataSpecs();
        if (!(dataSpecs instanceof ThingModelDateOrTextDataSpecs)) {
            return null;
        }
        return ((ThingModelDateOrTextDataSpecs) dataSpecs).getLength();
    }

    private String convertToVarchar(String value, Integer length) {
        if (value == null) {
            return null;
        }
        if (length != null && value.getBytes(StandardCharsets.UTF_8).length > length) {
            return null;
        }
        return value;
    }

    private Integer convertToInt(Object value) {
        BigDecimal decimal = convertToBigDecimal(value);
        if (decimal == null) {
            return null;
        }
        try {
            return decimal.intValueExact();
        } catch (ArithmeticException e) {
            return null;
        }
    }

    private Float convertToFloat(Object value) {
        BigDecimal decimal = convertToBigDecimal(value);
        if (decimal == null) {
            return null;
        }
        float result = decimal.floatValue();
        return Float.isFinite(result) ? result : null;
    }

    private Double convertToDouble(Object value) {
        BigDecimal decimal = convertToBigDecimal(value);
        if (decimal == null) {
            return null;
        }
        double result = decimal.doubleValue();
        return Double.isFinite(result) ? result : null;
    }

    private BigDecimal convertToBigDecimal(Object value) {
        if (value instanceof Boolean) {
            return null;
        }
        String text = Convert.toStr(value);
        if (StrUtil.isBlank(text)) {
            return null;
        }
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Byte convertEnumToTinyInt(IotThingModelDO thingModel, Object value) {
        Integer intValue = convertToInt(value);
        if (intValue == null && value instanceof CharSequence) {
            intValue = getEnumValueByName(thingModel, value.toString());
        }
        if (intValue == null || !isTinyInt(intValue)) {
            return null;
        }
        if (CollUtil.isNotEmpty(thingModel.getProperty().getDataSpecsList())
                && getEnumDataSpecsByValue(thingModel, intValue) == null) {
            return null;
        }
        return intValue.byteValue();
    }

    private Integer getEnumValueByName(IotThingModelDO thingModel, String name) {
        ThingModelBoolOrEnumDataSpecs dataSpecs = getEnumDataSpecsByName(thingModel, name);
        return dataSpecs != null ? dataSpecs.getValue() : null;
    }

    private ThingModelBoolOrEnumDataSpecs getEnumDataSpecsByName(IotThingModelDO thingModel, String name) {
        if (CollUtil.isEmpty(thingModel.getProperty().getDataSpecsList())) {
            return null;
        }
        for (ThingModelDataSpecs dataSpecs : thingModel.getProperty().getDataSpecsList()) {
            if (!(dataSpecs instanceof ThingModelBoolOrEnumDataSpecs)) {
                continue;
            }
            ThingModelBoolOrEnumDataSpecs enumDataSpecs = (ThingModelBoolOrEnumDataSpecs) dataSpecs;
            if (StrUtil.equals(enumDataSpecs.getName(), name)) {
                return enumDataSpecs;
            }
        }
        return null;
    }

    private ThingModelBoolOrEnumDataSpecs getEnumDataSpecsByValue(IotThingModelDO thingModel, Integer value) {
        if (CollUtil.isEmpty(thingModel.getProperty().getDataSpecsList())) {
            return null;
        }
        for (ThingModelDataSpecs dataSpecs : thingModel.getProperty().getDataSpecsList()) {
            if (!(dataSpecs instanceof ThingModelBoolOrEnumDataSpecs)) {
                continue;
            }
            ThingModelBoolOrEnumDataSpecs enumDataSpecs = (ThingModelBoolOrEnumDataSpecs) dataSpecs;
            if (Objects.equals(enumDataSpecs.getValue(), value)) {
                return enumDataSpecs;
            }
        }
        return null;
    }

    private Byte convertBoolToTinyInt(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value ? (byte) 1 : (byte) 0;
        }
        if (value instanceof CharSequence) {
            String text = StrUtil.trim(value.toString());
            if (StrUtil.equalsIgnoreCase(text, "true")) {
                return (byte) 1;
            }
            if (StrUtil.equalsIgnoreCase(text, "false")) {
                return (byte) 0;
            }
        }
        Integer intValue = convertToInt(value);
        if (intValue == null || (intValue != 0 && intValue != 1)) {
            return null;
        }
        return intValue.byteValue();
    }

    private boolean isTinyInt(Integer value) {
        return value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE;
    }

    private Long convertToTimestamp(Object value) {
        if (value instanceof LocalDateTime) {
            return LocalDateTimeUtil.toEpochMilli((LocalDateTime) value);
        }
        if (value instanceof LocalDate) {
            return LocalDateTimeUtil.toEpochMilli(((LocalDate) value).atStartOfDay());
        }
        if (value instanceof Instant) {
            return ((Instant) value).toEpochMilli();
        }
        if (value instanceof OffsetDateTime) {
            return ((OffsetDateTime) value).toInstant().toEpochMilli();
        }
        if (value instanceof ZonedDateTime) {
            return ((ZonedDateTime) value).toInstant().toEpochMilli();
        }
        if (value instanceof Date) {
            return ((Date) value).getTime();
        }
        Long timestamp = convertToLong(value);
        if (timestamp != null) {
            return timestamp;
        }
        if (!(value instanceof CharSequence)) {
            return null;
        }
        String text = StrUtil.trim(value.toString());
        if (StrUtil.isBlank(text)) {
            return null;
        }
        try {
            return OffsetDateTime.parse(text).toInstant().toEpochMilli();
        } catch (Exception ignored) {
            // 尝试本地时间格式，例如 yyyy-MM-dd HH:mm:ss
        }
        try {
            return LocalDateTimeUtil.toEpochMilli(LocalDateTimeUtil.parse(text, DatePattern.NORM_DATETIME_PATTERN));
        } catch (Exception ignored) {
            // 尝试其它 Hutool 支持的本地时间格式
        }
        try {
            return LocalDateTimeUtil.toEpochMilli(LocalDateTimeUtils.parse(text));
        } catch (Exception ignored) {
            return null;
        }
    }

    private Long convertToLong(Object value) {
        BigDecimal decimal = convertToBigDecimal(value);
        if (decimal == null) {
            return null;
        }
        try {
            return decimal.longValueExact();
        } catch (ArithmeticException e) {
            return null;
        }
    }

    private void validateProductThingModelMapperExists(Long id) {
        if (thingModelMapper.selectById(id) == null) {
            throw exception(THING_MODEL_NOT_EXISTS);
        }
    }

    private void validateIdentifierUnique(Long id, Long productId, String identifier) {
        // 1. 情况一：创建时校验
        if (id == null) {
            // 1.1 系统保留字段，不能用于标识符定义
            if (StrUtil.equalsAny(identifier, "set", "get", "post", "property", "event", "time", "value")) {
                throw exception(THING_MODEL_IDENTIFIER_INVALID);
            }
            // 1.2 校验唯一
            IotThingModelDO thingModel = thingModelMapper.selectByProductIdAndIdentifier(productId, identifier);
            if (thingModel != null) {
                throw exception(THING_MODEL_IDENTIFIER_EXISTS);
            }
            return;
        }

        // 2. 情况二：更新时校验
        IotThingModelDO thingModel = thingModelMapper.selectByProductIdAndIdentifier(productId, identifier);
        if (thingModel != null && ObjectUtil.notEqual(thingModel.getId(), id)) {
            throw exception(THING_MODEL_IDENTIFIER_EXISTS);
        }
    }

    private void validateProductStatus(Long createReqVO) {
        IotProductDO product = productService.validateProductExists(createReqVO);
        if (Objects.equals(product.getStatus(), IotProductStatusEnum.PUBLISHED.getStatus())) {
            throw exception(PRODUCT_STATUS_NOT_ALLOW_THING_MODEL);
        }
    }

    private void validateNameUnique(Long productId, String name) {
        IotThingModelDO thingModel = thingModelMapper.selectByProductIdAndName(productId, name);
        if (thingModel != null) {
            throw exception(THING_MODEL_NAME_EXISTS);
        }
    }

    private void deleteThingModelListCache(Long productId) {
        // 保证 Spring AOP 触发
        getSelf().deleteThingModelListCache0(productId);
    }

    @CacheEvict(value = RedisKeyConstants.THING_MODEL_LIST, key = "#productId")
    @TenantIgnore // 忽略租户信息
    public void deleteThingModelListCache0(Long productId) {
    }

    private IotThingModelServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
