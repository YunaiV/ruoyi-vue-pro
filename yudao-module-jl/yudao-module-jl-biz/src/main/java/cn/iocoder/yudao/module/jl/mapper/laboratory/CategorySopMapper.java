package cn.iocoder.yudao.module.jl.mapper.laboratory;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo.*;
import cn.iocoder.yudao.module.jl.entity.laboratory.CategorySop;
import org.mapstruct.*;



@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategorySopMapper {
    CategorySop toEntity(CategorySopCreateReqVO dto);

    CategorySop toEntity(CategorySopUpdateReqVO dto);

    CategorySopRespVO toDto(CategorySop entity);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    CategorySop partialUpdate(CategorySopUpdateReqVO dto, @MappingTarget CategorySop entity);

    List<CategorySopExcelVO> toExcelList(List<CategorySop> list);

    PageResult<CategorySopRespVO> toPage(PageResult<CategorySop> page);
}