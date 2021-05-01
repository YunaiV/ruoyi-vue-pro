package cn.iocoder.dashboard.modules.system.dal.mysql.permission;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.dashboard.modules.system.dal.dataobject.permission.SysUserRoleDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface SysUserRoleMapper extends BaseMapperX<SysUserRoleDO> {

    default List<SysUserRoleDO> selectListByUserId(Long userId) {
        return selectList(new QueryWrapper<SysUserRoleDO>().eq("user_id", userId));
    }

    default void insertList(Long userId, Collection<Long> roleIds) {
        List<SysUserRoleDO> list = roleIds.stream().map(roleId -> {
            SysUserRoleDO entity = new SysUserRoleDO();
            entity.setUserId(userId);
            entity.setRoleId(roleId);
            return entity;
        }).collect(Collectors.toList());
        // TODO 芋艿，mybatis plus 增加批量插入的功能
        list.forEach(this::insert);
    }

    default void deleteListByUserIdAndRoleIdIds(Long userId, Collection<Long> roleIds) {
        delete(new QueryWrapper<SysUserRoleDO>().eq("user_id", userId)
                .in("role_id", roleIds));
    }

    default void deleteListByUserId(Long userId) {
        delete(new QueryWrapper<SysUserRoleDO>().eq("user_id", userId));
    }

    default void deleteListByRoleId(Long roleId) {
        delete(new QueryWrapper<SysUserRoleDO>().eq("role_id", roleId));
    }

}
