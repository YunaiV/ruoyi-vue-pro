package cn.iocoder.yudao.module.jl.mapper;

import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.InstitutionCreateReq;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.InstitutionDto;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.Institution;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface InstitutionMapper {
    Institution toEntity(InstitutionCreateReq institutionCreateReq);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Institution partialUpdate(InstitutionCreateReq institutionCreateReq, @MappingTarget Institution institution);

    Institution toEntity(InstitutionDto institutionDto);

    InstitutionDto toDto(Institution institution);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Institution partialUpdate(InstitutionDto institutionDto, @MappingTarget Institution institution);
}