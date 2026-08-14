package cn.iocoder.yudao.module.hrm.service.performance.assessment;

import cn.iocoder.yudao.module.hrm.service.performance.plan.HrmPerformancePlanService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.process.HrmPortalPerformanceAppealReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.process.HrmPortalPerformanceConfirmReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.process.HrmPortalPerformanceHandleStageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.process.HrmPortalPerformanceProcessRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.task.HrmPortalPerformanceTaskPageReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentAppealRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentStageDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.plan.HrmPerformancePlanDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.plan.HrmPerformancePlanDO.HandlerStage;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.plan.HrmPerformancePlanDO.ReviewStage;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.assessment.HrmPerformanceAssessmentAppealRecordMapper;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.assessment.HrmPerformanceAssessmentMapper;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.assessment.HrmPerformanceAssessmentQuotaMapper;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.assessment.HrmPerformanceAssessmentQuotaScoreMapper;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.assessment.HrmPerformanceAssessmentStageMapper;
import cn.iocoder.yudao.module.hrm.enums.MessageTemplateConstants;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAppealStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAppealRecordStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAppealTimeoutActionEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAssessmentActionTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAssessmentProcessStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAssessmentStageStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceConfirmationResultEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformancePlanOperationTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformancePlanStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceQuotaSettingTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceRaterTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceResultAuditStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceStageTypeEnum;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_ASSESSMENT_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_DATA_ILLEGAL;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_PROCESS_CONFIG_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_PROCESS_RUNNING_CANNOT_MODIFY;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_REVIEW_STAGE_CONFIG_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_REVIEW_STAGE_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_STAGE_ACTION_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_STAGE_HANDLER_USER_NOT_BOUND;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_STAGE_NO_PERMISSION;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_TARGET_CONFIRM_CONFIG_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_TARGET_CONFIRM_NO_PERMISSION;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_APPEAL_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_APPEAL_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_APPEAL_HANDLE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_APPEAL_HANDLE_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_APPEAL_TIMEOUT_HANDLE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_APPEAL_TIMEOUT_HANDLE_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_CONFIRM_RESULT_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_CONFIRM_RESULT_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_CONFIRM_TARGET_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_CONFIRM_TARGET_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_RESULT_AUDIT_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_RESULT_AUDIT_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_TYPE;

