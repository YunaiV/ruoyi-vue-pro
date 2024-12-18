package cn.iocoder.yudao.module.iot.service.thinkmodel;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodel.model.ThinkModelEvent;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodel.model.ThinkModelProperty;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodel.model.ThinkModelService;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodel.model.dataType.ThinkModelArgument;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodel.model.dataType.ThinkModelArrayDataSpecs;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodel.model.dataType.ThinkModelDateOrTextDataSpecs;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodel.vo.IotProductThinkModelPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodel.vo.IotProductThinkModelSaveReqVO;
import cn.iocoder.yudao.module.iot.convert.thinkmodel.IotProductThinkModelConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thinkmodel.IotProductThinkModelDO;
import cn.iocoder.yudao.module.iot.dal.mysql.thinkmodel.IotProductThinkModelMapper;
import cn.iocoder.yudao.module.iot.enums.product.IotProductStatusEnum;
import cn.iocoder.yudao.module.iot.enums.thinkmodel.IotProductThinkModelAccessModeEnum;
import cn.iocoder.yudao.module.iot.enums.thinkmodel.IotProductThinkModelTypeEnum;
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
public class IotProductThinkModelServiceImpl implements IotProductThinkModelService {

    @Resource
    private IotProductThinkModelMapper productThinkModelMapper;

