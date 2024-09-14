package cn.iocoder.yudao.module.iot.convert.thinkmodelfunction;

import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.ThingModelEvent;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.ThingModelProperty;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.ThingModelService;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.vo.IotThinkModelFunctionRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.vo.IotThinkModelFunctionSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thinkmodelfunction.IotThinkModelFunctionDO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface IotThinkModelFunctionConvert {

    IotThinkModelFunctionConvert INSTANCE = Mappers.getMapper(IotThinkModelFunctionConvert.class);

    ObjectMapper objectMapper = new ObjectMapper();

    // 将 SaveReqVO 转换为 DO 对象，处理 properties, services, events 字段
    @Mapping(target = "properties", expression = "java(convertPropertiesToJson(bean.getProperties()))")
    @Mapping(target = "services", expression = "java(convertServicesToJson(bean.getServices()))")
    @Mapping(target = "events", expression = "java(convertEventsToJson(bean.getEvents()))")
    IotThinkModelFunctionDO convert(IotThinkModelFunctionSaveReqVO bean);

    default String convertPropertiesToJson(List<ThingModelProperty> properties) {
        try {
            return properties != null ? objectMapper.writeValueAsString(properties) : "[]";
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化 properties 时发生错误", e);
        }
    }

    default String convertServicesToJson(List<ThingModelService> services) {
        try {
            return services != null ? objectMapper.writeValueAsString(services) : "[]";
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化 services 时发生错误", e);
        }
    }

    default String convertEventsToJson(List<ThingModelEvent> events) {
        try {
            return events != null ? objectMapper.writeValueAsString(events) : "[]";
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化 events 时发生错误", e);
        }
    }

    // 将 DO 转换为 RespVO 对象，处理 properties, services, events 字段
    @Mapping(target = "properties", expression = "java(convertJsonToProperties(bean.getProperties()))")
    @Mapping(target = "services", expression = "java(convertJsonToServices(bean.getServices()))")
    @Mapping(target = "events", expression = "java(convertJsonToEvents(bean.getEvents()))")
    IotThinkModelFunctionRespVO convert(IotThinkModelFunctionDO bean);

    default List<ThingModelProperty> convertJsonToProperties(String propertiesJson) {
        try {
            return propertiesJson != null ? objectMapper.readValue(propertiesJson, objectMapper.getTypeFactory().constructCollectionType(List.class, ThingModelProperty.class)) : new ArrayList<>();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("反序列化 properties 时发生错误", e);
        }
    }

    default List<ThingModelService> convertJsonToServices(String servicesJson) {
        try {
            return servicesJson != null ? objectMapper.readValue(servicesJson, objectMapper.getTypeFactory().constructCollectionType(List.class, ThingModelService.class)) : new ArrayList<>();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("反序列化 services 时发生错误", e);
        }
    }

    default List<ThingModelEvent> convertJsonToEvents(String eventsJson) {
        try {
            return eventsJson != null ? objectMapper.readValue(eventsJson, objectMapper.getTypeFactory().constructCollectionType(List.class, ThingModelEvent.class)) : new ArrayList<>();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("反序列化 events 时发生错误", e);
        }
    }

    // 批量转换 DO 列表到 RespVO 列表
    List<IotThinkModelFunctionRespVO> convertList(List<IotThinkModelFunctionDO> list);
}