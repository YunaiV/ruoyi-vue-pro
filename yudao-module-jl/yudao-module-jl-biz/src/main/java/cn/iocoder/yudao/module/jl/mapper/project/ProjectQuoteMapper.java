package cn.iocoder.yudao.module.jl.mapper.project;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.jl.controller.admin.project.vo.*;
import cn.iocoder.yudao.module.jl.entity.project.ProjectQuote;
import org.mapstruct.*;



@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProjectQuoteMapper {
    ProjectQuote toEntity(ProjectQuoteCreateReqVO dto);

    ProjectQuote toEntity(ProjectQuoteUpdateReqVO dto);

    ProjectQuoteRespVO toDto(ProjectQuote entity);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ProjectQuote partialUpdate(ProjectQuoteUpdateReqVO dto, @MappingTarget ProjectQuote entity);

    List<ProjectQuoteExcelVO> toExcelList(List<ProjectQuote> list);

    PageResult<ProjectQuoteRespVO> toPage(PageResult<ProjectQuote> page);
}