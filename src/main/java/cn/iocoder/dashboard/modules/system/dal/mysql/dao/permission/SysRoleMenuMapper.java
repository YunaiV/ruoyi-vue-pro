package cn.iocoder.dashboard.modules.system.dal.mysql.dao.permission;

import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.permission.SysRoleMenuDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenuDO> {

    default List<SysRoleMenuDO> selectListByRoleId(Long roleId) {
        return selectList(new QueryWrapper<SysRoleMenuDO>().eq("role_id", roleId));
    }

    default void insertList(Long roleId, Collection<Long> menuIds) {
        List<SysRoleMenuDO> list = menuIds.stream().map(menuId -> {
            SysRoleMenuDO entity = new SysRoleMenuDO();
            entity.setRoleId(roleId);
            entity.setMenuId(menuId);
            return entity;
        }).collect(Collectors.toList());
        // TODO 芋艿，mybatis plus 增加批量插入的功能
        list.forEach(this::insert);
    }

    default void deleteListByRoleIdAndMenuIds(Long roleId, Collection<Long> menuIds) {
        delete(new QueryWrapper<SysRoleMenuDO>().eq("role_id", roleId)
                .in("menu_id", menuIds));
    }

}
