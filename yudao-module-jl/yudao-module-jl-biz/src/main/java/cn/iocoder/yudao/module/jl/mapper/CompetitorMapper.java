package cn.iocoder.yudao.module.jl.mapper;

import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.CompetitorCreateReq;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.CompetitorDto;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.Competitor;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CompetitorMapper {
    Competitor toEntity(CompetitorDto competitorDto);

    CompetitorDto toDto(Competitor competitor);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Competitor partialUpdate(CompetitorDto competitorDto, @MappingTarget Competitor competitor);

    Competitor toEntity(CompetitorCreateReq competitorCreateReq);

    CompetitorCreateReq toDto(CompetitorDto competitorDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    CompetitorDto partialUpdate(CompetitorCreateReq competitorCreateReq, @MappingTarget CompetitorDto competitorDto);
}