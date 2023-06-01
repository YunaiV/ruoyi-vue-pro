package cn.iocoder.yudao.module.jl.mapper.laboratory;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo.*;
import cn.iocoder.yudao.module.jl.entity.laboratory.CategoryReference;
import org.mapstruct.*;



@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryReferenceMapper {
    CategoryReference toEntity(CategoryReferenceCreateReqVO dto);

    CategoryReference toEntity(CategoryReferenceUpdateReqVO dto);

    CategoryReferenceRespVO toDto(CategoryReference entity);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    CategoryReference partialUpdate(CategoryReferenceUpdateReqVO dto, @MappingTarget CategoryReference entity);

    List<CategoryReferenceExcelVO> toExcelList(List<CategoryReference> list);

    PageResult<CategoryReferenceRespVO> toPage(PageResult<CategoryReference> page);
}