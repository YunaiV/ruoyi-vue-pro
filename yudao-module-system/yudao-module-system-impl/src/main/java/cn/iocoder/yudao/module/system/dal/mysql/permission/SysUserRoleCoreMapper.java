package cn.iocoder.yudao.module.system.dal.mysql.permission;

import cn.iocoder.yudao.module.system.dal.dataobject.permission.SysUserRoleDO;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface SysUserRoleCoreMapper extends BaseMapperX<SysUserRoleDO>  {

    default List<SysUserRoleDO> selectListByRoleIds(Collection<Long> roleIds) {
        return selectList(SysUserRoleDO::getRoleId, roleIds);
    }

}
