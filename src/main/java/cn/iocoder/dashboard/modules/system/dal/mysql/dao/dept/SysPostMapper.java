package cn.iocoder.dashboard.modules.system.dal.mysql.dao.dept;

import cn.iocoder.dashboard.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.dashboard.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.dashboard.modules.system.controller.dept.vo.post.SysPostPageReqVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dept.SysPostDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface SysPostMapper extends BaseMapper<SysPostDO> {

    default List<SysPostDO> selectList(Collection<Long> ids, Collection<Integer> statuses) {
        return selectList(new QueryWrapperX<SysPostDO>().inIfPresent("id", ids)
                .inIfPresent("status", statuses));
    }

    default IPage<SysPostDO> selectList(SysPostPageReqVO reqVO) {
        return selectPage(MyBatisUtils.buildPage(reqVO),
                new QueryWrapperX<SysPostDO>().likeIfPresent("name", reqVO.getName())
                        .eqIfPresent("status", reqVO.getStatus()));
    }

    default SysPostDO selectByName(String name) {
        return selectOne(new QueryWrapper<SysPostDO>().eq("name", name));
    }

    default SysPostDO selectByCode(String code) {
        return selectOne(new QueryWrapper<SysPostDO>().eq("code", code));
    }

}
