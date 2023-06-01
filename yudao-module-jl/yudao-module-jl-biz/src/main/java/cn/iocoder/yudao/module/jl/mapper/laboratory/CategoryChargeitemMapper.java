package cn.iocoder.yudao.module.jl.mapper.laboratory;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo.*;
import cn.iocoder.yudao.module.jl.entity.laboratory.CategoryChargeitem;
import org.mapstruct.*;



@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryChargeitemMapper {
    CategoryChargeitem toEntity(CategoryChargeitemCreateReqVO dto);

    CategoryChargeitem toEntity(CategoryChargeitemUpdateReqVO dto);

    CategoryChargeitemRespVO toDto(CategoryChargeitem entity);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    CategoryChargeitem partialUpdate(CategoryChargeitemUpdateReqVO dto, @MappingTarget CategoryChargeitem entity);

    List<CategoryChargeitemExcelVO> toExcelList(List<CategoryChargeitem> list);

    PageResult<CategoryChargeitemRespVO> toPage(PageResult<CategoryChargeitem> page);
}