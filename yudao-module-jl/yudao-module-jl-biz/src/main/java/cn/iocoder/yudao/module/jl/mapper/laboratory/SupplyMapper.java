package cn.iocoder.yudao.module.jl.mapper.laboratory;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo.*;
import cn.iocoder.yudao.module.jl.entity.laboratory.Supply;
import org.mapstruct.*;



@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface SupplyMapper {
    Supply toEntity(SupplyCreateReqVO dto);

    Supply toEntity(SupplyUpdateReqVO dto);

    SupplyRespVO toDto(Supply entity);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Supply partialUpdate(SupplyUpdateReqVO dto, @MappingTarget Supply entity);

    List<SupplyExcelVO> toExcelList(List<Supply> list);

    PageResult<SupplyRespVO> toPage(PageResult<Supply> page);
}