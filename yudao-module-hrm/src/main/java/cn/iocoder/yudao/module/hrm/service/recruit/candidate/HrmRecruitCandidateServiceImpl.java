package cn.iocoder.yudao.module.hrm.service.recruit.candidate;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeSaveReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.candidate.HrmRecruitCandidateUpdateChannelReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.candidate.HrmRecruitCandidateEntryReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.candidate.HrmRecruitCandidatePageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.candidate.HrmRecruitCandidateSaveReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.candidate.HrmRecruitCandidateUpdateEliminateReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.candidate.HrmRecruitCandidateUpdatePostReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.candidate.HrmRecruitCandidateUpdateStatusReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.candidate.HrmRecruitCandidateDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.config.HrmRecruitChannelDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.post.HrmRecruitPostDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.recruit.candidate.HrmRecruitCandidateMapper;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeEntryStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.recruit.candidate.HrmRecruitCandidateStatusEnum;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.hrm.service.recruit.config.HrmRecruitChannelService;
import cn.iocoder.yudao.module.hrm.service.recruit.post.HrmRecruitPostService;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import javax.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.equalsAny;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.notEqualsAny;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_CANDIDATE_CONVERTED;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.RECRUIT_CANDIDATE_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.RECRUIT_CANDIDATE_CONVERT_STATUS_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.RECRUIT_CANDIDATE_DELETE_STATUS_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.RECRUIT_CANDIDATE_ELIMINATE_STATUS_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.RECRUIT_CANDIDATE_HAS_EMPLOYEE;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.RECRUIT_CANDIDATE_STATUS_TRANSITION_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.*;