/**
 * HRM 绩效流程 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmPerformanceAssessmentProcessServiceImpl
        implements HrmPerformanceAssessmentProcessService {

    @Resource
    private HrmPerformanceAssessmentMapper assessmentMapper;
    @Resource
    private HrmPerformanceAssessmentQuotaMapper assessmentQuotaMapper;
    @Resource
    private HrmPerformanceAssessmentStageMapper assessmentStageMapper;
    @Resource
    private HrmPerformanceAssessmentQuotaScoreMapper assessmentQuotaScoreMapper;
    @Resource
    private HrmPerformanceAssessmentAppealRecordMapper assessmentAppealRecordMapper;

    @Resource
    private HrmEmployeeService employeeService;
    @Resource
    private HrmPerformancePlanService planService;
    @Resource
    private HrmPerformanceAssessmentActionRecordService assessmentActionRecordService;

    @Resource
    private DeptApi deptApi;
    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;

    // ==================== 阶段管理 ====================

    @Override
    public void initializeAssessmentStages(
            HrmPerformancePlanDO plan, HrmPerformanceAssessmentDO assessment) {
        // 1. 校验绩效阶段是否已初始化
        List<HrmPerformanceAssessmentStageDO> existingStages =
                assessmentStageMapper.selectListByAssessmentId(assessment.getId());
        if (CollUtil.isNotEmpty(existingStages)) {
            return;
        }
        HrmEmployeeDO employee = employeeService.validateEmployeeExists(
                assessment.getEmployeeId());

        // 重要：收集所有阶段，统一设置阶段顺序后批量插入，避免阶段顺序不一致导致的流程异常。
        List<HrmPerformanceAssessmentStageDO> stages = new ArrayList<>();

        // 2.1 初始化指标制定、目标确认阶段
        if (Objects.equals(plan.getQuotaSettingType(), HrmPerformanceQuotaSettingTypeEnum.EMPLOYEE.getType())) {
            stages.add(buildAssessmentStage(assessment.getId(), HrmPerformanceStageTypeEnum.FILL_QUOTA,
                    employee.getId()));
        }
        if (Boolean.TRUE.equals(plan.getTargetConfirmation())) {
            HrmEmployeeDO handler = resolveReviewer(employee, plan.getTargetConfirmationStage());
            if (handler == null) {
                throw exception(PERFORMANCE_TARGET_CONFIRM_CONFIG_INVALID);
            }
            stages.add(buildAssessmentStage(assessment.getId(), HrmPerformanceStageTypeEnum.TARGET_CONFIRM,
                    handler.getId()));
        }
        // 2.2 初始化评分阶段。无法解析的评分人权重合并到相邻可执行阶段
        List<HrmPerformanceAssessmentStageDO> reviewStages = new ArrayList<>();
        BigDecimal carriedWeight = BigDecimal.ZERO;
        for (ReviewStage planStage : plan.getReviewStages()) {
            HrmEmployeeDO reviewer = resolveReviewer(employee, planStage.getRater());
            // 特殊：无法生成可执行任务时，将权重合并到相邻阶段，保证实际评分阶段权重合计为 100%。
            if (reviewer == null) {
                if (CollUtil.isEmpty(reviewStages)) {
                    carriedWeight = carriedWeight.add(planStage.getWeight());
                } else {
                    HrmPerformanceAssessmentStageDO lastStage = CollUtil.getLast(reviewStages);
                    lastStage.setWeight(lastStage.getWeight().add(planStage.getWeight()));
                }
                continue;
            }
            // 同一员工命中多个评分人配置时，复用已有阶段并累计评分权重
            HrmPerformanceAssessmentStageDO existingStage = CollUtil.findOne(reviewStages,
                    stage -> Objects.equals(stage.getHandlerEmployeeId(), reviewer.getId()));
            if (existingStage != null) {
                existingStage.setWeight(existingStage.getWeight().add(planStage.getWeight()));
                continue;
            }
            HrmPerformanceAssessmentStageDO assessmentStage = BeanUtils.toBean(
                    planStage, HrmPerformanceAssessmentStageDO.class);
            assessmentStage.setId(null).setAssessmentId(assessment.getId())
                    .setName(StringUtils.hasText(planStage.getName())
                            ? planStage.getName() : buildReviewName(planStage))
                    .setRaterType(planStage.getRater().getType())
                    .setType(Objects.equals(planStage.getRater().getType(),
                            HrmPerformanceRaterTypeEnum.SELF.getType())
                            ? HrmPerformanceStageTypeEnum.SELF_SCORE.getType()
                            : HrmPerformanceStageTypeEnum.OTHER_SCORE.getType())
                    .setHandlerEmployeeId(reviewer.getId())
                    .setWeight(planStage.getWeight().add(carriedWeight))
                    .setStatus(HrmPerformanceAssessmentStageStatusEnum.NOT_PROCESSED.getStatus());
            carriedWeight = BigDecimal.ZERO;
            reviewStages.add(assessmentStage);
        }
        if (CollUtil.isEmpty(reviewStages)) {
            throw exception(PERFORMANCE_REVIEW_STAGE_CONFIG_INVALID);
        }
        if (carriedWeight.signum() > 0) {
            HrmPerformanceAssessmentStageDO lastStage = CollUtil.getLast(reviewStages);
            lastStage.setWeight(lastStage.getWeight().add(carriedWeight));
        }
        stages.addAll(reviewStages);
        // 2.3 初始化结果审核、结果确认、申诉和结束阶段
        if (Boolean.TRUE.equals(plan.getResultAudit())) {
            addHandlerStages(stages, plan.getResultAuditStages(), assessment, employee,
                    HrmPerformanceStageTypeEnum.RESULT_AUDIT);
        }
        if (Boolean.TRUE.equals(plan.getResultConfirmation())) {
            stages.add(buildAssessmentStage(assessment.getId(), HrmPerformanceStageTypeEnum.RESULT_CONFIRM,
                    employee.getId()));
            addHandlerStages(stages, plan.getAppealStages(), assessment, employee,
                    HrmPerformanceStageTypeEnum.APPEAL_CONFIRM);
        }
        stages.add(buildAssessmentStage(assessment.getId(), HrmPerformanceStageTypeEnum.END, null));

        // 3.1 校验所有必办节点处理人均已绑定后台账号
        Set<Long> handlerEmployeeIds = convertSet(stages,
                HrmPerformanceAssessmentStageDO::getHandlerEmployeeId, Objects::nonNull);
        Map<Long, HrmEmployeeDO> handlerEmployeeMap = employeeService.getEmployeeMap(handlerEmployeeIds);
        for (Long handlerEmployeeId : handlerEmployeeIds) {
            HrmEmployeeDO handler = handlerEmployeeMap.get(handlerEmployeeId);
            if (handler == null || handler.getUserId() == null) {
                throw exception(PERFORMANCE_STAGE_HANDLER_USER_NOT_BOUND,
                        handler == null ? handlerEmployeeId : handler.getName());
            }
        }
        // 3.2 统一设置阶段顺序并批量插入
        for (int i = 0; i < stages.size(); i++) {
            stages.get(i).setSort(i + 1);
        }
        assessmentStageMapper.insertBatch(stages);
    }

    @Override
    public HrmPerformanceAssessmentStageDO activateFirstReviewStage(HrmPerformanceAssessmentDO assessment) {
        // 1. 查询待激活的首个评分阶段
        List<HrmPerformanceAssessmentStageDO> stages = assessmentStageMapper
                .selectListByAssessmentIdAndTypes(assessment.getId(), HrmPerformanceStageTypeEnum.REVIEW_TYPES);
        HrmPerformanceAssessmentStageDO stage = CollUtil.findOne(stages,
                item -> Objects.equals(item.getStatus(),
                        HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus()));
        if (stage == null) {
            stage = CollUtil.findOne(stages, item -> Objects.equals(item.getStatus(),
                    HrmPerformanceAssessmentStageStatusEnum.NOT_PROCESSED.getStatus()));
        }
        if (stage == null) {
            throw exception(PERFORMANCE_REVIEW_STAGE_NOT_EXISTS);
        }

        // 2. 激活尚未处理的评分阶段
        boolean activated = false;
        if (Objects.equals(stage.getStatus(),
                HrmPerformanceAssessmentStageStatusEnum.NOT_PROCESSED.getStatus())) {
            stage.setStatus(HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus());
            assessmentStageMapper.updateById(stage);
            activated = true;
        }

        // 3. 同步员工绩效考核的当前阶段
        assessment.setStageSort(stage.getSort()).setStageType(stage.getType());
        if (activated) {
            notifyPendingStage(assessment, stage);
        }
        return stage;
    }

    @Override
    public HrmPerformanceAssessmentStageDO activateAssessmentStage(HrmPerformanceAssessmentDO assessment, Integer type) {
        return activateStage(assessment, type, null);
    }

    // ==================== 流程处理 ====================

    @Override
    public PageResult<HrmPerformanceAssessmentStageDO> getMyTargetConfirmationTaskPage(
            Long userId, HrmPortalPerformanceTaskPageReqVO reqVO) {
        return getMyTaskPage(userId, reqVO,
                Collections.singleton(HrmPerformanceStageTypeEnum.TARGET_CONFIRM.getType()));
    }

    @Override
    public PageResult<HrmPerformanceAssessmentStageDO> getMyResultAuditTaskPage(
            Long userId, HrmPortalPerformanceTaskPageReqVO reqVO) {
        return getMyTaskPage(userId, reqVO,
                Collections.singleton(HrmPerformanceStageTypeEnum.RESULT_AUDIT.getType()));
    }

    @Override
    public PageResult<HrmPerformanceAssessmentStageDO> getMyResultConfirmationTaskPage(
            Long userId, HrmPortalPerformanceTaskPageReqVO reqVO) {
        return getMyTaskPage(userId, reqVO,
                Collections.singleton(HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType()));
    }

    @Override
    public PageResult<HrmPerformanceAssessmentStageDO> getMyAppealTaskPage(
            Long userId, HrmPortalPerformanceTaskPageReqVO reqVO) {
        return getMyTaskPage(userId, reqVO,
                Collections.singleton(HrmPerformanceStageTypeEnum.APPEAL_CONFIRM.getType()));
    }

    @Override
    public Map<Integer, Map<Integer, Long>> getMyTaskCountMap(
            Long userId, String search) {
        HrmEmployeeDO employee = employeeService.getEmployeeByUserId(userId);
        if (employee == null) {
            return Collections.emptyMap();
        }
        return assessmentStageMapper.selectPortalTaskCountMap(employee.getId(), search);
    }

    @Override
    public HrmPerformanceAssessmentStageDO validateTaskStage(
            Long userId, Long assessmentId, Long stageId) {
        // 1. 校验员工绩效考核和阶段
        validatePerformanceAssessmentExists(assessmentId);
        HrmPerformanceAssessmentStageDO stage = assessmentStageMapper.selectById(stageId);
        if (stage == null || ObjUtil.notEqual(stage.getAssessmentId(), assessmentId)) {
            throw exception(PERFORMANCE_STAGE_ACTION_INVALID);
        }
        // 2. 校验当前账号绑定的员工是阶段处理人
        HrmEmployeeDO employee = employeeService.getEmployeeByUserId(userId);
        if (employee == null || ObjUtil.notEqual(stage.getHandlerEmployeeId(), employee.getId())) {
            throw exception(PERFORMANCE_STAGE_NO_PERMISSION);
        }
        return stage;
    }

    @Override
    public void notifyPendingStage(
            HrmPerformanceAssessmentDO assessment, HrmPerformanceAssessmentStageDO stage) {
        if (stage == null || ObjUtil.notEqual(stage.getStatus(),
                HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus())
                || stage.getHandlerEmployeeId() == null) {
            return;
        }
        // 1. 获得阶段处理员工和绩效计划
        HrmEmployeeDO handler = employeeService.getEmployee(stage.getHandlerEmployeeId());
        HrmPerformancePlanDO plan = planService.getPerformancePlan(assessment.getPlanId());
        if (handler == null || handler.getUserId() == null || plan == null) {
            return;
        }
        // 2. 发送绩效待办通知
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("planName", plan.getName());
        templateParams.put("stageName", stage.getName());
        templateParams.put("assessmentId", assessment.getId());
        // TODO DONE @芋艿：通知模板保留员工绩效入口，接收人进入页面后按待办页签处理。
        templateParams.put("route", "/hrm/portal/performance/assessment");
        notifyMessageSendApi.sendSingleMessageToAdmin(new NotifySendSingleToUserReqDTO()
                .setUserId(handler.getUserId())
                .setTemplateCode(MessageTemplateConstants.PERFORMANCE_TASK_PENDING)
                .setTemplateParams(templateParams));
    }

    @Override
    public void notifyProcessResult(HrmPerformanceAssessmentDO assessment,
                                    Collection<Long> employeeIds, String actionName, String result) {
        if (CollUtil.isEmpty(employeeIds)) {
            return;
        }
        // 1. 批量获得接收通知的员工和绩效计划
        Set<Long> validEmployeeIds = convertSet(employeeIds, employeeId -> employeeId,
                Objects::nonNull);
        if (CollUtil.isEmpty(validEmployeeIds)) {
            return;
        }
        List<HrmEmployeeDO> employees = employeeService.getEmployeeListByIds(validEmployeeIds);
        HrmPerformancePlanDO plan = planService.getPerformancePlan(assessment.getPlanId());
        if (CollUtil.isEmpty(employees) || plan == null) {
            return;
        }

        // 2. 发送绩效处理结果通知
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("planName", plan.getName());
        templateParams.put("actionName", actionName);
        templateParams.put("result", result);
        templateParams.put("assessmentId", assessment.getId());
        // TODO DONE @芋艿：处理结果与待办共用员工绩效入口，避免维护不同阶段的临时地址。
        templateParams.put("route", "/hrm/portal/performance/assessment");
        for (HrmEmployeeDO employee : employees) {
            if (employee.getUserId() == null) {
                continue;
            }
            notifyMessageSendApi.sendSingleMessageToAdmin(new NotifySendSingleToUserReqDTO()
                    .setUserId(employee.getUserId())
                    .setTemplateCode(MessageTemplateConstants.PERFORMANCE_PROCESS_RESULT)
                    .setTemplateParams(templateParams));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_PERFORMANCE_TYPE, subType = HRM_PERFORMANCE_CONFIRM_TARGET_SUB_TYPE,
            bizNo = "{{#reqVO.assessmentId}}", success = HRM_PERFORMANCE_CONFIRM_TARGET_SUCCESS)
    public void confirmTarget(Long userId, HrmPortalPerformanceConfirmReqVO reqVO) {
        // 1. 校验目标确认阶段
        HrmPerformanceAssessmentDO assessment = validatePerformanceAssessmentExists(reqVO.getAssessmentId());
        validatePlanRunning(assessment);
        validateStageType(assessment, HrmPerformanceStageTypeEnum.TARGET_CONFIRM.getType());
        HrmPerformanceAssessmentStageDO stage = validatePendingStage(
                assessment.getId(), HrmPerformanceStageTypeEnum.TARGET_CONFIRM.getType(), null);
        if (!isStageHandler(userId, stage)) {
            throw exception(PERFORMANCE_TARGET_CONFIRM_NO_PERMISSION);
        }
        validateConfirmValue(reqVO.getPass(), true);

        // 2. 更新目标确认结果
        LocalDateTime currentTime = LocalDateTime.now();
        boolean pass = Objects.equals(reqVO.getPass(), HrmPerformanceConfirmationResultEnum.PASS.getResult());
        // 2.1 完成目标确认阶段
        stage.setStatus(pass ? HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus()
                : HrmPerformanceAssessmentStageStatusEnum.REJECTED.getStatus())
                .setComment(reqVO.getComment()).setSubmitTime(currentTime);
        assessmentStageMapper.updateById(stage);
        // 2.2 根据确认结果切换员工绩效考核阶段
        assessment.setTargetConfirmationResult(reqVO.getPass())
                .setTargetConfirmationComment(reqVO.getComment()).setTargetConfirmationTime(currentTime);
        if (pass) {
            assessment.setStageType(HrmPerformanceStageTypeEnum.EXECUTING.getType());
        } else {
            List<HrmPerformanceAssessmentStageDO> fillStages =
                    assessmentStageMapper.selectListByAssessmentIdAndType(
                            assessment.getId(), HrmPerformanceStageTypeEnum.FILL_QUOTA.getType());
            HrmPerformanceAssessmentStageDO fillStage = CollUtil.getFirst(fillStages);
            if (fillStage == null) {
                throw exception(PERFORMANCE_STAGE_ACTION_INVALID);
            }
            assessmentStageMapper.updateToResetByAssessmentIdAndType(
                    assessment.getId(), HrmPerformanceStageTypeEnum.FILL_QUOTA.getType(),
                    HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus());
            fillStage.setStatus(HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus())
                    .setComment(null).setRejectReason(null).setSubmitTime(null).setDeadlineTime(null);
            assessment.setStageType(HrmPerformanceStageTypeEnum.FILL_QUOTA.getType())
                    .setStageSort(fillStage.getSort());
            notifyPendingStage(assessment, fillStage);
            notifyProcessResult(assessment, Collections.singleton(assessment.getEmployeeId()),
                    "目标确认", "已驳回，请重新填写指标");
        }
        // 2.3 保存员工绩效考核
        assessmentMapper.updateById(assessment);

        // 3. 刷新绩效计划的完成状态
        refreshPlanCompletionState(assessment.getPlanId());

        // 4. 追加绩效动作记录
        assessmentActionRecordService.createPerformanceAssessmentActionRecord(
                stage.getHandlerEmployeeId(), assessment.getId(), stage.getId(),
                pass ? HrmPerformanceAssessmentActionTypeEnum.CONFIRM_TARGET
                        : HrmPerformanceAssessmentActionTypeEnum.REJECT_TARGET,
                null, stage.getStatus(), buildActionComment(reqVO.getComment()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_PERFORMANCE_TYPE, subType = HRM_PERFORMANCE_CONFIRM_RESULT_SUB_TYPE,
            bizNo = "{{#reqVO.assessmentId}}", success = HRM_PERFORMANCE_CONFIRM_RESULT_SUCCESS)
    public void confirmResult(Long userId, HrmPortalPerformanceConfirmReqVO reqVO) {
        // 1. 校验结果确认阶段
        HrmPerformanceAssessmentDO assessment = validatePerformanceAssessmentExists(reqVO.getAssessmentId());
        validatePlanRunning(assessment);
        validateOwnAssessment(userId, assessment);
        validateStageType(assessment, HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType());
        validateConfirmValue(reqVO.getPass(), false);
        HrmPerformanceAssessmentStageDO stage = validatePendingStage(
                assessment.getId(), HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType(), null);

        // 2. 完成结果确认
        LocalDateTime currentTime = LocalDateTime.now();
        // 2.1 完成结果确认阶段
        stage.setStatus(HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus())
                .setComment(reqVO.getComment()).setSubmitTime(currentTime);
        assessmentStageMapper.updateById(stage);
        // 2.2 关闭不再执行的申诉确认阶段
        assessmentStageMapper.updateToProcessedByAssessmentIdAndTypeAndSortGreaterThan(
                assessment.getId(), HrmPerformanceStageTypeEnum.APPEAL_CONFIRM.getType(), stage.getSort(),
                currentTime);
        // 2.3 完成绩效结束阶段
        HrmPerformanceAssessmentStageDO endStage = completeEndStage(assessment, currentTime);
        // 2.4 保存员工绩效考核的结束状态
        assessment.setResultComment(reqVO.getComment()).setResultConfirmationTime(currentTime)
                .setProcessStatus(HrmPerformanceAssessmentProcessStatusEnum.FINISHED.getStatus())
                .setStageType(HrmPerformanceStageTypeEnum.END.getType()).setStageSort(endStage.getSort());
        assessmentMapper.updateById(assessment);

        // 3. 刷新绩效计划的完成状态
        refreshPlanCompletionState(assessment.getPlanId());

        // 4. 追加绩效动作记录
        assessmentActionRecordService.createPerformanceAssessmentActionRecord(
                stage.getHandlerEmployeeId(), assessment.getId(), stage.getId(),
                HrmPerformanceAssessmentActionTypeEnum.CONFIRM_RESULT,
                null, stage.getStatus(), buildActionComment(reqVO.getComment()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_PERFORMANCE_TYPE, subType = HRM_PERFORMANCE_APPEAL_SUB_TYPE,
            bizNo = "{{#reqVO.assessmentId}}", success = HRM_PERFORMANCE_APPEAL_SUCCESS)
    public HrmPortalPerformanceProcessRespVO submitAppeal(Long userId, HrmPortalPerformanceAppealReqVO reqVO) {
        // 1. 校验申诉条件
        HrmPerformanceAssessmentDO assessment = validatePerformanceAssessmentExists(reqVO.getAssessmentId());
        HrmPerformancePlanDO plan = validatePlanRunning(assessment);
        validateOwnAssessment(userId, assessment);
        validateStageType(assessment, HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType());
        if (Objects.equals(assessment.getAppealStatus(), HrmPerformanceAppealStatusEnum.PENDING.getStatus())) {
            throw exception(PERFORMANCE_PROCESS_RUNNING_CANNOT_MODIFY);
        }
        validateReviewStageIds(assessment.getId(), reqVO.getReviewStageIds());

        // 2. 启动申诉确认阶段
        // 2.1 标记结果确认阶段已申诉
        HrmPerformanceAssessmentStageDO resultConfirmStage = validatePendingStage(
                assessment.getId(), HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType(), null);
        resultConfirmStage.setStatus(HrmPerformanceAssessmentStageStatusEnum.APPEALED.getStatus());
        assessmentStageMapper.updateById(resultConfirmStage);
        // 2.2 激活首个申诉确认阶段
        LocalDateTime currentTime = LocalDateTime.now();
        HrmPerformanceAssessmentStageDO appealStage = activateStage(assessment,
                HrmPerformanceStageTypeEnum.APPEAL_CONFIRM.getType(),
                currentTime.plusDays(plan.getAppealTimeoutDays()));
        // 2.3 批量保存申诉涉及的评分阶段
        assessmentAppealRecordMapper.insertBatch(convertList(reqVO.getReviewStageIds(), reviewStageId ->
                new HrmPerformanceAssessmentAppealRecordDO().setAssessmentId(assessment.getId())
                        .setStageId(reviewStageId)
                        .setStatus(HrmPerformanceAppealRecordStatusEnum.NOT_PROCESSED.getStatus())));
        // 2.4 使用独立更新对象保存申诉信息
        HrmPerformanceAssessmentDO updateObj = new HrmPerformanceAssessmentDO().setId(assessment.getId())
                .setAppealReason(reqVO.getAppealReason()).setAppealFileUrls(reqVO.getAppealFileUrls())
                .setAppealSubmitTime(currentTime).setAppealStatus(HrmPerformanceAppealStatusEnum.PENDING.getStatus())
                .setProcessStatus(HrmPerformanceAssessmentProcessStatusEnum.PROCESSING.getStatus())
                .setStageType(HrmPerformanceStageTypeEnum.APPEAL_CONFIRM.getType())
                .setStageSort(appealStage.getSort());
        assessmentMapper.updateById(updateObj);
        assessmentMapper.updateAppealResultToClearById(assessment.getId());

        // 3. 刷新绩效计划的完成状态
        refreshPlanCompletionState(assessment.getPlanId());

        // 4. 追加绩效动作记录
        assessmentActionRecordService.createPerformanceAssessmentActionRecord(
                assessment.getEmployeeId(), assessment.getId(), resultConfirmStage.getId(),
                HrmPerformanceAssessmentActionTypeEnum.SUBMIT_APPEAL,
                reqVO.getAppealFileUrls(), resultConfirmStage.getStatus(), reqVO.getAppealReason());
        return new HrmPortalPerformanceProcessRespVO().setId(assessment.getId()).setNextStageId(appealStage.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HrmPortalPerformanceProcessRespVO startResultAudit(Long assessmentId) {
        // 1. 初始化结果审核阶段
        HrmPerformanceAssessmentDO assessment = validatePerformanceAssessmentExists(assessmentId);
        HrmPerformancePlanDO plan = validatePlanRunning(assessment);
        LocalDateTime currentTime = LocalDateTime.now();
        // 结果审核驳回重评时保留已通过的前置审核节点，只激活首个未处理节点。
        assessment.setStatus(HrmPerformancePlanStatusEnum.RUNNING.getStatus())
                .setProcessStatus(HrmPerformanceAssessmentProcessStatusEnum.PROCESSING.getStatus())
                .setStageType(HrmPerformanceStageTypeEnum.RESULT_AUDIT.getType())
                .setResultAuditTime(null).setResultAuditReason(null);

        // 2. 根据计划配置启动首个处理节点
        HrmPerformanceAssessmentStageDO stage;
        if (Boolean.TRUE.equals(plan.getResultAudit())) {
            stage = activateStage(assessment, HrmPerformanceStageTypeEnum.RESULT_AUDIT.getType(), null);
            assessment.setResultAuditStatus(HrmPerformanceResultAuditStatusEnum.PENDING.getStatus());
        } else {
            assessment.setResultAuditStatus(HrmPerformanceResultAuditStatusEnum.PASS.getStatus())
                    .setResultAuditTime(currentTime).setResultAuditReason("无需结果审核");
            stage = completeResultAudit(plan, assessment, currentTime);
        }
        assessmentMapper.updateById(assessment);

        // 3. 刷新绩效计划的完成状态
        refreshPlanCompletionState(assessment.getPlanId());

        // 4. 返回下一个待处理阶段
        return new HrmPortalPerformanceProcessRespVO().setId(assessmentId)
                .setNextStageId(stage == null ? null : stage.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_PERFORMANCE_TYPE, subType = HRM_PERFORMANCE_RESULT_AUDIT_SUB_TYPE,
            bizNo = "{{#reqVO.assessmentId}}", success = HRM_PERFORMANCE_RESULT_AUDIT_SUCCESS)
    public void handleResultAudit(Long userId, HrmPortalPerformanceHandleStageReqVO reqVO) {
        // 1. 校验结果审核阶段
        HrmPerformanceAssessmentDO assessment = validatePerformanceAssessmentExists(reqVO.getAssessmentId());
        HrmPerformancePlanDO plan = validatePlanRunning(assessment);
        validateStageType(assessment, HrmPerformanceStageTypeEnum.RESULT_AUDIT.getType());
        HrmPerformanceAssessmentStageDO stage = validatePendingStage(assessment.getId(),
                HrmPerformanceStageTypeEnum.RESULT_AUDIT.getType(), reqVO.getStageId());
        validateStageHandler(userId, stage);

        // 2. 处理结果审核
        handleResultAudit(userId, plan, assessment, stage, reqVO.getPass(), reqVO.getComment(),
                reqVO.getReviewStageIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_PERFORMANCE_TYPE, subType = HRM_PERFORMANCE_APPEAL_HANDLE_SUB_TYPE,
            bizNo = "{{#reqVO.assessmentId}}", success = HRM_PERFORMANCE_APPEAL_HANDLE_SUCCESS)
    public void handleAppeal(Long userId, HrmPortalPerformanceHandleStageReqVO reqVO) {
        // 1. 校验申诉处理阶段
        HrmPerformanceAssessmentDO assessment = validatePerformanceAssessmentExists(reqVO.getAssessmentId());
        HrmPerformancePlanDO plan = validatePlanRunning(assessment);
        validateStageType(assessment, HrmPerformanceStageTypeEnum.APPEAL_CONFIRM.getType());
        HrmPerformanceAssessmentStageDO stage = validatePendingStage(assessment.getId(),
                HrmPerformanceStageTypeEnum.APPEAL_CONFIRM.getType(), reqVO.getStageId());
        validateStageHandler(userId, stage);

        // 2. 处理绩效申诉
        handleAppeal(userId, plan, assessment, stage, reqVO.getPass(), reqVO.getComment());
    }

    @Override
    public List<HrmPerformanceAssessmentStageDO> getAppealTimeoutStageList(LocalDateTime deadlineTime) {
        return assessmentStageMapper.selectListByTypeAndStatusAndDeadlineTimeBeforeOrEqual(
                HrmPerformanceStageTypeEnum.APPEAL_CONFIRM.getType(),
                HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus(), deadlineTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_PERFORMANCE_TYPE, subType = HRM_PERFORMANCE_APPEAL_TIMEOUT_HANDLE_SUB_TYPE,
            bizNo = "{{#assessment.id}}", success = HRM_PERFORMANCE_APPEAL_TIMEOUT_HANDLE_SUCCESS,
            condition = "{{#_ret}}")
    public boolean processAppealTimeout(Long stageId) {
        // 1.1 校验申诉阶段仍处于待处理状态
        HrmPerformanceAssessmentStageDO stage = assessmentStageMapper.selectOneForUpdate(
                HrmPerformanceAssessmentStageDO::getId, stageId);
        if (stage == null
                || ObjUtil.notEqual(stage.getType(), HrmPerformanceStageTypeEnum.APPEAL_CONFIRM.getType())
                || ObjUtil.notEqual(stage.getStatus(),
                HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus())
                || stage.getDeadlineTime() == null || stage.getDeadlineTime().isAfter(LocalDateTime.now())) {
            return false;
        }
        // 1.2 校验员工绩效考核和绩效计划仍处于运行状态
        HrmPerformanceAssessmentDO assessment = assessmentMapper.selectById(stage.getAssessmentId());
        if (assessment == null || ObjUtil.notEqual(assessment.getStageType(),
                HrmPerformanceStageTypeEnum.APPEAL_CONFIRM.getType())) {
            return false;
        }
        HrmPerformancePlanDO plan = planService.getPerformancePlan(assessment.getPlanId());
        if (plan == null || ObjUtil.notEqual(plan.getStatus(), HrmPerformancePlanStatusEnum.RUNNING.getStatus())) {
            return false;
        }

        // 2. 根据计划的超时策略处理绩效申诉
        boolean appealPass = Objects.equals(plan.getAppealTimeoutAction(),
                HrmPerformanceAppealTimeoutActionEnum.APPROVE.getAction());
        Integer pass = appealPass
                ? HrmPerformanceConfirmationResultEnum.PASS.getResult()
                : HrmPerformanceConfirmationResultEnum.REJECT.getResult();
        String comment = appealPass ? "申诉确认超期，系统自动通过" : "申诉确认超期，系统自动驳回";
        handleAppeal(null, plan, assessment, stage, pass, comment);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("assessment", assessment);
        LogRecordContext.putVariable("appealPass", appealPass);
        return true;
    }

    /**
     * 处理绩效结果审核阶段
     *
     * @param userId 用户编号
     * @param plan 绩效计划
     * @param assessment 员工绩效考核
     * @param stage 当前审核阶段
     * @param pass 审核结果
     * @param comment 审核意见
     * @param reviewStageIds 需要重新评分的阶段编号列表
     */
    private void handleResultAudit(Long userId, HrmPerformancePlanDO plan,
                                   HrmPerformanceAssessmentDO assessment,
                                   HrmPerformanceAssessmentStageDO stage, Integer pass,
                                   String comment, List<Long> reviewStageIds) {
        // 1. 校验审核结果
        validateConfirmValue(pass, true);

        // 2. 根据审核结果推进流程或退回评分阶段
        LocalDateTime currentTime = LocalDateTime.now();
        if (Objects.equals(pass, HrmPerformanceConfirmationResultEnum.PASS.getResult())) {
            stage.setStatus(HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus())
                    .setComment(comment).setSubmitTime(currentTime);
            assessmentStageMapper.updateById(stage);
            HrmPerformanceAssessmentStageDO nextStage = activateNextStage(assessment, stage, null);
            if (nextStage == null) {
                assessment.setResultAuditStatus(HrmPerformanceResultAuditStatusEnum.PASS.getStatus())
                        .setResultAuditTime(currentTime).setResultAuditReason(comment);
                completeResultAudit(plan, assessment, currentTime);
            } else {
                assessment.setStageSort(nextStage.getSort());
            }
        } else {
            validateReviewStageIds(assessment.getId(), reviewStageIds);
            stage.setStatus(HrmPerformanceAssessmentStageStatusEnum.REJECTED.getStatus())
                    .setComment(comment).setRejectReason(comment).setSubmitTime(currentTime);
            assessmentStageMapper.updateById(stage);
            assessment.setResultAuditStatus(HrmPerformanceResultAuditStatusEnum.REJECT.getStatus())
                    .setResultAuditTime(currentTime).setResultAuditReason(comment);
            clearFinalResult(assessment);
            List<HrmPerformanceAssessmentStageDO> reopenStages =
                    reopenReviewStages(assessment, reviewStageIds, stage.getSort());
            notifyProcessResult(assessment, convertSet(reopenStages,
                            HrmPerformanceAssessmentStageDO::getHandlerEmployeeId,
                            reviewStage -> reviewStage.getHandlerEmployeeId() != null),
                    "结果审核", "已驳回，请重新评分");
        }

        // 3. 保存员工绩效考核并刷新计划状态
        assessmentMapper.updateById(assessment);
        refreshPlanCompletionState(assessment.getPlanId());

        // 4. 追加绩效动作记录
        assessmentActionRecordService.createPerformanceAssessmentActionRecord(
                stage.getHandlerEmployeeId(), assessment.getId(), stage.getId(),
                Objects.equals(pass, HrmPerformanceConfirmationResultEnum.PASS.getResult())
                        ? HrmPerformanceAssessmentActionTypeEnum.PASS_RESULT_AUDIT
                        : HrmPerformanceAssessmentActionTypeEnum.REJECT_RESULT_AUDIT,
                null, stage.getStatus(), buildActionComment(comment));
    }

    /**
     * 完成绩效结果审核，并进入结果确认或结束阶段
     *
     * @param plan 绩效计划
     * @param assessment 员工绩效考核
     * @param completeTime 完成时间
     * @return 已激活的结果确认阶段；无需立即激活时返回 {@code null}
     */
    private HrmPerformanceAssessmentStageDO completeResultAudit(
            HrmPerformancePlanDO plan, HrmPerformanceAssessmentDO assessment, LocalDateTime completeTime) {
        // 1. 配置结果确认时，切换到结果确认阶段
        if (Boolean.TRUE.equals(plan.getResultConfirmation())) {
            List<HrmPerformanceAssessmentStageDO> resultConfirmStages = assessmentStageMapper
                    .selectListByAssessmentIdAndType(assessment.getId(),
                            HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType());
            HrmPerformanceAssessmentStageDO resultConfirmStage = CollUtil.getFirst(resultConfirmStages);
            if (resultConfirmStage == null) {
                throw exception(PERFORMANCE_STAGE_ACTION_INVALID);
            }
            boolean interviewStarted = resultConfirmStages.stream().anyMatch(stage -> ObjUtil.notEqual(
                    stage.getStatus(), HrmPerformanceAssessmentStageStatusEnum.NOT_PROCESSED.getStatus()));
            resetProcessStages(assessment.getId(), HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType());
            assessment.setProcessStatus(HrmPerformanceAssessmentProcessStatusEnum.PROCESSING.getStatus())
                    .setStageType(HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType())
                    .setStageSort(resultConfirmStage.getSort());
            // 申诉重评已发起过面谈，审核完成后立即重新激活结果确认阶段。
            if (interviewStarted) {
                return activateStage(
                        assessment, HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType(), null);
            }
            // 首次审核完成时保持结果确认未处理，等待管理员发起绩效面谈。
            return null;
        }

        // 2. 未配置结果确认时，直接结束员工绩效考核
        HrmPerformanceAssessmentStageDO endStage = completeEndStage(assessment, completeTime);
        assessment.setProcessStatus(HrmPerformanceAssessmentProcessStatusEnum.FINISHED.getStatus())
                .setStageType(HrmPerformanceStageTypeEnum.END.getType()).setStageSort(endStage.getSort());
        return null;
    }

    /**
     * 处理绩效申诉确认阶段
     *
     * @param userId 用户编号；系统自动处理时为空
     * @param plan 绩效计划
     * @param assessment 员工绩效考核
     * @param stage 当前申诉确认阶段
     * @param pass 确认结果
     * @param comment 处理意见
     */
    private void handleAppeal(Long userId, HrmPerformancePlanDO plan,
                              HrmPerformanceAssessmentDO assessment,
                              HrmPerformanceAssessmentStageDO stage, Integer pass, String comment) {
        // 1. 校验申诉确认结果
        validateConfirmValue(pass, true);

        // 2. 根据确认结果继续下一级处理、重新评分或结束考核
        LocalDateTime currentTime = LocalDateTime.now();
        if (Objects.equals(pass, HrmPerformanceConfirmationResultEnum.PASS.getResult())) {
            stage.setStatus(HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus())
                    .setComment(comment).setSubmitTime(currentTime);
            assessmentStageMapper.updateById(stage);
            HrmPerformanceAssessmentStageDO nextStage = activateNextStage(assessment, stage,
                    currentTime.plusDays(plan.getAppealTimeoutDays()));
            if (nextStage != null) {
                assessment.setStageSort(nextStage.getSort());
            } else {
                assessment.setAppealStatus(HrmPerformanceAppealStatusEnum.PASS.getStatus())
                        .setAppealTime(currentTime).setAppealComment(comment);
                clearFinalResult(assessment);
                reopenReviewStages(assessment, completeAppealRecords(assessment.getId()), null);
                notifyProcessResult(assessment, Collections.singleton(assessment.getEmployeeId()),
                        "绩效申诉", "已通过，相关评分将重新处理");
            }
        } else {
            stage.setStatus(HrmPerformanceAssessmentStageStatusEnum.REJECTED.getStatus())
                    .setComment(comment).setRejectReason(comment).setSubmitTime(currentTime);
            assessmentStageMapper.updateById(stage);
            assessmentStageMapper.updateToProcessedByAssessmentIdAndTypeAndSortGreaterThan(
                    assessment.getId(), HrmPerformanceStageTypeEnum.APPEAL_CONFIRM.getType(), stage.getSort(),
                    currentTime);
            HrmPerformanceAssessmentStageDO endStage = completeEndStage(assessment, currentTime);
            assessment.setAppealStatus(HrmPerformanceAppealStatusEnum.REJECT.getStatus())
                    .setAppealTime(currentTime).setAppealComment(comment)
                    .setProcessStatus(HrmPerformanceAssessmentProcessStatusEnum.FINISHED.getStatus())
                    .setStageType(HrmPerformanceStageTypeEnum.END.getType()).setStageSort(endStage.getSort());
            completeAppealRecords(assessment.getId());
            notifyProcessResult(assessment, Collections.singleton(assessment.getEmployeeId()),
                    "绩效申诉", "已驳回，考核流程已结束");
        }

        // 3. 保存员工绩效考核并刷新计划状态
        assessmentMapper.updateById(assessment);
        refreshPlanCompletionState(assessment.getPlanId());

        // 4. 追加绩效动作记录
        boolean passed = Objects.equals(pass, HrmPerformanceConfirmationResultEnum.PASS.getResult());
        HrmPerformanceAssessmentActionTypeEnum actionType = userId == null
                ? (passed ? HrmPerformanceAssessmentActionTypeEnum.APPEAL_TIMEOUT_PASS
                : HrmPerformanceAssessmentActionTypeEnum.APPEAL_TIMEOUT_REJECT)
                : (passed ? HrmPerformanceAssessmentActionTypeEnum.PASS_APPEAL
                : HrmPerformanceAssessmentActionTypeEnum.REJECT_APPEAL);
        assessmentActionRecordService.createPerformanceAssessmentActionRecord(
                userId == null ? null : stage.getHandlerEmployeeId(), assessment.getId(), stage.getId(),
                actionType, null, stage.getStatus(), buildActionComment(comment));
    }

    /**
     * 构建绩效动作的处理意见内容
     *
     * @param comment 处理意见
     * @return 动作内容后缀
     */
    private String buildActionComment(String comment) {
        return StringUtils.hasText(comment) ? "，意见：" + comment : "";
    }

    /**
     * 重新打开指定评分阶段
     *
     * @param assessment 员工绩效考核
     * @param selectedStageIds 指定评分阶段编号列表
     * @param resultAuditResumeSort 结果审核恢复层级；为空时重新开始结果审核
     * @return 重新打开的评分阶段列表
     */
    private List<HrmPerformanceAssessmentStageDO> reopenReviewStages(
            HrmPerformanceAssessmentDO assessment, List<Long> selectedStageIds,
            Integer resultAuditResumeSort) {
        // 1. 获得明确选择的已完成评分阶段
        List<HrmPerformanceAssessmentStageDO> reviewStages = assessmentStageMapper.selectListByAssessmentIdAndTypes(
                assessment.getId(), HrmPerformanceStageTypeEnum.REVIEW_TYPES);
        List<HrmPerformanceAssessmentStageDO> completedStages = filterList(reviewStages,
                stage -> Objects.equals(stage.getStatus(),
                        HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus()));
        Set<Long> selectedIdSet = selectedStageIds == null ? Collections.emptySet() : new HashSet<>(selectedStageIds);
        List<HrmPerformanceAssessmentStageDO> reopenStages = filterList(completedStages,
                stage -> selectedIdSet.contains(stage.getId()));
        HrmPerformanceAssessmentStageDO firstStage = CollUtil.getFirst(reopenStages);
        if (firstStage == null) {
            return Collections.emptyList();
        }

        // 2. 仅清理并重新打开选中阶段的评分结果
        // 2.1 删除重新评分阶段的指标评分
        List<Long> reopenStageIds = convertList(reopenStages, HrmPerformanceAssessmentStageDO::getId);
        assessmentQuotaScoreMapper.deleteByAssessmentStageIds(reopenStageIds);
        // 2.2 将所有选中阶段设为待处理
        assessmentStageMapper.updateToResetByIds(reopenStageIds,
                HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus());
        for (HrmPerformanceAssessmentStageDO reopenStage : reopenStages) {
            reopenStage.setStatus(HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus());
            notifyPendingStage(assessment, reopenStage);
        }

        // 3. 重置后续流程阶段，并以首个重新评分阶段作为当前阶段
        if (resultAuditResumeSort == null) {
            resetProcessStages(assessment.getId(), HrmPerformanceStageTypeEnum.RESULT_AUDIT.getType());
        } else {
            assessmentStageMapper.updateToResetByAssessmentIdAndTypeAndSortGreaterThanOrEqual(
                    assessment.getId(), HrmPerformanceStageTypeEnum.RESULT_AUDIT.getType(),
                    resultAuditResumeSort, HrmPerformanceAssessmentStageStatusEnum.NOT_PROCESSED.getStatus());
        }
        resetProcessStages(assessment.getId(), HrmPerformanceStageTypeEnum.APPEAL_CONFIRM.getType());
        assessment.setProcessStatus(HrmPerformanceAssessmentProcessStatusEnum.PROCESSING.getStatus())
                .setStageSort(firstStage.getSort()).setStageType(firstStage.getType());
        return reopenStages;
    }

    /**
     * 完成员工绩效考核的待处理申诉记录
     *
     * @param assessmentId 员工绩效考核编号
     * @return 申诉涉及的评分阶段编号列表
     */
    private List<Long> completeAppealRecords(Long assessmentId) {
        // 1. 查询待处理的申诉记录
        List<HrmPerformanceAssessmentAppealRecordDO> records = assessmentAppealRecordMapper
                .selectListByAssessmentIdAndStatus(assessmentId,
                        HrmPerformanceAppealRecordStatusEnum.NOT_PROCESSED.getStatus());
        if (CollUtil.isEmpty(records)) {
            return Collections.emptyList();
        }

        // 2. 批量完成申诉记录
        records.forEach(record -> record.setStatus(HrmPerformanceAppealRecordStatusEnum.PROCESSED.getStatus()));
        assessmentAppealRecordMapper.updateBatch(records);
        return convertList(records, HrmPerformanceAssessmentAppealRecordDO::getStageId);
    }

    /**
     * 重置员工绩效考核的指定流程阶段
     *
     * @param assessmentId 员工绩效考核编号
     * @param type 阶段类型
     */
    private void resetProcessStages(Long assessmentId, Integer type) {
        assessmentStageMapper.updateToResetByAssessmentIdAndType(assessmentId, type,
                HrmPerformanceAssessmentStageStatusEnum.NOT_PROCESSED.getStatus());
    }

    /**
     * 清空员工绩效考核及指标的最终结果
     *
     * @param assessment 员工绩效考核
     */
    private void clearFinalResult(HrmPerformanceAssessmentDO assessment) {
        // 1. 同步清空内存中的最终结果
        assessment.setScore(null).setResultLevel(null).setCoefficient(BigDecimal.ONE)
                .setSelfComment(null).setReviewerComment(null);

        // 2. 清空员工绩效考核和指标结果
        assessmentMapper.updateResultToClearById(assessment.getId());
        assessmentQuotaMapper.updateReviewScoreToNullByAssessmentId(assessment.getId());
    }

    /**
     * 完成员工绩效考核的结束阶段
     *
     * @param assessment 员工绩效考核
     * @param submitTime 完成时间
     * @return 结束阶段
     */
    private HrmPerformanceAssessmentStageDO completeEndStage(
            HrmPerformanceAssessmentDO assessment, LocalDateTime submitTime) {
        // 1. 查询结束阶段
        List<HrmPerformanceAssessmentStageDO> stages =
                assessmentStageMapper.selectListByAssessmentIdAndType(
                        assessment.getId(), HrmPerformanceStageTypeEnum.END.getType());
        HrmPerformanceAssessmentStageDO stage = CollUtil.getFirst(stages);
        if (stage == null) {
            throw exception(PERFORMANCE_STAGE_ACTION_INVALID);
        }

        // 2. 完成结束阶段
        stage.setStatus(HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus())
                .setSubmitTime(submitTime);
        assessmentStageMapper.updateById(stage);
        return stage;
    }

    /**
     * 添加多级处理人阶段
     *
     * @param stages 员工绩效考核阶段列表
     * @param handlerStages 处理人配置列表
     * @param assessment 员工绩效考核
     * @param employee 被考核员工
     * @param stageType 阶段类型
     */
    private void addHandlerStages(
            List<HrmPerformanceAssessmentStageDO> stages,
            List<HandlerStage> handlerStages, HrmPerformanceAssessmentDO assessment,
            HrmEmployeeDO employee, HrmPerformanceStageTypeEnum stageType) {
        // 1. 校验处理人阶段配置
        if (CollUtil.isEmpty(handlerStages)) {
            throw exception(PERFORMANCE_PROCESS_CONFIG_INVALID);
        }

        // 2. 解析并添加处理人阶段
        for (int i = 0; i < handlerStages.size(); i++) {
            HrmEmployeeDO handler = resolveReviewer(employee, handlerStages.get(i));
            if (handler == null) {
                throw exception(PERFORMANCE_PROCESS_CONFIG_INVALID);
            }
            HrmPerformanceAssessmentStageDO existingStage = CollUtil.findOne(stages,
                    stage -> Objects.equals(stage.getType(), stageType.getType())
                            && Objects.equals(stage.getHandlerEmployeeId(), handler.getId()));
            if (existingStage != null) {
                continue;
            }
            String name = handlerStages.size() == 1 ? stageType.getName()
                    : stageType.getName() + "（第" + (i + 1) + "级）";
            stages.add(buildAssessmentStage(assessment.getId(), stageType, name,
                    handler.getId()));
        }
    }

    private HrmPerformanceAssessmentStageDO buildAssessmentStage(
            Long assessmentId, HrmPerformanceStageTypeEnum type,
            Long handlerEmployeeId) {
        return buildAssessmentStage(assessmentId, type, type.getName(), handlerEmployeeId);
    }

    private HrmPerformanceAssessmentStageDO buildAssessmentStage(
            Long assessmentId, HrmPerformanceStageTypeEnum type, String name,
            Long handlerEmployeeId) {
        return new HrmPerformanceAssessmentStageDO().setAssessmentId(assessmentId)
                .setType(type.getType()).setName(name)
                .setHandlerEmployeeId(handlerEmployeeId)
                .setStatus(HrmPerformanceAssessmentStageStatusEnum.NOT_PROCESSED.getStatus());
    }

    private String buildReviewName(ReviewStage stage) {
        HandlerStage rater = stage.getRater();
        if (Objects.equals(rater.getType(), HrmPerformanceRaterTypeEnum.SELF.getType())) {
            return "员工自评";
        }
        if (Objects.equals(rater.getType(), HrmPerformanceRaterTypeEnum.SUPERIOR.getType())) {
            return Objects.equals(rater.getLevel(), 1)
                    ? "直属上级评分" : "第" + rater.getLevel() + "级上级评分";
        }
        if (Objects.equals(rater.getType(), HrmPerformanceRaterTypeEnum.DEPT_LEADER.getType())) {
            return Objects.equals(rater.getLevel(), 1)
                    ? "直属部门负责人评分" : "第" + rater.getLevel() + "级部门负责人评分";
        }
        HrmEmployeeDO employee = employeeService.getEmployee(rater.getEmployeeId());
        return employee == null ? "指定员工评分" : employee.getName() + "评分";
    }

    private HrmEmployeeDO resolveReviewer(
            HrmEmployeeDO employee, HandlerStage handlerStage) {
        return resolveReviewer(employee, handlerStage.getType(), handlerStage.getLevel(), handlerStage.getEmployeeId());
    }

    /**
     * 根据评分人配置解析实际处理员工
     *
     * @param employee 被考核员工
     * @param raterType 评分人类型
     * @param raterLevel 评分人层级
     * @param raterEmployeeId 指定评分员工编号
     * @return 实际处理人；无法解析时返回 {@code null}
     */
    @SuppressWarnings("ReassignedVariable")
    private HrmEmployeeDO resolveReviewer(
            HrmEmployeeDO employee, Integer raterType, Integer raterLevel,
            Long raterEmployeeId) {
        // 1. 解析员工本人或指定员工
        if (Objects.equals(raterType, HrmPerformanceRaterTypeEnum.SELF.getType())) {
            return employee;
        }
        if (Objects.equals(raterType, HrmPerformanceRaterTypeEnum.SPECIFIED.getType())) {
            return employeeService.getEmployee(raterEmployeeId);
        }

        // 2. 按层级解析员工直属上级
        if (Objects.equals(raterType, HrmPerformanceRaterTypeEnum.SUPERIOR.getType())) {
            HrmEmployeeDO reviewer = employee;
            for (int level = 0; level < ObjUtil.defaultIfNull(raterLevel, 1); level++) {
                reviewer = reviewer.getLeaderEmployeeId() == null
                        ? null : employeeService.getEmployee(reviewer.getLeaderEmployeeId());
                if (reviewer == null) {
                    return null;
                }
            }
            return reviewer;
        }

        // 3. 按层级解析部门负责人
        if (Objects.equals(raterType, HrmPerformanceRaterTypeEnum.DEPT_LEADER.getType())) {
            Long deptId = employee.getDeptId();
            DeptRespDTO dept = null;
            for (int level = 0; level < ObjUtil.defaultIfNull(raterLevel, 1); level++) {
                dept = deptId == null ? null : deptApi.getDept(deptId);
                if (dept == null) {
                    return null;
                }
                deptId = dept.getParentId();
            }
            if (dept.getLeaderUserId() == null) {
                return null;
            }
            return employeeService.getEmployeeByUserId(dept.getLeaderUserId());
        }
        return null;
    }

    /**
     * 获得当前用户指定类型的绩效任务分页
     *
     * @param userId 用户编号
     * @param reqVO 分页查询
     * @param types 阶段类型集合
     * @return 员工绩效考核阶段分页
     */
    private PageResult<HrmPerformanceAssessmentStageDO> getMyTaskPage(
            Long userId, HrmPortalPerformanceTaskPageReqVO reqVO,
            Collection<Integer> types) {
        // 1. 根据当前登录账号查询员工
        HrmEmployeeDO employee = employeeService.getEmployeeByUserId(userId);
        if (employee == null) {
            return PageResult.empty();
        }
        // 2. 分页查询当前员工的阶段任务
        return assessmentStageMapper.selectPortalTaskPage(
                reqVO, employee.getId(), types);
    }

    /**
     * 激活员工绩效考核的指定类型阶段
     *
     * @param assessment 员工绩效考核
     * @param type 阶段类型
     * @param deadlineTime 截止时间
     * @return 已激活的阶段
     */
    private HrmPerformanceAssessmentStageDO activateStage(
            HrmPerformanceAssessmentDO assessment, Integer type, LocalDateTime deadlineTime) {
        // 1. 查询首个可激活阶段
        List<HrmPerformanceAssessmentStageDO> stages =
                assessmentStageMapper.selectListByAssessmentIdAndType(assessment.getId(), type);
        HrmPerformanceAssessmentStageDO stage = CollUtil.findOne(stages,
                item -> ObjectUtils.equalsAny(item.getStatus(),
                        HrmPerformanceAssessmentStageStatusEnum.NOT_PROCESSED.getStatus(),
                        HrmPerformanceAssessmentStageStatusEnum.REJECTED.getStatus()));
        if (stage == null) {
            throw exception(PERFORMANCE_STAGE_ACTION_INVALID);
        }

        // 2. 激活阶段并同步员工绩效考核的当前阶段
        stage.setStatus(HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus())
                .setDeadlineTime(deadlineTime).setComment(null).setRejectReason(null).setSubmitTime(null);
        assessmentStageMapper.updateById(stage);
        assessment.setStageType(type).setStageSort(stage.getSort());
        notifyPendingStage(assessment, stage);
        return stage;
    }

    /**
     * 激活同类型的下一个员工绩效考核阶段
     *
     * @param assessment 员工绩效考核
     * @param currentStage 当前阶段
     * @param deadlineTime 截止时间
     * @return 下一个阶段；不存在时返回 {@code null}
     */
    private HrmPerformanceAssessmentStageDO activateNextStage(
            HrmPerformanceAssessmentDO assessment, HrmPerformanceAssessmentStageDO currentStage,
            LocalDateTime deadlineTime) {
        // 1. 查询当前阶段之后的首个未处理阶段
        List<HrmPerformanceAssessmentStageDO> stages = assessmentStageMapper
                .selectListByAssessmentIdAndType(assessment.getId(), currentStage.getType());
        HrmPerformanceAssessmentStageDO nextStage = CollUtil.findOne(stages,
                stage -> Objects.equals(stage.getStatus(),
                        HrmPerformanceAssessmentStageStatusEnum.NOT_PROCESSED.getStatus())
                        && ObjUtil.defaultIfNull(stage.getSort(), 0)
                        > ObjUtil.defaultIfNull(currentStage.getSort(), 0));
        if (nextStage == null) {
            return null;
        }

        // 2. 激活下一个阶段
        nextStage.setStatus(HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus())
                .setDeadlineTime(deadlineTime);
        assessmentStageMapper.updateById(nextStage);
        notifyPendingStage(assessment, nextStage);
        return nextStage;
    }

    /**
     * 校验并获得待处理的员工绩效考核阶段
     *
     * @param assessmentId 员工绩效考核编号
     * @param type 阶段类型
     * @param stageId 阶段编号
     * @return 待处理阶段
     */
    private HrmPerformanceAssessmentStageDO validatePendingStage(
            Long assessmentId, Integer type, Long stageId) {
        // 1. 查询指定类型的员工绩效考核阶段
        List<HrmPerformanceAssessmentStageDO> stages =
                assessmentStageMapper.selectListByAssessmentIdAndType(assessmentId, type);
        HrmPerformanceAssessmentStageDO stage = CollUtil.findOne(stages,
                item -> (stageId == null || Objects.equals(item.getId(), stageId))
                        && Objects.equals(item.getStatus(),
                        HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus()));

        // 2. 校验待处理阶段存在
        if (stage == null) {
            throw exception(PERFORMANCE_STAGE_ACTION_INVALID);
        }
        HrmPerformanceAssessmentStageDO lockedStage = assessmentStageMapper.selectOneForUpdate(
                HrmPerformanceAssessmentStageDO::getId, stage.getId());
        if (lockedStage == null || ObjUtil.notEqual(lockedStage.getAssessmentId(), assessmentId)
                || ObjUtil.notEqual(lockedStage.getType(), type)
                || ObjUtil.notEqual(lockedStage.getStatus(),
                HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus())) {
            throw exception(PERFORMANCE_STAGE_ACTION_INVALID);
        }
        return lockedStage;
    }

    private void validateStageHandler(Long userId, HrmPerformanceAssessmentStageDO stage) {
        if (!isStageHandler(userId, stage)) {
            throw exception(PERFORMANCE_STAGE_NO_PERMISSION);
        }
    }

    private boolean isStageHandler(Long userId, HrmPerformanceAssessmentStageDO stage) {
        HrmEmployeeDO employee = employeeService.getEmployeeByUserId(userId);
        return employee != null
                && Objects.equals(stage.getHandlerEmployeeId(), employee.getId());
    }

    /**
     * 校验重新评分的阶段编号列表
     *
     * @param assessmentId 员工绩效考核编号
     * @param reviewStageIds 评分阶段编号列表
     */
    private void validateReviewStageIds(Long assessmentId, List<Long> reviewStageIds) {
        // 1. 校验评分阶段编号列表非空
        if (CollUtil.isEmpty(reviewStageIds)) {
            throw exception(PERFORMANCE_DATA_ILLEGAL);
        }

        // 2. 校验所有评分阶段均属于当前考核且已经完成
        List<HrmPerformanceAssessmentStageDO> stages =
                assessmentStageMapper.selectListByAssessmentIdAndTypes(assessmentId, HrmPerformanceStageTypeEnum.REVIEW_TYPES);
        Set<Long> completedStageIds = convertSet(stages, HrmPerformanceAssessmentStageDO::getId,
                stage -> Objects.equals(stage.getStatus(),
                        HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus()));
        if (!completedStageIds.containsAll(reviewStageIds)) {
            throw exception(PERFORMANCE_DATA_ILLEGAL);
        }
    }

    /**
     * 根据全部员工绩效考核进度刷新绩效计划阶段和操作类型
     *
     * @param planId 绩效计划编号
     */
    @Override
    public void refreshPlanCompletionState(Long planId) {
        // 1. 校验绩效计划仍在运行
        HrmPerformancePlanDO plan = validatePerformancePlanExists(planId);
        if (ObjUtil.notEqual(plan.getStatus(), HrmPerformancePlanStatusEnum.RUNNING.getStatus())) {
            return;
        }

        // 2. 查询计划下的员工绩效考核
        List<HrmPerformanceAssessmentDO> assessments = assessmentMapper.selectListByPlanId(planId);
        if (CollUtil.isEmpty(assessments)) {
            return;
        }

        // 3. 根据全部考核进度计算计划阶段和下一操作
        Integer stageType = assessments.stream().map(HrmPerformanceAssessmentDO::getStageType)
                .filter(Objects::nonNull).min(Integer::compareTo).orElse(plan.getStageType());
        Integer operationType = null;
        if (assessments.stream().allMatch(assessment -> Objects.equals(
                assessment.getStageType(), HrmPerformanceStageTypeEnum.END.getType()))) {
            stageType = HrmPerformanceStageTypeEnum.END.getType();
            operationType = Boolean.TRUE.equals(plan.getResultConfirmation())
                    || Objects.equals(plan.getOperationType(), HrmPerformancePlanOperationTypeEnum.ARCHIVE.getType())
                    ? HrmPerformancePlanOperationTypeEnum.ARCHIVE.getType()
                    : HrmPerformancePlanOperationTypeEnum.START_INTERVIEW.getType();
        } else if (isInterviewReady(plan, assessments)) {
            stageType = HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType();
            operationType = HrmPerformancePlanOperationTypeEnum.START_INTERVIEW.getType();
        } else if (assessments.stream().allMatch(assessment -> Objects.equals(
                assessment.getStageType(), HrmPerformanceStageTypeEnum.EXECUTING.getType()))) {
            stageType = HrmPerformanceStageTypeEnum.EXECUTING.getType();
            operationType = HrmPerformancePlanOperationTypeEnum.START_SCORING.getType();
        }

        // 4. 更新绩效计划阶段和操作类型
        planService.updatePerformancePlanStageTypeAndOperationType(planId, stageType, operationType);
    }

    /**
     * 判断绩效计划是否已经可以发起结果面谈
     *
     * @param plan 绩效计划
     * @param assessments 员工绩效考核列表
     * @return 是否可以发起结果面谈
     */
    private boolean isInterviewReady(HrmPerformancePlanDO plan,
                                     List<HrmPerformanceAssessmentDO> assessments) {
        // 1. 校验计划启用结果确认
        if (ObjUtil.notEqual(Boolean.TRUE, plan.getResultConfirmation())) {
            return false;
        }

        // 2. 校验全部考核已完成结果审核并等待结果确认
        boolean assessmentReady = assessments.stream().allMatch(assessment ->
                Objects.equals(assessment.getStageType(), HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType())
                        && (ObjUtil.notEqual(Boolean.TRUE, plan.getResultAudit()) || Objects.equals(
                        assessment.getResultAuditStatus(), HrmPerformanceResultAuditStatusEnum.PASS.getStatus())));
        if (!assessmentReady) {
            return false;
        }

        // 3. 校验全部结果确认阶段尚未开始
        Set<Long> assessmentIds = convertSet(assessments, HrmPerformanceAssessmentDO::getId);
        List<HrmPerformanceAssessmentStageDO> stages =
                assessmentStageMapper.selectListByAssessmentIds(assessmentIds);
        Set<Long> waitingAssessmentIds = convertSet(stages,
                HrmPerformanceAssessmentStageDO::getAssessmentId,
                stage -> Objects.equals(stage.getType(), HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType())
                        && Objects.equals(stage.getStatus(),
                        HrmPerformanceAssessmentStageStatusEnum.NOT_PROCESSED.getStatus()));
        return waitingAssessmentIds.containsAll(assessmentIds);
    }

    private void validateConfirmValue(Integer pass, boolean allowReject) {
        if (Objects.equals(pass, HrmPerformanceConfirmationResultEnum.PASS.getResult())) {
            return;
        }
        if (allowReject && Objects.equals(pass, HrmPerformanceConfirmationResultEnum.REJECT.getResult())) {
            return;
        }
        throw exception(PERFORMANCE_DATA_ILLEGAL);
    }

    private void validateStageType(HrmPerformanceAssessmentDO assessment, Integer expectedType) {
        if (ObjUtil.notEqual(assessment.getStageType(), expectedType)) {
            throw exception(PERFORMANCE_STAGE_ACTION_INVALID);
        }
    }

    private HrmPerformancePlanDO validatePlanRunning(HrmPerformanceAssessmentDO assessment) {
        HrmPerformancePlanDO plan = validatePerformancePlanExists(assessment.getPlanId());
        if (ObjUtil.notEqual(plan.getStatus(), HrmPerformancePlanStatusEnum.RUNNING.getStatus())) {
            throw exception(PERFORMANCE_STAGE_ACTION_INVALID);
        }
        return plan;
    }

    private HrmPerformancePlanDO validatePerformancePlanExists(Long id) {
        return planService.validatePerformancePlanExists(id);
    }

    private HrmPerformanceAssessmentDO validatePerformanceAssessmentExists(Long id) {
        HrmPerformanceAssessmentDO assessment = assessmentMapper.selectById(id);
        if (assessment == null) {
            throw exception(PERFORMANCE_ASSESSMENT_NOT_EXISTS);
        }
        return assessment;
    }

    @SuppressWarnings("UnusedReturnValue")
    private HrmEmployeeDO validateOwnAssessment(Long userId, HrmPerformanceAssessmentDO assessment) {
        HrmEmployeeDO employee = employeeService.getEmployee(assessment.getEmployeeId());
        if (employee == null || ObjUtil.notEqual(employee.getUserId(), userId)) {
            throw exception(PERFORMANCE_ASSESSMENT_NOT_EXISTS);
        }
        return employee;
    }

}
