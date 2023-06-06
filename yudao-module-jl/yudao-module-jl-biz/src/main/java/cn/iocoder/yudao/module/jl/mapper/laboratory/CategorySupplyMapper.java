package cn.iocoder.yudao.module.jl.mapper.laboratory;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo.*;
import cn.iocoder.yudao.module.jl.entity.laboratory.CategorySupply;
import org.mapstruct.*;



@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategorySupplyMapper {
    CategorySupply toEntity(CategorySupplyCreateReqVO dto);

    CategorySupply toEntity(CategorySupplyUpdateReqVO dto);

    CategorySupplyRespVO toDto(CategorySupply entity);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    CategorySupply partialUpdate(CategorySupplyUpdateReqVO dto, @MappingTarget CategorySupply entity);

    List<CategorySupplyExcelVO> toExcelList(List<CategorySupply> list);

    PageResult<CategorySupplyRespVO> toPage(PageResult<CategorySupply> page);
}