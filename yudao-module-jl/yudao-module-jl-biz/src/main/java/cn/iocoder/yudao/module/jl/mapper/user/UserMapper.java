package cn.iocoder.yudao.module.jl.mapper.user;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.jl.controller.admin.user.vo.*;
import cn.iocoder.yudao.module.jl.entity.user.User;
import org.mapstruct.*;



@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    User toEntity(UserCreateReqVO dto);

    User toEntity(UserUpdateReqVO dto);

    UserRespVO toDto(User entity);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    User partialUpdate(UserUpdateReqVO dto, @MappingTarget User entity);

    List<UserExcelVO> toExcelList(List<User> list);

    PageResult<UserRespVO> toPage(PageResult<User> page);
}