package cn.iocoder.yudao.module.hrm.dal.mysql.recruit.candidate;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.candidate.HrmRecruitInterviewDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Mapper
public interface HrmRecruitInterviewMapper extends BaseMapperX<HrmRecruitInterviewDO> {

    default List<HrmRecruitInterviewDO> selectListByCandidateId(Long candidateId) {
        return selectList(new LambdaQueryWrapperX<HrmRecruitInterviewDO>()
                .eq(HrmRecruitInterviewDO::getCandidateId, candidateId)
                .orderByDesc(HrmRecruitInterviewDO::getStageNumber)
                .orderByDesc(HrmRecruitInterviewDO::getId));
    }

    default HrmRecruitInterviewDO selectLatestByCandidateId(Long candidateId) {
        return selectOne(new LambdaQueryWrapperX<HrmRecruitInterviewDO>()
                .eq(HrmRecruitInterviewDO::getCandidateId, candidateId)
                .orderByDesc(HrmRecruitInterviewDO::getStageNumber)
                .orderByDesc(HrmRecruitInterviewDO::getId)
                .last("LIMIT 1"));
    }

    default void deleteByCandidateId(Long candidateId) {
        delete(new LambdaQueryWrapperX<HrmRecruitInterviewDO>()
                .eq(HrmRecruitInterviewDO::getCandidateId, candidateId));
    }

    default List<HrmRecruitInterviewDO> selectListByCandidateIds(Collection<Long> candidateIds) {
        return selectList(new LambdaQueryWrapperX<HrmRecruitInterviewDO>()
                .in(HrmRecruitInterviewDO::getCandidateId, candidateIds)
                .orderByAsc(HrmRecruitInterviewDO::getCandidateId)
                .orderByDesc(HrmRecruitInterviewDO::getStageNumber)
                .orderByDesc(HrmRecruitInterviewDO::getId));
    }

    default List<HrmRecruitInterviewDO> selectListByInterviewTimeBetween(
            LocalDateTime[] interviewTimes) {
        return selectList(new LambdaQueryWrapperX<HrmRecruitInterviewDO>()
                .betweenIfPresent(HrmRecruitInterviewDO::getInterviewTime, interviewTimes)
                .orderByAsc(HrmRecruitInterviewDO::getInterviewTime)
                .orderByAsc(HrmRecruitInterviewDO::getId));
    }

    @SuppressWarnings("UnusedReturnValue")
    default int updateRecruitInterviewArrangement(HrmRecruitInterviewDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<HrmRecruitInterviewDO>()
                .set(HrmRecruitInterviewDO::getEvaluate, null)
                .set(HrmRecruitInterviewDO::getCancelReason, null)
                .eq(HrmRecruitInterviewDO::getId, updateObj.getId()));
    }

    @SuppressWarnings("UnusedReturnValue")
    default int updateRecruitInterviewResult(HrmRecruitInterviewDO updateObj) {
        return update(new LambdaUpdateWrapper<HrmRecruitInterviewDO>()
                .set(HrmRecruitInterviewDO::getResult, updateObj.getResult())
                .set(HrmRecruitInterviewDO::getEvaluate, updateObj.getEvaluate())
                .set(HrmRecruitInterviewDO::getCancelReason, updateObj.getCancelReason())
                .eq(HrmRecruitInterviewDO::getId, updateObj.getId()));
    }

}
