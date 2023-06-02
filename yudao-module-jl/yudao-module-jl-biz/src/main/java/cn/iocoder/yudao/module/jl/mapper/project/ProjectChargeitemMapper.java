package cn.iocoder.yudao.module.jl.mapper.project;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.jl.controller.admin.project.vo.*;
import cn.iocoder.yudao.module.jl.entity.project.ProjectChargeitem;
import org.mapstruct.*;



@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProjectChargeitemMapper {
    ProjectChargeitem toEntity(ProjectChargeitemCreateReqVO dto);

    ProjectChargeitem toEntity(ProjectChargeitemUpdateReqVO dto);

    ProjectChargeitemRespVO toDto(ProjectChargeitem entity);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ProjectChargeitem partialUpdate(ProjectChargeitemUpdateReqVO dto, @MappingTarget ProjectChargeitem entity);

    List<ProjectChargeitemExcelVO> toExcelList(List<ProjectChargeitem> list);

    PageResult<ProjectChargeitemRespVO> toPage(PageResult<ProjectChargeitem> page);
}