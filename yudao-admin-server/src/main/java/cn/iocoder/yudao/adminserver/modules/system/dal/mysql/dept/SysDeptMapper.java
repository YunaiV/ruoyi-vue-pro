package cn.iocoder.yudao.adminserver.modules.system.dal.mysql.dept;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.adminserver.modules.system.controller.dept.vo.dept.SysDeptListReqVO;
import cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.dept.SysDeptDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;

@Mapper
public interface SysDeptMapper extends BaseMapperX<SysDeptDO> {

    default List<SysDeptDO> selectList(SysDeptListReqVO reqVO) {
        return selectList(new QueryWrapperX<SysDeptDO>().likeIfPresent("name", reqVO.getName())
                .eqIfPresent("status", reqVO.getStatus()));
    }

    default SysDeptDO selectByParentIdAndName(Long parentId, String name) {
        return selectOne(new QueryWrapper<SysDeptDO>().eq("parent_id", parentId)
                .eq("name", name));
    }

    default Integer selectCountByParentId(Long parentId) {
        return selectCount("parent_id", parentId);
    }

    default boolean selectExistsByUpdateTimeAfter(Date maxUpdateTime) {
        return selectOne(new QueryWrapper<SysDeptDO>().select("id")
                .gt("update_time", maxUpdateTime).last("LIMIT 1")) != null;
    }

}
