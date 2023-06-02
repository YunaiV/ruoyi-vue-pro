package cn.iocoder.yudao.module.jl.mapper.project;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.jl.controller.admin.project.vo.*;
import cn.iocoder.yudao.module.jl.entity.project.ProjectSupply;
import org.mapstruct.*;



@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProjectSupplyMapper {
    ProjectSupply toEntity(ProjectSupplyCreateReqVO dto);

    ProjectSupply toEntity(ProjectSupplyUpdateReqVO dto);

    ProjectSupplyRespVO toDto(ProjectSupply entity);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ProjectSupply partialUpdate(ProjectSupplyUpdateReqVO dto, @MappingTarget ProjectSupply entity);

    List<ProjectSupplyExcelVO> toExcelList(List<ProjectSupply> list);

    PageResult<ProjectSupplyRespVO> toPage(PageResult<ProjectSupply> page);
}