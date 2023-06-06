package cn.iocoder.yudao.module.jl.mapper.crm;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.entity.crm.Saleslead;
import org.mapstruct.*;



@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface SalesleadMapper {
    Saleslead toEntity(SalesleadCreateReqVO dto);

    Saleslead toEntity(SalesleadUpdateReqVO dto);

    SalesleadRespVO toDto(Saleslead entity);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Saleslead partialUpdate(SalesleadUpdateReqVO dto, @MappingTarget Saleslead entity);

    List<SalesleadExcelVO> toExcelList(List<Saleslead> list);

    PageResult<SalesleadRespVO> toPage(PageResult<Saleslead> page);
}