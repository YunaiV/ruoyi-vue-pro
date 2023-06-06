package cn.iocoder.yudao.module.jl.mapper.crm;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.entity.crm.SalesleadManager;
import org.mapstruct.*;



@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface SalesleadManagerMapper {
    SalesleadManager toEntity(SalesleadManagerCreateReqVO dto);

    SalesleadManager toEntity(SalesleadManagerUpdateReqVO dto);

    SalesleadManagerRespVO toDto(SalesleadManager entity);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    SalesleadManager partialUpdate(SalesleadManagerUpdateReqVO dto, @MappingTarget SalesleadManager entity);

    List<SalesleadManagerExcelVO> toExcelList(List<SalesleadManager> list);

    PageResult<SalesleadManagerRespVO> toPage(PageResult<SalesleadManager> page);
}