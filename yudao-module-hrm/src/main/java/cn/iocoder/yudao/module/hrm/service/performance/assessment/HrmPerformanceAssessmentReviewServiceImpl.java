package cn.iocoder.yudao.module.hrm.service.performance.assessment;

import cn.iocoder.yudao.module.hrm.service.performance.plan.HrmPerformancePlanService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.review.HrmPortalPerformanceFillQuotaReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.process.HrmPortalPerformanceProcessRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.review.HrmPortalPerformanceQuotaSaveReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.review.HrmPortalPerformanceReviewRejectReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.review.HrmPortalPerformanceScorePreviewRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.review.HrmPortalPerformanceScoreReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.task.HrmPortalPerformanceTaskPageReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentDimensionDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentQuotaDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentQuotaScoreDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentStageDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.config.HrmPerformanceAssessmentTemplateDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.plan.HrmPerformancePlanDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.plan.HrmPerformancePlanDO.ResultConfig;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.config.HrmPerformanceResultTemplateDO.Level;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.assessment.HrmPerformanceAssessmentDimensionMapper;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.assessment.HrmPerformanceAssessmentMapper;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.assessment.HrmPerformanceAssessmentQuotaMapper;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.assessment.HrmPerformanceAssessmentQuotaScoreMapper;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.assessment.HrmPerformanceAssessmentStageMapper;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAppealStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAssessmentActionTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAssessmentProcessStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAssessmentStageStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformancePlanStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceQuotaSettingTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceRaterTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceStageTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.config.HrmPerformanceUpperLimitTypeEnum;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import com.mzt.logapi.starter.annotation.LogRecord;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.getSumValue;
import static cn.iocoder.yudao.framework.common.util.number.MoneyUtils.PERCENT_100;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_ASSESSMENT_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_DATA_ILLEGAL;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_QUOTA_SETTING_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_REVIEW_STAGE_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_REVIEW_STAGE_NO_PERMISSION;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_REVIEW_STAGE_REJECT_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_RESULT_LEVEL_NOT_MATCH;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_STAGE_ACTION_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_TARGET_CONFIRM_CONFIG_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_FILL_QUOTA_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_FILL_QUOTA_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_REJECT_SCORE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_REJECT_SCORE_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_SCORE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_SCORE_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_TYPE;

