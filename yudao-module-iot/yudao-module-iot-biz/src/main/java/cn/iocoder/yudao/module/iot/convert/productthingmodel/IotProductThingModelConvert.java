package cn.iocoder.yudao.module.iot.convert.productthingmodel;

import cn.iocoder.yudao.module.iot.controller.admin.productthingmodel.thingmodel.ThingModelEvent;
import cn.iocoder.yudao.module.iot.controller.admin.productthingmodel.thingmodel.ThingModelProperty;
import cn.iocoder.yudao.module.iot.controller.admin.productthingmodel.thingmodel.ThingModelService;
import cn.iocoder.yudao.module.iot.controller.admin.productthingmodel.vo.IotProductThingModelRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.productthingmodel.vo.IotProductThingModelSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.productthingmodel.IotProductThingModelDO;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotProductThingModelTypeEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Objects;

@Mapper
public interface IotProductThingModelConvert {

    IotProductThingModelConvert INSTANCE = Mappers.getMapper(IotProductThingModelConvert.class);

    // 将 SaveReqVO 转换为 DO
    @Mapping(target = "property", expression = "java(convertToProperty(bean))")
    @Mapping(target = "event", expression = "java(convertToEvent(bean))")
    @Mapping(target = "service", expression = "java(convertToService(bean))")
    IotProductThingModelDO convert(IotProductThingModelSaveReqVO bean);

    // 将 DO 转换为 RespVO
    @Mapping(target = "property", source = "property")
    @Mapping(target = "event", source = "event")
    @Mapping(target = "service", source = "service")
    IotProductThingModelRespVO convert(IotProductThingModelDO bean);

    // 批量转换
    List<IotProductThingModelRespVO> convertList(List<IotProductThingModelDO> list);

    @Named("convertToProperty")
    default ThingModelProperty convertToProperty(IotProductThingModelSaveReqVO bean) {
        if (Objects.equals(bean.getType(), IotProductThingModelTypeEnum.PROPERTY.getType())) {
            return bean.getProperty();
        }
        return null;
    }

    @Named("convertToEvent")
    default ThingModelEvent convertToEvent(IotProductThingModelSaveReqVO bean) {
        if (Objects.equals(bean.getType(), IotProductThingModelTypeEnum.EVENT.getType())) {
            return bean.getEvent();
        }
        return null;
    }

    @Named("convertToService")
    default ThingModelService convertToService(IotProductThingModelSaveReqVO bean) {
        if (Objects.equals(bean.getType(), IotProductThingModelTypeEnum.SERVICE.getType())) {
            return bean.getService();
        }
        return null;
    }

}
