package cn.iocoder.yudao.module.system.convert.permission;

import cn.iocoder.yudao.module.system.controller.admin.permission.vo.role.*;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.SysRoleDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface RoleConvert {

    RoleConvert INSTANCE = Mappers.getMapper(RoleConvert.class);

    SysRoleDO convert(RoleUpdateReqVO bean);

    RoleRespVO convert(SysRoleDO bean);

    SysRoleDO convert(RoleCreateReqVO bean);

    List<RoleSimpleRespVO> convertList02(List<SysRoleDO> list);

    List<RoleExcelVO> convertList03(List<SysRoleDO> list);

}
