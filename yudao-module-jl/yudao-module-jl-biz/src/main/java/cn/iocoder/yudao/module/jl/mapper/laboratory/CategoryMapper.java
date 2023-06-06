package cn.iocoder.yudao.module.jl.mapper.laboratory;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo.*;
import cn.iocoder.yudao.module.jl.entity.laboratory.Category;
import org.mapstruct.*;



@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper {
    Category toEntity(CategoryCreateReqVO dto);

    Category toEntity(CategoryUpdateReqVO dto);

    CategoryRespVO toDto(Category entity);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Category partialUpdate(CategoryUpdateReqVO dto, @MappingTarget Category entity);

    List<CategoryExcelVO> toExcelList(List<Category> list);

    PageResult<CategoryRespVO> toPage(PageResult<Category> page);
}