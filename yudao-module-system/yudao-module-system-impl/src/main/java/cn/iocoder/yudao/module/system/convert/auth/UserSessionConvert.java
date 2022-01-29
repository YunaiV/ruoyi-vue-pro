package cn.iocoder.yudao.module.system.convert.auth;

import cn.iocoder.yudao.module.system.controller.admin.auth.vo.session.UserSessionPageItemRespVO;
import cn.iocoder.yudao.module.system.dal.dataobject.auth.SysUserSessionDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserSessionConvert {

    UserSessionConvert INSTANCE = Mappers.getMapper(UserSessionConvert.class);

    UserSessionPageItemRespVO convert(SysUserSessionDO session);

}
