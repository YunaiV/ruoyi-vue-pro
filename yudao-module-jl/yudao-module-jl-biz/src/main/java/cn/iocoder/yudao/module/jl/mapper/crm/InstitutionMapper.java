package cn.iocoder.yudao.module.jl.mapper.crm;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.entity.crm.Institution;
import org.mapstruct.*;



@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface InstitutionMapper {
    Institution toEntity(InstitutionCreateReqVO dto);

    Institution toEntity(InstitutionUpdateReqVO dto);

    InstitutionRespVO toDto(Institution entity);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Institution partialUpdate(InstitutionUpdateReqVO dto, @MappingTarget Institution entity);

    List<InstitutionExcelVO> toExcelList(List<Institution> list);

    PageResult<InstitutionRespVO> toPage(PageResult<Institution> page);
}