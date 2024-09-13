package cn.iocoder.yudao.module.iot.convert.thinkmodelfunction;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.vo.IotThingModelProperty;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.vo.IotThinkModelFunctionRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.vo.IotThinkModelFunctionSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thinkmodelfunction.IotThinkModelFunctionDO;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface IotThinkModelFunctionConvert {

    IotThinkModelFunctionConvert INSTANCE = Mappers.getMapper(IotThinkModelFunctionConvert.class);

    // 将 SaveReqVO 转换为 DO
    @Mapping(target = "properties", ignore = true)
    IotThinkModelFunctionDO convert(IotThinkModelFunctionSaveReqVO bean);

    // 将 DO 转换为 RespVO
    @Mapping(target = "properties", ignore = true)
    IotThinkModelFunctionRespVO convert(IotThinkModelFunctionDO bean);

    // 处理 properties 字段的转换，从 VO 到 DO
    @AfterMapping
    default void convertPropertiesToDO(IotThinkModelFunctionSaveReqVO source, @MappingTarget IotThinkModelFunctionDO target) {
        target.setProperties(JSONUtil.toJsonStr(source.getProperties()));
    }

    // 处理 properties 字段的转换，从 DO 到 VO
    @AfterMapping
    default void convertPropertiesToVO(IotThinkModelFunctionDO source, @MappingTarget IotThinkModelFunctionRespVO target) {
        target.setProperties(JSONUtil.toList(source.getProperties(), IotThingModelProperty.class));
    }

    // 批量转换 DO 列表到 RespVO 列表
    List<IotThinkModelFunctionRespVO> convertList(List<IotThinkModelFunctionDO> list);

    // 批量转换处理 properties 字段
    @AfterMapping
    default void convertPropertiesListToVO(List<IotThinkModelFunctionDO> sourceList, @MappingTarget List<IotThinkModelFunctionRespVO> targetList) {
        for (int i = 0; i < sourceList.size(); i++) {
            convertPropertiesToVO(sourceList.get(i), targetList.get(i));
        }
    }
}
