package cn.iocoder.yudao.module.jl.mapper.laboratory;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo.*;
import cn.iocoder.yudao.module.jl.entity.laboratory.ChargeItem;
import org.mapstruct.*;



@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ChargeItemMapper {
    ChargeItem toEntity(ChargeItemCreateReqVO dto);

    ChargeItem toEntity(ChargeItemUpdateReqVO dto);

    ChargeItemRespVO toDto(ChargeItem entity);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ChargeItem partialUpdate(ChargeItemUpdateReqVO dto, @MappingTarget ChargeItem entity);

    List<ChargeItemExcelVO> toExcelList(List<ChargeItem> list);

    PageResult<ChargeItemRespVO> toPage(PageResult<ChargeItem> page);
}