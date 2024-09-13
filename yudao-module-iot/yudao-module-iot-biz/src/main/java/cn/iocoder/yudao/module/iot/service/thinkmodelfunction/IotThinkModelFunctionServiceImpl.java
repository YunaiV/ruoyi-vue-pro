package cn.iocoder.yudao.module.iot.service.thinkmodelfunction;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.*;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.vo.IotThinkModelFunctionSaveReqVO;
import cn.iocoder.yudao.module.iot.convert.thinkmodelfunction.IotThinkModelFunctionConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.thinkmodelfunction.IotThinkModelFunctionDO;
import cn.iocoder.yudao.module.iot.dal.mysql.thinkmodelfunction.IotThinkModelFunctionMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.THINK_MODEL_FUNCTION_EXISTS_BY_PRODUCT_KEY;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.THINK_MODEL_FUNCTION_NOT_EXISTS;

@Slf4j
@Service
@Validated
public class IotThinkModelFunctionServiceImpl implements IotThinkModelFunctionService {

    @Resource
    private IotThinkModelFunctionMapper thinkModelFunctionMapper;

    @Override
    public Long createThinkModelFunction(IotThinkModelFunctionSaveReqVO createReqVO) {
        log.info("创建物模型，参数：{}", createReqVO);
        // 验证 ProductKey 对应的产品物模型是否已存在
        validateThinkModelFunctionNotExistsByProductKey(createReqVO.getProductKey());
        // 转换请求对象为数据对象
        IotThinkModelFunctionDO thinkModelFunction = IotThinkModelFunctionConvert.INSTANCE.convert(createReqVO);
        // 自动生成属性上报事件和属性设置、获取服务
        generateDefaultEventsAndServices(createReqVO, thinkModelFunction);
        // 插入数据库
        thinkModelFunctionMapper.insert(thinkModelFunction);
        // 返回生成的 ID
        return thinkModelFunction.getId();
    }

    private void validateThinkModelFunctionNotExistsByProductKey(String productKey) {
        if (thinkModelFunctionMapper.selectByProductKey(productKey) != null) {
            throw exception(THINK_MODEL_FUNCTION_EXISTS_BY_PRODUCT_KEY);
        }
    }

    @Override
    public void deleteThinkModelFunction(Long id) {
        log.info("删除物模型，id：{}", id);
        // 校验物模型是否存在
        validateThinkModelFunctionExists(id);
        // 删除物模型
        thinkModelFunctionMapper.deleteById(id);
    }

    private void validateThinkModelFunctionExists(Long id) {
        if (thinkModelFunctionMapper.selectById(id) == null) {
            throw exception(THINK_MODEL_FUNCTION_NOT_EXISTS);
        }
    }

    @Override
    public IotThinkModelFunctionDO getThinkModelFunctionByProductKey(String productKey) {
        return thinkModelFunctionMapper.selectByProductKey(productKey);
    }

    @Override
    public IotThinkModelFunctionDO getThinkModelFunctionByProductId(Long productId) {
        return thinkModelFunctionMapper.selectByProductId(productId);
    }

    @Override
    public void updateThinkModelFunction(IotThinkModelFunctionSaveReqVO updateReqVO) {
        log.info("更新物模型，参数：{}", updateReqVO);
        // 校验物模型是否存在
        validateThinkModelFunctionExists(updateReqVO.getId());
        // 校验 ProductKey 是否唯一
        validateProductKeyUnique(updateReqVO.getId(), updateReqVO.getProductKey());
        // 转换请求对象为数据对象
        IotThinkModelFunctionDO thinkModelFunction = IotThinkModelFunctionConvert.INSTANCE.convert(updateReqVO);
        // 自动生成或更新属性上报事件和属性设置、获取服务
        generateDefaultEventsAndServices(updateReqVO, thinkModelFunction);
        // 更新数据库
        thinkModelFunctionMapper.updateById(thinkModelFunction);
    }

    private void validateProductKeyUnique(Long id, String productKey) {
        IotThinkModelFunctionDO existingFunction = thinkModelFunctionMapper.selectByProductKey(productKey);
        if (existingFunction != null && !existingFunction.getId().equals(id)) {
            throw exception(THINK_MODEL_FUNCTION_EXISTS_BY_PRODUCT_KEY);
        }
    }

