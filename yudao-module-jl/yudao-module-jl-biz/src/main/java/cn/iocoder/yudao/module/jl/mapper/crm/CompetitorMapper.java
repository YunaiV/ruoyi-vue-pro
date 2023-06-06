package cn.iocoder.yudao.module.jl.mapper.crm;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.entity.crm.Competitor;
import org.mapstruct.*;



@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CompetitorMapper {
    Competitor toEntity(CompetitorCreateReqVO dto);

    Competitor toEntity(CompetitorUpdateReqVO dto);

    CompetitorRespVO toDto(Competitor entity);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Competitor partialUpdate(CompetitorUpdateReqVO dto, @MappingTarget Competitor entity);

    List<CompetitorExcelVO> toExcelList(List<Competitor> list);

    PageResult<CompetitorRespVO> toPage(PageResult<Competitor> page);
}