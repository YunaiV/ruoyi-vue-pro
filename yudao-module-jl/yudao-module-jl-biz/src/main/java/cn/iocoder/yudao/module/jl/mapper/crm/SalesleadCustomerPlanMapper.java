package cn.iocoder.yudao.module.jl.mapper.crm;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.entity.crm.SalesleadCompetitor;
import cn.iocoder.yudao.module.jl.entity.crm.SalesleadCustomerPlan;
import org.mapstruct.*;



@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface SalesleadCustomerPlanMapper {
    SalesleadCustomerPlan toEntity(SalesleadCustomerPlanCreateReqVO dto);

    SalesleadCustomerPlan toEntity(SalesleadCustomerPlanUpdateReqVO dto);

    SalesleadCustomerPlanRespVO toDto(SalesleadCustomerPlan entity);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    SalesleadCustomerPlan partialUpdate(SalesleadCustomerPlanUpdateReqVO dto, @MappingTarget SalesleadCustomerPlan entity);

    List<SalesleadCustomerPlanExcelVO> toExcelList(List<SalesleadCustomerPlan> list);

    PageResult<SalesleadCustomerPlanRespVO> toPage(PageResult<SalesleadCustomerPlan> page);

    List<SalesleadCustomerPlan> toEntityList(List<SalesleadCustomerPlanItemVO> competitorQuotations);
}