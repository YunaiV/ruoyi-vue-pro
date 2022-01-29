package cn.iocoder.yudao.module.system.dal.mysql.notice;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.system.controller.notice.vo.SysNoticePageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.notice.SysNoticeDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysNoticeMapper extends BaseMapperX<SysNoticeDO> {

    default PageResult<SysNoticeDO> selectPage(SysNoticePageReqVO reqVO) {
        return selectPage(reqVO, new QueryWrapperX<SysNoticeDO>()
                .likeIfPresent("title", reqVO.getTitle())
                .eqIfPresent("status", reqVO.getStatus()));
    }

}
