package cn.iocoder.yudao.module.jl.mapper.project;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.jl.controller.admin.project.vo.*;
import cn.iocoder.yudao.module.jl.entity.project.ProjectSop;
import org.mapstruct.*;



@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProjectSopMapper {
    ProjectSop toEntity(ProjectSopCreateReqVO dto);

    ProjectSop toEntity(ProjectSopUpdateReqVO dto);

    List<ProjectSop> toEntity(List<ProjectSopBaseVO> dto);

    ProjectSopRespVO toDto(ProjectSop entity);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ProjectSop partialUpdate(ProjectSopUpdateReqVO dto, @MappingTarget ProjectSop entity);

    List<ProjectSopExcelVO> toExcelList(List<ProjectSop> list);

    PageResult<ProjectSopRespVO> toPage(PageResult<ProjectSop> page);
}