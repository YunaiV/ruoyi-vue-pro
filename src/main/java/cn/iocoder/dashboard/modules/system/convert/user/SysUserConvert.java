package cn.iocoder.dashboard.modules.system.convert.user;

import cn.iocoder.dashboard.modules.system.controller.user.vo.user.*;
import cn.iocoder.dashboard.modules.system.dal.dataobject.dept.SysDeptDO;
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

}
