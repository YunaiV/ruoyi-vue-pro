package cn.iocoder.yudao.module.system.convert.user;

import cn.iocoder.yudao.module.system.controller.admin.user.vo.profile.UserProfileRespVO;
import cn.iocoder.yudao.module.system.controller.admin.user.vo.profile.UserProfileUpdatePasswordReqVO;
import cn.iocoder.yudao.module.system.controller.admin.user.vo.profile.UserProfileUpdateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.user.vo.user.*;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.SysDeptDO;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.SysPostDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.SysRoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.social.SysSocialUserDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.UserDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface UserConvert {

    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    UserPageItemRespVO convert(UserDO bean);

    UserPageItemRespVO.Dept convert(SysDeptDO bean);

    UserDO convert(UserCreateReqVO bean);

    UserDO convert(UserUpdateReqVO bean);

    UserExcelVO convert02(UserDO bean);

    UserDO convert(UserImportExcelVO bean);

    UserProfileRespVO convert03(UserDO bean);

    List<UserProfileRespVO.Role> convertList(List<SysRoleDO> list);

    UserProfileRespVO.Dept convert02(SysDeptDO bean);

    UserDO convert(UserProfileUpdateReqVO bean);

    UserDO convert(UserProfileUpdatePasswordReqVO bean);

    List<UserProfileRespVO.Post> convertList02(List<SysPostDO> list);

    List<UserProfileRespVO.SocialUser> convertList03(List<SysSocialUserDO> list);

    List<UserSimpleRespVO> convertList04(List<UserDO> list);
}
