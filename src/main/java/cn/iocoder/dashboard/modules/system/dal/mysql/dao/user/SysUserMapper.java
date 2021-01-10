package cn.iocoder.dashboard.modules.system.dal.mysql.dao.user;

import cn.iocoder.dashboard.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.dashboard.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.dashboard.modules.system.controller.user.vo.user.SysUserPageReqVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.user.SysUserDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUserDO> {

    default SysUserDO selectByUsername(String username) {
        return selectOne(new QueryWrapper<SysUserDO>().eq("username", username));
    }

    default IPage<SysUserDO> selectList(SysUserPageReqVO reqVO, List<Long> deptIds) {
        return selectPage(MyBatisUtils.buildPage(reqVO),
                new QueryWrapperX<SysUserDO>().likeIfPresent("username", reqVO.getUsername())
                        .likeIfPresent("mobile", reqVO.getMobile())
                        .eqIfPresent("status", reqVO.getStatus())
                        .betweenIfPresent("create_time", reqVO.getBeginTime(), reqVO.getEndTime())
                        .inIfPresent("dept_id", deptIds));
    }

}

