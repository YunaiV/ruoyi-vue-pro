package cn.iocoder.yudao.module.iot.service.thingmodel;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.model.ThingModelEvent;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.model.ThingModelInputOutputParam;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.model.ThingModelService;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.vo.IotProductThingModelPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.vo.IotProductThingModelSaveReqVO;
import cn.iocoder.yudao.module.iot.convert.thingmodel.IotProductThingModelConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotProductThingModelDO;
import cn.iocoder.yudao.module.iot.dal.mysql.thingmodel.IotProductThingModelMapper;
import cn.iocoder.yudao.module.iot.enums.product.IotProductStatusEnum;
import cn.iocoder.yudao.module.iot.enums.thingmodel.*;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import cn.iocoder.yudao.module.iot.service.tdengine.IotSuperTableService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

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
public class IotProductThingModelServiceImpl implements IotProductThingModelService {

    @Resource
    private IotProductThingModelMapper productThingModelMapper;

    @Resource
    private IotProductService productService;
    @Resource
    private IotSuperTableService superTableService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createProductThingModel(IotProductThingModelSaveReqVO createReqVO) {
        // 1. 校验功能标识符在同一产品下是否唯一
        validateIdentifierUnique(createReqVO.getProductId(), createReqVO.getIdentifier());

        // 2. 功能名称在同一产品下是否唯一
        validateNameUnique(createReqVO.getProductId(), createReqVO.getName());

        // 3. 系统保留字段，不能用于标识符定义
        validateNotDefaultEventAndService(createReqVO.getIdentifier());

        // 4. 校验产品状态，发布状态下，不允许新增功能
        validateProductStatus(createReqVO.getProductId());

        // 5. 插入数据库
        IotProductThingModelDO thingModel = IotProductThingModelConvert.INSTANCE.convert(createReqVO);
        productThingModelMapper.insert(thingModel);

        // 6. 如果创建的是属性，需要更新默认的事件和服务
        if (Objects.equals(createReqVO.getType(), IotProductThingModelTypeEnum.PROPERTY.getType())) {
            createDefaultEventsAndServices(createReqVO.getProductId(), createReqVO.getProductKey());
        }
        return thingModel.getId();
    }

