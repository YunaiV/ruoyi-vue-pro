package cn.iocoder.yudao.module.iot.convert.thinkmodel;

import cn.iocoder.yudao.module.iot.controller.admin.thinkmodel.model.ThinkModelEvent;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodel.model.ThinkModelProperty;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodel.model.ThinkModelService;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodel.vo.IotProductThinkModelRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodel.vo.IotProductThinkModelSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thinkmodel.IotProductThinkModelDO;
import cn.iocoder.yudao.module.iot.enums.thinkmodel.IotProductThinkModelTypeEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Objects;

@Mapper
public interface IotProductThinkModelConvert {

    IotProductThinkModelConvert INSTANCE = Mappers.getMapper(IotProductThinkModelConvert.class);

    // 将 SaveReqVO 转换为 DO
    @Mapping(target = "property", expression = "java(convertToProperty(bean))")
    @Mapping(target = "event", expression = "java(convertToEvent(bean))")
    @Mapping(target = "service", expression = "java(convertToService(bean))")
    IotProductThinkModelDO convert(IotProductThinkModelSaveReqVO bean);

    // 将 DO 转换为 RespVO
    @Mapping(target = "property", source = "property")
    @Mapping(target = "event", source = "event")
    @Mapping(target = "service", source = "service")
    IotProductThinkModelRespVO convert(IotProductThinkModelDO bean);

    // 批量转换
    List<IotProductThinkModelRespVO> convertList(List<IotProductThinkModelDO> list);

    @Named("convertToProperty")
    default ThinkModelProperty convertToProperty(IotProductThinkModelSaveReqVO bean) {
        if (Objects.equals(bean.getType(), IotProductThinkModelTypeEnum.PROPERTY.getType())) {
            return bean.getProperty();
        }
        return null;
    }

    @Named("convertToEvent")
    default ThinkModelEvent convertToEvent(IotProductThinkModelSaveReqVO bean) {
        if (Objects.equals(bean.getType(), IotProductThinkModelTypeEnum.EVENT.getType())) {
            return bean.getEvent();
        }
        return null;
    }

    @Named("convertToService")
    default ThinkModelService convertToService(IotProductThinkModelSaveReqVO bean) {
        if (Objects.equals(bean.getType(), IotProductThinkModelTypeEnum.SERVICE.getType())) {
            return bean.getService();
        }
        return null;
    }

}
