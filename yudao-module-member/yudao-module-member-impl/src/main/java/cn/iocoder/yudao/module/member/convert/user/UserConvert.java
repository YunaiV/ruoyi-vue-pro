package cn.iocoder.yudao.module.member.convert.user;

import cn.iocoder.yudao.module.member.api.user.dto.UserRespDTO;
import cn.iocoder.yudao.module.member.controller.app.user.vo.AppUserInfoRespVO;
import cn.iocoder.yudao.module.member.dal.dataobject.user.UserDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserConvert {

    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    AppUserInfoRespVO convert(UserDO bean);

    UserRespDTO convert2(UserDO bean);
}
