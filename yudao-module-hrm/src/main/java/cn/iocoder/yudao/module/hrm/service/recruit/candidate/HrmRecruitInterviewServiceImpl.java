package cn.iocoder.yudao.module.hrm.service.recruit.candidate;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.interview.HrmRecruitInterviewResultReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.interview.HrmRecruitInterviewSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.candidate.HrmRecruitCandidateDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.candidate.HrmRecruitInterviewDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.recruit.candidate.HrmRecruitInterviewMapper;
import cn.iocoder.yudao.module.hrm.enums.MessageTemplateConstants;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeEntryStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.recruit.candidate.HrmRecruitCandidateStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.recruit.candidate.HrmRecruitInterviewResultEnum;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.notEqualsAny;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.RECRUIT_INTERVIEW_CANCEL_REASON_REQUIRED;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.RECRUIT_INTERVIEW_FINISHED;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.RECRUIT_INTERVIEW_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.RECRUIT_INTERVIEW_NOT_CURRENT;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.RECRUIT_INTERVIEW_RESULT_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.RECRUIT_INTERVIEW_STATE_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.*;

/**
 * 招聘面试 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmRecruitInterviewServiceImpl implements HrmRecruitInterviewService {

    private static final DateTimeFormatter INTERVIEW_TIME_FORMATTER = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    @Resource
    private HrmRecruitInterviewMapper recruitInterviewMapper;
    @Resource
    private HrmRecruitCandidateService recruitCandidateService;
    @Resource
    private HrmEmployeeService employeeService;
    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_RECRUIT_CANDIDATE_TYPE, subType = HRM_RECRUIT_CANDIDATE_ARRANGE_INTERVIEW_SUB_TYPE,
            bizNo = "{{#createReqVO.candidateId}}", success = HRM_RECRUIT_CANDIDATE_ARRANGE_INTERVIEW_SUCCESS)
    public Long createRecruitInterview(HrmRecruitInterviewSaveReqVO createReqVO) {
        // 1. 校验候选人和面试官存在
        HrmRecruitCandidateDO recruitCandidate = recruitCandidateService
                .validateRecruitCandidateExistsForUpdate(createReqVO.getCandidateId());
        validateCandidateCanArrangeInterview(recruitCandidate);
        validateInterviewEmployees(createReqVO);

        // 2. 创建面试记录。候选人仍在当前面试轮次时，复用最近一条记录
        HrmRecruitInterviewDO recruitInterview = BeanUtils.toBean(createReqVO, HrmRecruitInterviewDO.class);
        HrmRecruitInterviewDO latestInterview = recruitInterviewMapper
                .selectLatestByCandidateId(createReqVO.getCandidateId());
        if (latestInterview != null && HrmRecruitCandidateStatusEnum.INTERVIEW.getStatus()
                .equals(recruitCandidate.getStatus())) {
            validateCurrentInterview(recruitCandidate, latestInterview);
            if (notEqualsAny(latestInterview.getResult(), HrmRecruitInterviewResultEnum.UNFINISHED.getResult(),
                    HrmRecruitInterviewResultEnum.CANCEL.getResult())) {
                throw exception(RECRUIT_INTERVIEW_STATE_INVALID);
            }
            recruitInterview.setId(latestInterview.getId()).setCandidateId(latestInterview.getCandidateId())
                    .setStageNumber(latestInterview.getStageNumber())
                    .setResult(HrmRecruitInterviewResultEnum.UNFINISHED.getResult());
            recruitInterviewMapper.updateRecruitInterviewArrangement(recruitInterview);
        } else {
            recruitInterview.setStageNumber(latestInterview == null ? 1 : latestInterview.getStageNumber() + 1)
                    .setResult(HrmRecruitInterviewResultEnum.UNFINISHED.getResult());
            recruitInterviewMapper.insert(recruitInterview);
        }

        // 3. 更新候选人的面试状态和轮次
        recruitCandidateService.updateRecruitCandidateInterview(
                recruitInterview.getCandidateId(), recruitInterview.getStageNumber());

        // 4. 通知面试官
        sendRecruitInterviewArrangedMessage(recruitCandidate, recruitInterview);

        // 5. 记录操作日志上下文
        LogRecordContext.putVariable("recruitCandidate", recruitCandidate);
        LogRecordContext.putVariable("recruitInterview", recruitInterview);
        return recruitInterview.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_RECRUIT_CANDIDATE_TYPE, subType = HRM_RECRUIT_CANDIDATE_UPDATE_INTERVIEW_SUB_TYPE,
            bizNo = "{{#updateReqVO.candidateId}}", success = HRM_RECRUIT_CANDIDATE_UPDATE_INTERVIEW_SUCCESS)
    public void updateRecruitInterview(HrmRecruitInterviewSaveReqVO updateReqVO) {
        // 1.1 校验面试记录、候选人和面试官是否存在
        HrmRecruitInterviewDO recruitInterview = validateRecruitInterviewExists(updateReqVO.getId());
        HrmRecruitCandidateDO recruitCandidate = recruitCandidateService
                .validateRecruitCandidateExistsForUpdate(recruitInterview.getCandidateId());
        validateCurrentInterview(recruitCandidate, recruitInterview);
        validateInterviewEmployees(updateReqVO);
        // 1.2 已完成的历史面试不允许再修改安排
        if (notEqualsAny(recruitInterview.getResult(), HrmRecruitInterviewResultEnum.UNFINISHED.getResult(),
                HrmRecruitInterviewResultEnum.CANCEL.getResult())) {
            throw exception(RECRUIT_INTERVIEW_FINISHED);
        }

        // 2.1 更新当前轮次面试安排；取消后重新安排时恢复为未完成
        updateReqVO.setCandidateId(recruitInterview.getCandidateId()).setStageNumber(recruitInterview.getStageNumber());
        HrmRecruitInterviewDO updateObj = BeanUtils.toBean(updateReqVO, HrmRecruitInterviewDO.class)
                .setResult(HrmRecruitInterviewResultEnum.UNFINISHED.getResult());
        recruitInterviewMapper.updateRecruitInterviewArrangement(updateObj);
        // 2.2 重新安排已取消的面试时，恢复候选人的面试状态和轮次
        if (HrmRecruitInterviewResultEnum.CANCEL.getResult().equals(recruitInterview.getResult())) {
            recruitCandidateService.updateRecruitCandidateInterview(
                    recruitInterview.getCandidateId(), recruitInterview.getStageNumber());
        }

        // 3. 通知面试官
        sendRecruitInterviewArrangedMessage(recruitCandidate, updateObj);

        // 4. 记录操作日志上下文
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT,
                BeanUtils.toBean(recruitInterview, HrmRecruitInterviewSaveReqVO.class));
        LogRecordContext.putVariable("recruitCandidate", recruitCandidate);
        LogRecordContext.putVariable("recruitInterview", recruitInterview);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRecruitInterview(Long id) {
        // 1. 校验面试记录和候选人
        HrmRecruitInterviewDO recruitInterview = validateRecruitInterviewExists(id);
        HrmRecruitCandidateDO recruitCandidate = recruitCandidateService
                .validateRecruitCandidateExistsForUpdate(recruitInterview.getCandidateId());
        HrmRecruitInterviewDO latestInterview = recruitInterviewMapper
                .selectLatestByCandidateId(recruitInterview.getCandidateId());
        boolean currentInterview = latestInterview != null && Objects.equals(latestInterview.getId(), id);
        if (currentInterview) {
            validateCurrentInterview(recruitCandidate, recruitInterview);
        }

        // 2. 删除面试记录
        recruitInterviewMapper.deleteById(id);

        // 3. 删除当前面试时，根据剩余最近一轮修复候选人状态和轮次
        if (currentInterview) {
            repairCandidateInterviewState(recruitCandidate.getId(),
                    recruitInterviewMapper.selectLatestByCandidateId(recruitCandidate.getId()));
        }
    }

    @Override
    public HrmRecruitInterviewDO getRecruitInterview(Long id) {
        return recruitInterviewMapper.selectById(id);
    }

    @Override
    public List<HrmRecruitInterviewDO> getRecruitInterviewListByCandidateId(Long candidateId) {
        return recruitInterviewMapper.selectListByCandidateId(candidateId);
    }

    @Override
    public List<HrmRecruitInterviewDO> getRecruitInterviewListByCandidateIds(Collection<Long> candidateIds) {
        if (CollUtil.isEmpty(candidateIds)) {
            return Collections.emptyList();
        }
        return recruitInterviewMapper.selectListByCandidateIds(candidateIds);
    }

    @Override
    public List<HrmRecruitInterviewDO> getRecruitInterviewListByInterviewTimeBetween(
            LocalDateTime[] interviewTimes) {
        return recruitInterviewMapper.selectListByInterviewTimeBetween(interviewTimes);
    }

    @Override
    public void deleteRecruitInterviewByCandidateId(Long candidateId) {
        recruitInterviewMapper.deleteByCandidateId(candidateId);
    }

    @Override
    public void finishCurrentRecruitInterviewForElimination(Long candidateId, String eliminateReason) {
        HrmRecruitInterviewDO latestInterview = recruitInterviewMapper.selectLatestByCandidateId(candidateId);
        if (latestInterview == null) {
            throw exception(RECRUIT_INTERVIEW_STATE_INVALID);
        }
        if (Objects.equals(latestInterview.getResult(), HrmRecruitInterviewResultEnum.CANCEL.getResult())) {
            return;
        }
        if (ObjUtil.notEqual(latestInterview.getResult(), HrmRecruitInterviewResultEnum.UNFINISHED.getResult())) {
            throw exception(RECRUIT_INTERVIEW_STATE_INVALID);
        }
        recruitInterviewMapper.updateRecruitInterviewResult(new HrmRecruitInterviewDO()
                .setId(latestInterview.getId()).setResult(HrmRecruitInterviewResultEnum.NOT_PASS.getResult())
                .setEvaluate(eliminateReason));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_RECRUIT_CANDIDATE_TYPE, subType = HRM_RECRUIT_CANDIDATE_INTERVIEW_RESULT_SUB_TYPE,
            bizNo = "{{#recruitInterview.candidateId}}", success = HRM_RECRUIT_CANDIDATE_INTERVIEW_RESULT_SUCCESS)
    public void updateRecruitInterviewResult(HrmRecruitInterviewResultReqVO reqVO) {
        // 1.1 校验面试记录、候选人是否存在
        HrmRecruitInterviewDO recruitInterview = validateRecruitInterviewExists(reqVO.getId());
        HrmRecruitCandidateDO recruitCandidate = recruitCandidateService
                .validateRecruitCandidateExistsForUpdate(recruitInterview.getCandidateId());
        // 1.2 只有当前未完成面试允许登记完成结果
        validateCurrentInterview(recruitCandidate, recruitInterview);
        if (ObjUtil.notEqual(recruitInterview.getResult(), HrmRecruitInterviewResultEnum.UNFINISHED.getResult())
                || HrmRecruitInterviewResultEnum.UNFINISHED.getResult().equals(reqVO.getResult())) {
            throw exception(RECRUIT_INTERVIEW_RESULT_INVALID);
        }
        // 1.3 校验取消原因
        if (HrmRecruitInterviewResultEnum.CANCEL.getResult().equals(reqVO.getResult())
                && StrUtil.isBlank(reqVO.getCancelReason())) {
            throw exception(RECRUIT_INTERVIEW_CANCEL_REASON_REQUIRED);
        }

        // 2. 更新面试结果
        boolean cancel = HrmRecruitInterviewResultEnum.CANCEL.getResult().equals(reqVO.getResult());
        reqVO.setEvaluate(cancel ? null : reqVO.getEvaluate());
        reqVO.setCancelReason(cancel ? reqVO.getCancelReason() : null);
        recruitInterviewMapper.updateRecruitInterviewResult(BeanUtils.toBean(reqVO, HrmRecruitInterviewDO.class));

        // 3. 根据面试结果更新候选人状态；取消时仅刷新最后处理时间
        Integer candidateStatus = null;
        if (HrmRecruitInterviewResultEnum.PASS.getResult().equals(reqVO.getResult())) {
            candidateStatus = HrmRecruitCandidateStatusEnum.INTERVIEW_PASS.getStatus();
        } else if (HrmRecruitInterviewResultEnum.NOT_PASS.getResult().equals(reqVO.getResult())) {
            candidateStatus = HrmRecruitCandidateStatusEnum.ELIMINATED.getStatus();
        }
        if (candidateStatus != null) {
            recruitCandidateService.updateRecruitCandidateStatusByInterviewResult(
                    recruitInterview.getCandidateId(), candidateStatus);
        } else {
            recruitCandidateService.updateRecruitCandidateInterviewState(recruitCandidate.getId(),
                    recruitCandidate.getStatus(), recruitCandidate.getStageNumber());
        }

        // 4. 记录操作日志上下文
        LogRecordContext.putVariable("recruitCandidate", recruitCandidate);
        LogRecordContext.putVariable("recruitInterview", recruitInterview);
    }

    private void validateCandidateCanArrangeInterview(HrmRecruitCandidateDO recruitCandidate) {
        if (!HrmRecruitCandidateStatusEnum.INTERVIEW_ARRANGE_STATUSES.contains(recruitCandidate.getStatus())) {
            throw exception(RECRUIT_INTERVIEW_STATE_INVALID);
        }
    }

    private void validateCurrentInterview(HrmRecruitCandidateDO recruitCandidate,
                                          HrmRecruitInterviewDO recruitInterview) {
        HrmRecruitInterviewDO latestInterview = recruitInterviewMapper
                .selectLatestByCandidateId(recruitCandidate.getId());
        if (latestInterview == null || ObjUtil.notEqual(latestInterview.getId(), recruitInterview.getId())
                || ObjUtil.notEqual(recruitCandidate.getStatus(), HrmRecruitCandidateStatusEnum.INTERVIEW.getStatus())
                || ObjUtil.notEqual(recruitCandidate.getStageNumber(), recruitInterview.getStageNumber())) {
            throw exception(RECRUIT_INTERVIEW_NOT_CURRENT);
        }
    }

    private void repairCandidateInterviewState(Long candidateId, HrmRecruitInterviewDO latestInterview) {
        if (latestInterview == null) {
            recruitCandidateService.updateRecruitCandidateInterviewState(
                    candidateId, HrmRecruitCandidateStatusEnum.NEW.getStatus(), 0);
            return;
        }
        Integer status = HrmRecruitCandidateStatusEnum.INTERVIEW.getStatus();
        if (Objects.equals(latestInterview.getResult(), HrmRecruitInterviewResultEnum.PASS.getResult())) {
            status = HrmRecruitCandidateStatusEnum.INTERVIEW_PASS.getStatus();
        } else if (Objects.equals(latestInterview.getResult(), HrmRecruitInterviewResultEnum.NOT_PASS.getResult())) {
            status = HrmRecruitCandidateStatusEnum.ELIMINATED.getStatus();
        }
        recruitCandidateService.updateRecruitCandidateInterviewState(
                candidateId, status, latestInterview.getStageNumber());
    }

    private HrmRecruitInterviewDO validateRecruitInterviewExists(Long id) {
        HrmRecruitInterviewDO interview = recruitInterviewMapper.selectById(id);
        if (interview == null) {
            throw exception(RECRUIT_INTERVIEW_NOT_EXISTS);
        }
        return interview;
    }

    private void validateInterviewEmployees(HrmRecruitInterviewSaveReqVO reqVO) {
        // 1. 其他面试官按首次出现顺序去重，并排除主面试官
        Set<Long> otherEmployeeIds = new LinkedHashSet<>(
                CollUtil.emptyIfNull(reqVO.getOtherInterviewEmployeeIds()));
        otherEmployeeIds.remove(reqVO.getInterviewEmployeeId());
        reqVO.setOtherInterviewEmployeeIds(new ArrayList<>(otherEmployeeIds));

        // 2. 一次性校验全部面试官员工档案存在
        Set<Long> employeeIds = new HashSet<>(otherEmployeeIds);
        employeeIds.add(reqVO.getInterviewEmployeeId());
        employeeService.validateEmployeeListByEntryStatus(
                employeeIds, HrmEmployeeEntryStatusEnum.ACTIVE.getStatus());
    }

    private void sendRecruitInterviewArrangedMessage(HrmRecruitCandidateDO recruitCandidate,
                                                       HrmRecruitInterviewDO recruitInterview) {
        // 1. 获得已绑定后台账号的面试官用户编号
        Set<Long> employeeIds = new LinkedHashSet<>();
        if (recruitInterview.getInterviewEmployeeId() != null) {
            employeeIds.add(recruitInterview.getInterviewEmployeeId());
        }
        if (CollUtil.isNotEmpty(recruitInterview.getOtherInterviewEmployeeIds())) {
            employeeIds.addAll(recruitInterview.getOtherInterviewEmployeeIds());
        }
        if (CollUtil.isEmpty(employeeIds)) {
            return;
        }
        Map<Long, HrmEmployeeDO> employeeMap = employeeService.getEmployeeMap(employeeIds);
        if (CollUtil.isEmpty(employeeMap)) {
            return;
        }
        Set<Long> userIds = convertSet(employeeMap.values(), HrmEmployeeDO::getUserId,
                employee -> employee.getUserId() != null);

        // 2. 发送面试安排站内信
        Map<String, Object> templateParams = new HashMap<>(4);
        templateParams.put("candidateName", recruitCandidate.getName());
        templateParams.put("stageNumber", recruitInterview.getStageNumber());
        templateParams.put("interviewTime", INTERVIEW_TIME_FORMATTER.format(recruitInterview.getInterviewTime()));
        userIds.forEach(userId -> notifyMessageSendApi.sendSingleMessageToAdmin(new NotifySendSingleToUserReqDTO()
                .setUserId(userId).setTemplateCode(MessageTemplateConstants.RECRUIT_INTERVIEW_ARRANGED)
                .setTemplateParams(templateParams)));
    }

}