    @Override
    public void createSuperTableDataModel(Long productId) {
        // 1. 查询产品
        IotProductDO product = productService.getProduct(productId);

        // 2. 查询产品的物模型功能列表
        List<IotProductThingModelDO> thingModelList = productThingModelMapper.selectListByProductId(productId);

        // 3. 生成 TDengine 的数据模型
        superTableService.createSuperTableDataModel(product, thingModelList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProductThingModel(IotProductThingModelSaveReqVO updateReqVO) {
        // 1. 校验功能是否存在
        validateProductThingModelMapperExists(updateReqVO.getId());

        // 2. 校验功能标识符是否唯一
        validateIdentifierUniqueForUpdate(updateReqVO.getId(), updateReqVO.getProductId(), updateReqVO.getIdentifier());

        // 3. 校验产品状态，发布状态下，不允许操作功能
        validateProductStatus(updateReqVO.getProductId());

        // 4. 更新数据库
        IotProductThingModelDO thingModel = IotProductThingModelConvert.INSTANCE.convert(updateReqVO);
        productThingModelMapper.updateById(thingModel);

        // 5. 如果更新的是属性，需要更新默认的事件和服务
        if (Objects.equals(updateReqVO.getType(), IotProductThingModelTypeEnum.PROPERTY.getType())) {
            createDefaultEventsAndServices(updateReqVO.getProductId(), updateReqVO.getProductKey());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProductThingModel(Long id) {
        // 1. 校验功能是否存在
        IotProductThingModelDO thingModel = productThingModelMapper.selectById(id);
        if (thingModel == null) {
            throw exception(THING_MODEL_NOT_EXISTS);
        }

        // 3. 校验产品状态，发布状态下，不允许操作功能
        validateProductStatus(thingModel.getProductId());

        // 2. 删除功能
        productThingModelMapper.deleteById(id);

        // 3. 如果删除的是属性，需要更新默认的事件和服务
        if (Objects.equals(thingModel.getType(), IotProductThingModelTypeEnum.PROPERTY.getType())) {
            createDefaultEventsAndServices(thingModel.getProductId(), thingModel.getProductKey());
        }
    }

    @Override
    public IotProductThingModelDO getProductThingModel(Long id) {
        return productThingModelMapper.selectById(id);
    }

    @Override
    public List<IotProductThingModelDO> getProductThingModelListByProductId(Long productId) {
        return productThingModelMapper.selectListByProductId(productId);
    }

    @Override
    public PageResult<IotProductThingModelDO> getProductThingModelPage(IotProductThingModelPageReqVO pageReqVO) {
        return productThingModelMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotProductThingModelDO> getProductThingModelListByProductKey(String productKey) {
        return productThingModelMapper.selectListByProductKey(productKey);
    }

    /**
     * 校验功能是否存在
     *
     * @param id 功能编号
     */
    private void validateProductThingModelMapperExists(Long id) {
        if (productThingModelMapper.selectById(id) == null) {
            throw exception(THING_MODEL_NOT_EXISTS);
        }
    }

    private void validateIdentifierUniqueForUpdate(Long id, Long productId, String identifier) {
        IotProductThingModelDO thingModel = productThingModelMapper.selectByProductIdAndIdentifier(productId, identifier);
        if (thingModel != null && ObjectUtil.notEqual(thingModel.getId(), id)) {
            throw exception(THING_MODEL_IDENTIFIER_EXISTS);
        }
    }

    private void validateProductStatus(Long createReqVO) {
        IotProductDO product = productService.getProduct(createReqVO);
        if (Objects.equals(product.getStatus(), IotProductStatusEnum.PUBLISHED.getStatus())) {
            throw exception(PRODUCT_STATUS_NOT_ALLOW_THING_MODEL);
        }
    }

    private void validateNotDefaultEventAndService(String identifier) {
        // set, get, post, property, event, time, value 是系统保留字段，不能用于标识符定义
        if (CollUtil.containsAny(Arrays.asList("set", "get", "post", "property", "event", "time", "value"), Collections.singletonList(identifier))) {
            throw exception(THING_MODEL_IDENTIFIER_INVALID);
        }
    }

    private void validateNameUnique(Long productId, String name) {
        IotProductThingModelDO thingModel = productThingModelMapper.selectByProductIdAndName(productId, name);
        if (thingModel != null) {
            throw exception(THING_MODEL_NAME_EXISTS);
        }
    }

    private void validateIdentifierUnique(Long productId, String identifier) {
        IotProductThingModelDO thingModel = productThingModelMapper.selectByProductIdAndIdentifier(productId, identifier);
        if (thingModel != null) {
            throw exception(THING_MODEL_IDENTIFIER_EXISTS);
        }
    }

    /**
     * 创建默认的事件和服务
     */
    public void createDefaultEventsAndServices(Long productId, String productKey) {
        // 1. 获取当前属性列表
        List<IotProductThingModelDO> propertyList = productThingModelMapper
                .selectListByProductIdAndType(productId, IotProductThingModelTypeEnum.PROPERTY.getType());

        // 2. 生成新的事件和服务列表
        List<IotProductThingModelDO> newThingModelList = new ArrayList<>();
        // 2.1 生成属性上报事件
        ThingModelEvent propertyPostEvent = generatePropertyPostEvent(propertyList);
        if (propertyPostEvent != null) {
            newThingModelList.add(buildEventThingModelDO(productId, productKey, propertyPostEvent));
        }
        // 2.2 生成属性设置服务
        ThingModelService propertySetService = generatePropertySetService(propertyList);
        if (propertySetService != null) {
            newThingModelList.add(buildServiceThingModelDO(productId, productKey, propertySetService));
        }
        // 2.3 生成属性获取服务
        ThingModelService propertyGetService = generatePropertyGetService(propertyList);
        if (propertyGetService != null) {
            newThingModelList.add(buildServiceThingModelDO(productId, productKey, propertyGetService));
        }

        // 3.1 获取数据库中的默认的旧事件和服务列表
        List<IotProductThingModelDO> oldThingModelList = productThingModelMapper.selectListByProductIdAndIdentifiersAndTypes(
                productId,
                Arrays.asList("post", "set", "get"),
                Arrays.asList(IotProductThingModelTypeEnum.EVENT.getType(), IotProductThingModelTypeEnum.SERVICE.getType())
        );
        // 3.2 创建默认的事件和服务
        createDefaultEventsAndServices(oldThingModelList, newThingModelList);
    }

    /**
     * 创建默认的事件和服务
     */
    private void createDefaultEventsAndServices(List<IotProductThingModelDO> oldThingModelList, List<IotProductThingModelDO> newThingModelList) {
        // 1.1 使用 diffList 方法比较新旧列表
        List<List<IotProductThingModelDO>> diffResult = diffList(oldThingModelList, newThingModelList,
                (oldVal, newVal) -> {
                    // 继续使用 identifier 和 type 进行比较：这样可以准确地匹配对应的功能对象。
                    boolean same = Objects.equals(oldVal.getIdentifier(), newVal.getIdentifier())
                            && Objects.equals(oldVal.getType(), newVal.getType());
                    if (same) {
                        newVal.setId(oldVal.getId()); // 设置编号
                    }
                    return same;
                });
        // 1.2 批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffResult.get(0))) {
            productThingModelMapper.insertBatch(diffResult.get(0));
        }
        if (CollUtil.isNotEmpty(diffResult.get(1))) {
            productThingModelMapper.updateBatch(diffResult.get(1));
        }
        if (CollUtil.isNotEmpty(diffResult.get(2))) {
            productThingModelMapper.deleteByIds(convertSet(diffResult.get(2), IotProductThingModelDO::getId));
        }
    }

    /**
     * 构建事件功能对象
     */
    private IotProductThingModelDO buildEventThingModelDO(Long productId, String productKey, ThingModelEvent event) {
        return new IotProductThingModelDO().setProductId(productId).setProductKey(productKey)
                .setIdentifier(event.getIdentifier()).setName(event.getName()).setDescription(event.getDescription())
                .setType(IotProductThingModelTypeEnum.EVENT.getType()).setEvent(event);
    }

    /**
     * 构建服务功能对象
     */
    private IotProductThingModelDO buildServiceThingModelDO(Long productId, String productKey, ThingModelService service) {
        return new IotProductThingModelDO().setProductId(productId).setProductKey(productKey)
                .setIdentifier(service.getIdentifier()).setName(service.getName()).setDescription(service.getDescription())
                .setType(IotProductThingModelTypeEnum.SERVICE.getType()).setService(service);
    }

    /**
     * 生成属性上报事件
     */
    private ThingModelEvent generatePropertyPostEvent(List<IotProductThingModelDO> thingModelList) {
        // 1.1 没有属性则不生成
        if (CollUtil.isEmpty(thingModelList)) {
            return null;
        }

        // 1.2 生成属性上报事件
        return new ThingModelEvent().setIdentifier("post").setName("属性上报").setDescription("属性上报事件")
                .setType(IotProductThingModelServiceEventTypeEnum.INFO.getType()).setMethod("thing.event.property.post")
                .setOutputParams(buildInputOutputParam(thingModelList, IotProductThingModelParamDirectionEnum.OUTPUT));
    }

    /**
     * 生成属性设置服务
     */
    private ThingModelService generatePropertySetService(List<IotProductThingModelDO> thingModelList) {
        // 1.1 过滤出所有可写属性
        thingModelList = filterList(thingModelList, thingModel ->
                IotProductThingModelAccessModeEnum.READ_WRITE.getMode().equals(thingModel.getProperty().getAccessMode()));
        // 1.2 没有可写属性则不生成
        if (CollUtil.isEmpty(thingModelList)) {
            return null;
        }

        // 2. 生成属性设置服务
        return new ThingModelService().setIdentifier("set").setName("属性设置").setDescription("属性设置服务")
                .setCallType(IotProductThingModelServiceCallTypeEnum.ASYNC.getType()).setMethod("thing.service.property.set")
                .setInputParams(buildInputOutputParam(thingModelList, IotProductThingModelParamDirectionEnum.INPUT))
                .setOutputParams(Collections.emptyList()); // 属性设置服务一般不需要输出参数
    }

    /**
     * 生成属性获取服务
     */
    private ThingModelService generatePropertyGetService(List<IotProductThingModelDO> thingModelList) {
        // 1.1 没有属性则不生成
        if (CollUtil.isEmpty(thingModelList)) {
            return null;
        }

        // 1.2 生成属性获取服务
        return new ThingModelService().setIdentifier("get").setName("属性获取").setDescription("属性获取服务")
                .setCallType(IotProductThingModelServiceCallTypeEnum.ASYNC.getType()).setMethod("thing.service.property.get")
                .setInputParams(buildInputOutputParam(thingModelList, IotProductThingModelParamDirectionEnum.INPUT))
                .setOutputParams(buildInputOutputParam(thingModelList, IotProductThingModelParamDirectionEnum.OUTPUT));
    }

    /**
     * 构建输入/输出参数列表
     *
     * @param thingModelList 属性列表
     * @return 输入/输出参数列表
     */
    private List<ThingModelInputOutputParam> buildInputOutputParam(List<IotProductThingModelDO> thingModelList,
                                                                   IotProductThingModelParamDirectionEnum directionEnum) {
        return convertList(thingModelList, thingModel ->
                BeanUtils.toBean(thingModel.getProperty(), ThingModelInputOutputParam.class).setParaOrder(0) // TODO @puhui999: 先搞个默认值看看怎么个事
                        .setDirection(directionEnum.getDirection()));
    }

}
