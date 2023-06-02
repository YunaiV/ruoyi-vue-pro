package cn.iocoder.yudao.module.jl.mapper.project;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.jl.controller.admin.project.vo.*;
import cn.iocoder.yudao.module.jl.entity.project.ProjectSchedule;
import org.mapstruct.*;



@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProjectScheduleMapper {
    ProjectSchedule toEntity(ProjectScheduleCreateReqVO dto);

    ProjectSchedule toEntity(ProjectScheduleUpdateReqVO dto);

    ProjectScheduleRespVO toDto(ProjectSchedule entity);

    ProjectSchedule toEntity(ProjectScheduleSaveReqVO entity);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ProjectSchedule partialUpdate(ProjectScheduleUpdateReqVO dto, @MappingTarget ProjectSchedule entity);

    List<ProjectScheduleExcelVO> toExcelList(List<ProjectSchedule> list);

    PageResult<ProjectScheduleRespVO> toPage(PageResult<ProjectSchedule> page);
}