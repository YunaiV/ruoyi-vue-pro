package cn.iocoder.yudao.module.hrm.service.recruit.candidate;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.candidate.HrmRecruitCandidateEntryReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.candidate.HrmRecruitCandidatePageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.candidate.HrmRecruitCandidateSaveReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.candidate.HrmRecruitCandidateUpdateChannelReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.candidate.HrmRecruitCandidateUpdateEliminateReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.candidate.HrmRecruitCandidateUpdatePostReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.candidate.HrmRecruitCandidateUpdateStatusReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.candidate.HrmRecruitCandidateDO;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 招聘候选人 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmRecruitCandidateService {

    /**
     * 创建招聘候选人
     *
     * @param createReqVO 候选人信息
     * @return 候选人编号
     */
    Long createRecruitCandidate(HrmRecruitCandidateSaveReqVO createReqVO);

    /**
     * 更新招聘候选人
     *
     * @param updateReqVO 候选人信息
     */
    void updateRecruitCandidate(HrmRecruitCandidateSaveReqVO updateReqVO);

    /**
     * 删除招聘候选人
     *
     * @param id 候选人编号
     */
    void deleteRecruitCandidate(Long id);

    /**
     * 获得招聘候选人
     *
     * @param id 候选人编号
     * @return 招聘候选人
     */
    HrmRecruitCandidateDO getRecruitCandidate(Long id);

    /**
     * 获得招聘候选人列表
     *
     * @param ids 候选人编号集合
     * @return 候选人列表
     */
    List<HrmRecruitCandidateDO> getRecruitCandidateListByIds(Collection<Long> ids);

    /**
     * 获得招聘候选人 Map
     *
     * @param ids 候选人编号集合
     * @return 候选人 Map
     */
    default Map<Long, HrmRecruitCandidateDO> getRecruitCandidateMap(Collection<Long> ids) {
        return convertMap(getRecruitCandidateListByIds(ids), HrmRecruitCandidateDO::getId);
    }

    /**
     * 获得招聘候选人分页列表
     *
     * @param pageReqVO 分页查询
     * @return 招聘候选人分页列表
     */
    PageResult<HrmRecruitCandidateDO> getRecruitCandidatePage(HrmRecruitCandidatePageReqVO pageReqVO);

    /**
     * 校验招聘候选人是否存在
     *
     * @param id 候选人编号
     * @return 招聘候选人
     */
    HrmRecruitCandidateDO validateRecruitCandidateExists(Long id);

    /**
     * 锁定并校验招聘候选人是否存在
     *
     * @param id 候选人编号
     * @return 候选人
     */
    HrmRecruitCandidateDO validateRecruitCandidateExistsForUpdate(Long id);

    /**
     * 更新招聘候选人的面试状态和轮次
     *
     * @param id 候选人编号
     * @param stageNumber 面试轮次
     */
    void updateRecruitCandidateInterview(Long id, Integer stageNumber);

    /**
     * 根据面试结果更新招聘候选人状态
     *
     * @param id 候选人编号
     * @param status 候选人状态
     */
    void updateRecruitCandidateStatusByInterviewResult(Long id, Integer status);

    /**
     * 修复删除面试后的候选人状态和轮次
     *
     * @param id 候选人编号
     * @param status 候选人状态
     * @param stageNumber 面试轮次
     */
    void updateRecruitCandidateInterviewState(Long id, Integer status, Integer stageNumber);

    /**
     * 更新招聘候选人状态
     *
     * @param reqVO 候选人状态信息
     */
    void updateRecruitCandidateStatus(HrmRecruitCandidateUpdateStatusReqVO reqVO);

    /**
     * 确认招聘候选人入职
     *
     * @param id 候选人编号
     * @param entryTime 实际入职时间
     */
    void confirmRecruitCandidateEntry(Long id, LocalDateTime entryTime);

    /**
     * 更新招聘候选人的招聘职位
     *
     * @param reqVO 候选人招聘职位信息
     */
    void updateRecruitCandidatePost(HrmRecruitCandidateUpdatePostReqVO reqVO);

    /**
     * 更新招聘候选人的招聘渠道
     *
     * @param reqVO 候选人招聘渠道信息
     */
    void updateRecruitCandidateChannel(HrmRecruitCandidateUpdateChannelReqVO reqVO);

    /**
     * 将指定招聘渠道的候选人迁移到新渠道
     *
     * @param channelId 原招聘渠道编号
     * @param newChannelId 新招聘渠道编号
     */
    void updateRecruitCandidateChannelByChannelId(Long channelId, Long newChannelId);

    /**
     * 淘汰招聘候选人
     *
     * @param reqVO 候选人淘汰信息
     */
    void eliminateRecruitCandidate(HrmRecruitCandidateUpdateEliminateReqVO reqVO);

    /**
     * 将招聘候选人转为员工
     *
     * @param reqVO 候选人入职信息
     * @return 员工编号
     */
    Long convertRecruitCandidateToEmployee(HrmRecruitCandidateEntryReqVO reqVO);

    /**
     * 获得招聘候选人列表
     *
     * @param statuses 候选人状态集合
     * @param statusUpdateTime 状态更新时间上限
     * @return 招聘候选人列表
     */
    List<HrmRecruitCandidateDO> getRecruitCandidateList(Collection<Integer> statuses,
                                                        LocalDateTime statusUpdateTime);

    /**
     * 获得指定状态和状态更新时间范围的候选人数
     *
     * @param status 候选人状态
     * @param statusUpdateTimes 状态更新时间范围
     * @return 候选人数
     */
    Long getRecruitCandidateCountByStatusAndStatusUpdateTimeBetween(
            Integer status, LocalDateTime[] statusUpdateTimes);

    /**
     * 获得指定状态和入职时间范围的候选人数
     *
     * @param status 候选人状态
     * @param entryTimes 入职时间范围
     * @return 候选人数
     */
    Long getRecruitCandidateCountByStatusAndEntryTimeBetween(
            Integer status, LocalDateTime[] entryTimes);

    /**
     * 获得招聘职位的已入职候选人数
     *
     * @param postIds 招聘职位编号集合
     * @return 招聘职位编号与已入职候选人数的映射
     */
    Map<Long, Long> getJoinedCandidateCountMap(Collection<Long> postIds);

    /**
     * 获得招聘候选人状态统计列表
     *
     * @param pageReqVO 分页查询
     * @return 状态与数量的映射
     */
    Map<Integer, Long> getRecruitCandidateStatusCount(HrmRecruitCandidatePageReqVO pageReqVO);

}
