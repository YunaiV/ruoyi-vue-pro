package cn.iocoder.yudao.module.jl.mapper;

import cn.iocoder.yudao.module.jl.dal.dataobject.system.SystemUser;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface SystemUserMapper {
    SystemUser toEntity(SystemUser systemUser);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    SystemUser partialUpdate(SystemUser systemUser1, @MappingTarget SystemUser systemUser);
}