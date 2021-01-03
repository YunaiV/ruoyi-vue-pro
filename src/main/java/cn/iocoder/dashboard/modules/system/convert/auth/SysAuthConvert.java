package cn.iocoder.dashboard.modules.system.convert.auth;

import cn.iocoder.dashboard.framework.security.core.LoginUser;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.user.SysUserDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SysAuthConvert {

    SysAuthConvert INSTANCE = Mappers.getMapper(SysAuthConvert.class);

    @Mapping(source = "updateTime", target = "updateTime", ignore = true) // 字段相同，但是含义不同，忽略
    LoginUser convert(SysUserDO bean);

}
