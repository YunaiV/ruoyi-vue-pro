package cn.iocoder.yudao.module.oa.dal.mysql.schedule;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.oa.controller.admin.schedule.vo.OaSchedulePageReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.schedule.OaScheduleDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * OA 日程 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaScheduleMapper extends BaseMapperX<OaScheduleDO> {

    default PageResult<OaScheduleDO> selectMyPage(OaSchedulePageReqVO pageReqVO, Long userId) {
        return selectPage(pageReqVO, buildPageQuery(pageReqVO)
                .eq(OaScheduleDO::getCreator, userId.toString()));
    }

    default PageResult<OaScheduleDO> selectReceivedPage(OaSchedulePageReqVO pageReqVO, Collection<Long> ids) {
        return selectPage(pageReqVO, buildPageQuery(pageReqVO).in(OaScheduleDO::getId, ids));
    }

    default PageResult<OaScheduleDO> selectPageByCreatorOrIds(OaSchedulePageReqVO pageReqVO, Long userId,
                                                           Collection<Long> ids) {
        return selectPage(pageReqVO, buildPageQuery(pageReqVO)
                .and(query -> query.eq(OaScheduleDO::getCreator, userId.toString())
                        .or().in(OaScheduleDO::getId, ids)));
    }

    default LambdaQueryWrapperX<OaScheduleDO> buildPageQuery(OaSchedulePageReqVO pageReqVO) {
        return new LambdaQueryWrapperX<OaScheduleDO>()
                .likeIfPresent(OaScheduleDO::getTitle, pageReqVO.getTitle())
                .eqIfPresent(OaScheduleDO::getType, pageReqVO.getType())
                .eqIfPresent(OaScheduleDO::getPriority, pageReqVO.getPriority())
                .betweenIfPresent(OaScheduleDO::getStartTime, pageReqVO.getStartTime())
                .leIfPresent(OaScheduleDO::getStartTime, ArrayUtil.get(pageReqVO.getOverlapTime(), 1))
                .geIfPresent(OaScheduleDO::getEndTime, ArrayUtil.get(pageReqVO.getOverlapTime(), 0))
                .orderByDesc(OaScheduleDO::getStartTime)
                .orderByDesc(OaScheduleDO::getId);
    }

    default List<OaScheduleDO> selectListByRemindAndRemindedAndStartTime(Boolean remind, Boolean reminded, LocalDateTime[] startTime) {
        return selectList(new LambdaQueryWrapperX<OaScheduleDO>()
                .eq(OaScheduleDO::getRemind, remind)
                .eq(OaScheduleDO::getReminded, reminded)
                .gt(OaScheduleDO::getStartTime, startTime[0])
                .lt(OaScheduleDO::getStartTime, startTime[1])
                .orderByAsc(OaScheduleDO::getStartTime)
                .orderByAsc(OaScheduleDO::getId));
    }

}
