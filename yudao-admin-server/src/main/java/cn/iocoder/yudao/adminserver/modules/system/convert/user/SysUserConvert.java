package cn.iocoder.yudao.adminserver.modules.system.convert.user;

import cn.iocoder.yudao.adminserver.modules.system.controller.user.vo.profile.SysUserProfileRespVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.user.vo.profile.SysUserProfileUpdatePasswordReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.user.vo.profile.SysUserProfileUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.user.vo.user.*;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.dept.SysDeptDO;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.dept.SysPostDO;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.permission.SysRoleDO;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.social.SysSocialUserDO;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.user.SysUserDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SysUserConvert {

    SysUserConvert INSTANCE = Mappers.getMapper(SysUserConvert.class);

    SysUserPageItemRespVO convert(SysUserDO bean);

    SysUserPageItemRespVO.Dept convert(SysDeptDO bean);

    SysUserDO convert(SysUserCreateReqVO bean);

    SysUserDO convert(SysUserUpdateReqVO bean);

    SysUserExcelVO convert02(SysUserDO bean);

    SysUserDO convert(SysUserImportExcelVO bean);

    SysUserProfileRespVO convert03(SysUserDO bean);

    List<SysUserProfileRespVO.Role> convertList(List<SysRoleDO> list);

    SysUserProfileRespVO.Dept convert02(SysDeptDO bean);

    SysUserDO convert(SysUserProfileUpdateReqVO bean);

    SysUserDO convert(SysUserProfileUpdatePasswordReqVO bean);

    List<SysUserProfileRespVO.Post> convertList02(List<SysPostDO> list);

    List<SysUserProfileRespVO.SocialUser> convertList03(List<SysSocialUserDO> list);

    List<SysUserSimpleRespVO> convertList04(List<SysUserDO> list);
}
