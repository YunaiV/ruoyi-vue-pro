package cn.iocoder.yudao.module.system.convert.auth;

import cn.iocoder.yudao.module.system.api.auth.dto.OAuth2AccessTokenCheckRespDTO;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.session.UserSessionPageItemRespVO;
import cn.iocoder.yudao.module.system.dal.dataobject.auth.OAuth2AccessTokenDO;
import cn.iocoder.yudao.module.system.dal.dataobject.auth.UserSessionDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserSessionConvert {

    UserSessionConvert INSTANCE = Mappers.getMapper(UserSessionConvert.class);

    UserSessionPageItemRespVO convert(UserSessionDO session);

    OAuth2AccessTokenCheckRespDTO convert(OAuth2AccessTokenDO bean);

}