    /**
     * @ TODO 还要再优化
     * 根据属性列表，自动生成属性上报事件和属性设置、获取服务
     */
    private void generateDefaultEventsAndServices(IotThinkModelFunctionSaveReqVO reqVO, IotThinkModelFunctionDO thinkModelFunction) {
        // 获取属性列表
        List<ThingModelProperty> properties = reqVO.getProperties();
        if (properties == null) {
            properties = new ArrayList<>();
        }

        // 生成属性上报事件
        List<ThingModelEvent> events = reqVO.getEvents() != null ? new ArrayList<>(reqVO.getEvents()) : new ArrayList<>();
        ThingModelEvent propertyPostEvent = generatePropertyPostEvent(properties);
        events.add(propertyPostEvent);

        // 生成属性设置和获取服务
        List<ThingModelService> services = reqVO.getServices() != null ? new ArrayList<>(reqVO.getServices()) : new ArrayList<>();
        ThingModelService propertySetService = generatePropertySetService(properties);
        if (propertySetService != null) {
            services.add(propertySetService);
        }
        ThingModelService propertyGetService = generatePropertyGetService(properties);
        if (propertyGetService != null) {
            services.add(propertyGetService);
        }

        // 更新 thinkModelFunction 对象的 events 和 services 字段
        thinkModelFunction.setEvents(JSONUtil.toJsonStr(events));
        thinkModelFunction.setServices(JSONUtil.toJsonStr(services));
    }

    /**
     * 生成属性上报事件
     */
    private ThingModelEvent generatePropertyPostEvent(List<ThingModelProperty> properties) {
        ThingModelEvent event = new ThingModelEvent();
        event.setIdentifier("post");
        event.setName("属性上报");
        event.setType("info");
        event.setDescription("属性上报事件");
        event.setMethod("thing.event.property.post");

        // 将属性列表转换为事件的输出参数
        List<ThingModelArgument> outputData = new ArrayList<>();
        for (ThingModelProperty property : properties) {
            ThingModelArgument arg = new ThingModelArgument();
            arg.setIdentifier(property.getIdentifier());
            arg.setName(property.getName());
            arg.setDataType(property.getDataType());
            arg.setDescription(property.getDescription());
            arg.setDirection("output"); // 设置为输出参数
            outputData.add(arg);
        }
        event.setOutputData(outputData);

        return event;
    }

    /**
     * 生成属性设置服务
     */
    private ThingModelService generatePropertySetService(List<ThingModelProperty> properties) {
        List<ThingModelArgument> inputData = new ArrayList<>();
        for (ThingModelProperty property : properties) {
            if ("w".equals(property.getAccessMode()) || "rw".equals(property.getAccessMode())) {
                ThingModelArgument arg = new ThingModelArgument();
                arg.setIdentifier(property.getIdentifier());
                arg.setName(property.getName());
                arg.setDataType(property.getDataType());
                arg.setDescription(property.getDescription());
                arg.setDirection("input"); // 设置为输入参数
                inputData.add(arg);
            }
        }
        if (inputData.isEmpty()) {
            // 如果没有可写属性，不生成属性设置服务
            return null;
        }

        ThingModelService service = new ThingModelService();
        service.setIdentifier("set");
        service.setName("属性设置");
        service.setCallType("async");
        service.setDescription("属性设置服务");
        service.setMethod("thing.service.property.set");
        service.setInputData(inputData);
        // 属性设置服务一般不需要输出参数
        service.setOutputData(new ArrayList<>());

        return service;
    }

    /**
     * 生成属性获取服务
     */
    private ThingModelService generatePropertyGetService(List<ThingModelProperty> properties) {
        List<ThingModelArgument> outputData = new ArrayList<>();
        for (ThingModelProperty property : properties) {
            if ("r".equals(property.getAccessMode()) || "rw".equals(property.getAccessMode())) {
                ThingModelArgument arg = new ThingModelArgument();
                arg.setIdentifier(property.getIdentifier());
                arg.setName(property.getName());
                arg.setDataType(property.getDataType());
                arg.setDescription(property.getDescription());
                arg.setDirection("output"); // 设置为输出参数
                outputData.add(arg);
            }
        }
        if (outputData.isEmpty()) {
            // 如果没有可读属性，不生成属性获取服务
            return null;
        }

        ThingModelService service = new ThingModelService();
        service.setIdentifier("get");
        service.setName("属性获取");
        service.setCallType("async");
        service.setDescription("属性获取服务");
        service.setMethod("thing.service.property.get");

        // 定义输入参数：属性标识符列表
        ThingModelArgument inputArg = new ThingModelArgument();
        inputArg.setIdentifier("properties");
        inputArg.setName("属性标识符列表");
        inputArg.setDescription("需要获取的属性标识符列表");
        inputArg.setDirection("input"); // 设置为输入参数

        // 创建数组类型，元素类型为文本类型（字符串）
        ThingModelArrayType arrayType = new ThingModelArrayType();
        arrayType.setType("array");
        ThingModelArraySpecs arraySpecs = new ThingModelArraySpecs();
        // 不指定数组长度，size 可以为 0 或者省略
        ThingModelTextType textType = new ThingModelTextType();
        textType.setType("text");
        // 如果有需要，可以设置 TextType 的 specs，如长度限制
        arraySpecs.setItem(textType);
        arrayType.setSpecs(arraySpecs);

        inputArg.setDataType(arrayType);

        service.setInputData(Collections.singletonList(inputArg));
        service.setOutputData(outputData);

        return service;
    }
}
