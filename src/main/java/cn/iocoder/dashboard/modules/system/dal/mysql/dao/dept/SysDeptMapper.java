package cn.iocoder.dashboard.modules.system.dal.mysql.dao.dept;

import cn.iocoder.dashboard.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.dashboard.modules.system.controller.dept.vo.dept.SysDeptListReqVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dept.SysDeptDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysDeptMapper extends BaseMapper<SysDeptDO> {

    default List<SysDeptDO> selectList() {
        return selectList(new QueryWrapper<>());
    }

    default List<SysDeptDO> selectList(SysDeptListReqVO reqVO) {
        return selectList(new QueryWrapperX<SysDeptDO>().likeIfPresent("name", reqVO.getName())
                .eqIfPresent("status", reqVO.getStatus()));
    }

    default SysDeptDO selectByParentIdAndName(Long parentId, String name) {
        return selectOne(new QueryWrapper<SysDeptDO>().eq("parent_id", parentId)
                .eq("name", name));
    }

    default Integer selectCountByParentId(Long parentId) {
        return selectCount(new QueryWrapper<SysDeptDO>().eq("parent_id", parentId));
    }

}
