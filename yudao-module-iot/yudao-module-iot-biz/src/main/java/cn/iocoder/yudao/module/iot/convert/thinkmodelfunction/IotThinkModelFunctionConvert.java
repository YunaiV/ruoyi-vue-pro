package cn.iocoder.yudao.module.iot.convert.thinkmodelfunction;

import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.ThingModelEvent;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.ThingModelProperty;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.ThingModelService;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.vo.IotThinkModelFunctionRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.vo.IotThinkModelFunctionSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thinkmodelfunction.IotThinkModelFunctionDO;
import cn.iocoder.yudao.module.iot.enums.product.IotProductFunctionTypeEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Objects;

@Mapper
public interface IotThinkModelFunctionConvert {

    IotThinkModelFunctionConvert INSTANCE = Mappers.getMapper(IotThinkModelFunctionConvert.class);

    // 将 SaveReqVO 转换为 DO
    @Mapping(target = "property", expression = "java(convertToProperty(bean))")
    @Mapping(target = "event", expression = "java(convertToEvent(bean))")
    @Mapping(target = "service", expression = "java(convertToService(bean))")
    IotThinkModelFunctionDO convert(IotThinkModelFunctionSaveReqVO bean);

    default ThingModelProperty convertToProperty(IotThinkModelFunctionSaveReqVO bean) {
        if (Objects.equals(bean.getType(), IotProductFunctionTypeEnum.PROPERTY.getType())) {
            return bean.getProperty();
        }
        return null;
    }

    default ThingModelEvent convertToEvent(IotThinkModelFunctionSaveReqVO bean) {
        if (Objects.equals(bean.getType(), IotProductFunctionTypeEnum.EVENT.getType())) {
            return bean.getEvent();
        }
        return null;
    }

    default ThingModelService convertToService(IotThinkModelFunctionSaveReqVO bean) {
        if (Objects.equals(bean.getType(), IotProductFunctionTypeEnum.SERVICE.getType())) {
            return bean.getService();
        }
        return null;
    }

    // 将 DO 转换为 RespVO
    @Mapping(target = "property", source = "property")
    @Mapping(target = "event", source = "event")
    @Mapping(target = "service", source = "service")
    IotThinkModelFunctionRespVO convert(IotThinkModelFunctionDO bean);

    // 批量转换
    List<IotThinkModelFunctionRespVO> convertList(List<IotThinkModelFunctionDO> list);
}
