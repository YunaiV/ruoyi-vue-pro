package cn.iocoder.yudao.module.iot.service.thingmodel;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.model.ThingModelEvent;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.model.ThingModelParam;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.model.ThingModelService;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.vo.IotThingModelListReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.vo.IotThingModelPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.vo.IotThingModelSaveReqVO;
import cn.iocoder.yudao.module.iot.convert.thingmodel.IotThingModelConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import cn.iocoder.yudao.module.iot.dal.mysql.thingmodel.IotThingModelMapper;
import cn.iocoder.yudao.module.iot.dal.redis.RedisKeyConstants;
import cn.iocoder.yudao.module.iot.enums.product.IotProductStatusEnum;
import cn.iocoder.yudao.module.iot.enums.thingmodel.*;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
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
    private IotProductService productService;

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

        // 3. 如果创建的是属性，需要更新默认的事件和服务
        if (Objects.equals(createReqVO.getType(), IotThingModelTypeEnum.PROPERTY.getType())) {
            createDefaultEventsAndServices(createReqVO.getProductId(), createReqVO.getProductKey());
        }

        // 4. 删除缓存
        deleteThingModelListCache(createReqVO.getProductKey());
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

        // 3. 如果更新的是属性，需要更新默认的事件和服务
        if (Objects.equals(updateReqVO.getType(), IotThingModelTypeEnum.PROPERTY.getType())) {
            createDefaultEventsAndServices(updateReqVO.getProductId(), updateReqVO.getProductKey());
        }

        // 4. 删除缓存
        deleteThingModelListCache(updateReqVO.getProductKey());
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

        // 3. 如果删除的是属性，需要更新默认的事件和服务
        if (Objects.equals(thingModel.getType(), IotThingModelTypeEnum.PROPERTY.getType())) {
            createDefaultEventsAndServices(thingModel.getProductId(), thingModel.getProductKey());
        }

        // 4. 删除缓存
        deleteThingModelListCache(thingModel.getProductKey());
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
    @Cacheable(value = RedisKeyConstants.THING_MODEL_LIST, key = "#productKey")
    @TenantIgnore // 忽略租户信息，跨租户 productKey 是唯一的
    public List<IotThingModelDO> getThingModelListByProductKeyFromCache(String productKey) {
        return thingModelMapper.selectListByProductKey(productKey);
    }

    @Override
    public PageResult<IotThingModelDO> getProductThingModelPage(IotThingModelPageReqVO pageReqVO) {
        return thingModelMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotThingModelDO> getThingModelList(IotThingModelListReqVO reqVO) {
        return thingModelMapper.selectList(reqVO);
    }

    /**
     * 校验功能是否存在
     *
     * @param id 功能编号
     */
    private void validateProductThingModelMapperExists(Long id) {
        if (thingModelMapper.selectById(id) == null) {
            throw exception(THING_MODEL_NOT_EXISTS);
        }
    }

    private void validateIdentifierUnique(Long id, Long productId, String identifier) {
        // 1.0 情况一：创建时校验
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

        // 2.0 情况二：更新时校验
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

    /**
     * 创建默认的事件和服务
     *
     * @param productId  产品编号
     * @param productKey 产品标识
     */
    public void createDefaultEventsAndServices(Long productId, String productKey) {
        // 1. 获取当前属性列表
        List<IotThingModelDO> properties = thingModelMapper
                .selectListByProductIdAndType(productId, IotThingModelTypeEnum.PROPERTY.getType());

        // 2. 生成新的事件和服务列表
        List<IotThingModelDO> newThingModels = new ArrayList<>();
        // 2.1 生成属性上报事件
        ThingModelEvent propertyPostEvent = generatePropertyPostEvent(properties);
        if (propertyPostEvent != null) {
            newThingModels.add(buildEventThingModel(productId, productKey, propertyPostEvent, "属性上报事件"));
        }
        // 2.2 生成属性设置服务
        ThingModelService propertySetService = generatePropertySetService(properties);
        if (propertySetService != null) {
            newThingModels.add(buildServiceThingModel(productId, productKey, propertySetService, "属性设置服务"));
        }
        // 2.3 生成属性获取服务
        ThingModelService propertyGetService = generatePropertyGetService(properties);
        if (propertyGetService != null) {
            newThingModels.add(buildServiceThingModel(productId, productKey, propertyGetService, "属性获取服务"));
        }

        // 3.1 获取数据库中的默认的旧事件和服务列表
        List<IotThingModelDO> oldThingModels = thingModelMapper.selectListByProductIdAndIdentifiersAndTypes(
                productId,
                Arrays.asList("post", "set", "get"),
                Arrays.asList(IotThingModelTypeEnum.EVENT.getType(), IotThingModelTypeEnum.SERVICE.getType())
        );
        // 3.2 创建默认的事件和服务
        createDefaultEventsAndServices(oldThingModels, newThingModels);
    }

    /**
     * 创建默认的事件和服务
     */
    private void createDefaultEventsAndServices(List<IotThingModelDO> oldThingModels,
                                                List<IotThingModelDO> newThingModels) {
        // 使用 diffList 方法比较新旧列表
        List<List<IotThingModelDO>> diffResult = diffList(oldThingModels, newThingModels,
                (oldVal, newVal) -> {
                    // 继续使用 identifier 和 type 进行比较：这样可以准确地匹配对应的功能对象。
                    boolean same = Objects.equals(oldVal.getIdentifier(), newVal.getIdentifier())
                            && Objects.equals(oldVal.getType(), newVal.getType());
                    if (same) {
                        newVal.setId(oldVal.getId()); // 设置编号
                    }
                    return same;
                });
        // 批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffResult.get(0))) {
            thingModelMapper.insertBatch(diffResult.get(0));
        }
        if (CollUtil.isNotEmpty(diffResult.get(1))) {
            thingModelMapper.updateBatch(diffResult.get(1));
        }
        if (CollUtil.isNotEmpty(diffResult.get(2))) {
            thingModelMapper.deleteByIds(convertSet(diffResult.get(2), IotThingModelDO::getId));
        }
    }

    /**
     * 构建事件功能对象
     */
    private IotThingModelDO buildEventThingModel(Long productId, String productKey,
                                                 ThingModelEvent event, String description) {
        return new IotThingModelDO().setProductId(productId).setProductKey(productKey)
                .setIdentifier(event.getIdentifier()).setName(event.getName()).setDescription(description)
                .setType(IotThingModelTypeEnum.EVENT.getType()).setEvent(event);
    }

    /**
     * 构建服务功能对象
     */
    private IotThingModelDO buildServiceThingModel(Long productId, String productKey,
                                                   ThingModelService service, String description) {
        return new IotThingModelDO().setProductId(productId).setProductKey(productKey)
                .setIdentifier(service.getIdentifier()).setName(service.getName()).setDescription(description)
                .setType(IotThingModelTypeEnum.SERVICE.getType()).setService(service);
    }

    // TODO @haohao：是不是不用生成这个？目前属性上报，是个批量接口

    /**
     * 生成属性上报事件
     */
    private ThingModelEvent generatePropertyPostEvent(List<IotThingModelDO> thingModels) {
        // 没有属性则不生成
        if (CollUtil.isEmpty(thingModels)) {
            return null;
        }

        // 生成属性上报事件
        return new ThingModelEvent().setIdentifier("post").setName("属性上报").setMethod("thing.event.property.post")
                .setType(IotThingModelServiceEventTypeEnum.INFO.getType())
                .setOutputParams(buildInputOutputParam(thingModels, IotThingModelParamDirectionEnum.OUTPUT));
    }

    // TODO @haohao：是不是不用生成这个？目前属性上报，是个批量接口

    /**
     * 生成属性设置服务
     */
    private ThingModelService generatePropertySetService(List<IotThingModelDO> thingModels) {
        // 1.1 过滤出所有可写属性
        thingModels = filterList(thingModels, thingModel ->
                IotThingModelAccessModeEnum.READ_WRITE.getMode().equals(thingModel.getProperty().getAccessMode()));
        // 1.2 没有可写属性则不生成
        if (CollUtil.isEmpty(thingModels)) {
            return null;
        }

        // 2. 生成属性设置服务
        return new ThingModelService().setIdentifier("set").setName("属性设置").setMethod("thing.service.property.set")
                .setCallType(IotThingModelServiceCallTypeEnum.ASYNC.getType())
                .setInputParams(buildInputOutputParam(thingModels, IotThingModelParamDirectionEnum.INPUT))
                .setOutputParams(Collections.emptyList()); // 属性设置服务一般不需要输出参数
    }

    /**
     * 生成属性获取服务
     */
    private ThingModelService generatePropertyGetService(List<IotThingModelDO> thingModels) {
        // 1.1 没有属性则不生成
        if (CollUtil.isEmpty(thingModels)) {
            return null;
        }

        // 1.2 生成属性获取服务
        return new ThingModelService().setIdentifier("get").setName("属性获取").setMethod("thing.service.property.get")
                .setCallType(IotThingModelServiceCallTypeEnum.ASYNC.getType())
                .setInputParams(buildInputOutputParam(thingModels, IotThingModelParamDirectionEnum.INPUT))
                .setOutputParams(buildInputOutputParam(thingModels, IotThingModelParamDirectionEnum.OUTPUT));
    }

    /**
     * 构建输入/输出参数列表
     *
     * @param thingModels 属性列表
     * @return 输入/输出参数列表
     */
    private List<ThingModelParam> buildInputOutputParam(List<IotThingModelDO> thingModels,
                                                        IotThingModelParamDirectionEnum direction) {
        return convertList(thingModels, thingModel ->
                BeanUtils.toBean(thingModel.getProperty(), ThingModelParam.class).setParaOrder(0) // TODO @puhui999: 先搞个默认值看看怎么个事
                        .setDirection(direction.getDirection()));
    }

    private void deleteThingModelListCache(String productKey) {
        // 保证 Spring AOP 触发
        getSelf().deleteThingModelListCache0(productKey);
    }

    @CacheEvict(value = RedisKeyConstants.THING_MODEL_LIST, key = "#productKey")
    public void deleteThingModelListCache0(String productKey) {
    }

    private IotThingModelServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

    // TODO @super：用不到，删除下；
    @Override
    public Long getThingModelCount(LocalDateTime createTime) {
        return thingModelMapper.selectCountByCreateTime(createTime);
    }

}
