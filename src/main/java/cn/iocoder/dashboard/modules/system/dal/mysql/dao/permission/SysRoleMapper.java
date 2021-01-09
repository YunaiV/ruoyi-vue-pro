package cn.iocoder.dashboard.modules.system.dal.mysql.dao.permission;

import cn.iocoder.dashboard.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.dashboard.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.dashboard.modules.system.controller.permission.vo.role.SysRolePageReqVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.permission.SysRoleDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRoleDO> {

    default IPage<SysRoleDO> selectPage(SysRolePageReqVO reqVO) {
        return selectPage(MyBatisUtils.buildPage(reqVO),
                new QueryWrapperX<SysRoleDO>().likeIfPresent("name", reqVO.getName())
                    .likeIfPresent("code", reqVO.getCode())
                    .eqIfPresent("status", reqVO.getStatus())
                    .betweenIfPresent("create_time", reqVO.getBeginTime(), reqVO.getEndTime()));
    }

    default SysRoleDO selectByName(String name) {
        return selectOne(new QueryWrapperX<SysRoleDO>().eq("name", name));
    }

    default SysRoleDO selectByCode(String code) {
        return selectOne(new QueryWrapperX<SysRoleDO>().eq("code", code));
    }


}
