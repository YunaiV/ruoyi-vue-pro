package cn.iocoder.yudao.module.iot.service.thinkmodelfunction;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.ThingModelEvent;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.ThingModelProperty;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.ThingModelService;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.dataType.ThingModelArgument;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.dataType.ThingModelArraySpecs;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.dataType.ThingModelArrayType;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.dataType.ThingModelTextType;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.vo.IotThinkModelFunctionSaveReqVO;
import cn.iocoder.yudao.module.iot.convert.thinkmodelfunction.IotThinkModelFunctionConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.thinkmodelfunction.IotThinkModelFunctionDO;
import cn.iocoder.yudao.module.iot.dal.mysql.thinkmodelfunction.IotThinkModelFunctionMapper;
import cn.iocoder.yudao.module.iot.enums.product.IotAccessModeEnum;
import cn.iocoder.yudao.module.iot.enums.product.IotThingModelTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.THINK_MODEL_FUNCTION_EXISTS_BY_IDENTIFIER;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.THINK_MODEL_FUNCTION_NOT_EXISTS;

/**
 * IoT 产品物模型 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class IotThinkModelFunctionServiceImpl implements IotThinkModelFunctionService {

    @Resource
    private IotThinkModelFunctionMapper thinkModelFunctionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createThinkModelFunction(IotThinkModelFunctionSaveReqVO createReqVO) {
        // 1. 校验功能标识符在同一产品下是否唯一
        validateIdentifierUnique(createReqVO.getProductId(), createReqVO.getIdentifier());

        // 2. 插入数据库
        IotThinkModelFunctionDO function = IotThinkModelFunctionConvert.INSTANCE.convert(createReqVO);
        thinkModelFunctionMapper.insert(function);

        // 3. 如果创建的是属性，需要更新默认的事件和服务
        if (Objects.equals(createReqVO.getType(), IotThingModelTypeEnum.PROPERTY.getType())) {
            // 获取当前属性列表，并添加新插入的属性
            // TODO @haohao：是不是插入后，查询已经包含了 function
            List<IotThinkModelFunctionDO> propertyList = thinkModelFunctionMapper
                    .selectListByProductIdAndType(createReqVO.getProductId(), IotThingModelTypeEnum.PROPERTY.getType());
            propertyList.add(function);
            createDefaultEventsAndServices(createReqVO.getProductId(), createReqVO.getProductKey(), propertyList);
        }
        return function.getId();
    }

    private void validateIdentifierUnique(Long productId, String identifier) {
        IotThinkModelFunctionDO function = thinkModelFunctionMapper.selectByProductIdAndIdentifier(productId, identifier);
        if (function != null) {
            throw exception(THINK_MODEL_FUNCTION_EXISTS_BY_IDENTIFIER);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateThinkModelFunction(IotThinkModelFunctionSaveReqVO updateReqVO) {
        // 1. 校验功能是否存在
        validateThinkModelFunctionExists(updateReqVO.getId());
        // 2. 校验功能标识符是否唯一
        validateIdentifierUniqueForUpdate(updateReqVO.getId(), updateReqVO.getProductId(), updateReqVO.getIdentifier());

        // 3. 更新数据库
        IotThinkModelFunctionDO thinkModelFunction = IotThinkModelFunctionConvert.INSTANCE.convert(updateReqVO);
        thinkModelFunctionMapper.updateById(thinkModelFunction);

        // 4. 如果更新的是属性，需要更新默认的事件和服务
        if (Objects.equals(updateReqVO.getType(), IotThingModelTypeEnum.PROPERTY.getType())) {
            // 获取当前属性列表，更新其中的属性
            // TODO @haohao：是不是更新后，查询出来的，已经是最新的 thinkModelFunction
            List<IotThinkModelFunctionDO> propertyList = thinkModelFunctionMapper
                    .selectListByProductIdAndType(updateReqVO.getProductId(), IotThingModelTypeEnum.PROPERTY.getType());
            for (int i = 0; i < propertyList.size(); i++) {
                if (propertyList.get(i).getId().equals(thinkModelFunction.getId())) {
                    propertyList.set(i, thinkModelFunction);
                    break;
                }
            }
            createDefaultEventsAndServices(updateReqVO.getProductId(), updateReqVO.getProductKey(), propertyList);
        }
    }

    private void validateIdentifierUniqueForUpdate(Long id, Long productId, String identifier) {
        IotThinkModelFunctionDO function = thinkModelFunctionMapper.selectByProductIdAndIdentifier(productId, identifier);
        if (function != null && ObjectUtil.notEqual(function.getId(), id)) {
            throw exception(THINK_MODEL_FUNCTION_EXISTS_BY_IDENTIFIER);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteThinkModelFunction(Long id) {
        // 1. 校验功能是否存在
        IotThinkModelFunctionDO functionDO = thinkModelFunctionMapper.selectById(id);
        if (functionDO == null) {
            throw exception(THINK_MODEL_FUNCTION_NOT_EXISTS);
        }

        // 2. 删除功能
        thinkModelFunctionMapper.deleteById(id);

        // 3. 如果删除的是属性，需要更新默认的事件和服务
        if (Objects.equals(functionDO.getType(), IotThingModelTypeEnum.PROPERTY.getType())) {
            // 获取当前属性列表，移除已删除的属性
            // TODO @haohao：是不是删除后，已经没有 id 对应的记录啦？
            List<IotThinkModelFunctionDO> propertyList = thinkModelFunctionMapper
                    .selectListByProductIdAndType(functionDO.getProductId(), IotThingModelTypeEnum.PROPERTY.getType());
            propertyList.removeIf(property -> property.getId().equals(id));
            createDefaultEventsAndServices(functionDO.getProductId(), functionDO.getProductKey(), propertyList);
        }
    }

    /**
     * 校验功能是否存在
     *
     * @param id 功能编号
     */
    private void validateThinkModelFunctionExists(Long id) {
        if (thinkModelFunctionMapper.selectById(id) == null) {
            throw exception(THINK_MODEL_FUNCTION_NOT_EXISTS);
        }
    }

    @Override
    public IotThinkModelFunctionDO getThinkModelFunction(Long id) {
        return thinkModelFunctionMapper.selectById(id);
    }

    @Override
    public List<IotThinkModelFunctionDO> getThinkModelFunctionListByProductId(Long productId) {
        return thinkModelFunctionMapper.selectListByProductId(productId);
    }

    /**
     * 创建默认的事件和服务
     */
    public void createDefaultEventsAndServices(Long productId, String productKey, List<IotThinkModelFunctionDO> propertyList) {
        // 1. 生成新的事件和服务列表
        List<IotThinkModelFunctionDO> newFunctionList = new ArrayList<>();
        // 生成属性上报事件
        ThingModelEvent propertyPostEvent = generatePropertyPostEvent(propertyList);
        if (propertyPostEvent != null) {
            IotThinkModelFunctionDO eventFunction = buildEventFunctionDO(productId, productKey, propertyPostEvent);
            newFunctionList.add(eventFunction);
        }
        // 生成属性设置服务
        ThingModelService propertySetService = generatePropertySetService(propertyList);
        if (propertySetService != null) {
            IotThinkModelFunctionDO setServiceFunction = buildServiceFunctionDO(productId, productKey, propertySetService);
            newFunctionList.add(setServiceFunction);
        }
        // 生成属性获取服务
        ThingModelService propertyGetService = generatePropertyGetService(propertyList);
        if (propertyGetService != null) {
            IotThinkModelFunctionDO getServiceFunction = buildServiceFunctionDO(productId, productKey, propertyGetService);
            newFunctionList.add(getServiceFunction);
        }

        // 2. 获取数据库中的默认的旧事件和服务列表
        List<IotThinkModelFunctionDO> oldFunctionList = thinkModelFunctionMapper.selectListByProductIdAndIdentifiersAndTypes(
                productId,
                Arrays.asList("post", "set", "get"),
                Arrays.asList(IotThingModelTypeEnum.EVENT.getType(), IotThingModelTypeEnum.SERVICE.getType())
        );

        // 3.1 使用 diffList 方法比较新旧列表
        List<List<IotThinkModelFunctionDO>> diffResult = diffList(oldFunctionList, newFunctionList,
                // TODO @haohao：是不是用 id 比较相同就 ok 哈。如果可以的化，下面的 update 可以更简单
                (oldFunc, newFunc) -> Objects.equals(oldFunc.getIdentifier(), newFunc.getIdentifier())
                        && Objects.equals(oldFunc.getType(), newFunc.getType()));
        List<IotThinkModelFunctionDO> createList = diffResult.get(0); // 需要新增的
        List<IotThinkModelFunctionDO> updateList = diffResult.get(1); // 需要更新的
        List<IotThinkModelFunctionDO> deleteList = diffResult.get(2); // 需要删除的
        // 3.2 批量执行数据库操作
        if (CollUtil.isNotEmpty(createList)) {
            thinkModelFunctionMapper.insertBatch(createList);
        }
        if (CollUtil.isNotEmpty(updateList)) {
            for (IotThinkModelFunctionDO updateFunc : updateList) {
                // 设置 ID，以便更新
                IotThinkModelFunctionDO oldFunc = findFunctionByIdentifierAndType(
                        oldFunctionList, updateFunc.getIdentifier(), updateFunc.getType());
                if (oldFunc != null) {
                    updateFunc.setId(oldFunc.getId());
                    thinkModelFunctionMapper.updateById(updateFunc);
                }
            }
        }
        if (CollUtil.isNotEmpty(deleteList)) {
            // TODO @haohao：使用 convertSet 简化。
            List<Long> idsToDelete = deleteList.stream().map(IotThinkModelFunctionDO::getId).collect(Collectors.toList());
            thinkModelFunctionMapper.deleteByIds(idsToDelete);
        }
    }

    /**
     * 根据标识符和类型查找功能对象
     */
    private IotThinkModelFunctionDO findFunctionByIdentifierAndType(List<IotThinkModelFunctionDO> functionList,
                                                                    String identifier, Integer type) {
        // TODO @haohao：这个可以使用 CollUtil.findOne 简化只有一行
        return functionList.stream()
                .filter(func -> Objects.equals(func.getIdentifier(), identifier) && Objects.equals(func.getType(), type))
                .findFirst()
                .orElse(null);
    }

    /**
     * 构建事件功能对象
     */
    private IotThinkModelFunctionDO buildEventFunctionDO(Long productId, String productKey, ThingModelEvent event) {
        return new IotThinkModelFunctionDO()
                .setProductId(productId)
                .setProductKey(productKey)
                .setIdentifier(event.getIdentifier())
                .setName(event.getName())
                .setDescription(event.getDescription())
                .setType(IotThingModelTypeEnum.EVENT.getType())
                .setEvent(event);
    }

    /**
     * 构建服务功能对象
     */
    private IotThinkModelFunctionDO buildServiceFunctionDO(Long productId, String productKey, ThingModelService service) {
        return new IotThinkModelFunctionDO()
                .setProductId(productId)
                .setProductKey(productKey)
                .setIdentifier(service.getIdentifier())
                .setName(service.getName())
                .setDescription(service.getDescription())
                .setType(IotThingModelTypeEnum.SERVICE.getType())
                .setService(service);
    }

    /**
     * 生成属性上报事件
     */
    private ThingModelEvent generatePropertyPostEvent(List<IotThinkModelFunctionDO> propertyList) {
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
        for (IotThinkModelFunctionDO functionDO : propertyList) {
            ThingModelProperty property = functionDO.getProperty();
            ThingModelArgument arg = new ThingModelArgument()
                    .setIdentifier(property.getIdentifier())
                    .setName(property.getName())
                    .setDataType(property.getDataType())
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
    private ThingModelService generatePropertySetService(List<IotThinkModelFunctionDO> propertyList) {
        if (propertyList == null || propertyList.isEmpty()) {
            return null;
        }

        List<ThingModelArgument> inputData = new ArrayList<>();
        for (IotThinkModelFunctionDO functionDO : propertyList) {
            ThingModelProperty property = functionDO.getProperty();
            if (IotAccessModeEnum.WRITE.getMode().equals(property.getAccessMode()) || IotAccessModeEnum.READ_WRITE.getMode().equals(property.getAccessMode())) {
                ThingModelArgument arg = new ThingModelArgument()
                        .setIdentifier(property.getIdentifier())
                        .setName(property.getName())
                        .setDataType(property.getDataType())
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
    private ThingModelService generatePropertyGetService(List<IotThinkModelFunctionDO> propertyList) {
        if (propertyList == null || propertyList.isEmpty()) {
            return null;
        }

        List<ThingModelArgument> outputData = new ArrayList<>();
        for (IotThinkModelFunctionDO functionDO : propertyList) {
            ThingModelProperty property = functionDO.getProperty();
            // TODO @haohao：ObjectUtils.equalsAny()，进一步简化判断
            if (IotAccessModeEnum.READ.getMode().equals(property.getAccessMode())
                    || IotAccessModeEnum.READ_WRITE.getMode().equals(property.getAccessMode())) {
                ThingModelArgument arg = new ThingModelArgument()
                        .setIdentifier(property.getIdentifier())
                        .setName(property.getName())
                        .setDataType(property.getDataType())
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

        // 创建数组类型，元素类型为文本类型（字符串）
        ThingModelArrayType arrayType = new ThingModelArrayType();
        arrayType.setType("array");
        ThingModelArraySpecs arraySpecs = new ThingModelArraySpecs();
        ThingModelTextType textType = new ThingModelTextType();
        textType.setType("text");
        arraySpecs.setItem(textType);
        arrayType.setSpecs(arraySpecs);
        inputArg.setDataType(arrayType);

        service.setInputData(Collections.singletonList(inputArg));
        service.setOutputData(outputData);
        return service;
    }

}
