package cn.iocoder.dashboard.modules.system.dal.mysql.dao.notice;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.dashboard.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.dashboard.modules.system.controller.notice.vo.SysNoticePageReqVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.notice.SysNoticeDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysNoticeMapper extends BaseMapperX<SysNoticeDO> {

    default PageResult<SysNoticeDO> selectPage(SysNoticePageReqVO reqVO) {
        return selectPage(reqVO, new QueryWrapperX<SysNoticeDO>()
                .likeIfPresent("title", reqVO.getTitle())
                .eqIfPresent("status", reqVO.getStatus()));
    }

}
