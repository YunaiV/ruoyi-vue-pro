package cn.iocoder.yudao.module.hrm.service.recruit.candidate;

import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.interview.HrmRecruitInterviewResultReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.interview.HrmRecruitInterviewSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.candidate.HrmRecruitInterviewDO;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 招聘面试 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmRecruitInterviewService {

    /**
     * 创建招聘面试
     *
     * @param createReqVO 招聘面试信息
     * @return 招聘面试编号
     */
    Long createRecruitInterview(HrmRecruitInterviewSaveReqVO createReqVO);

    /**
     * 更新招聘面试
     *
     * @param updateReqVO 招聘面试信息
     */
    void updateRecruitInterview(HrmRecruitInterviewSaveReqVO updateReqVO);

    /**
     * 删除招聘面试
     *
     * @param id 招聘面试编号
     */
    void deleteRecruitInterview(Long id);

    /**
     * 获得招聘面试
     *
     * @param id 招聘面试编号
     * @return 招聘面试
     */
    HrmRecruitInterviewDO getRecruitInterview(Long id);

    /**
     * 获得候选人的招聘面试列表
     *
     * @param candidateId 候选人编号
     * @return 招聘面试列表
     */
    List<HrmRecruitInterviewDO> getRecruitInterviewListByCandidateId(Long candidateId);

    /**
     * 获得候选人的最新面试 Map
     *
     * @param candidateIds 候选人编号集合
     * @return 候选人编号与最新面试的映射
     */
    default Map<Long, HrmRecruitInterviewDO> getLatestRecruitInterviewMapByCandidateIds(Collection<Long> candidateIds) {
        return convertMap(getRecruitInterviewListByCandidateIds(candidateIds), HrmRecruitInterviewDO::getCandidateId);
    }

    /**
     * 获得候选人的招聘面试列表
     *
     * @param candidateIds 候选人编号集合
     * @return 招聘面试列表
     */
    List<HrmRecruitInterviewDO> getRecruitInterviewListByCandidateIds(Collection<Long> candidateIds);

    /**
     * 获得指定面试时间范围内的招聘面试列表
     *
     * @param interviewTimes 面试时间双闭区间
     * @return 招聘面试列表
     */
    List<HrmRecruitInterviewDO> getRecruitInterviewListByInterviewTimeBetween(
            LocalDateTime[] interviewTimes);

    /**
     * 删除候选人的面试记录
     *
     * @param candidateId 候选人编号
     */
    void deleteRecruitInterviewByCandidateId(Long candidateId);

    /**
     * 淘汰候选人时结束当前面试
     *
     * @param candidateId 候选人编号
     * @param eliminateReason 淘汰原因
     */
    void finishCurrentRecruitInterviewForElimination(Long candidateId, String eliminateReason);

    /**
     * 更新招聘面试结果
     *
     * @param reqVO 招聘面试结果信息
     */
    void updateRecruitInterviewResult(HrmRecruitInterviewResultReqVO reqVO);

}
