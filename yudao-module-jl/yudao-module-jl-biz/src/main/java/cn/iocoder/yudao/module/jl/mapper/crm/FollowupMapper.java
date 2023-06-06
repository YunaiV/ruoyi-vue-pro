package cn.iocoder.yudao.module.jl.mapper.crm;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.entity.crm.Followup;
import org.mapstruct.*;



@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface FollowupMapper {
    Followup toEntity(FollowupCreateReqVO dto);

    Followup toEntity(FollowupUpdateReqVO dto);

    FollowupRespVO toDto(Followup entity);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Followup partialUpdate(FollowupUpdateReqVO dto, @MappingTarget Followup entity);

    List<FollowupExcelVO> toExcelList(List<Followup> list);

    PageResult<FollowupRespVO> toPage(PageResult<Followup> page);
}