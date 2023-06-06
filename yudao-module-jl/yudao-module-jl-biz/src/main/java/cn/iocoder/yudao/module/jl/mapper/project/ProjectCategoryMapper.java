package cn.iocoder.yudao.module.jl.mapper.project;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.jl.controller.admin.project.vo.*;
import cn.iocoder.yudao.module.jl.entity.project.ProjectCategory;
import org.mapstruct.*;



@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProjectCategoryMapper {
    ProjectCategory toEntity(ProjectCategoryCreateReqVO dto);

    ProjectCategory toEntity(ProjectCategoryUpdateReqVO dto);

    ProjectCategoryRespVO toDto(ProjectCategory entity);

    ProjectCategory toEntity(ProjectCategoryWithSupplyAndChargeItemVO dto);

    List<ProjectCategory> toEntity(List<ProjectCategoryWithSupplyAndChargeItemVO> list);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ProjectCategory partialUpdate(ProjectCategoryUpdateReqVO dto, @MappingTarget ProjectCategory entity);

    List<ProjectCategoryExcelVO> toExcelList(List<ProjectCategory> list);

    PageResult<ProjectCategoryRespVO> toPage(PageResult<ProjectCategory> page);
}