package cn.iocoder.yudao.module.jl.mapper.project;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.jl.controller.admin.project.vo.*;
import cn.iocoder.yudao.module.jl.entity.project.ProjectConstract;
import org.mapstruct.*;



@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProjectConstractMapper {
    ProjectConstract toEntity(ProjectConstractCreateReqVO dto);

    ProjectConstract toEntity(ProjectConstractUpdateReqVO dto);

    ProjectConstractRespVO toDto(ProjectConstract entity);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ProjectConstract partialUpdate(ProjectConstractUpdateReqVO dto, @MappingTarget ProjectConstract entity);

    List<ProjectConstractExcelVO> toExcelList(List<ProjectConstract> list);

    PageResult<ProjectConstractRespVO> toPage(PageResult<ProjectConstract> page);

    List<ProjectConstract> toEntityList(List<ProjectConstractItemVO> projectConstracts);
}