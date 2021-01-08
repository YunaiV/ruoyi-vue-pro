package cn.iocoder.dashboard.modules.system.dal.mysql.dao.permission;

import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.permission.SysMenuDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenuDO> {

    default SysMenuDO selectByParentIdAndName(Long parentId, String name) {
        return selectOne(new QueryWrapper<SysMenuDO>().eq("parent_id", parentId)
                .eq("name", name));
    }

    default Integer selectCountByParentId(Long parentId) {
        return selectCount(new QueryWrapper<SysMenuDO>().eq("parent_id", parentId));
    }

}
