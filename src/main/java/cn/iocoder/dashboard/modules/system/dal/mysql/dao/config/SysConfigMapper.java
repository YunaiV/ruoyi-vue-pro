package cn.iocoder.dashboard.modules.system.dal.mysql.dao.config;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.dashboard.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.dashboard.modules.system.controller.config.vo.SysConfigExportReqVO;
import cn.iocoder.dashboard.modules.system.controller.config.vo.SysConfigPageReqVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.config.SysConfigDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysConfigMapper extends BaseMapperX<SysConfigDO> {

    default PageResult<SysConfigDO> selectPage(SysConfigPageReqVO reqVO) {
        return selectPage(reqVO,
                new QueryWrapperX<SysConfigDO>().likeIfPresent("name", reqVO.getName())
                        .likeIfPresent("`key`", reqVO.getKey())
                        .eqIfPresent("`type`", reqVO.getType())
                        .betweenIfPresent("create_time", reqVO.getBeginTime(), reqVO.getEndTime()));
    }

    default SysConfigDO selectByKey(String key) {
        return selectOne(new QueryWrapper<SysConfigDO>().eq("`key`", key));
    }

    default List<SysConfigDO> selectList(SysConfigExportReqVO reqVO) {
        return selectList(new QueryWrapperX<SysConfigDO>().likeIfPresent("name", reqVO.getName())
                .likeIfPresent("`key`", reqVO.getKey())
                .eqIfPresent("`type`", reqVO.getType())
                .betweenIfPresent("create_time", reqVO.getBeginTime(), reqVO.getEndTime()));
    }

}
