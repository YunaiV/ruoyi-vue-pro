package cn.iocoder.yudao.module.jl.mapper.crm;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.entity.crm.SalesleadCompetitor;
import org.mapstruct.*;



@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface SalesleadCompetitorMapper {
    SalesleadCompetitor toEntity(SalesleadCompetitorCreateReqVO dto);

    List<SalesleadCompetitor> toEntityList(List<SalesleadCompetitorItemVO> dto);

    SalesleadCompetitor toEntity(SalesleadCompetitorUpdateReqVO dto);

    SalesleadCompetitorRespVO toDto(SalesleadCompetitor entity);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    SalesleadCompetitor partialUpdate(SalesleadCompetitorUpdateReqVO dto, @MappingTarget SalesleadCompetitor entity);

    List<SalesleadCompetitorExcelVO> toExcelList(List<SalesleadCompetitor> list);

    PageResult<SalesleadCompetitorRespVO> toPage(PageResult<SalesleadCompetitor> page);
}