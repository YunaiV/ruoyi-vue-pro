package cn.iocoder.yudao.module.system.convert.permission;

import cn.iocoder.yudao.module.system.controller.admin.permission.vo.role.*;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface RoleConvert {

    RoleConvert INSTANCE = Mappers.getMapper(RoleConvert.class);

    RoleDO convert(RoleUpdateReqVO bean);

    RoleRespVO convert(RoleDO bean);

    RoleDO convert(RoleCreateReqVO bean);

    List<RoleSimpleRespVO> convertList02(List<RoleDO> list);

    List<RoleExcelVO> convertList03(List<RoleDO> list);

}
