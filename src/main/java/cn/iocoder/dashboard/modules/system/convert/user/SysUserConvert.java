package cn.iocoder.dashboard.modules.system.convert.user;

import cn.iocoder.dashboard.modules.system.controller.user.vo.user.SysUserCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.user.vo.user.SysUserExcelVO;
import cn.iocoder.dashboard.modules.system.controller.user.vo.user.SysUserImportExcelVO;
import cn.iocoder.dashboard.modules.system.controller.user.vo.user.SysUserPageItemRespVO;
import cn.iocoder.dashboard.modules.system.controller.user.vo.user.SysUserProfileRespVO;
import cn.iocoder.dashboard.modules.system.controller.user.vo.user.SysUserProfileUpdateReqVO;
import cn.iocoder.dashboard.modules.system.controller.user.vo.user.SysUserUpdateReqVO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.dept.SysDeptDO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.permission.SysRoleDO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.user.SysUserDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

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

    SysUserProfileRespVO.Role convert(SysRoleDO bean);

    SysUserDO convert(SysUserProfileUpdateReqVO bean);



}
