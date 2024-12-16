package cn.iocoder.yudao.module.iot.service.productthingmodel;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.iot.controller.admin.productthingmodel.thingmodel.ThingModelEvent;
import cn.iocoder.yudao.module.iot.controller.admin.productthingmodel.thingmodel.ThingModelProperty;
import cn.iocoder.yudao.module.iot.controller.admin.productthingmodel.thingmodel.ThingModelService;
import cn.iocoder.yudao.module.iot.controller.admin.productthingmodel.thingmodel.dataType.ThingModelArgument;
import cn.iocoder.yudao.module.iot.controller.admin.productthingmodel.thingmodel.dataType.ThingModelArrayDataSpecs;
import cn.iocoder.yudao.module.iot.controller.admin.productthingmodel.thingmodel.dataType.ThingModelDateOrTextDataSpecs;
import cn.iocoder.yudao.module.iot.controller.admin.productthingmodel.vo.IotProductThingModelPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.productthingmodel.vo.IotProductThingModelSaveReqVO;
import cn.iocoder.yudao.module.iot.convert.productthingmodel.IotProductThingModelConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.productthingmodel.IotProductThingModelDO;
import cn.iocoder.yudao.module.iot.dal.mysql.thinkmodelfunction.IotProductThingModelMapper;
import cn.iocoder.yudao.module.iot.enums.product.IotProductStatusEnum;
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
        IotProductThingModelDO function = IotProductThingModelConvert.INSTANCE.convert(createReqVO);
        productThingModelMapper.insert(function);

        // 6. 如果创建的是属性，需要更新默认的事件和服务
        if (Objects.equals(createReqVO.getType(), IotProductThingModelTypeEnum.PROPERTY.getType())) {
            //createDefaultEventsAndServices(createReqVO.getProductId(), createReqVO.getProductKey());
        }
        return function.getId();
    }

    private void validateProductStatus(Long createReqVO) {
        IotProductDO product = productService.getProduct(createReqVO);
        if (Objects.equals(product.getStatus(), IotProductStatusEnum.PUBLISHED.getStatus())) {
            throw exception(PRODUCT_STATUS_NOT_ALLOW_FUNCTION);
        }
    }

    private void validateNotDefaultEventAndService(String identifier) {
        // set, get, post, property, event, time, value 是系统保留字段，不能用于标识符定义
        if (CollUtil.containsAny(Arrays.asList("set", "get", "post", "property", "event", "time", "value"), Collections.singletonList(identifier))) {
            throw exception(THINK_MODEL_FUNCTION_IDENTIFIER_INVALID);
        }
//        if (CollUtil.containsAny(Arrays.asList("post", "set", "get"), identifier)) {
//            throw exception(THINK_MODEL_FUNCTION_IDENTIFIER_INVALID);
//        }
    }

    private void validateNameUnique(Long productId, String name) {
        IotProductThingModelDO function = productThingModelMapper.selectByProductIdAndName(productId, name);
        if (function != null) {
            throw exception(THINK_MODEL_FUNCTION_NAME_EXISTS);
        }
    }

    private void validateIdentifierUnique(Long productId, String identifier) {
        IotProductThingModelDO function = productThingModelMapper.selectByProductIdAndIdentifier(productId, identifier);
        if (function != null) {
            throw exception(THINK_MODEL_FUNCTION_IDENTIFIER_EXISTS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProductThingModel(IotProductThingModelSaveReqVO updateReqVO) {
        // 1. 校验功能是否存在
        validateproductThingModelMapperExists(updateReqVO.getId());

        // 2. 校验功能标识符是否唯一
        validateIdentifierUniqueForUpdate(updateReqVO.getId(), updateReqVO.getProductId(), updateReqVO.getIdentifier());

        // 3. 校验产品状态，发布状态下，不允许操作功能
        validateProductStatus(updateReqVO.getProductId());

        // 4. 更新数据库
        IotProductThingModelDO productThingModelDO = IotProductThingModelConvert.INSTANCE.convert(updateReqVO);
        productThingModelMapper.updateById(productThingModelDO);

        // 5. 如果更新的是属性，需要更新默认的事件和服务
        if (Objects.equals(updateReqVO.getType(), IotProductThingModelTypeEnum.PROPERTY.getType())) {
            createDefaultEventsAndServices(updateReqVO.getProductId(), updateReqVO.getProductKey());
        }
    }

    private void validateIdentifierUniqueForUpdate(Long id, Long productId, String identifier) {
        IotProductThingModelDO function = productThingModelMapper.selectByProductIdAndIdentifier(productId, identifier);
        if (function != null && ObjectUtil.notEqual(function.getId(), id)) {
            throw exception(THINK_MODEL_FUNCTION_IDENTIFIER_EXISTS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProductThingModel(Long id) {
        // 1. 校验功能是否存在
        IotProductThingModelDO functionDO = productThingModelMapper.selectById(id);
        if (functionDO == null) {
            throw exception(THINK_MODEL_FUNCTION_NOT_EXISTS);
        }

        // 3. 校验产品状态，发布状态下，不允许操作功能
        validateProductStatus(functionDO.getProductId());

        // 2. 删除功能
        productThingModelMapper.deleteById(id);

        // 3. 如果删除的是属性，需要更新默认的事件和服务
        if (Objects.equals(functionDO.getType(), IotProductThingModelTypeEnum.PROPERTY.getType())) {
            createDefaultEventsAndServices(functionDO.getProductId(), functionDO.getProductKey());
        }
    }

    /**
     * 校验功能是否存在
     *
     * @param id 功能编号
     */
    private void validateproductThingModelMapperExists(Long id) {
        if (productThingModelMapper.selectById(id) == null) {
            throw exception(THINK_MODEL_FUNCTION_NOT_EXISTS);
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
        List<IotProductThingModelDO> functionList = productThingModelMapper.selectListByProductId(productId);

        // 3. 生成 TDengine 的数据模型
        dbStructureDataService.createSuperTableDataModel(product, functionList);
    }

    @Override
    public List<IotProductThingModelDO> getProductThingModelListByProductKey(String productKey) {
        return productThingModelMapper.selectListByProductKey(productKey);
    }

    // TODO @puhui999: 需要重构
    /**
     * 创建默认的事件和服务
     */
    public void createDefaultEventsAndServices(Long productId, String productKey) {
        // 1. 获取当前属性列表
        List<IotProductThingModelDO> propertyList = productThingModelMapper
                .selectListByProductIdAndType(productId, IotProductThingModelTypeEnum.PROPERTY.getType());

        // 2. 生成新的事件和服务列表
        List<IotProductThingModelDO> newFunctionList = new ArrayList<>();
        // 生成属性上报事件
        ThingModelEvent propertyPostEvent = generatePropertyPostEvent(propertyList);
        if (propertyPostEvent != null) {
            IotProductThingModelDO eventFunction = buildEventFunctionDO(productId, productKey, propertyPostEvent);
            newFunctionList.add(eventFunction);
        }
        // 生成属性设置服务
        ThingModelService propertySetService = generatePropertySetService(propertyList);
        if (propertySetService != null) {
            IotProductThingModelDO setServiceFunction = buildServiceFunctionDO(productId, productKey, propertySetService);
            newFunctionList.add(setServiceFunction);
        }
        // 生成属性获取服务
        ThingModelService propertyGetService = generatePropertyGetService(propertyList);
        if (propertyGetService != null) {
            IotProductThingModelDO getServiceFunction = buildServiceFunctionDO(productId, productKey, propertyGetService);
            newFunctionList.add(getServiceFunction);
        }

        // 3. 获取数据库中的默认的旧事件和服务列表
        List<IotProductThingModelDO> oldFunctionList = productThingModelMapper.selectListByProductIdAndIdentifiersAndTypes(
                productId,
                Arrays.asList("post", "set", "get"),
                Arrays.asList(IotProductThingModelTypeEnum.EVENT.getType(), IotProductThingModelTypeEnum.SERVICE.getType())
        );

        // 3.1 使用 diffList 方法比较新旧列表
        List<List<IotProductThingModelDO>> diffResult = diffList(oldFunctionList, newFunctionList,
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
                IotProductThingModelDO oldFunc = findFunctionByIdentifierAndType(
                        oldFunctionList, updateFunc.getIdentifier(), updateFunc.getType());
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
    private IotProductThingModelDO findFunctionByIdentifierAndType(List<IotProductThingModelDO> functionList,
                                                                   String identifier, Integer type) {
        return CollUtil.findOne(functionList, func ->
                Objects.equals(func.getIdentifier(), identifier) && Objects.equals(func.getType(), type));
    }

    /**
     * 构建事件功能对象
     */
    private IotProductThingModelDO buildEventFunctionDO(Long productId, String productKey, ThingModelEvent event) {
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
    private IotProductThingModelDO buildServiceFunctionDO(Long productId, String productKey, ThingModelService service) {
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
        // TODO @puhui999: 需要重构
        for (IotProductThingModelDO functionDO : propertyList) {
            ThingModelProperty property = functionDO.getProperty();
            ThingModelArgument arg = new ThingModelArgument()
                    .setIdentifier(property.getIdentifier())
                    .setName(property.getName())
                    //.setDataType(property.getDataType())
                    .setDescription(property.getDescription())
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
        // TODO @puhui999: 需要重构
        for (IotProductThingModelDO functionDO : propertyList) {
            ThingModelProperty property = functionDO.getProperty();
            //if (IotProductThingModelAccessModeEnum.WRITE.getMode().equals(property.getAccessMode()) || IotProductThingModelAccessModeEnum.READ_WRITE.getMode().equals(property.getAccessMode())) {
            //    ThingModelArgument arg = new ThingModelArgument()
            //            .setIdentifier(property.getIdentifier())
            //            .setName(property.getName())
            //            .setDataType(property.getDataType())
            //            .setDescription(property.getDescription())
            //            .setDirection("input"); // 设置为输入参数
            //    inputData.add(arg);
            //}
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
        // TODO @puhui999: 需要重构
        List<ThingModelArgument> outputData = new ArrayList<>();
        for (IotProductThingModelDO functionDO : propertyList) {
            ThingModelProperty property = functionDO.getProperty();
            //if (ObjectUtils.equalsAny(property.getAccessMode(),
            //        IotProductThingModelAccessModeEnum.READ.getMode(), IotProductThingModelAccessModeEnum.READ_WRITE.getMode())) {
            //    ThingModelArgument arg = new ThingModelArgument()
            //            .setIdentifier(property.getIdentifier())
            //            .setName(property.getName())
            //            .setDataType(property.getDataType())
            //            .setDescription(property.getDescription())
            //            .setDirection("output"); // 设置为输出参数
            //    outputData.add(arg);
            //}
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

        // 创建数组类型，元素类型为文本类型（字符串）
        ThingModelArrayDataSpecs arrayType = new ThingModelArrayDataSpecs();
        arrayType.setDataType("array");
        //ThingModelArraySpecs arraySpecs = new ThingModelArraySpecs();
        ThingModelDateOrTextDataSpecs textType = new ThingModelDateOrTextDataSpecs();
        textType.setDataType("text");
        //arraySpecs.setItem(textType);
        //arrayType.setSpecs(arraySpecs);
        inputArg.setDataType(arrayType);

        service.setInputData(Collections.singletonList(inputArg));
        service.setOutputData(outputData);
        return service;
    }

}
