package cn.iocoder.yudao.module.system.dal.mysql.permission;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleMenuDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface RoleMenuMapper extends BaseMapperX<RoleMenuDO> {

    default List<RoleMenuDO> selectListByRoleId(Long roleId) {
        return selectList(new QueryWrapper<RoleMenuDO>().eq("role_id", roleId));
    }

    default void insertList(Long roleId, Collection<Long> menuIds) {
        List<RoleMenuDO> list = menuIds.stream().map(menuId -> {
            RoleMenuDO entity = new RoleMenuDO();
            entity.setRoleId(roleId);
            entity.setMenuId(menuId);
            return entity;
        }).collect(Collectors.toList());
        insertBatch(list);
    }

    default void deleteListByRoleIdAndMenuIds(Long roleId, Collection<Long> menuIds) {
        delete(new QueryWrapper<RoleMenuDO>().eq("role_id", roleId)
                .in("menu_id", menuIds));
    }

    default void deleteListByMenuId(Long menuId) {
        delete(new QueryWrapper<RoleMenuDO>().eq("menu_id", menuId));
    }

    default void deleteListByRoleId(Long roleId) {
        delete(new QueryWrapper<RoleMenuDO>().eq("role_id", roleId));
    }

    @Select("SELECT id FROM system_role_menu WHERE update_time > #{maxUpdateTime} LIMIT 1")
    Long selectExistsByUpdateTimeAfter(Date maxUpdateTime);

}
