package cn.iocoder.yudao.module.member.convert.user;

import cn.iocoder.yudao.module.member.api.user.dto.UserInfoDTO;
import cn.iocoder.yudao.module.member.api.user.dto.UserRespDTO;
import cn.iocoder.yudao.module.member.controller.app.user.vo.AppUserInfoRespVO;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserConvert {

    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    AppUserInfoRespVO convert(MemberUserDO bean);

    UserRespDTO convert2(MemberUserDO bean);
    UserInfoDTO convertInfo(MemberUserDO bean);
}
