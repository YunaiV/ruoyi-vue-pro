package cn.iocoder.dashboard.modules.system.convert.permission;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.permission.vo.role.SysRoleCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.permission.vo.role.SysRoleRespVO;
import cn.iocoder.dashboard.modules.system.controller.permission.vo.role.SysRoleSimpleRespVO;
import cn.iocoder.dashboard.modules.system.controller.permission.vo.role.SysRoleUpdateReqVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.permission.SysRoleDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SysRoleConvert {

    SysRoleConvert INSTANCE = Mappers.getMapper(SysRoleConvert.class);

    SysRoleDO convert(SysRoleUpdateReqVO bean);

    SysRoleRespVO convert(SysRoleDO bean);

    @Mapping(source = "records", target = "list")
    PageResult<SysRoleDO> convertPage(IPage<SysRoleDO> page);

    SysRoleDO convert(SysRoleCreateReqVO bean);

    List<SysRoleSimpleRespVO> convertList02(List<SysRoleDO> list);

}