    @Resource
    private IotProductService productService;
    @Resource
    private IotSuperTableService dbStructureDataService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createProductThinkModel(IotProductThinkModelSaveReqVO createReqVO) {
        // 1. 校验功能标识符在同一产品下是否唯一
        validateIdentifierUnique(createReqVO.getProductId(), createReqVO.getIdentifier());

        // 2. 功能名称在同一产品下是否唯一
        validateNameUnique(createReqVO.getProductId(), createReqVO.getName());

        // 3. 系统保留字段，不能用于标识符定义
        validateNotDefaultEventAndService(createReqVO.getIdentifier());

        // 4. 校验产品状态，发布状态下，不允许新增功能
        validateProductStatus(createReqVO.getProductId());

        // 5. 插入数据库
        IotProductThinkModelDO function = IotProductThinkModelConvert.INSTANCE.convert(createReqVO);
        productThinkModelMapper.insert(function);

        // 6. 如果创建的是属性，需要更新默认的事件和服务
        if (Objects.equals(createReqVO.getType(), IotProductThinkModelTypeEnum.PROPERTY.getType())) {
            createDefaultEventsAndServices(createReqVO.getProductId(), createReqVO.getProductKey());
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
        IotProductThinkModelDO function = productThinkModelMapper.selectByProductIdAndName(productId, name);
        if (function != null) {
            throw exception(THINK_MODEL_FUNCTION_NAME_EXISTS);
        }
    }

    private void validateIdentifierUnique(Long productId, String identifier) {
        IotProductThinkModelDO function = productThinkModelMapper.selectByProductIdAndIdentifier(productId, identifier);
        if (function != null) {
            throw exception(THINK_MODEL_FUNCTION_IDENTIFIER_EXISTS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProductThinkModel(IotProductThinkModelSaveReqVO updateReqVO) {
        // 1. 校验功能是否存在
        validateProductThinkModelMapperExists(updateReqVO.getId());

        // 2. 校验功能标识符是否唯一
        validateIdentifierUniqueForUpdate(updateReqVO.getId(), updateReqVO.getProductId(), updateReqVO.getIdentifier());

        // 3. 校验产品状态，发布状态下，不允许操作功能
        validateProductStatus(updateReqVO.getProductId());

        // 4. 更新数据库
        IotProductThinkModelDO productThinkModelDO = IotProductThinkModelConvert.INSTANCE.convert(updateReqVO);
        productThinkModelMapper.updateById(productThinkModelDO);

        // 5. 如果更新的是属性，需要更新默认的事件和服务
        if (Objects.equals(updateReqVO.getType(), IotProductThinkModelTypeEnum.PROPERTY.getType())) {
            createDefaultEventsAndServices(updateReqVO.getProductId(), updateReqVO.getProductKey());
        }
    }

    private void validateIdentifierUniqueForUpdate(Long id, Long productId, String identifier) {
        IotProductThinkModelDO function = productThinkModelMapper.selectByProductIdAndIdentifier(productId, identifier);
        if (function != null && ObjectUtil.notEqual(function.getId(), id)) {
            throw exception(THINK_MODEL_FUNCTION_IDENTIFIER_EXISTS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProductThinkModel(Long id) {
        // 1. 校验功能是否存在
        IotProductThinkModelDO functionDO = productThinkModelMapper.selectById(id);
        if (functionDO == null) {
            throw exception(THINK_MODEL_FUNCTION_NOT_EXISTS);
        }

        // 3. 校验产品状态，发布状态下，不允许操作功能
        validateProductStatus(functionDO.getProductId());

        // 2. 删除功能
        productThinkModelMapper.deleteById(id);

        // 3. 如果删除的是属性，需要更新默认的事件和服务
        if (Objects.equals(functionDO.getType(), IotProductThinkModelTypeEnum.PROPERTY.getType())) {
            createDefaultEventsAndServices(functionDO.getProductId(), functionDO.getProductKey());
        }
    }

    /**
     * 校验功能是否存在
     *
     * @param id 功能编号
     */
    private void validateProductThinkModelMapperExists(Long id) {
        if (productThinkModelMapper.selectById(id) == null) {
            throw exception(THINK_MODEL_FUNCTION_NOT_EXISTS);
        }
    }

    @Override
    public IotProductThinkModelDO getProductThinkModel(Long id) {
        return productThinkModelMapper.selectById(id);
    }

    @Override
    public List<IotProductThinkModelDO> getProductThinkModelListByProductId(Long productId) {
        return productThinkModelMapper.selectListByProductId(productId);
    }

    @Override
    public PageResult<IotProductThinkModelDO> getProductThinkModelPage(IotProductThinkModelPageReqVO pageReqVO) {
        return productThinkModelMapper.selectPage(pageReqVO);
    }

    @Override
    public void createSuperTableDataModel(Long productId) {
        // 1. 查询产品
        IotProductDO product = productService.getProduct(productId);

        // 2. 查询产品的物模型功能列表
        List<IotProductThinkModelDO> functionList = productThinkModelMapper.selectListByProductId(productId);

        // 3. 生成 TDengine 的数据模型
        dbStructureDataService.createSuperTableDataModel(product, functionList);
    }

    @Override
    public List<IotProductThinkModelDO> getProductThinkModelListByProductKey(String productKey) {
        return productThinkModelMapper.selectListByProductKey(productKey);
    }

    /**
     * 创建默认的事件和服务
     */
    public void createDefaultEventsAndServices(Long productId, String productKey) {
        // 1. 获取当前属性列表
        List<IotProductThinkModelDO> propertyList = productThinkModelMapper
                .selectListByProductIdAndType(productId, IotProductThinkModelTypeEnum.PROPERTY.getType());

        // 2. 生成新的事件和服务列表
        List<IotProductThinkModelDO> newFunctionList = new ArrayList<>();
        // 生成属性上报事件
        ThinkModelEvent propertyPostEvent = generatePropertyPostEvent(propertyList);
        if (propertyPostEvent != null) {
            IotProductThinkModelDO eventFunction = buildEventFunctionDO(productId, productKey, propertyPostEvent);
            newFunctionList.add(eventFunction);
        }
        // 生成属性设置服务
        ThinkModelService propertySetService = generatePropertySetService(propertyList);
        if (propertySetService != null) {
            IotProductThinkModelDO setServiceFunction = buildServiceFunctionDO(productId, productKey, propertySetService);
            newFunctionList.add(setServiceFunction);
        }
        // 生成属性获取服务
        ThinkModelService propertyGetService = generatePropertyGetService(propertyList);
        if (propertyGetService != null) {
            IotProductThinkModelDO getServiceFunction = buildServiceFunctionDO(productId, productKey, propertyGetService);
            newFunctionList.add(getServiceFunction);
        }

        // 3. 获取数据库中的默认的旧事件和服务列表
        List<IotProductThinkModelDO> oldFunctionList = productThinkModelMapper.selectListByProductIdAndIdentifiersAndTypes(
                productId,
                Arrays.asList("post", "set", "get"),
                Arrays.asList(IotProductThinkModelTypeEnum.EVENT.getType(), IotProductThinkModelTypeEnum.SERVICE.getType())
        );

        // 3.1 使用 diffList 方法比较新旧列表
        List<List<IotProductThinkModelDO>> diffResult = diffList(oldFunctionList, newFunctionList,
                // 继续使用 identifier 和 type 进行比较：这样可以准确地匹配对应的功能对象。
                (oldFunc, newFunc) -> Objects.equals(oldFunc.getIdentifier(), newFunc.getIdentifier())
                        && Objects.equals(oldFunc.getType(), newFunc.getType()));
        List<IotProductThinkModelDO> createList = diffResult.get(0); // 需要新增的
        List<IotProductThinkModelDO> updateList = diffResult.get(1); // 需要更新的
        List<IotProductThinkModelDO> deleteList = diffResult.get(2); // 需要删除的

        // 3.2 批量执行数据库操作
        // 新增数据库中的新事件和服务列表
        if (CollUtil.isNotEmpty(createList)) {
            productThinkModelMapper.insertBatch(createList);
        }
        // 更新数据库中的事件和服务列表
        if (CollUtil.isNotEmpty(updateList)) {
            // 首先，为每个需要更新的对象设置其对应的 ID
            updateList.forEach(updateFunc -> {
                IotProductThinkModelDO oldFunc = findFunctionByIdentifierAndType(
                        oldFunctionList, updateFunc.getIdentifier(), updateFunc.getType());
                if (oldFunc != null) {
                    updateFunc.setId(oldFunc.getId());
                }
            });
            // 过滤掉没有设置 ID 的对象
            List<IotProductThinkModelDO> validUpdateList = updateList.stream()
                    .filter(func -> func.getId() != null)
                    .collect(Collectors.toList());
            // 执行批量更新
            if (CollUtil.isNotEmpty(validUpdateList)) {
                productThinkModelMapper.updateBatch(validUpdateList);
            }
        }

        // 删除数据库中的旧事件和服务列表
        if (CollUtil.isNotEmpty(deleteList)) {
            Set<Long> idsToDelete = CollectionUtils.convertSet(deleteList, IotProductThinkModelDO::getId);
            productThinkModelMapper.deleteByIds(idsToDelete);
        }
    }

    /**
     * 根据标识符和类型查找功能对象
     */
    private IotProductThinkModelDO findFunctionByIdentifierAndType(List<IotProductThinkModelDO> functionList,
                                                                   String identifier, Integer type) {
        return CollUtil.findOne(functionList, func ->
                Objects.equals(func.getIdentifier(), identifier) && Objects.equals(func.getType(), type));
    }

    /**
     * 构建事件功能对象
     */
    private IotProductThinkModelDO buildEventFunctionDO(Long productId, String productKey, ThinkModelEvent event) {
        return new IotProductThinkModelDO()
                .setProductId(productId)
                .setProductKey(productKey)
                .setIdentifier(event.getIdentifier())
                .setName(event.getName())
                .setDescription(event.getDescription())
                .setType(IotProductThinkModelTypeEnum.EVENT.getType())
                .setEvent(event);
    }

    /**
     * 构建服务功能对象
     */
    private IotProductThinkModelDO buildServiceFunctionDO(Long productId, String productKey, ThinkModelService service) {
        return new IotProductThinkModelDO()
                .setProductId(productId)
                .setProductKey(productKey)
                .setIdentifier(service.getIdentifier())
                .setName(service.getName())
                .setDescription(service.getDescription())
                .setType(IotProductThinkModelTypeEnum.SERVICE.getType())
                .setService(service);
    }

    /**
     * 生成属性上报事件
     */
    private ThinkModelEvent generatePropertyPostEvent(List<IotProductThinkModelDO> propertyList) {
        if (CollUtil.isEmpty(propertyList)) {
            return null;
        }

        ThinkModelEvent event = new ThinkModelEvent()
                .setIdentifier("post")
                .setName("属性上报")
                .setType("info")
                .setDescription("属性上报事件")
                .setMethod("thing.event.property.post");

        // 将属性列表转换为事件的输出参数
        List<ThinkModelArgument> outputData = new ArrayList<>();
        for (IotProductThinkModelDO thinkModel : propertyList) {
            ThinkModelArgument arg = new ThinkModelArgument()
                    .setIdentifier(thinkModel.getIdentifier())
                    .setName(thinkModel.getName())
                    .setProperty(thinkModel.getProperty())
                    .setDescription(thinkModel.getDescription())
                    .setDirection("output"); // 设置为输出参数
            outputData.add(arg);
        }
        event.setOutputData(outputData);
        return event;
    }

    /**
     * 生成属性设置服务
     */
    private ThinkModelService generatePropertySetService(List<IotProductThinkModelDO> propertyList) {
        if (propertyList == null || propertyList.isEmpty()) {
            return null;
        }

        List<ThinkModelArgument> inputData = new ArrayList<>();
        for (IotProductThinkModelDO thinkModel : propertyList) {
            ThinkModelProperty property = thinkModel.getProperty();
            if (IotProductThinkModelAccessModeEnum.READ_WRITE.getMode().equals(property.getAccessMode())) {
                ThinkModelArgument arg = new ThinkModelArgument()
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
        return new ThinkModelService()
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
    private ThinkModelService generatePropertyGetService(List<IotProductThinkModelDO> propertyList) {
        if (propertyList == null || propertyList.isEmpty()) {
            return null;
        }

        List<ThinkModelArgument> outputData = new ArrayList<>();
        for (IotProductThinkModelDO functionDO : propertyList) {
            ThinkModelProperty property = functionDO.getProperty();
            if (ObjectUtils.equalsAny(property.getAccessMode(),
                    IotProductThinkModelAccessModeEnum.READ_ONLY.getMode(), IotProductThinkModelAccessModeEnum.READ_WRITE.getMode())) {
                ThinkModelArgument arg = new ThinkModelArgument()
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

        ThinkModelService service = new ThinkModelService()
                .setIdentifier("get")
                .setName("属性获取")
                .setCallType("async")
                .setDescription("属性获取服务")
                .setMethod("thing.service.property.get");

        // 定义输入参数：属性标识符列表
        ThinkModelArgument inputArg = new ThinkModelArgument()
                .setIdentifier("properties")
                .setName("属性标识符列表")
                .setDescription("需要获取的属性标识符列表")
                .setDirection("input"); // 设置为输入参数

        // 创建数组类型，元素类型为文本类型（字符串）TODO @puhui999: 还得研究研究
        ThinkModelArrayDataSpecs arrayType = new ThinkModelArrayDataSpecs();
        arrayType.setDataType("array");
        inputArg.setProperty(new ThinkModelProperty().setIdentifier(inputArg.getIdentifier()).setName(inputArg.getName())
                .setDescription(inputArg.getDescription()).setDataSpecs(arrayType));

        ThinkModelDateOrTextDataSpecs textType = new ThinkModelDateOrTextDataSpecs();
        textType.setDataType("text");
        inputArg.setProperty(new ThinkModelProperty().setIdentifier(inputArg.getIdentifier()).setName(inputArg.getName())
                .setDescription(inputArg.getDescription()).setDataSpecs(textType));

        service.setInputData(Collections.singletonList(inputArg));
        service.setOutputData(outputData);
        return service;
    }

}