/**
 * HRM 绩效评分 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmPerformanceAssessmentReviewServiceImpl
        implements HrmPerformanceAssessmentReviewService {

    @Resource
    private HrmPerformanceAssessmentMapper assessmentMapper;
    @Resource
    private HrmPerformanceAssessmentQuotaMapper assessmentQuotaMapper;
    @Resource
    private HrmPerformanceAssessmentDimensionMapper assessmentDimensionMapper;
    @Resource
    private HrmPerformanceAssessmentStageMapper assessmentStageMapper;
    @Resource
    private HrmPerformanceAssessmentQuotaScoreMapper assessmentQuotaScoreMapper;

    @Resource
    private HrmEmployeeService employeeService;
    @Resource
    private HrmPerformanceAssessmentProcessService assessmentProcessService;
    @Resource
    private HrmPerformanceAssessmentActionRecordService assessmentActionRecordService;
    @Resource
    private HrmPerformancePlanService planService;

    @Override
    public PageResult<HrmPerformanceAssessmentStageDO> getMyFillQuotaTaskPage(
            Long userId, HrmPortalPerformanceTaskPageReqVO reqVO) {
        return getMyTaskPage(userId, reqVO,
                Collections.singleton(HrmPerformanceStageTypeEnum.FILL_QUOTA.getType()));
    }

    @Override
    public PageResult<HrmPerformanceAssessmentStageDO> getMyReviewTaskPage(
            Long userId, HrmPortalPerformanceTaskPageReqVO reqVO) {
        return getMyTaskPage(userId, reqVO, HrmPerformanceStageTypeEnum.REVIEW_TYPES);
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
        // 2. 分页查询当前员工的指定阶段
        return assessmentStageMapper.selectPortalTaskPage(
                reqVO, employee.getId(), types);
    }

    // ==================== 指标管理 ====================

    @Override
    public void ensureAssessmentQuotaList(
            HrmPerformancePlanDO plan, HrmPerformanceAssessmentDO assessment) {
        // 1.1 已初始化指标时，无需重复处理
        List<HrmPerformanceAssessmentQuotaDO> existingQuotas =
                assessmentQuotaMapper.selectListByAssessmentId(assessment.getId());
        if (CollUtil.isNotEmpty(existingQuotas)) {
            return;
        }
        // 1.2 校验计划的考核配置
        if (plan.getAssessmentConfig() == null
                || CollUtil.isEmpty(plan.getAssessmentConfig().getDimensions())) {
            throw exception(PERFORMANCE_DATA_ILLEGAL);
        }

        // 2.1 构造并批量插入考核维度
        List<HrmPerformanceAssessmentTemplateDO.Dimension> dimensions = plan.getAssessmentConfig().getDimensions();
        List<HrmPerformanceAssessmentDimensionDO> assessmentDimensions =
                new ArrayList<>(dimensions.size());
        for (int i = 0; i < dimensions.size(); i++) {
            HrmPerformanceAssessmentTemplateDO.Dimension dimension = dimensions.get(i);
            assessmentDimensions.add(new HrmPerformanceAssessmentDimensionDO()
                    .setAssessmentId(assessment.getId()).setName(dimension.getName())
                    .setQuotaType(dimension.getQuotaType()).setWeight(dimension.getWeight())
                    .setRemark(dimension.getRemark())
                    .setAllowEdit(Boolean.TRUE.equals(dimension.getAllowEdit())).setSort(i + 1));
        }
        assessmentDimensionMapper.insertBatch(assessmentDimensions);

        // 2.2 构造并批量插入考核指标
        int quotaSort = 1;
        List<HrmPerformanceAssessmentQuotaDO> assessmentQuotas = new ArrayList<>();
        for (int i = 0; i < dimensions.size(); i++) {
            HrmPerformanceAssessmentTemplateDO.Dimension dimension = dimensions.get(i);
            HrmPerformanceAssessmentDimensionDO assessmentDimension =
                    assessmentDimensions.get(i);
            for (HrmPerformanceAssessmentTemplateDO.Quota quota : dimension.getQuotas()) {
                assessmentQuotas.add(new HrmPerformanceAssessmentQuotaDO()
                        .setAssessmentId(assessment.getId())
                        .setDimensionId(assessmentDimension.getId()).setPreset(true)
                        .setName(quota.getName()).setDescription(quota.getIllustrate())
                        .setStandard(quota.getStandard())
                        .setWeight(ObjUtil.defaultIfNull(quota.getWeight(), PERCENT_100))
                        .setScoreType(quota.getScoreType())
                        .setSelfScore(BigDecimal.ZERO).setReviewerScore(BigDecimal.ZERO)
                        .setFinalScore(BigDecimal.ZERO).setSort(quotaSort++));
            }
        }
        assessmentQuotaMapper.insertBatch(assessmentQuotas);
    }

    /**
     * 使用员工提交的指标替换当前自定义指标
     *
     * @param plan 绩效计划
     * @param assessment 员工绩效考核
     * @param quotaList 指标列表
     */
    void replaceAssessmentQuotaList(
            HrmPerformancePlanDO plan, HrmPerformanceAssessmentDO assessment,
            List<HrmPortalPerformanceQuotaSaveReqVO> quotaList) {
        // 1. 校验已有指标和请求指标
        if (quotaList == null) {
            throw exception(PERFORMANCE_DATA_ILLEGAL);
        }
        ensureAssessmentQuotaList(plan, assessment);
        List<HrmPerformanceAssessmentDimensionDO> dimensions =
                assessmentDimensionMapper.selectListByAssessmentId(assessment.getId());
        Map<Long, HrmPerformanceAssessmentDimensionDO> dimensionMap =
                convertMap(dimensions, HrmPerformanceAssessmentDimensionDO::getId);
        List<HrmPerformanceAssessmentQuotaDO> existingQuotas =
                assessmentQuotaMapper.selectListByAssessmentId(assessment.getId());
        Map<Long, HrmPerformanceAssessmentQuotaDO> existingQuotaMap =
                convertMap(existingQuotas, HrmPerformanceAssessmentQuotaDO::getId);
        Set<Long> presetQuotaIds = convertSet(existingQuotas, HrmPerformanceAssessmentQuotaDO::getId,
                quota -> Boolean.TRUE.equals(quota.getPreset()));
        // 拆分请求中的预置指标和自定义指标，并校验请求编号不重复且属于当前考核。
        Set<Long> requestIds = new HashSet<>();
        List<HrmPortalPerformanceQuotaSaveReqVO> customRequests = new ArrayList<>();
        for (HrmPortalPerformanceQuotaSaveReqVO request : quotaList) {
            if (request.getId() != null) {
                if (!requestIds.add(request.getId())
                        || !existingQuotaMap.containsKey(request.getId())) {
                    throw exception(PERFORMANCE_DATA_ILLEGAL);
                }
                if (presetQuotaIds.contains(request.getId())) {
                    continue;
                }
            }
            customRequests.add(request);
        }
        if (!requestIds.containsAll(presetQuotaIds)) {
            throw exception(PERFORMANCE_DATA_ILLEGAL);
        }

        // 2. 构造自定义指标，并校验各维度的指标配置
        List<HrmPerformanceAssessmentQuotaDO> presetQuotas = filterList(existingQuotas,
                quota -> presetQuotaIds.contains(quota.getId()));
        Map<Long, List<HrmPerformanceAssessmentQuotaDO>> finalQuotaMap =
                convertMultiMap(presetQuotas, HrmPerformanceAssessmentQuotaDO::getDimensionId);
        List<HrmPerformanceAssessmentQuotaDO> customQuotas = new ArrayList<>();
        int nextSort = existingQuotas.stream().map(HrmPerformanceAssessmentQuotaDO::getSort)
                .filter(Objects::nonNull).max(Integer::compareTo).orElse(0) + 1;
        for (HrmPortalPerformanceQuotaSaveReqVO request : customRequests) {
            HrmPerformanceAssessmentDimensionDO dimension = dimensionMap.get(request.getDimensionId());
            validateCustomQuota(request, dimension);
            HrmPerformanceAssessmentQuotaDO quota = new HrmPerformanceAssessmentQuotaDO()
                    .setAssessmentId(assessment.getId()).setDimensionId(dimension.getId())
                    .setPreset(false)
                    .setName(request.getName().trim()).setDescription(request.getDescription())
                    .setStandard(request.getStandard().trim())
                    .setWeight(request.getWeight()).setScoreType(request.getScoreType())
                    .setTargetValue(request.getTargetValue()).setActualValue(request.getActualValue())
                    .setSelfScore(BigDecimal.ZERO).setReviewerScore(BigDecimal.ZERO)
                    .setFinalScore(BigDecimal.ZERO).setSort(nextSort++);
            customQuotas.add(quota);
            finalQuotaMap.computeIfAbsent(
                    request.getDimensionId(), key -> new ArrayList<>()).add(quota);
        }

        // 3. 批量替换自定义指标
        validateDimensionQuotaList(dimensions, finalQuotaMap);
        List<Long> oldCustomQuotaIds = convertList(existingQuotas, HrmPerformanceAssessmentQuotaDO::getId,
                quota -> Boolean.FALSE.equals(quota.getPreset()));
        if (CollUtil.isNotEmpty(oldCustomQuotaIds)) {
            assessmentQuotaMapper.deleteByIds(oldCustomQuotaIds);
        }
        if (CollUtil.isNotEmpty(customQuotas)) {
            assessmentQuotaMapper.insertBatch(customQuotas);
        }
    }

    /**
     * 更新绩效考核的全部指标
     *
     * @param assessmentId 员工绩效考核编号
     * @param quotaList 指标列表
     */
    void updateAssessmentQuotaList(
            Long assessmentId, List<HrmPortalPerformanceQuotaSaveReqVO> quotaList) {
        // 1. 校验指标编号完整且不重复
        List<HrmPerformanceAssessmentQuotaDO> oldQuotas =
                assessmentQuotaMapper.selectListByAssessmentId(assessmentId);
        if (CollUtil.isEmpty(oldQuotas) || quotaList == null) {
            throw exception(PERFORMANCE_DATA_ILLEGAL);
        }
        Map<Long, HrmPerformanceAssessmentQuotaDO> oldQuotaMap =
                convertMap(oldQuotas, HrmPerformanceAssessmentQuotaDO::getId);
        Map<Long, HrmPortalPerformanceQuotaSaveReqVO> requestQuotaMap = new HashMap<>();
        for (HrmPortalPerformanceQuotaSaveReqVO quota : quotaList) {
            if (quota.getId() == null || requestQuotaMap.put(quota.getId(), quota) != null) {
                throw exception(PERFORMANCE_DATA_ILLEGAL);
            }
        }
        if (ObjUtil.notEqual(requestQuotaMap.keySet(), oldQuotaMap.keySet())) {
            throw exception(PERFORMANCE_DATA_ILLEGAL);
        }

        // 2. 批量更新指标
        List<HrmPerformanceAssessmentQuotaDO> updateQuotas = new ArrayList<>(quotaList.size());
        for (int i = 0; i < quotaList.size(); i++) {
            HrmPerformanceAssessmentQuotaDO quota = BeanUtils.toBean(quotaList.get(i), HrmPerformanceAssessmentQuotaDO.class)
                    .setAssessmentId(assessmentId).setSort(ObjUtil.defaultIfNull(quotaList.get(i).getSort(), i + 1));
            updateQuotas.add(quota);
        }
        assessmentQuotaMapper.updateBatch(updateQuotas);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_PERFORMANCE_TYPE, subType = HRM_PERFORMANCE_FILL_QUOTA_SUB_TYPE,
            bizNo = "{{#reqVO.assessmentId}}", success = HRM_PERFORMANCE_FILL_QUOTA_SUCCESS)
    public void fillQuota(Long userId, HrmPortalPerformanceFillQuotaReqVO reqVO) {
        // 1. 校验员工绩效考核
        HrmPerformanceAssessmentDO assessment =
                validatePerformanceAssessmentExistsForUpdate(reqVO.getAssessmentId());
        // 2. 校验当前用户是被考核员工
        validateOwnAssessment(userId, assessment);
        // 3. 填写指标并推进流程
        HrmPerformanceAssessmentStageDO fillStage = fillQuotaInternal(assessment, reqVO);

        // 4. 追加绩效动作记录
        assessmentActionRecordService.createPerformanceAssessmentActionRecord(
                assessment.getEmployeeId(), assessment.getId(), fillStage.getId(),
                HrmPerformanceAssessmentActionTypeEnum.FILL_QUOTA,
                null, HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus());
    }

    /**
     * 填写绩效指标，并推动员工绩效考核进入下一阶段
     *
     * @param assessment 员工绩效考核
     * @param reqVO 指标信息
     * @return 已完成的指标填写阶段
     */
    private HrmPerformanceAssessmentStageDO fillQuotaInternal(
            HrmPerformanceAssessmentDO assessment,
            HrmPortalPerformanceFillQuotaReqVO reqVO) {
        // 1. 校验绩效计划和指标填写阶段
        HrmPerformancePlanDO plan = validatePerformancePlanExists(assessment.getPlanId());
        if (ObjUtil.notEqual(plan.getStatus(), HrmPerformancePlanStatusEnum.RUNNING.getStatus())
                || ObjUtil.notEqual(plan.getQuotaSettingType(),
                HrmPerformanceQuotaSettingTypeEnum.EMPLOYEE.getType())) {
            throw exception(PERFORMANCE_QUOTA_SETTING_INVALID);
        }
        validateStage(assessment, HrmPerformanceStageTypeEnum.FILL_QUOTA.getType());

        // 2.1 替换员工提交的自定义指标
        replaceAssessmentQuotaList(plan, assessment, reqVO.getQuotas());
        // 2.2 完成指标填写阶段，并清空历史目标确认结果
        List<HrmPerformanceAssessmentStageDO> fillStages =
                assessmentStageMapper.selectListByAssessmentIdAndType(
                        assessment.getId(), HrmPerformanceStageTypeEnum.FILL_QUOTA.getType());
        HrmPerformanceAssessmentStageDO fillStage = CollUtil.findOne(fillStages,
                stage -> Objects.equals(stage.getStatus(),
                        HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus()));
        if (fillStage == null) {
            throw exception(PERFORMANCE_STAGE_ACTION_INVALID);
        }
        fillStage.setStatus(HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus()).setSubmitTime(LocalDateTime.now());
        assessmentStageMapper.updateById(fillStage);
        assessment.setTargetConfirmationResult(null).setTargetConfirmationComment(null).setTargetConfirmationTime(null);

        // 3. 进入目标确认或绩效执行阶段
        if (Boolean.TRUE.equals(plan.getTargetConfirmation())) {
            // 3.1 已启用目标确认时，激活目标确认阶段
            List<HrmPerformanceAssessmentStageDO> targetStages = assessmentStageMapper.selectListByAssessmentIdAndType(
                    assessment.getId(), HrmPerformanceStageTypeEnum.TARGET_CONFIRM.getType());
            HrmPerformanceAssessmentStageDO targetStage = CollUtil.getFirst(targetStages);
            if (targetStage == null) {
                throw exception(PERFORMANCE_TARGET_CONFIRM_CONFIG_INVALID);
            }
            assessmentStageMapper.updateToResetByAssessmentIdAndType(
                    assessment.getId(), HrmPerformanceStageTypeEnum.TARGET_CONFIRM.getType(),
                    HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus());
            targetStage.setStatus(HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus())
                    .setComment(null).setRejectReason(null).setSubmitTime(null).setDeadlineTime(null);
            assessment.setStageType(HrmPerformanceStageTypeEnum.TARGET_CONFIRM.getType())
                    .setStageSort(targetStage.getSort());
            // 3.2 通知目标确认人处理待办
            assessmentProcessService.notifyPendingStage(assessment, targetStage);
        } else {
            // 3.3 未启用目标确认时，直接进入绩效执行阶段
            assessment.setStageType(HrmPerformanceStageTypeEnum.EXECUTING.getType());
        }
        assessmentMapper.updateById(assessment);

        // 4. 刷新绩效计划的完成状态
        assessmentProcessService.refreshPlanCompletionState(assessment.getPlanId());
        return fillStage;
    }

    @Override
    public HrmPortalPerformanceScorePreviewRespVO previewScore(Long userId, HrmPortalPerformanceScoreReqVO reqVO) {
        // 1.1 校验评分阶段和评分人
        HrmPerformanceAssessmentDO assessment = validatePerformanceAssessmentExists(reqVO.getAssessmentId());
        validatePlanRunning(assessment);
        List<HrmPerformanceAssessmentStageDO> reviewStages =
                assessmentStageMapper.selectListByAssessmentIdAndTypes(
                        assessment.getId(), HrmPerformanceStageTypeEnum.REVIEW_TYPES);
        HrmPerformanceAssessmentStageDO currentStage = CollUtil.findOne(reviewStages, stage ->
                (reqVO.getReviewStageId() == null || Objects.equals(stage.getId(), reqVO.getReviewStageId()))
                        && Objects.equals(stage.getStatus(), HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus()));
        if (currentStage == null) {
            throw exception(PERFORMANCE_REVIEW_STAGE_NOT_EXISTS);
        }
        if (!isStageHandler(userId, currentStage)) {
            throw exception(PERFORMANCE_REVIEW_STAGE_NO_PERMISSION);
        }
        validateReviewStage(assessment);
        // 1.2 校验评分指标
        List<HrmPerformanceAssessmentQuotaDO> quotas =
                assessmentQuotaMapper.selectListByAssessmentId(assessment.getId());
        if (CollUtil.isEmpty(quotas) || reqVO.getQuotas() == null) {
            throw exception(PERFORMANCE_DATA_ILLEGAL);
        }
        Map<Long, HrmPortalPerformanceQuotaSaveReqVO> requestQuotaMap = new HashMap<>();
        for (HrmPortalPerformanceQuotaSaveReqVO requestQuota : reqVO.getQuotas()) {
            if (requestQuota.getId() == null || requestQuotaMap.put(requestQuota.getId(), requestQuota) != null) {
                throw exception(PERFORMANCE_DATA_ILLEGAL);
            }
        }
        Set<Long> quotaIds = convertSet(quotas, HrmPerformanceAssessmentQuotaDO::getId);
        if (ObjUtil.notEqual(requestQuotaMap.keySet(), quotaIds)) {
            throw exception(PERFORMANCE_DATA_ILLEGAL);
        }

        // 2. 计算当前阶段和当前累计得分
        HrmPerformancePlanDO plan = validatePerformancePlanExists(assessment.getPlanId());
        BigDecimal maximumScore = getMaximumScore(plan);
        Map<Long, BigDecimal> currentScoreMap = new HashMap<>();
        for (HrmPerformanceAssessmentQuotaDO quota : quotas) {
            BigDecimal score = getRequestedReviewScore(currentStage, requestQuotaMap.get(quota.getId()));
            validateReviewScore(score, maximumScore);
            currentScoreMap.put(quota.getId(), score);
        }
        BigDecimal stageScore = computeScore(plan, quotas, currentScoreMap);

        // 按阶段权重合并当前输入和已完成阶段得分，不将尚未评分的阶段按零分推断为最终结果。
        List<HrmPerformanceAssessmentStageDO> completedStages = filterList(reviewStages,
                stage -> Objects.equals(stage.getStatus(),
                        HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus()));
        Map<Long, HrmPerformanceAssessmentStageDO> completedStageMap =
                convertMap(completedStages, HrmPerformanceAssessmentStageDO::getId);
        Map<Long, BigDecimal> cumulativeQuotaScoreMap = new HashMap<>();
        for (HrmPerformanceAssessmentQuotaDO quota : quotas) {
            cumulativeQuotaScoreMap.put(quota.getId(), currentScoreMap.get(quota.getId())
                    .multiply(currentStage.getWeight()).divide(PERCENT_100, 4, RoundingMode.HALF_UP));
        }
        List<HrmPerformanceAssessmentQuotaScoreDO> completedScores = CollUtil.isEmpty(completedStageMap)
                ? Collections.emptyList()
                : assessmentQuotaScoreMapper.selectListByAssessmentStageIds(completedStageMap.keySet());
        for (HrmPerformanceAssessmentQuotaScoreDO reviewScore : completedScores) {
            HrmPerformanceAssessmentStageDO completedStage =
                    completedStageMap.get(reviewScore.getAssessmentStageId());
            cumulativeQuotaScoreMap.compute(reviewScore.getAssessmentQuotaId(), (quotaId, oldScore) ->
                    ObjUtil.defaultIfNull(oldScore, BigDecimal.ZERO).add(reviewScore.getScore()
                            .multiply(completedStage.getWeight()).divide(PERCENT_100, 4, RoundingMode.HALF_UP)));
        }
        BigDecimal cumulativeScore = computeScore(plan, quotas, cumulativeQuotaScoreMap);
        Level stageLevel = matchResultLevel(plan.getResultConfig(), stageScore);
        boolean allReviewStagesIncluded = completedStages.size() + 1 == reviewStages.size();
        Level cumulativeLevel = allReviewStagesIncluded
                ? matchResultLevel(plan.getResultConfig(), cumulativeScore) : null;
        return new HrmPortalPerformanceScorePreviewRespVO()
                .setStageScore(stageScore).setStageResultLevel(stageLevel == null ? null : stageLevel.getName())
                .setCumulativeScore(cumulativeScore)
                .setCumulativeResultLevel(cumulativeLevel == null ? null : cumulativeLevel.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_PERFORMANCE_TYPE, subType = HRM_PERFORMANCE_SCORE_SUB_TYPE,
            bizNo = "{{#reqVO.assessmentId}}", success = HRM_PERFORMANCE_SCORE_SUCCESS)
    public HrmPortalPerformanceProcessRespVO scoreAssessment(Long userId, HrmPortalPerformanceScoreReqVO reqVO) {
        // 1. 校验员工绩效考核
        HrmPerformanceAssessmentDO assessment =
                validatePerformanceAssessmentExistsForUpdate(reqVO.getAssessmentId());
        validatePlanRunning(assessment);

        // 2. 提交当前阶段评分
        List<HrmPerformanceAssessmentStageDO> reviewStages = assessmentStageMapper.selectListByAssessmentIdAndTypes(
                assessment.getId(), HrmPerformanceStageTypeEnum.REVIEW_TYPES);
        // 评分阶段应在计划启动时完成初始化；缺失时无法继续评分。
        if (CollUtil.isEmpty(reviewStages)) {
            throw exception(PERFORMANCE_REVIEW_STAGE_NOT_EXISTS);
        }
        return scoreReviewStage(userId, assessment, reviewStages, reqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_PERFORMANCE_TYPE, subType = HRM_PERFORMANCE_REJECT_SCORE_SUB_TYPE,
            bizNo = "{{#reqVO.assessmentId}}", success = HRM_PERFORMANCE_REJECT_SCORE_SUCCESS)
    public void rejectReviewStage(Long userId, HrmPortalPerformanceReviewRejectReqVO reqVO) {
        // 1. 校验当前评分阶段
        HrmPerformanceAssessmentDO assessment =
                validatePerformanceAssessmentExistsForUpdate(reqVO.getAssessmentId());
        validatePlanRunning(assessment);
        List<HrmPerformanceAssessmentStageDO> reviewStages = assessmentStageMapper.selectListByAssessmentIdAndTypes(
                assessment.getId(), HrmPerformanceStageTypeEnum.REVIEW_TYPES);
        HrmPerformanceAssessmentStageDO currentStageCandidate = CollUtil.findOne(reviewStages, stage ->
                Objects.equals(stage.getId(), reqVO.getReviewStageId())
                        && Objects.equals(stage.getStatus(), HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus()));
        if (currentStageCandidate == null) {
            throw exception(PERFORMANCE_REVIEW_STAGE_NOT_EXISTS);
        }
        HrmPerformanceAssessmentStageDO currentStage = assessmentStageMapper.selectOneForUpdate(
                HrmPerformanceAssessmentStageDO::getId, currentStageCandidate.getId());
        if (currentStage == null || ObjUtil.notEqual(currentStage.getStatus(),
                HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus())) {
            throw exception(PERFORMANCE_REVIEW_STAGE_NOT_EXISTS);
        }
        reviewStages.set(reviewStages.indexOf(currentStageCandidate), currentStage);
        if (!isStageHandler(userId, currentStage)) {
            throw exception(PERFORMANCE_REVIEW_STAGE_NO_PERMISSION);
        }
        validateReviewStage(assessment);
        if (ObjUtil.notEqual(Boolean.TRUE, currentStage.getRejectAuthority())) {
            throw exception(PERFORMANCE_REVIEW_STAGE_REJECT_INVALID);
        }
        HrmPerformanceAssessmentStageDO previousStage = CollUtil.getLast(filterList(reviewStages,
                stage -> Objects.equals(stage.getStatus(),
                        HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus())
                        && stage.getSort() < currentStage.getSort()));
        if (previousStage == null) {
            throw exception(PERFORMANCE_REVIEW_STAGE_REJECT_INVALID);
        }

        // 2. 驳回到上一评分阶段
        assessmentQuotaScoreMapper.deleteByAssessmentStageId(previousStage.getId());
        previousStage.setStatus(HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus())
                .setScore(null).setComment(null).setRejectReason(reqVO.getReason()).setSubmitTime(null);
        assessmentStageMapper.updateToResetForReject(previousStage.getId(),
                HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus(), reqVO.getReason());
        currentStage.setStatus(HrmPerformanceAssessmentStageStatusEnum.NOT_PROCESSED.getStatus());
        assessmentStageMapper.updateById(currentStage);

        // 3. 重新汇总指标得分和评语，并回退员工绩效考核的当前阶段
        refreshPartialReviewAggregates(assessment.getId(), reviewStages);
        assessment.setStageSort(previousStage.getSort()).setStageType(toPerformanceStageType(previousStage))
                .setSelfComment(joinReviewComments(reviewStages, true))
                .setReviewerComment(joinReviewComments(reviewStages, false));
        assessmentMapper.updateById(assessment);
        assessmentProcessService.notifyPendingStage(assessment, previousStage);
        assessmentProcessService.notifyProcessResult(assessment,
                Collections.singleton(previousStage.getHandlerEmployeeId()),
                "考核评分", "已被驳回，请重新评分");

        // 4. 追加绩效动作记录
        assessmentActionRecordService.createPerformanceAssessmentActionRecord(
                currentStage.getHandlerEmployeeId(), assessment.getId(), previousStage.getId(),
                HrmPerformanceAssessmentActionTypeEnum.REJECT_SCORE,
                null, HrmPerformanceAssessmentStageStatusEnum.REJECTED.getStatus(),
                previousStage.getName(), reqVO.getReason());
    }

    /**
     * 提交当前评分阶段，并激活下一评分阶段或完成评分
     *
     * @param userId 用户编号
     * @param assessment 员工绩效考核
     * @param reviewStages 评分阶段列表
     * @param reqVO 评分信息
     * @return 流程处理结果
     */
    private HrmPortalPerformanceProcessRespVO scoreReviewStage(
            Long userId, HrmPerformanceAssessmentDO assessment,
            List<HrmPerformanceAssessmentStageDO> reviewStages,
            HrmPortalPerformanceScoreReqVO reqVO) {
        // 1. 校验当前评分阶段和评分人
        HrmPerformanceAssessmentStageDO reviewStageCandidate = CollUtil.findOne(reviewStages,
                stage -> (reqVO.getReviewStageId() == null
                        || Objects.equals(stage.getId(), reqVO.getReviewStageId()))
                        && Objects.equals(stage.getStatus(),
                        HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus()));
        if (reviewStageCandidate == null) {
            throw exception(PERFORMANCE_REVIEW_STAGE_NOT_EXISTS);
        }
        HrmPerformanceAssessmentStageDO reviewStage = assessmentStageMapper.selectOneForUpdate(
                HrmPerformanceAssessmentStageDO::getId, reviewStageCandidate.getId());
        if (reviewStage == null || ObjUtil.notEqual(reviewStage.getStatus(),
                HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus())) {
            throw exception(PERFORMANCE_REVIEW_STAGE_NOT_EXISTS);
        }
        reviewStages.set(reviewStages.indexOf(reviewStageCandidate), reviewStage);
        if (!isStageHandler(userId, reviewStage)) {
            throw exception(PERFORMANCE_REVIEW_STAGE_NO_PERMISSION);
        }
        validateReviewStage(assessment);

        // 2. 校验评分指标和评语
        // 2.1 校验评分指标
        List<HrmPerformanceAssessmentQuotaDO> quotas =
                assessmentQuotaMapper.selectListByAssessmentId(assessment.getId());
        if (CollUtil.isEmpty(quotas) || reqVO.getQuotas() == null) {
            throw exception(PERFORMANCE_DATA_ILLEGAL);
        }
        Map<Long, HrmPortalPerformanceQuotaSaveReqVO> requestQuotaMap = new HashMap<>();
        for (HrmPortalPerformanceQuotaSaveReqVO requestQuota : reqVO.getQuotas()) {
            if (requestQuota.getId() == null || requestQuotaMap.put(requestQuota.getId(), requestQuota) != null) {
                throw exception(PERFORMANCE_DATA_ILLEGAL);
            }
        }
        Set<Long> quotaIds = convertSet(quotas, HrmPerformanceAssessmentQuotaDO::getId);
        if (ObjUtil.notEqual(requestQuotaMap.keySet(), quotaIds)) {
            throw exception(PERFORMANCE_DATA_ILLEGAL);
        }

        // 2.2 获得并校验当前评分阶段的评语
        String roleComment = Objects.equals(
                reviewStage.getRaterType(), HrmPerformanceRaterTypeEnum.SELF.getType())
                ? reqVO.getSelfComment() : reqVO.getReviewerComment();
        String stageComment = StringUtils.hasText(reqVO.getComment())
                ? reqVO.getComment() : roleComment;
        if (Boolean.TRUE.equals(reviewStage.getRequiredSetting())
                && !StringUtils.hasText(stageComment)) {
            throw exception(PERFORMANCE_DATA_ILLEGAL);
        }

        // 3. 保存当前阶段的指标评分
        HrmPerformancePlanDO plan = validatePerformancePlanExists(assessment.getPlanId());
        BigDecimal maximumScore = getMaximumScore(plan);
        assessmentQuotaScoreMapper.deleteByAssessmentStageId(reviewStage.getId());
        Map<Long, BigDecimal> stageQuotaScoreMap = new HashMap<>();
        List<HrmPerformanceAssessmentQuotaScoreDO> reviewScores = new ArrayList<>(quotas.size());
        for (HrmPerformanceAssessmentQuotaDO quota : quotas) {
            HrmPortalPerformanceQuotaSaveReqVO requestQuota = requestQuotaMap.get(quota.getId());
            BigDecimal quotaScore = getRequestedReviewScore(reviewStage, requestQuota);
            validateReviewScore(quotaScore, maximumScore);
            reviewScores.add(new HrmPerformanceAssessmentQuotaScoreDO()
                    .setAssessmentStageId(reviewStage.getId()).setAssessmentQuotaId(quota.getId())
                    .setScore(quotaScore).setComment(requestQuota.getComment()));
            stageQuotaScoreMap.put(quota.getId(), quotaScore);
            // 同步指标实际值以及当前评分角色对应的得分。
            if (requestQuota.getActualValue() != null) {
                quota.setActualValue(requestQuota.getActualValue());
            }
            if (Objects.equals(reviewStage.getRaterType(), HrmPerformanceRaterTypeEnum.SELF.getType())) {
                quota.setSelfScore(quotaScore);
            } else {
                quota.setReviewerScore(quotaScore);
            }
        }
        assessmentQuotaScoreMapper.insertBatch(reviewScores);
        assessmentQuotaMapper.updateBatch(quotas);

        // 4. 完成当前评分阶段
        reviewStage.setScore(computeScore(plan, quotas, stageQuotaScoreMap)).setComment(stageComment)
                .setSubmitTime(LocalDateTime.now())
                .setStatus(HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus());
        assessmentStageMapper.updateById(reviewStage);
        assessmentActionRecordService.createPerformanceAssessmentActionRecord(
                reviewStage.getHandlerEmployeeId(), assessment.getId(), reviewStage.getId(),
                HrmPerformanceAssessmentActionTypeEnum.SCORE,
                null, HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus(),
                reviewStage.getScore(), StringUtils.hasText(stageComment) ? "，说明：" + stageComment : "");
        assessment.setSelfComment(joinReviewComments(reviewStages, true))
                .setReviewerComment(joinReviewComments(reviewStages, false));

        // 5. 激活下一评分阶段；全部评分完成时汇总结果
        HrmPerformanceAssessmentStageDO nextStage = CollUtil.findOne(reviewStages,
                stage -> Objects.equals(stage.getStatus(),
                        HrmPerformanceAssessmentStageStatusEnum.NOT_PROCESSED.getStatus())
                        && stage.getSort() > reviewStage.getSort());
        if (nextStage != null) {
            nextStage.setStatus(HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus());
            assessmentStageMapper.updateById(nextStage);
            refreshPartialReviewAggregates(assessment.getId(), reviewStages);
            assessment.setStageSort(nextStage.getSort()).setStageType(toPerformanceStageType(nextStage));
            assessmentMapper.updateById(assessment);
            assessmentProcessService.notifyPendingStage(assessment, nextStage);
            return new HrmPortalPerformanceProcessRespVO().setId(assessment.getId());
        }
        HrmPerformanceAssessmentStageDO pendingStage = CollUtil.findOne(reviewStages,
                stage -> Objects.equals(stage.getStatus(),
                        HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus()));
        if (pendingStage != null) {
            refreshPartialReviewAggregates(assessment.getId(), reviewStages);
            assessment.setStageSort(pendingStage.getSort())
                    .setStageType(toPerformanceStageType(pendingStage));
            assessmentMapper.updateById(assessment);
            return new HrmPortalPerformanceProcessRespVO().setId(assessment.getId());
        }
        return finishReviewScoring(plan, assessment);
    }

    /**
     * 汇总全部评分阶段，并启动结果审核
     *
     * @param plan 绩效计划
     * @param assessment 员工绩效考核
     * @return 流程处理结果
     */
    private HrmPortalPerformanceProcessRespVO finishReviewScoring(
            HrmPerformancePlanDO plan, HrmPerformanceAssessmentDO assessment) {
        // 1. 校验全部评分阶段已经完成
        List<HrmPerformanceAssessmentStageDO> reviewStages =
                assessmentStageMapper.selectListByAssessmentIdAndTypes(
                        assessment.getId(), HrmPerformanceStageTypeEnum.REVIEW_TYPES);
        if (CollUtil.isEmpty(reviewStages) || reviewStages.stream()
                .anyMatch(stage -> ObjUtil.notEqual(stage.getStatus(),
                        HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus()))) {
            throw exception(PERFORMANCE_REVIEW_STAGE_NOT_EXISTS);
        }
        Map<Long, HrmPerformanceAssessmentStageDO> stageMap =
                convertMap(reviewStages, HrmPerformanceAssessmentStageDO::getId);
        List<HrmPerformanceAssessmentQuotaScoreDO> reviewScores = assessmentQuotaScoreMapper
                .selectListByAssessmentStageIds(stageMap.keySet());
        Map<Long, List<HrmPerformanceAssessmentQuotaScoreDO>> quotaScoreMap =
                convertMultiMap(reviewScores, HrmPerformanceAssessmentQuotaScoreDO::getAssessmentQuotaId);

        // 2. 汇总每个指标的自评、他评和最终得分
        List<HrmPerformanceAssessmentQuotaDO> quotas =
                assessmentQuotaMapper.selectListByAssessmentId(assessment.getId());
        assessmentQuotaMapper.updateReviewScoreToNullByAssessmentId(assessment.getId());
        for (HrmPerformanceAssessmentQuotaDO quota : quotas) {
            quota.setSelfScore(null).setReviewerScore(null).setFinalScore(null);
            List<HrmPerformanceAssessmentQuotaScoreDO> quotaScores =
                    quotaScoreMap.getOrDefault(quota.getId(), Collections.emptyList());
            if (quotaScores.size() != reviewStages.size()) {
                throw exception(PERFORMANCE_DATA_ILLEGAL);
            }
            BigDecimal finalScore = BigDecimal.ZERO;
            BigDecimal reviewerWeightedScore = BigDecimal.ZERO;
            BigDecimal reviewerWeight = BigDecimal.ZERO;
            for (HrmPerformanceAssessmentQuotaScoreDO quotaScore : quotaScores) {
                HrmPerformanceAssessmentStageDO stage = stageMap.get(quotaScore.getAssessmentStageId());
                BigDecimal weightedScore = quotaScore.getScore().multiply(stage.getWeight())
                        .divide(PERCENT_100, 4, RoundingMode.HALF_UP);
                finalScore = finalScore.add(weightedScore);
                if (Objects.equals(stage.getRaterType(), HrmPerformanceRaterTypeEnum.SELF.getType())) {
                    quota.setSelfScore(quotaScore.getScore());
                } else {
                    reviewerWeightedScore = reviewerWeightedScore.add(weightedScore);
                    reviewerWeight = reviewerWeight.add(stage.getWeight());
                }
            }
            quota.setReviewerScore(reviewerWeight.signum() == 0 ? null
                    : reviewerWeightedScore.multiply(PERCENT_100)
                    .divide(reviewerWeight, 2, RoundingMode.HALF_UP));
            quota.setFinalScore(finalScore.setScale(2, RoundingMode.HALF_UP));
        }
        assessmentQuotaMapper.updateBatch(quotas);

        // 3. 汇总员工绩效考核结果，并启动结果审核
        BigDecimal score = computeScore(plan, quotas,
                convertMap(quotas, HrmPerformanceAssessmentQuotaDO::getId,
                        HrmPerformanceAssessmentQuotaDO::getFinalScore));
        Level level = matchResultLevel(plan.getResultConfig(), score);
        if (level == null) {
            throw exception(PERFORMANCE_RESULT_LEVEL_NOT_MATCH);
        }
        assessment.setScore(score).setResultLevel(level.getName())
                .setCoefficient(ObjUtil.defaultIfNull(level.getCoefficient(), BigDecimal.ONE))
                .setSelfComment(joinReviewComments(reviewStages, true))
                .setReviewerComment(joinReviewComments(reviewStages, false));
        assessmentMapper.updateById(assessment);
        return assessmentProcessService.startResultAudit(assessment.getId());
    }

    /**
     * 根据已完成的评分阶段刷新指标汇总分数
     *
     * @param assessmentId 员工绩效考核编号
     * @param reviewStages 评分阶段列表
     */
    private void refreshPartialReviewAggregates(Long assessmentId,
                                                List<HrmPerformanceAssessmentStageDO> reviewStages) {
        List<HrmPerformanceAssessmentStageDO> completedStages = filterList(reviewStages,
                stage -> Objects.equals(stage.getStatus(),
                        HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus()));
        Map<Long, HrmPerformanceAssessmentStageDO> completedStageMap =
                convertMap(completedStages, HrmPerformanceAssessmentStageDO::getId);
        List<HrmPerformanceAssessmentQuotaScoreDO> completedScores = CollUtil.isEmpty(completedStageMap)
                ? Collections.emptyList()
                : assessmentQuotaScoreMapper.selectListByAssessmentStageIds(completedStageMap.keySet());
        Map<Long, List<HrmPerformanceAssessmentQuotaScoreDO>> quotaScoreMap =
                convertMultiMap(completedScores, HrmPerformanceAssessmentQuotaScoreDO::getAssessmentQuotaId);
        List<HrmPerformanceAssessmentQuotaDO> quotas =
                assessmentQuotaMapper.selectListByAssessmentId(assessmentId);
        assessmentQuotaMapper.updateReviewScoreToNullByAssessmentId(assessmentId);
        for (HrmPerformanceAssessmentQuotaDO quota : quotas) {
            BigDecimal selfScore = null;
            BigDecimal reviewerWeightedScore = BigDecimal.ZERO;
            BigDecimal reviewerWeight = BigDecimal.ZERO;
            for (HrmPerformanceAssessmentQuotaScoreDO score
                    : quotaScoreMap.getOrDefault(quota.getId(), Collections.emptyList())) {
                HrmPerformanceAssessmentStageDO stage = completedStageMap.get(score.getAssessmentStageId());
                if (stage == null) {
                    continue;
                }
                if (Objects.equals(stage.getRaterType(), HrmPerformanceRaterTypeEnum.SELF.getType())) {
                    selfScore = score.getScore();
                } else {
                    reviewerWeightedScore = reviewerWeightedScore.add(score.getScore().multiply(stage.getWeight()));
                    reviewerWeight = reviewerWeight.add(stage.getWeight());
                }
            }
            quota.setSelfScore(selfScore)
                    .setReviewerScore(reviewerWeight.signum() == 0 ? null
                            : reviewerWeightedScore.divide(reviewerWeight, 2, RoundingMode.HALF_UP))
                    .setFinalScore(null);
        }
        assessmentQuotaMapper.updateBatch(quotas);
    }

    private BigDecimal getRequestedReviewScore(HrmPerformanceAssessmentStageDO reviewStage,
                                               HrmPortalPerformanceQuotaSaveReqVO quota) {
        return Objects.equals(reviewStage.getRaterType(), HrmPerformanceRaterTypeEnum.SELF.getType())
                ? ObjUtil.defaultIfNull(quota.getFinalScore(), quota.getSelfScore())
                : ObjUtil.defaultIfNull(quota.getFinalScore(), quota.getReviewerScore());
    }

    private void validateReviewScore(BigDecimal score, BigDecimal maximumScore) {
        if (score == null || score.signum() < 0
                || (maximumScore != null && score.compareTo(maximumScore) > 0)) {
            throw exception(PERFORMANCE_DATA_ILLEGAL);
        }
    }

    private BigDecimal getMaximumScore(HrmPerformancePlanDO plan) {
        HrmPerformanceAssessmentTemplateDO.AssessmentConfig config = plan.getAssessmentConfig();
        if (config != null && Objects.equals(config.getUpperLimitType(),
                HrmPerformanceUpperLimitTypeEnum.UNIFIED.getType())) {
            return config.getUpperLimitScore();
        }
        return null;
    }

    private String joinReviewComments(List<HrmPerformanceAssessmentStageDO> reviewStages, boolean self) {
        List<HrmPerformanceAssessmentStageDO> stages = filterList(reviewStages, stage ->
                Objects.equals(stage.getStatus(), HrmPerformanceAssessmentStageStatusEnum.PROCESSED.getStatus())
                        && Objects.equals(stage.getRaterType(), HrmPerformanceRaterTypeEnum.SELF.getType()) == self && StringUtils.hasText(stage.getComment()));
        if (CollUtil.isEmpty(stages)) {
            return null;
        }
        return CollUtil.join(convertList(stages,
                stage -> stage.getName() + "：" + stage.getComment()), "；");
    }

    private Integer toPerformanceStageType(HrmPerformanceAssessmentStageDO stage) {
        return Objects.equals(stage.getRaterType(), HrmPerformanceRaterTypeEnum.SELF.getType())
                ? HrmPerformanceStageTypeEnum.SELF_SCORE.getType()
                : HrmPerformanceStageTypeEnum.OTHER_SCORE.getType();
    }

    private BigDecimal computeScore(HrmPerformancePlanDO plan, List<HrmPerformanceAssessmentQuotaDO> quotaList,
                                    Map<Long, BigDecimal> scoreMap) {
        // 1. 查询指标所属维度，用于应用维度权重
        Map<Long, HrmPerformanceAssessmentDimensionDO> dimensionMap = Collections.emptyMap();
        if (CollUtil.isNotEmpty(quotaList)) {
            List<HrmPerformanceAssessmentDimensionDO> dimensions =
                    assessmentDimensionMapper.selectListByAssessmentId(
                            CollUtil.getFirst(quotaList).getAssessmentId());
            dimensionMap = convertMap(dimensions, HrmPerformanceAssessmentDimensionDO::getId);
        }
        // 2. 按“指标得分 × 维度权重 × 指标权重”汇总总分
        BigDecimal total = BigDecimal.ZERO;
        for (HrmPerformanceAssessmentQuotaDO quota : quotaList) {
            BigDecimal score = ObjUtil.defaultIfNull(scoreMap.get(quota.getId()), BigDecimal.ZERO);
            HrmPerformanceAssessmentDimensionDO dimension = dimensionMap.get(quota.getDimensionId());
            BigDecimal weightedScore = score
                    .multiply(dimension == null ? PERCENT_100
                            : ObjUtil.defaultIfNull(dimension.getWeight(), PERCENT_100))
                    .divide(PERCENT_100, 4, RoundingMode.HALF_UP)
                    .multiply(ObjUtil.defaultIfNull(quota.getWeight(), PERCENT_100))
                    .divide(PERCENT_100, 4, RoundingMode.HALF_UP);
            total = total.add(weightedScore);
        }
        // 3. 应用统一评分上限，并保留两位小数
        BigDecimal maximumScore = getMaximumScore(plan);
        if (maximumScore != null && total.compareTo(maximumScore) > 0) {
            total = maximumScore;
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private Level matchResultLevel(ResultConfig resultConfig, BigDecimal score) {
        if (resultConfig == null || CollUtil.isEmpty(resultConfig.getLevels())) {
            return null;
        }
        return CollUtil.findOne(resultConfig.getLevels(), level -> isInLevel(score, level));
    }

    private boolean isInLevel(BigDecimal score, Level level) {
        boolean lowerOk = level.getMinScore() == null || score.compareTo(level.getMinScore()) >= 0;
        boolean upperOk = level.getMaxScore() == null || score.compareTo(level.getMaxScore()) <= 0;
        return lowerOk && upperOk;
    }

    private void validateStage(HrmPerformanceAssessmentDO assessment, Integer expectedStage) {
        if (ObjUtil.notEqual(assessment.getStageType(), expectedStage)) {
            throw exception(PERFORMANCE_STAGE_ACTION_INVALID);
        }
    }

    private void validateReviewStage(HrmPerformanceAssessmentDO assessment) {
        if (!HrmPerformanceStageTypeEnum.REVIEW_TYPES.contains(assessment.getStageType())) {
            throw exception(PERFORMANCE_STAGE_ACTION_INVALID);
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    private HrmEmployeeDO validateOwnAssessment(Long userId, HrmPerformanceAssessmentDO assessment) {
        HrmEmployeeDO employee = employeeService.getEmployee(assessment.getEmployeeId());
        if (employee == null) {
            throw exception(PERFORMANCE_ASSESSMENT_NOT_EXISTS);
        }
        if (ObjUtil.notEqual(employee.getUserId(), userId)) {
            throw exception(PERFORMANCE_ASSESSMENT_NOT_EXISTS);
        }
        return employee;
    }

    private HrmPerformancePlanDO validatePerformancePlanExists(Long id) {
        return planService.validatePerformancePlanExists(id);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isStageHandler(Long userId, HrmPerformanceAssessmentStageDO stage) {
        HrmEmployeeDO employee = employeeService.getEmployeeByUserId(userId);
        return employee != null
                && Objects.equals(stage.getHandlerEmployeeId(), employee.getId());
    }

    @SuppressWarnings("UnusedReturnValue")
    private HrmPerformancePlanDO validatePlanRunning(HrmPerformanceAssessmentDO assessment) {
        HrmPerformancePlanDO plan = validatePerformancePlanExists(assessment.getPlanId());
        return validatePlanRunning(plan);
    }

    private HrmPerformancePlanDO validatePlanRunning(HrmPerformancePlanDO plan) {
        if (ObjUtil.notEqual(plan.getStatus(), HrmPerformancePlanStatusEnum.RUNNING.getStatus())) {
            throw exception(PERFORMANCE_STAGE_ACTION_INVALID);
        }
        return plan;
    }

    private void validateCustomQuota(HrmPortalPerformanceQuotaSaveReqVO request,
                                     HrmPerformanceAssessmentDimensionDO dimension) {
        if (dimension == null || ObjUtil.notEqual(Boolean.TRUE, dimension.getAllowEdit())
                || request.getDimensionId() == null
                || !StringUtils.hasText(request.getName())
                || !StringUtils.hasText(request.getStandard())
                || request.getWeight() == null || request.getWeight().signum() <= 0
                || request.getWeight().compareTo(PERCENT_100) > 0
                || request.getScoreType() == null) {
            throw exception(PERFORMANCE_DATA_ILLEGAL);
        }
    }

    private void validateDimensionQuotaList(
            List<HrmPerformanceAssessmentDimensionDO> dimensions,
            Map<Long, List<HrmPerformanceAssessmentQuotaDO>> quotaMap) {
        for (HrmPerformanceAssessmentDimensionDO dimension : dimensions) {
            List<HrmPerformanceAssessmentQuotaDO> dimensionQuotas =
                    quotaMap.getOrDefault(dimension.getId(), Collections.emptyList());
            BigDecimal totalWeight = getSumValue(dimensionQuotas,
                    HrmPerformanceAssessmentQuotaDO::getWeight, BigDecimal::add, BigDecimal.ZERO);
            Set<String> quotaNames = convertSet(dimensionQuotas,
                    quota -> StringUtils.hasText(quota.getName())
                            ? quota.getName().trim() : null);
            if (CollUtil.isEmpty(dimensionQuotas)
                    || totalWeight.compareTo(PERCENT_100) != 0
                    || quotaNames.size() != dimensionQuotas.size()) {
                throw exception(PERFORMANCE_DATA_ILLEGAL);
            }
        }
    }

    private HrmPerformanceAssessmentDO validatePerformanceAssessmentExists(Long id) {
        HrmPerformanceAssessmentDO assessment = assessmentMapper.selectById(id);
        if (assessment == null) {
            throw exception(PERFORMANCE_ASSESSMENT_NOT_EXISTS);
        }
        return assessment;
    }

    private HrmPerformanceAssessmentDO validatePerformanceAssessmentExistsForUpdate(Long id) {
        HrmPerformanceAssessmentDO assessment = assessmentMapper.selectOneForUpdate(
                HrmPerformanceAssessmentDO::getId, id);
        if (assessment == null) {
            throw exception(PERFORMANCE_ASSESSMENT_NOT_EXISTS);
        }
        return assessment;
    }

}