/**
 * 招聘候选人 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmRecruitCandidateServiceImpl implements HrmRecruitCandidateService {

    @Resource
    private HrmRecruitCandidateMapper recruitCandidateMapper;

    @Resource
    private HrmRecruitPostService recruitPostService;
    @Resource
    private HrmRecruitChannelService recruitChannelService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private HrmRecruitInterviewService recruitInterviewService;
    @Resource
    private HrmEmployeeService employeeService;

    @Override
    @LogRecord(type = HRM_RECRUIT_CANDIDATE_TYPE, subType = HRM_RECRUIT_CANDIDATE_CREATE_SUB_TYPE,
            bizNo = "{{#recruitCandidate.id}}", success = HRM_RECRUIT_CANDIDATE_CREATE_SUCCESS)
    public Long createRecruitCandidate(HrmRecruitCandidateSaveReqVO createReqVO) {
        // 1.1 校验招聘职位是否存在
        recruitPostService.validateRecruitPostExists(createReqVO.getPostId());
        // 1.2 校验招聘渠道是否存在
        if (createReqVO.getChannelId() != null) {
            recruitChannelService.validateRecruitChannelExists(createReqVO.getChannelId());
        }

        // 2. 创建候选人，初始状态由后端统一设置
        HrmRecruitCandidateDO recruitCandidate = BeanUtils.toBean(createReqVO, HrmRecruitCandidateDO.class)
                .setStatus(HrmRecruitCandidateStatusEnum.NEW.getStatus()).setStageNumber(0)
                .setStatusUpdateTime(LocalDateTime.now());
        recruitCandidateMapper.insert(recruitCandidate);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("recruitCandidate", recruitCandidate);
        return recruitCandidate.getId();
    }

    @Override
    @LogRecord(type = HRM_RECRUIT_CANDIDATE_TYPE, subType = HRM_RECRUIT_CANDIDATE_UPDATE_SUB_TYPE,
            bizNo = "{{#updateReqVO.id}}", success = HRM_RECRUIT_CANDIDATE_UPDATE_SUCCESS)
    public void updateRecruitCandidate(HrmRecruitCandidateSaveReqVO updateReqVO) {
        // 1.1 校验候选人是否存在
        HrmRecruitCandidateDO recruitCandidate = validateRecruitCandidateExists(updateReqVO.getId());
        // 1.2 校验招聘职位是否存在
        recruitPostService.validateRecruitPostExists(updateReqVO.getPostId());
        // 1.3 校验招聘渠道是否存在
        if (updateReqVO.getChannelId() != null) {
            recruitChannelService.validateRecruitChannelExists(updateReqVO.getChannelId());
        }

        // 2. 更新候选人
        recruitCandidateMapper.updateForEdit(BeanUtils.toBean(updateReqVO, HrmRecruitCandidateDO.class));

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT,
                BeanUtils.toBean(recruitCandidate, HrmRecruitCandidateSaveReqVO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_RECRUIT_CANDIDATE_TYPE, subType = HRM_RECRUIT_CANDIDATE_DELETE_SUB_TYPE,
            bizNo = "{{#id}}", success = HRM_RECRUIT_CANDIDATE_DELETE_SUCCESS)
    public void deleteRecruitCandidate(Long id) {
        // 1. 校验候选人是否存在
        HrmRecruitCandidateDO recruitCandidate = validateRecruitCandidateExistsForUpdate(id);
        if (notEqualsAny(recruitCandidate.getStatus(), HrmRecruitCandidateStatusEnum.NEW.getStatus(),
                HrmRecruitCandidateStatusEnum.PRIMARY_PASS.getStatus(),
                HrmRecruitCandidateStatusEnum.INTERVIEW.getStatus(),
                HrmRecruitCandidateStatusEnum.INTERVIEW_PASS.getStatus(),
                HrmRecruitCandidateStatusEnum.ELIMINATED.getStatus())) {
            throw exception(RECRUIT_CANDIDATE_DELETE_STATUS_INVALID);
        }
        if (employeeService.getEmployeeByCandidateId(id) != null) {
            throw exception(RECRUIT_CANDIDATE_HAS_EMPLOYEE);
        }

        // 2.1 删除候选人的面试记录
        recruitInterviewService.deleteRecruitInterviewByCandidateId(id);
        // 2.2 删除候选人
        recruitCandidateMapper.deleteById(id);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("recruitCandidate", recruitCandidate);
    }

    @Override
    public HrmRecruitCandidateDO getRecruitCandidate(Long id) {
        return recruitCandidateMapper.selectById(id);
    }

    @Override
    public HrmRecruitCandidateDO validateRecruitCandidateExistsForUpdate(Long id) {
        HrmRecruitCandidateDO recruitCandidate = recruitCandidateMapper.selectByIdForUpdate(id);
        if (recruitCandidate == null) {
            throw exception(RECRUIT_CANDIDATE_NOT_EXISTS);
        }
        return recruitCandidate;
    }

    @Override
    public List<HrmRecruitCandidateDO> getRecruitCandidateListByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return recruitCandidateMapper.selectByIds(ids);
    }

    @Override
    public PageResult<HrmRecruitCandidateDO> getRecruitCandidatePage(HrmRecruitCandidatePageReqVO pageReqVO) {
        return recruitCandidateMapper.selectPage(pageReqVO);
    }

    @Override
    public void updateRecruitCandidateInterview(Long id, Integer stageNumber) {
        // 1. 校验候选人是否存在
        validateRecruitCandidateExists(id);

        // 2. 更新候选人的面试状态和轮次
        recruitCandidateMapper.updateById(new HrmRecruitCandidateDO()
                .setId(id).setStatus(HrmRecruitCandidateStatusEnum.INTERVIEW.getStatus())
                .setStageNumber(stageNumber).setStatusUpdateTime(LocalDateTime.now()));
    }

    @Override
    public void updateRecruitCandidateStatusByInterviewResult(Long id, Integer status) {
        // 1. 校验候选人是否存在
        validateRecruitCandidateExists(id);

        // 2. 更新候选人状态
        recruitCandidateMapper.updateById(new HrmRecruitCandidateDO()
                .setId(id).setStatus(status).setStatusUpdateTime(LocalDateTime.now()));
    }

    @Override
    public void updateRecruitCandidateInterviewState(Long id, Integer status, Integer stageNumber) {
        validateRecruitCandidateExists(id);
        recruitCandidateMapper.updateById(new HrmRecruitCandidateDO().setId(id)
                .setStatus(status).setStageNumber(stageNumber).setStatusUpdateTime(LocalDateTime.now()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_RECRUIT_CANDIDATE_TYPE, subType = HRM_RECRUIT_CANDIDATE_UPDATE_STATUS_SUB_TYPE,
            bizNo = "{{#reqVO.id}}", success = HRM_RECRUIT_CANDIDATE_UPDATE_STATUS_SUCCESS)
    public void updateRecruitCandidateStatus(HrmRecruitCandidateUpdateStatusReqVO reqVO) {
        // 1. 校验候选人是否存在
        HrmRecruitCandidateDO recruitCandidate = validateRecruitCandidateExistsForUpdate(reqVO.getId());
        if (Objects.equals(recruitCandidate.getStatus(), reqVO.getStatus())) {
            LogRecordContext.putVariable("recruitCandidate", recruitCandidate);
            return;
        }
        if (!isStatusTransitionAllowed(recruitCandidate.getStatus(), reqVO.getStatus())) {
            HrmRecruitCandidateStatusEnum targetStatus = HrmRecruitCandidateStatusEnum.valueOf(reqVO.getStatus());
            throw exception(RECRUIT_CANDIDATE_STATUS_TRANSITION_INVALID,
                    targetStatus == null ? reqVO.getStatus() : targetStatus.getName());
        }

        // 2. 更新候选人状态
        LocalDateTime currentTime = LocalDateTime.now();
        recruitCandidateMapper.updateById(new HrmRecruitCandidateDO()
                .setId(recruitCandidate.getId()).setStatus(reqVO.getStatus()).setStatusUpdateTime(currentTime));

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("recruitCandidate", recruitCandidate);
    }

    /**
     * 判断候选人是否允许从来源状态直接流转到目标状态
     *
     * 面试中、待入职和已入职等聚合动作必须走专用业务接口，不允许由通用状态接口跳转。
     *
     * @param sourceStatus 来源状态
     * @param targetStatus 目标状态
     * @return 是否允许流转
     */
    private boolean isStatusTransitionAllowed(Integer sourceStatus, Integer targetStatus) {
        // 新候选人允许通过初选，或不安排面试直接标记为面试通过
        if (Objects.equals(sourceStatus, HrmRecruitCandidateStatusEnum.NEW.getStatus())) {
            return equalsAny(targetStatus, HrmRecruitCandidateStatusEnum.PRIMARY_PASS.getStatus(),
                    HrmRecruitCandidateStatusEnum.INTERVIEW_PASS.getStatus());
        }
        // 初选通过允许退回新候选人，或不安排面试直接标记为面试通过
        if (Objects.equals(sourceStatus, HrmRecruitCandidateStatusEnum.PRIMARY_PASS.getStatus())) {
            return equalsAny(targetStatus, HrmRecruitCandidateStatusEnum.NEW.getStatus(),
                    HrmRecruitCandidateStatusEnum.INTERVIEW_PASS.getStatus());
        }
        // 面试通过允许发送 Offer，或退回到前置筛选状态
        if (Objects.equals(sourceStatus, HrmRecruitCandidateStatusEnum.INTERVIEW_PASS.getStatus())) {
            return equalsAny(targetStatus, HrmRecruitCandidateStatusEnum.OFFER_SENT.getStatus(),
                    HrmRecruitCandidateStatusEnum.NEW.getStatus(),
                    HrmRecruitCandidateStatusEnum.PRIMARY_PASS.getStatus());
        }
        // 已淘汰候选人只允许恢复为新候选人
        return Objects.equals(sourceStatus, HrmRecruitCandidateStatusEnum.ELIMINATED.getStatus())
                && Objects.equals(targetStatus, HrmRecruitCandidateStatusEnum.NEW.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmRecruitCandidateEntry(Long id, LocalDateTime entryTime) {
        // 1. 校验候选人处于待入职状态
        HrmRecruitCandidateDO recruitCandidate = validateRecruitCandidateExistsForUpdate(id);
        if (ObjUtil.notEqual(recruitCandidate.getStatus(),
                HrmRecruitCandidateStatusEnum.PENDING_ENTRY.getStatus())) {
            throw exception(RECRUIT_CANDIDATE_STATUS_TRANSITION_INVALID,
                    HrmRecruitCandidateStatusEnum.JOINED.getName());
        }

        // 2. 确认候选人已入职
        recruitCandidateMapper.updateById(new HrmRecruitCandidateDO().setId(id)
                .setStatus(HrmRecruitCandidateStatusEnum.JOINED.getStatus()).setEntryTime(entryTime)
                .setStatusUpdateTime(LocalDateTime.now()));
    }

    @Override
    @LogRecord(type = HRM_RECRUIT_CANDIDATE_TYPE, subType = HRM_RECRUIT_CANDIDATE_UPDATE_POST_SUB_TYPE,
            bizNo = "{{#reqVO.id}}", success = HRM_RECRUIT_CANDIDATE_UPDATE_POST_SUCCESS)
    public void updateRecruitCandidatePost(HrmRecruitCandidateUpdatePostReqVO reqVO) {
        // 1.1 校验候选人是否存在
        HrmRecruitCandidateDO recruitCandidate = validateRecruitCandidateExists(reqVO.getId());
        // 1.2 校验招聘职位是否存在
        HrmRecruitPostDO recruitPost = recruitPostService.validateRecruitPostExists(reqVO.getPostId());

        // 2. 更新候选人的招聘职位
        recruitCandidateMapper.updateById(new HrmRecruitCandidateDO()
                .setId(reqVO.getId()).setPostId(reqVO.getPostId()));

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("recruitCandidate", recruitCandidate);
        LogRecordContext.putVariable("recruitPost", recruitPost);
    }

    @Override
    @LogRecord(type = HRM_RECRUIT_CANDIDATE_TYPE, subType = HRM_RECRUIT_CANDIDATE_UPDATE_CHANNEL_SUB_TYPE,
            bizNo = "{{#reqVO.id}}", success = HRM_RECRUIT_CANDIDATE_UPDATE_CHANNEL_SUCCESS)
    public void updateRecruitCandidateChannel(HrmRecruitCandidateUpdateChannelReqVO reqVO) {
        // 1.1 校验候选人是否存在
        HrmRecruitCandidateDO recruitCandidate = validateRecruitCandidateExists(reqVO.getId());
        // 1.2 校验招聘渠道是否存在
        HrmRecruitChannelDO recruitChannel = recruitChannelService.validateRecruitChannelExists(reqVO.getChannelId());

        // 2. 更新候选人的招聘渠道
        recruitCandidateMapper.updateById(new HrmRecruitCandidateDO()
                .setId(reqVO.getId()).setChannelId(reqVO.getChannelId()));

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("recruitCandidate", recruitCandidate);
        LogRecordContext.putVariable("recruitChannel", recruitChannel);
    }

    @Override
    public void updateRecruitCandidateChannelByChannelId(Long channelId, Long newChannelId) {
        recruitCandidateMapper.updateChannelIdByChannelId(channelId, newChannelId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_RECRUIT_CANDIDATE_TYPE, subType = HRM_RECRUIT_CANDIDATE_ELIMINATE_SUB_TYPE,
            bizNo = "{{#reqVO.id}}", success = HRM_RECRUIT_CANDIDATE_ELIMINATE_SUCCESS)
    public void eliminateRecruitCandidate(HrmRecruitCandidateUpdateEliminateReqVO reqVO) {
        // 1. 校验候选人是否存在
        HrmRecruitCandidateDO recruitCandidate = validateRecruitCandidateExistsForUpdate(reqVO.getId());
        if (!HrmRecruitCandidateStatusEnum.ELIMINATE_STATUSES.contains(recruitCandidate.getStatus())) {
            throw exception(RECRUIT_CANDIDATE_ELIMINATE_STATUS_INVALID);
        }

        // 2. 正在面试时同步结束当前面试，避免候选人状态与面试结果矛盾
        if (Objects.equals(recruitCandidate.getStatus(), HrmRecruitCandidateStatusEnum.INTERVIEW.getStatus())) {
            recruitInterviewService.finishCurrentRecruitInterviewForElimination(
                    recruitCandidate.getId(), reqVO.getEliminate());
        }

        // 3.1 查询已生成的员工档案
        HrmEmployeeDO employee = employeeService.getEmployeeByCandidateId(recruitCandidate.getId());
        // 3.2 删除已生成的员工档案
        if (employee != null) {
            employeeService.deleteEmployee(employee.getId());
        }
        // 4. 淘汰候选人
        recruitCandidateMapper.updateById(new HrmRecruitCandidateDO()
                .setId(recruitCandidate.getId()).setEliminate(reqVO.getEliminate()).setRemark(reqVO.getRemark())
                .setStatus(HrmRecruitCandidateStatusEnum.ELIMINATED.getStatus())
                .setStatusUpdateTime(LocalDateTime.now()));

        // 5. 记录操作日志上下文
        LogRecordContext.putVariable("recruitCandidate", recruitCandidate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_RECRUIT_CANDIDATE_TYPE, subType = HRM_RECRUIT_CANDIDATE_CONVERT_EMPLOYEE_SUB_TYPE,
            bizNo = "{{#reqVO.candidateId}}", success = HRM_RECRUIT_CANDIDATE_CONVERT_EMPLOYEE_SUCCESS)
    public Long convertRecruitCandidateToEmployee(HrmRecruitCandidateEntryReqVO reqVO) {
        // 1.1 校验候选人是否存在
        HrmRecruitCandidateDO recruitCandidate = validateRecruitCandidateExistsForUpdate(reqVO.getCandidateId());
        if (!HrmRecruitCandidateStatusEnum.CONVERT_EMPLOYEE_STATUSES.contains(recruitCandidate.getStatus())) {
            throw exception(RECRUIT_CANDIDATE_CONVERT_STATUS_INVALID);
        }
        // 1.2 校验候选人是否已转为员工
        if (employeeService.getEmployeeByCandidateId(recruitCandidate.getId()) != null) {
            throw exception(EMPLOYEE_CANDIDATE_CONVERTED);
        }
        // 1.3 校验招聘职位是否存在
        recruitPostService.validateRecruitPostExists(recruitCandidate.getPostId());

        // 2. 创建员工
        HrmEmployeeSaveReqVO employeeReqVO = BeanUtils.toBean(reqVO, HrmEmployeeSaveReqVO.class)
                .setId(null).setCandidateId(recruitCandidate.getId())
                .setChannelId(recruitCandidate.getChannelId());
        Long employeeId = employeeService.createEmployee(employeeReqVO);

        // 3. 更新候选人的入职状态
        recruitCandidateMapper.updateById(new HrmRecruitCandidateDO()
                .setId(recruitCandidate.getId())
                .setStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus().equals(employeeReqVO.getEntryStatus())
                        ? HrmRecruitCandidateStatusEnum.JOINED.getStatus()
                        : HrmRecruitCandidateStatusEnum.PENDING_ENTRY.getStatus())
                .setEntryTime(reqVO.getEntryTime()).setStatusUpdateTime(LocalDateTime.now()));

        // 4. 记录操作日志上下文
        LogRecordContext.putVariable("recruitCandidate", recruitCandidate);
        LogRecordContext.putVariable("employeeId", employeeId);
        return employeeId;
    }

    @Override
    public List<HrmRecruitCandidateDO> getRecruitCandidateList(Collection<Integer> statuses,
                                                               LocalDateTime statusUpdateTime) {
        if (CollUtil.isEmpty(statuses)) {
            return Collections.emptyList();
        }
        return recruitCandidateMapper.selectListByStatusesAndStatusUpdateTimeBefore(statuses, statusUpdateTime);
    }

    @Override
    public Long getRecruitCandidateCountByStatusAndStatusUpdateTimeBetween(
            Integer status, LocalDateTime[] statusUpdateTimes) {
        return recruitCandidateMapper.selectCountByStatusAndStatusUpdateTimeBetween(
                status, statusUpdateTimes);
    }

    @Override
    public Long getRecruitCandidateCountByStatusAndEntryTimeBetween(
            Integer status, LocalDateTime[] entryTimes) {
        return recruitCandidateMapper.selectCountByStatusAndEntryTimeBetween(
                status, entryTimes);
    }

    @Override
    public Map<Long, Long> getJoinedCandidateCountMap(Collection<Long> postIds) {
        if (CollUtil.isEmpty(postIds)) {
            return Collections.emptyMap();
        }
        return recruitCandidateMapper.selectCountMapByPostIdsAndStatus(postIds,
                HrmRecruitCandidateStatusEnum.JOINED.getStatus());
    }

    @Override
    public Map<Integer, Long> getRecruitCandidateStatusCount(HrmRecruitCandidatePageReqVO pageReqVO) {
        return recruitCandidateMapper.selectCountMapByStatus(pageReqVO);
    }

    @Override
    public HrmRecruitCandidateDO validateRecruitCandidateExists(Long id) {
        HrmRecruitCandidateDO recruitCandidate = recruitCandidateMapper.selectById(id);
        if (recruitCandidate == null) {
            throw exception(RECRUIT_CANDIDATE_NOT_EXISTS);
        }
        return recruitCandidate;
    }

}
