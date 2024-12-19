package cn.iocoder.yudao.module.iot.service.thingmodel;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.model.ThingModelEvent;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.model.ThingModelProperty;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.model.ThingModelService;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.model.dataType.ThingModelArgument;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.model.dataType.ThingModelArrayDataSpecs;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.model.dataType.ThingModelDateOrTextDataSpecs;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.vo.IotProductThingModelPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.vo.IotProductThingModelSaveReqVO;
import cn.iocoder.yudao.module.iot.convert.thingmodel.IotProductThingModelConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotProductThingModelDO;
import cn.iocoder.yudao.module.iot.dal.mysql.thingmodel.IotProductThingModelMapper;
import cn.iocoder.yudao.module.iot.enums.product.IotProductStatusEnum;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotProductThingModelAccessModeEnum;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotProductThingModelTypeEnum;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import cn.iocoder.yudao.module.iot.service.tdengine.IotSuperTableService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
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
    private IotSuperTableService dbStructureDataService;

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

    private void validateIdentifierUniqueForUpdate(Long id, Long productId, String identifier) {
        IotProductThingModelDO thingModel = productThingModelMapper.selectByProductIdAndIdentifier(productId, identifier);
        if (thingModel != null && ObjectUtil.notEqual(thingModel.getId(), id)) {
            throw exception(THING_MODEL_IDENTIFIER_EXISTS);
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
    public void createSuperTableDataModel(Long productId) {
        // 1. 查询产品
        IotProductDO product = productService.getProduct(productId);

        // 2. 查询产品的物模型功能列表
        List<IotProductThingModelDO> thingModelList = productThingModelMapper.selectListByProductId(productId);

        // 3. 生成 TDengine 的数据模型
        dbStructureDataService.createSuperTableDataModel(product, thingModelList);
    }

    @Override
    public List<IotProductThingModelDO> getProductThingModelListByProductKey(String productKey) {
        return productThingModelMapper.selectListByProductKey(productKey);
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
        // 生成属性上报事件
        ThingModelEvent propertyPostEvent = generatePropertyPostEvent(propertyList);
        if (propertyPostEvent != null) {
            IotProductThingModelDO eventThingModel = buildEventThingModelDO(productId, productKey, propertyPostEvent);
            newThingModelList.add(eventThingModel);
        }
        // 生成属性设置服务
        ThingModelService propertySetService = generatePropertySetService(propertyList);
        if (propertySetService != null) {
            IotProductThingModelDO setServiceThingModel = buildServiceThingModelDO(productId, productKey, propertySetService);
            newThingModelList.add(setServiceThingModel);
        }
        // 生成属性获取服务
        ThingModelService propertyGetService = generatePropertyGetService(propertyList);
        if (propertyGetService != null) {
            IotProductThingModelDO getServiceThingModel = buildServiceThingModelDO(productId, productKey, propertyGetService);
            newThingModelList.add(getServiceThingModel);
        }

        // 3. 获取数据库中的默认的旧事件和服务列表
        List<IotProductThingModelDO> oldThingModelList = productThingModelMapper.selectListByProductIdAndIdentifiersAndTypes(
                productId,
                Arrays.asList("post", "set", "get"),
                Arrays.asList(IotProductThingModelTypeEnum.EVENT.getType(), IotProductThingModelTypeEnum.SERVICE.getType())
        );

        // 3.1 使用 diffList 方法比较新旧列表
        List<List<IotProductThingModelDO>> diffResult = diffList(oldThingModelList, newThingModelList,
                // 继续使用 identifier 和 type 进行比较：这样可以准确地匹配对应的功能对象。
                (oldFunc, newFunc) -> Objects.equals(oldFunc.getIdentifier(), newFunc.getIdentifier())
                        && Objects.equals(oldFunc.getType(), newFunc.getType()));
        List<IotProductThingModelDO> createList = diffResult.get(0); // 需要新增的
        List<IotProductThingModelDO> updateList = diffResult.get(1); // 需要更新的
        List<IotProductThingModelDO> deleteList = diffResult.get(2); // 需要删除的

        // 3.2 批量执行数据库操作
        // 新增数据库中的新事件和服务列表
        if (CollUtil.isNotEmpty(createList)) {
            productThingModelMapper.insertBatch(createList);
        }
        // 更新数据库中的事件和服务列表
        if (CollUtil.isNotEmpty(updateList)) {
            // 首先，为每个需要更新的对象设置其对应的 ID
            updateList.forEach(updateFunc -> {
                IotProductThingModelDO oldFunc = findThingModelByIdentifierAndType(
                        oldThingModelList, updateFunc.getIdentifier(), updateFunc.getType());
                if (oldFunc != null) {
                    updateFunc.setId(oldFunc.getId());
                }
            });
            // 过滤掉没有设置 ID 的对象
            List<IotProductThingModelDO> validUpdateList = updateList.stream()
                    .filter(func -> func.getId() != null)
                    .collect(Collectors.toList());
            // 执行批量更新
            if (CollUtil.isNotEmpty(validUpdateList)) {
                productThingModelMapper.updateBatch(validUpdateList);
            }
        }

        // 删除数据库中的旧事件和服务列表
        if (CollUtil.isNotEmpty(deleteList)) {
            Set<Long> idsToDelete = CollectionUtils.convertSet(deleteList, IotProductThingModelDO::getId);
            productThingModelMapper.deleteByIds(idsToDelete);
        }
    }

    /**
     * 根据标识符和类型查找功能对象
     */
    private IotProductThingModelDO findThingModelByIdentifierAndType(List<IotProductThingModelDO> thingModelList,
                                                                     String identifier, Integer type) {
        return CollUtil.findOne(thingModelList, func ->
                Objects.equals(func.getIdentifier(), identifier) && Objects.equals(func.getType(), type));
    }

    /**
     * 构建事件功能对象
     */
    private IotProductThingModelDO buildEventThingModelDO(Long productId, String productKey, ThingModelEvent event) {
        return new IotProductThingModelDO()
                .setProductId(productId)
                .setProductKey(productKey)
                .setIdentifier(event.getIdentifier())
                .setName(event.getName())
                .setDescription(event.getDescription())
                .setType(IotProductThingModelTypeEnum.EVENT.getType())
                .setEvent(event);
    }

    /**
     * 构建服务功能对象
     */
    private IotProductThingModelDO buildServiceThingModelDO(Long productId, String productKey, ThingModelService service) {
        return new IotProductThingModelDO()
                .setProductId(productId)
                .setProductKey(productKey)
                .setIdentifier(service.getIdentifier())
                .setName(service.getName())
                .setDescription(service.getDescription())
                .setType(IotProductThingModelTypeEnum.SERVICE.getType())
                .setService(service);
    }

    /**
     * 生成属性上报事件
     */
    private ThingModelEvent generatePropertyPostEvent(List<IotProductThingModelDO> propertyList) {
        if (CollUtil.isEmpty(propertyList)) {
            return null;
        }

        ThingModelEvent event = new ThingModelEvent()
                .setIdentifier("post")
                .setName("属性上报")
                .setType("info")
                .setDescription("属性上报事件")
                .setMethod("thing.event.property.post");

        // 将属性列表转换为事件的输出参数
        List<ThingModelArgument> outputData = new ArrayList<>();
        for (IotProductThingModelDO thingModel : propertyList) {
            ThingModelArgument arg = new ThingModelArgument()
                    .setIdentifier(thingModel.getIdentifier())
                    .setName(thingModel.getName())
                    .setProperty(thingModel.getProperty())
                    .setDescription(thingModel.getDescription())
                    .setDirection("output"); // 设置为输出参数
            outputData.add(arg);
        }
        event.setOutputData(outputData);
        return event;
    }

    /**
     * 生成属性设置服务
     */
    private ThingModelService generatePropertySetService(List<IotProductThingModelDO> propertyList) {
        if (propertyList == null || propertyList.isEmpty()) {
            return null;
        }

        List<ThingModelArgument> inputData = new ArrayList<>();
        for (IotProductThingModelDO thingModel : propertyList) {
            ThingModelProperty property = thingModel.getProperty();
            if (IotProductThingModelAccessModeEnum.READ_WRITE.getMode().equals(property.getAccessMode())) {
                ThingModelArgument arg = new ThingModelArgument()
                        .setIdentifier(property.getIdentifier())
                        .setName(property.getName())
                        .setProperty(property)
                        .setDescription(property.getDescription())
                        .setDirection("input"); // 设置为输入参数
                inputData.add(arg);
            }
        }
        if (inputData.isEmpty()) {
            // 如果没有可写属性，不生成属性设置服务
            return null;
        }

        // 属性设置服务一般不需要输出参数
        return new ThingModelService()
                .setIdentifier("set")
                .setName("属性设置")
                .setCallType("async")
                .setDescription("属性设置服务")
                .setMethod("thing.service.property.set")
                .setInputData(inputData)
                // 属性设置服务一般不需要输出参数
                .setOutputData(new ArrayList<>());
    }

    /**
     * 生成属性获取服务
     */
    private ThingModelService generatePropertyGetService(List<IotProductThingModelDO> propertyList) {
        if (propertyList == null || propertyList.isEmpty()) {
            return null;
        }

        List<ThingModelArgument> outputData = new ArrayList<>();
        for (IotProductThingModelDO thingModelDO : propertyList) {
            ThingModelProperty property = thingModelDO.getProperty();
            if (ObjectUtils.equalsAny(property.getAccessMode(),
                    IotProductThingModelAccessModeEnum.READ_ONLY.getMode(), IotProductThingModelAccessModeEnum.READ_WRITE.getMode())) {
                ThingModelArgument arg = new ThingModelArgument()
                        .setIdentifier(property.getIdentifier())
                        .setName(property.getName())
                        .setProperty(property)
                        .setDescription(property.getDescription())
                        .setDirection("output"); // 设置为输出参数
                outputData.add(arg);
            }
        }
        if (outputData.isEmpty()) {
            // 如果没有可读属性，不生成属性获取服务
            return null;
        }

        ThingModelService service = new ThingModelService()
                .setIdentifier("get")
                .setName("属性获取")
                .setCallType("async")
                .setDescription("属性获取服务")
                .setMethod("thing.service.property.get");

        // 定义输入参数：属性标识符列表
        ThingModelArgument inputArg = new ThingModelArgument()
                .setIdentifier("properties")
                .setName("属性标识符列表")
                .setDescription("需要获取的属性标识符列表")
                .setDirection("input"); // 设置为输入参数

        // 创建数组类型，元素类型为文本类型（字符串）TODO @puhui999: 还得研究研究
        ThingModelArrayDataSpecs arrayType = new ThingModelArrayDataSpecs();
        arrayType.setDataType("array");
        inputArg.setProperty(new ThingModelProperty().setIdentifier(inputArg.getIdentifier()).setName(inputArg.getName())
                .setDescription(inputArg.getDescription()).setDataSpecs(arrayType));

        ThingModelDateOrTextDataSpecs textType = new ThingModelDateOrTextDataSpecs();
        textType.setDataType("text");
        inputArg.setProperty(new ThingModelProperty().setIdentifier(inputArg.getIdentifier()).setName(inputArg.getName())
                .setDescription(inputArg.getDescription()).setDataSpecs(textType));

        service.setInputData(Collections.singletonList(inputArg));
        service.setOutputData(outputData);
        return service;
    }

}
