package cn.iocoder.yudao.module.hrm.dal.mysql.recruit.candidate;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.candidate.HrmRecruitCandidatePageReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.candidate.HrmRecruitCandidateDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.candidate.HrmRecruitInterviewDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.post.HrmRecruitPostDO;
import cn.iocoder.yudao.module.hrm.enums.recruit.candidate.HrmRecruitInterviewResultEnum;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mapper
public interface HrmRecruitCandidateMapper extends BaseMapperX<HrmRecruitCandidateDO> {

    default HrmRecruitCandidateDO selectByIdForUpdate(Long id) {
        return selectOneForUpdate(HrmRecruitCandidateDO::getId, id);
    }

    @SuppressWarnings("UnusedReturnValue")
    default int updateForEdit(HrmRecruitCandidateDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<HrmRecruitCandidateDO>()
                .eq(HrmRecruitCandidateDO::getId, updateObj.getId())
                .set(updateObj.getAge() == null, HrmRecruitCandidateDO::getAge, null)
                .set(updateObj.getEmail() == null, HrmRecruitCandidateDO::getEmail, null)
                .set(updateObj.getWorkTime() == null, HrmRecruitCandidateDO::getWorkTime, null)
                .set(updateObj.getGraduateSchool() == null, HrmRecruitCandidateDO::getGraduateSchool, null)
                .set(updateObj.getLatestWorkPlace() == null, HrmRecruitCandidateDO::getLatestWorkPlace, null)
                .set(updateObj.getChannelId() == null, HrmRecruitCandidateDO::getChannelId, null)
                .set(updateObj.getRemark() == null, HrmRecruitCandidateDO::getRemark, null)
                .set(updateObj.getResumeUrls() == null, HrmRecruitCandidateDO::getResumeUrls, null));
    }

    default PageResult<HrmRecruitCandidateDO> selectPage(HrmRecruitCandidatePageReqVO reqVO) {
        MPJLambdaWrapperX<HrmRecruitCandidateDO> query = buildQueryWrapper(reqVO)
                .selectAll(HrmRecruitCandidateDO.class)
                .eqIfPresent(HrmRecruitCandidateDO::getStatus, reqVO.getStatus())
                .orderByDesc(HrmRecruitCandidateDO::getCreateTime)
                .orderByDesc(HrmRecruitCandidateDO::getId);
        return selectJoinPage(reqVO, HrmRecruitCandidateDO.class, query);
    }

    default List<HrmRecruitCandidateDO> selectListByStatusesAndStatusUpdateTimeBefore(
            Collection<Integer> statuses, LocalDateTime statusUpdateTime) {
        return selectList(new LambdaQueryWrapperX<HrmRecruitCandidateDO>()
                .in(HrmRecruitCandidateDO::getStatus, statuses)
                .lt(HrmRecruitCandidateDO::getStatusUpdateTime, statusUpdateTime)
                .orderByDesc(HrmRecruitCandidateDO::getId));
    }

    default Long selectCountByStatusAndStatusUpdateTimeBetween(
            Integer status, LocalDateTime[] statusUpdateTimes) {
        return selectCount(new LambdaQueryWrapperX<HrmRecruitCandidateDO>()
                .eq(HrmRecruitCandidateDO::getStatus, status)
                .betweenIfPresent(HrmRecruitCandidateDO::getStatusUpdateTime, statusUpdateTimes));
    }

    default Long selectCountByStatusAndEntryTimeBetween(
            Integer status, LocalDateTime[] entryTimes) {
        return selectCount(new LambdaQueryWrapperX<HrmRecruitCandidateDO>()
                .eq(HrmRecruitCandidateDO::getStatus, status)
                .betweenIfPresent(HrmRecruitCandidateDO::getEntryTime, entryTimes));
    }

    default Map<Integer, Long> selectCountMapByStatus(HrmRecruitCandidatePageReqVO reqVO) {
        MPJLambdaWrapperX<HrmRecruitCandidateDO> query = buildQueryWrapper(reqVO)
                .selectAs(HrmRecruitCandidateDO::getStatus, "status");
        query.selectFunc("COUNT(DISTINCT %s)", arg -> arg.accept(HrmRecruitCandidateDO::getId), "count");
        query.groupBy(HrmRecruitCandidateDO::getStatus);
        return CollectionUtils.convertMap(selectJoinMaps(query),
                record -> MapUtil.getInt(record, "status"),
                record -> MapUtil.getLong(record, "count"));
    }

    @SuppressWarnings("UnusedReturnValue")
    default int updateChannelIdByChannelId(Long channelId, Long newChannelId) {
        return update(new LambdaUpdateWrapper<HrmRecruitCandidateDO>()
                .eq(HrmRecruitCandidateDO::getChannelId, channelId)
                .set(HrmRecruitCandidateDO::getChannelId, newChannelId));
    }

    default Map<Long, Long> selectCountMapByPostIdsAndStatus(Collection<Long> postIds, Integer status) {
        List<Map<String, Object>> result = selectMaps(new MPJLambdaWrapperX<HrmRecruitCandidateDO>()
                .selectAs(HrmRecruitCandidateDO::getPostId, "postId")
                .selectCount(HrmRecruitCandidateDO::getId, "count")
                .in(HrmRecruitCandidateDO::getPostId, postIds)
                .eq(HrmRecruitCandidateDO::getStatus, status)
                .groupBy(HrmRecruitCandidateDO::getPostId));
        return CollectionUtils.convertMap(result,
                record -> MapUtil.getLong(record, "postId"),
                record -> MapUtil.getLong(record, "count"));
    }

    default MPJLambdaWrapperX<HrmRecruitCandidateDO> buildQueryWrapper(HrmRecruitCandidatePageReqVO reqVO) {
        MPJLambdaWrapperX<HrmRecruitCandidateDO> query = new MPJLambdaWrapperX<HrmRecruitCandidateDO>()
                .eqIfPresent(HrmRecruitCandidateDO::getPostId, reqVO.getPostId())
                .eqIfPresent(HrmRecruitCandidateDO::getSex, reqVO.getSex())
                .geIfPresent(HrmRecruitCandidateDO::getAge, reqVO.getMinAge())
                .leIfPresent(HrmRecruitCandidateDO::getAge, reqVO.getMaxAge())
                .geIfPresent(HrmRecruitCandidateDO::getWorkTime, reqVO.getMinWorkTime())
                .leIfPresent(HrmRecruitCandidateDO::getWorkTime, reqVO.getMaxWorkTime())
                .eqIfPresent(HrmRecruitCandidateDO::getEducation, reqVO.getEducation())
                .likeIfPresent(HrmRecruitCandidateDO::getGraduateSchool, reqVO.getGraduateSchool())
                .likeIfPresent(HrmRecruitCandidateDO::getLatestWorkPlace, reqVO.getLatestWorkPlace())
                .eqIfPresent(HrmRecruitCandidateDO::getChannelId, reqVO.getChannelId())
                .eqIfPresent(HrmRecruitCandidateDO::getCreator, reqVO.getCreator())
                .betweenIfPresent(HrmRecruitCandidateDO::getCreateTime, reqVO.getCreateTime());
        // 拼接招聘负责人查询条件
        if (reqVO.getOwnerEmployeeId() != null) {
            query.innerJoin(HrmRecruitPostDO.class, HrmRecruitPostDO::getId, HrmRecruitCandidateDO::getPostId)
                    .eq(HrmRecruitPostDO::getOwnerEmployeeId, reqVO.getOwnerEmployeeId());
        }
        // 拼接关键字查询条件
        if (StrUtil.isNotBlank(reqVO.getSearch())) {
            query.and(wrapper -> wrapper.like(HrmRecruitCandidateDO::getName, reqVO.getSearch())
                    .or().like(HrmRecruitCandidateDO::getMobile, reqVO.getSearch())
                    .or().like(HrmRecruitCandidateDO::getEmail, reqVO.getSearch()));
        }
        // 拼接面试查询条件
        LocalDateTime[] interviewTimes = reqVO.getInterviewTime();
        if (reqVO.getInterviewEmployeeId() != null
                || LocalDateTimeUtils.isTimeRangePresent(interviewTimes)) {
            query.innerJoin(HrmRecruitInterviewDO.class, "interview", on -> on
                    .eq(HrmRecruitInterviewDO::getCandidateId, HrmRecruitCandidateDO::getId)
                    .eq(HrmRecruitInterviewDO::getStageNumber, HrmRecruitCandidateDO::getStageNumber)
                    .ne(HrmRecruitInterviewDO::getResult, HrmRecruitInterviewResultEnum.CANCEL.getResult()));
            query.distinct(); // 避免当前轮次存在多条面试记录时，候选人重复
            if (reqVO.getInterviewEmployeeId() != null) {
                query.and(wrapper -> wrapper
                        .eq(HrmRecruitInterviewDO::getInterviewEmployeeId, reqVO.getInterviewEmployeeId())
                        .or().apply(MyBatisUtils.findInSet("interview.other_interview_employee_ids"),
                                reqVO.getInterviewEmployeeId()));
            }
            query.betweenIfPresent(HrmRecruitInterviewDO::getInterviewTime, interviewTimes);
        }
        return query;
    }

}
