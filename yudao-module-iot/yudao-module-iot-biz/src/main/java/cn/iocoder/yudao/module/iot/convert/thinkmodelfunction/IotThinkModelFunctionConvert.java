package cn.iocoder.yudao.module.iot.convert.thinkmodelfunction;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.ThingModelEvent;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.ThingModelProperty;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.ThingModelService;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.vo.IotThinkModelFunctionRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.vo.IotThinkModelFunctionSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thinkmodelfunction.IotThinkModelFunctionDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface IotThinkModelFunctionConvert {

    IotThinkModelFunctionConvert INSTANCE = Mappers.getMapper(IotThinkModelFunctionConvert.class);

    // 将 SaveReqVO 转换为 DO 对象，处理 properties, services, events 字段
    @Mapping(target = "properties", expression = "java(convertPropertiesToJson(bean.getProperties()))")
    @Mapping(target = "services", expression = "java(convertServicesToJson(bean.getServices()))")
    @Mapping(target = "events", expression = "java(convertEventsToJson(bean.getEvents()))")
    IotThinkModelFunctionDO convert(IotThinkModelFunctionSaveReqVO bean);

    default String convertPropertiesToJson(List<ThingModelProperty> properties) {
        return properties != null ? JSONUtil.toJsonStr(properties) : "[]";
    }

    default String convertServicesToJson(List<ThingModelService> services) {
        return services != null ? JSONUtil.toJsonStr(services) : "[]";
    }

    default String convertEventsToJson(List<ThingModelEvent> events) {
        return events != null ? JSONUtil.toJsonStr(events) : "[]";
    }

    // 将 DO 转换为 RespVO 对象，处理 properties, services, events 字段
    @Mapping(target = "properties", expression = "java(convertJsonToProperties(bean.getProperties()))")
    @Mapping(target = "services", expression = "java(convertJsonToServices(bean.getServices()))")
    @Mapping(target = "events", expression = "java(convertJsonToEvents(bean.getEvents()))")
    IotThinkModelFunctionRespVO convert(IotThinkModelFunctionDO bean);

    default List<ThingModelProperty> convertJsonToProperties(String propertiesJson) {
        return propertiesJson != null ? JSONUtil.toList(propertiesJson, ThingModelProperty.class) : new ArrayList<>();
    }

    default List<ThingModelService> convertJsonToServices(String servicesJson) {
        return servicesJson != null ? JSONUtil.toList(servicesJson, ThingModelService.class) : new ArrayList<>();
    }

    default List<ThingModelEvent> convertJsonToEvents(String eventsJson) {
        return eventsJson != null ? JSONUtil.toList(eventsJson, ThingModelEvent.class) : new ArrayList<>();
    }

    // 批量转换 DO 列表到 RespVO 列表
    List<IotThinkModelFunctionRespVO> convertList(List<IotThinkModelFunctionDO> list);
}

