package cn.iocoder.yudao.module.jl.mapper.laboratory;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo.*;
import cn.iocoder.yudao.module.jl.entity.laboratory.CategorySkillUser;
import org.mapstruct.*;



@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategorySkillUserMapper {
    CategorySkillUser toEntity(CategorySkillUserCreateReqVO dto);

    CategorySkillUser toEntity(CategorySkillUserUpdateReqVO dto);

    CategorySkillUserRespVO toDto(CategorySkillUser entity);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    CategorySkillUser partialUpdate(CategorySkillUserUpdateReqVO dto, @MappingTarget CategorySkillUser entity);

    List<CategorySkillUserExcelVO> toExcelList(List<CategorySkillUser> list);

    PageResult<CategorySkillUserRespVO> toPage(PageResult<CategorySkillUser> page);
}