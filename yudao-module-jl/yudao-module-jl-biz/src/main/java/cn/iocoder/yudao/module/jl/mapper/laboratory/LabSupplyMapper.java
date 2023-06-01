package cn.iocoder.yudao.module.jl.mapper.laboratory;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo.*;
import cn.iocoder.yudao.module.jl.entity.laboratory.LabSupply;
import org.mapstruct.*;



@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface LabSupplyMapper {
    LabSupply toEntity(LabSupplyCreateReqVO dto);

    LabSupply toEntity(LabSupplyUpdateReqVO dto);

    LabSupplyRespVO toDto(LabSupply entity);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    LabSupply partialUpdate(LabSupplyUpdateReqVO dto, @MappingTarget LabSupply entity);

    List<LabSupplyExcelVO> toExcelList(List<LabSupply> list);

    PageResult<LabSupplyRespVO> toPage(PageResult<LabSupply> page);
}