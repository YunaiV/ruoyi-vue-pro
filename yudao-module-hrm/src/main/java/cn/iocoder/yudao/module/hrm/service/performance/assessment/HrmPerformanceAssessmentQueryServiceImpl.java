package cn.iocoder.yudao.module.hrm.service.performance.assessment;

import cn.iocoder.yudao.module.hrm.service.performance.plan.HrmPerformancePlanService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceArchiveEmployeeRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceAssessmentDimensionRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceAssessmentQuotaRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceAssessmentQuotaScoreRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceAssessmentRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceAssessmentStageRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceProcessRecordRespVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentActionRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentAppealRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentDimensionDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentQuotaDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentQuotaScoreDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentStageDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.plan.HrmPerformancePlanDO;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAppealRecordStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAssessmentActionTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAssessmentStageStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformancePlanStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceReviewVisibleContentEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceStageTypeEnum;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;

/**
 * HRM 员工绩效考核查询 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class HrmPerformanceAssessmentQueryServiceImpl
        implements HrmPerformanceAssessmentQueryService {

    private static final String PROCESS_RECORD_SOURCE_ACTION = "ACTION";

    @Resource
    private HrmPerformanceAssessmentService assessmentService;
    @Resource
    private HrmPerformanceAssessmentActionRecordService assessmentActionRecordService;
    @Resource
    private HrmPerformancePlanService planService;
    @Resource
    private HrmEmployeeService employeeService;

    @Resource
    private DeptApi deptApi;

    @Override
    public HrmPerformanceAssessmentRespVO getPerformanceAssessmentRespVO(HrmPerformanceAssessmentDO assessment) {
        if (assessment == null) {
            return null;
        }
        List<HrmPerformanceAssessmentRespVO> respVOList =
                getPerformanceAssessmentRespVOList(Collections.singletonList(assessment));
        return CollUtil.getFirst(respVOList);
    }

    @Override
    public PageResult<HrmPerformanceAssessmentRespVO> getPerformanceAssessmentRespVOPage(
            PageResult<HrmPerformanceAssessmentDO> pageResult) {
        return new PageResult<>(getPerformanceAssessmentRespVOList(pageResult.getList()), pageResult.getTotal());
    }

    @Override
    public List<HrmPerformanceAssessmentRespVO> getPerformanceAssessmentRespVOList(
            List<HrmPerformanceAssessmentDO> assessments) {
        if (CollUtil.isEmpty(assessments)) {
            return Collections.emptyList();
        }

        // 1.1 绩效计划信息
        Set<Long> planIds = convertSet(assessments, HrmPerformanceAssessmentDO::getPlanId);
        Map<Long, HrmPerformancePlanDO> planMap = planService.getPerformancePlanMap(planIds);
        // 1.2 维度和指标信息
        Set<Long> assessmentIds = convertSet(assessments, HrmPerformanceAssessmentDO::getId);
        List<HrmPerformanceAssessmentDimensionDO> dimensions =
                assessmentService.getPerformanceAssessmentDimensionList(assessmentIds);
        Map<Long, List<HrmPerformanceAssessmentDimensionDO>> dimensionMap =
                convertMultiMap(dimensions, HrmPerformanceAssessmentDimensionDO::getAssessmentId);
        Map<Long, HrmPerformanceAssessmentDimensionDO> dimensionDetailMap =
                convertMap(dimensions, HrmPerformanceAssessmentDimensionDO::getId);
        List<HrmPerformanceAssessmentQuotaDO> quotas =
                assessmentService.getPerformanceAssessmentQuotaList(assessmentIds);
        Map<Long, List<HrmPerformanceAssessmentQuotaDO>> quotaMap =
                convertMultiMap(quotas, HrmPerformanceAssessmentQuotaDO::getAssessmentId);
        // 1.3 流程阶段和评分信息
        List<HrmPerformanceAssessmentStageDO> assessmentStages =
                assessmentService.getPerformanceAssessmentStageList(assessmentIds);
        Map<Long, List<HrmPerformanceAssessmentStageDO>> assessmentStageMap =
                convertMultiMap(assessmentStages, HrmPerformanceAssessmentStageDO::getAssessmentId);
        List<HrmPerformanceAssessmentQuotaScoreDO> quotaScores =
                assessmentService.getPerformanceAssessmentQuotaScoreList(
                        convertSet(assessmentStages, HrmPerformanceAssessmentStageDO::getId));
        Map<Long, List<HrmPerformanceAssessmentQuotaScoreDO>> quotaScoreMap =
                convertMultiMap(quotaScores, HrmPerformanceAssessmentQuotaScoreDO::getAssessmentStageId);
        // 1.4 员工和部门信息
        Set<Long> employeeIds = new HashSet<>(convertSet(assessments, HrmPerformanceAssessmentDO::getEmployeeId));
        employeeIds.addAll(convertSet(assessmentStages, HrmPerformanceAssessmentStageDO::getHandlerEmployeeId));
        Map<Long, HrmEmployeeDO> employeeMap = employeeService.getEmployeeMap(employeeIds);
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
                convertSet(employeeMap.values(), HrmEmployeeDO::getDeptId));

        // 2. 拼接响应
        return convertList(assessments, assessment -> {
            HrmPerformanceAssessmentRespVO respVO = BeanUtils.toBean(assessment, HrmPerformanceAssessmentRespVO.class);
            buildPlanInfo(respVO, planMap.get(assessment.getPlanId()));
            buildEmployeeInfo(respVO, employeeMap.get(assessment.getEmployeeId()), deptMap);
            respVO.setCanConfirmTarget(false);
            respVO.setDimensions(BeanUtils.toBean(dimensionMap.getOrDefault(
                    assessment.getId(), Collections.emptyList()),
                    HrmPerformanceAssessmentDimensionRespVO.class));
            respVO.setQuotas(BeanUtils.toBean(quotaMap.getOrDefault(
                    assessment.getId(), Collections.emptyList()),
                    HrmPerformanceAssessmentQuotaRespVO.class, quotaRespVO -> {
                        MapUtils.findAndThen(dimensionDetailMap, quotaRespVO.getDimensionId(), dimension ->
                            quotaRespVO.setDimensionName(dimension.getName())
                                    .setDimensionWeight(dimension.getWeight())
                                    .setAllowEdit(dimension.getAllowEdit()));
                    }));
            buildStageInfo(respVO, assessment, assessmentStageMap.getOrDefault(
                    assessment.getId(), Collections.emptyList()), quotaScoreMap, employeeMap);
            return respVO;
        });
    }

    @Override
    public HrmPerformanceAssessmentRespVO getPerformanceAssessmentProcessRespVO(
            HrmPerformanceAssessmentDO assessment, Long userId) {
        if (assessment == null) {
            return null;
        }
        List<HrmPerformanceAssessmentRespVO> respVOList =
                getPerformanceAssessmentProcessRespVOList(Collections.singletonList(assessment), userId);
        return CollUtil.getFirst(respVOList);
    }

    @Override
    public List<HrmPerformanceAssessmentRespVO> getPerformanceAssessmentProcessRespVOList(
            List<HrmPerformanceAssessmentDO> assessments, Long userId) {
        List<HrmPerformanceAssessmentRespVO> result = getPerformanceAssessmentRespVOList(assessments);
        if (CollUtil.isEmpty(result)) {
            return result;
        }
        Set<Long> assessmentIds = convertSet(assessments, HrmPerformanceAssessmentDO::getId);
        List<HrmPerformanceAssessmentAppealRecordDO> appealRecords =
                assessmentService.getPerformanceAssessmentAppealRecordList(
                        assessmentIds, HrmPerformanceAppealRecordStatusEnum.NOT_PROCESSED.getStatus());
        Map<Long, List<HrmPerformanceAssessmentAppealRecordDO>> appealRecordMap =
                convertMultiMap(appealRecords, HrmPerformanceAssessmentAppealRecordDO::getAssessmentId);
        HrmEmployeeDO currentEmployee = employeeService.getEmployeeByUserId(userId);
        Long currentEmployeeId = currentEmployee == null ? null : currentEmployee.getId();
        for (HrmPerformanceAssessmentRespVO respVO : result) {
            List<HrmPerformanceAssessmentAppealRecordDO> assessmentAppealRecords =
                    appealRecordMap.getOrDefault(respVO.getId(), Collections.emptyList());
            markStageAccess(respVO, currentEmployeeId, assessmentAppealRecords);
            HrmPerformanceAssessmentStageRespVO viewerReviewStage = findReviewStageRespVOByHandler(
                    respVO.getReviewStages(), currentEmployeeId);
            if (viewerReviewStage != null) {
                filterReviewVisibility(respVO, viewerReviewStage);
            }
        }
        return result;
    }

    @Override
    public HrmPerformanceAssessmentRespVO getPerformanceAssessmentTaskRespVO(
            HrmPerformanceAssessmentDO assessment, Long stageId, Long userId) {
        if (assessment == null) {
            return null;
        }
        List<HrmPerformanceAssessmentRespVO> respVOList = getPerformanceAssessmentTaskRespVOList(
                Collections.singletonList(assessment), Collections.singletonList(stageId), userId);
        return CollUtil.getFirst(respVOList);
    }

    @Override
    public List<HrmPerformanceAssessmentRespVO> getPerformanceAssessmentTaskRespVOList(
            List<HrmPerformanceAssessmentDO> assessments, List<Long> stageIds, Long userId) {
        List<HrmPerformanceAssessmentRespVO> result = getPerformanceAssessmentRespVOList(assessments);
        if (CollUtil.isEmpty(result)) {
            return result;
        }
        Set<Long> assessmentIds = convertSet(assessments, HrmPerformanceAssessmentDO::getId);
        List<HrmPerformanceAssessmentAppealRecordDO> appealRecords =
                assessmentService.getPerformanceAssessmentAppealRecordList(
                        assessmentIds, HrmPerformanceAppealRecordStatusEnum.NOT_PROCESSED.getStatus());
        Map<Long, List<HrmPerformanceAssessmentAppealRecordDO>> appealRecordMap =
                convertMultiMap(appealRecords, HrmPerformanceAssessmentAppealRecordDO::getAssessmentId);
        HrmEmployeeDO currentEmployee = employeeService.getEmployeeByUserId(userId);
        Long currentEmployeeId = currentEmployee == null ? null : currentEmployee.getId();
        for (int i = 0; i < result.size(); i++) {
            HrmPerformanceAssessmentRespVO respVO = result.get(i);
            HrmPerformanceAssessmentStageRespVO selectedStage = markTaskStageAccess(
                    respVO, currentEmployeeId, stageIds.get(i),
                    appealRecordMap.getOrDefault(respVO.getId(), Collections.emptyList()));
            if (selectedStage != null && HrmPerformanceStageTypeEnum.REVIEW_TYPES.contains(selectedStage.getType())) {
                filterReviewVisibility(respVO, selectedStage);
            }
        }
        return result;
    }

    @Override
    public PageResult<HrmPerformanceAssessmentRespVO> getPerformanceAssessmentTaskRespVOPage(
            PageResult<HrmPerformanceAssessmentStageDO> pageResult, Long userId) {
        List<HrmPerformanceAssessmentStageDO> stages = pageResult.getList();
        if (CollUtil.isEmpty(stages)) {
            return PageResult.empty(pageResult.getTotal());
        }

        // 1. 批量查询任务关联的绩效考核，并过滤已不存在的考核
        Map<Long, HrmPerformanceAssessmentDO> assessmentMap = assessmentService.getPerformanceAssessmentMap(
                convertSet(stages, HrmPerformanceAssessmentStageDO::getAssessmentId));
        List<HrmPerformanceAssessmentStageDO> validStages = filterList(stages,
                stage -> assessmentMap.containsKey(stage.getAssessmentId()));

        // 2. 按任务顺序拼接可操作的绩效考核响应
        List<HrmPerformanceAssessmentRespVO> respVOList = getPerformanceAssessmentTaskRespVOList(
                convertList(validStages, stage -> assessmentMap.get(stage.getAssessmentId())),
                convertList(validStages, HrmPerformanceAssessmentStageDO::getId), userId);
        return new PageResult<>(respVOList, pageResult.getTotal());
    }

    @Override
    public List<HrmPerformanceProcessRecordRespVO> getPerformanceAssessmentProcessRecordList(
            HrmPerformanceAssessmentDO assessment) {
        return getPerformanceAssessmentProcessRecordList(assessment, null, false);
    }

    @Override
    public List<HrmPerformanceProcessRecordRespVO> getPerformanceAssessmentProcessRecordList(
            HrmPerformanceAssessmentDO assessment, Long userId) {
        return getPerformanceAssessmentProcessRecordList(assessment, userId, true);
    }

    private List<HrmPerformanceProcessRecordRespVO> getPerformanceAssessmentProcessRecordList(
            HrmPerformanceAssessmentDO assessment, Long userId, boolean filterVisibility) {
        if (assessment == null) {
            return Collections.emptyList();
        }
        // 1. 查询追加式动作记录和操作员工
        List<HrmPerformanceAssessmentActionRecordDO> actionRecords = assessmentActionRecordService
                .getPerformanceAssessmentActionRecordList(assessment.getId());
        if (filterVisibility) {
            HrmEmployeeDO currentEmployee = employeeService.getEmployeeByUserId(userId);
            Long currentEmployeeId = currentEmployee == null ? null : currentEmployee.getId();
            HrmPerformanceAssessmentStageDO viewerReviewStage = findReviewStageDOByHandler(
                    assessmentService.getPerformanceAssessmentStageList(
                            Collections.singleton(assessment.getId())), currentEmployeeId);
            if (isReviewContentRestricted(viewerReviewStage)) {
                actionRecords = filterList(actionRecords, actionRecord ->
                        ObjUtil.notEqual(actionRecord.getType(),
                                HrmPerformanceAssessmentActionTypeEnum.SCORE.getType())
                                || Objects.equals(actionRecord.getEmployeeId(), currentEmployeeId));
            }
        }
        Map<Long, HrmEmployeeDO> employeeMap = employeeService.getEmployeeMap(
                convertSet(actionRecords, HrmPerformanceAssessmentActionRecordDO::getEmployeeId));

        // 2. 拼接追加式动作并按操作时间排序
        List<HrmPerformanceProcessRecordRespVO> records = new ArrayList<>();
        addActionRecords(records, actionRecords, employeeMap);
        records.sort(Comparator.comparing(HrmPerformanceProcessRecordRespVO::getOperateTime,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return records;
    }

    @Override
    public PageResult<HrmPerformanceArchiveEmployeeRespVO> getPerformanceArchiveEmployeeRespVOPage(
            PageResult<HrmEmployeeDO> pageResult) {
        return new PageResult<>(buildArchiveEmployeeRespVOList(pageResult.getList()), pageResult.getTotal());
    }

    /**
     * 添加追加式绩效动作记录
     *
     * @param records 流程记录列表
     * @param actionRecords 动作记录列表
     * @param employeeMap 员工 Map
     */
    private void addActionRecords(
            List<HrmPerformanceProcessRecordRespVO> records,
            List<HrmPerformanceAssessmentActionRecordDO> actionRecords,
            Map<Long, HrmEmployeeDO> employeeMap) {
        for (HrmPerformanceAssessmentActionRecordDO actionRecord : actionRecords) {
            records.add(new HrmPerformanceProcessRecordRespVO()
                    .setTitle(actionRecord.getTitle())
                    .setContent(actionRecord.getContent())
                    .setSource(PROCESS_RECORD_SOURCE_ACTION).setStatus(actionRecord.getStatus())
                    .setOperatorName(getEmployeeName(employeeMap, actionRecord.getEmployeeId()))
                    .setOperateTime(actionRecord.getCreateTime()).setFileUrls(actionRecord.getFileUrls()));
        }
    }

    private String getEmployeeName(Map<Long, HrmEmployeeDO> employeeMap, Long employeeId) {
        HrmEmployeeDO employee = employeeMap.get(employeeId);
        return employee == null ? null : employee.getName();
    }

    private List<HrmPerformanceArchiveEmployeeRespVO> buildArchiveEmployeeRespVOList(
            List<HrmEmployeeDO> employees) {
        if (CollUtil.isEmpty(employees)) {
            return Collections.emptyList();
        }
        // 1.1 归档考核信息
        List<HrmPerformanceAssessmentDO> assessments =
                assessmentService.getPerformanceAssessmentListByEmployeeIdsAndStatus(
                        convertSet(employees, HrmEmployeeDO::getId),
                        HrmPerformancePlanStatusEnum.ARCHIVED.getStatus());
        Map<Long, List<HrmPerformanceAssessmentDO>> assessmentMap =
                convertMultiMap(assessments, HrmPerformanceAssessmentDO::getEmployeeId);
        // 1.2 绩效计划信息
        Map<Long, HrmPerformancePlanDO> planMap =
                planService.getPerformancePlanMap(convertSet(assessments, HrmPerformanceAssessmentDO::getPlanId));
        // 1.3 部门信息
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
                convertSet(employees, HrmEmployeeDO::getDeptId));

        // 2. 拼接响应
        return convertList(employees, employee -> {
            HrmPerformanceArchiveEmployeeRespVO respVO = BeanUtils.toBean(
                    employee, HrmPerformanceArchiveEmployeeRespVO.class);
            respVO.setEmployeeId(employee.getId()).setEmployeeName(employee.getName())
                    .setEmployeeStatus(employee.getStatus()).setEmployeeType(employee.getType());
            MapUtils.findAndThen(deptMap, employee.getDeptId(), dept -> respVO.setDeptName(dept.getName()));
            List<HrmPerformanceAssessmentDO> employeeAssessments = assessmentMap.getOrDefault(
                    employee.getId(), Collections.emptyList());
            respVO.setAssessmentCount((long) employeeAssessments.size());
            HrmPerformanceAssessmentDO latestAssessment = CollUtil.getFirst(employeeAssessments);
            if (latestAssessment != null) {
                respVO.setLatestAssessmentId(latestAssessment.getId())
                        .setLatestScore(latestAssessment.getScore())
                        .setLatestResultLevel(latestAssessment.getResultLevel());
                MapUtils.findAndThen(planMap, latestAssessment.getPlanId(),
                        plan -> respVO.setLatestPlanName(plan.getName()));
            }
            return respVO;
        });
    }

    private void buildPlanInfo(HrmPerformanceAssessmentRespVO respVO, HrmPerformancePlanDO plan) {
        if (plan == null) {
            return;
        }
        respVO.setName(plan.getName()).setCycleType(plan.getCycleType()).setCycle(plan.getCycle())
                .setStartTime(plan.getStartTime()).setEndTime(plan.getEndTime());
        if (plan.getAssessmentConfig() != null) {
            respVO.setUpperLimitScore(plan.getAssessmentConfig().getUpperLimitScore());
        }
    }

    private void buildEmployeeInfo(HrmPerformanceAssessmentRespVO respVO, HrmEmployeeDO employee,
                                   Map<Long, DeptRespDTO> deptMap) {
        if (employee == null) {
            return;
        }
        respVO.setEmployeeName(employee.getName()).setJobNumber(employee.getJobNumber())
                .setMobile(employee.getMobile()).setDeptId(employee.getDeptId())
                .setPostName(employee.getPostName()).setEmployeeType(employee.getType())
                .setEmployeeStatus(employee.getStatus());
        MapUtils.findAndThen(deptMap, employee.getDeptId(), dept -> respVO.setDeptName(dept.getName()));
    }

    private void buildStageInfo(HrmPerformanceAssessmentRespVO respVO,
                                HrmPerformanceAssessmentDO assessment,
                                List<HrmPerformanceAssessmentStageDO> assessmentStages,
                                Map<Long, List<HrmPerformanceAssessmentQuotaScoreDO>> quotaScoreMap,
                                Map<Long, HrmEmployeeDO> employeeMap) {
        List<HrmPerformanceAssessmentStageRespVO> stageRespVOs = convertList(assessmentStages, assessmentStage -> {
            HrmPerformanceAssessmentStageRespVO stageRespVO = BeanUtils.toBean(
                    assessmentStage, HrmPerformanceAssessmentStageRespVO.class);
            MapUtils.findAndThen(employeeMap, assessmentStage.getHandlerEmployeeId(),
                    handler -> stageRespVO.setHandlerName(handler.getName()));
            stageRespVO.setCanHandle(false).setCanScore(false);
            stageRespVO.setQuotaScoreList(BeanUtils.toBean(quotaScoreMap.getOrDefault(
                    assessmentStage.getId(), Collections.emptyList()),
                    HrmPerformanceAssessmentQuotaScoreRespVO.class));
            return stageRespVO;
        });
        respVO.setStages(stageRespVOs);
        respVO.setCurrentStage(CollUtil.findOne(stageRespVOs,
                stage -> Objects.equals(stage.getType(), assessment.getStageType())
                        && Objects.equals(stage.getStatus(),
                        HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus())));
        List<HrmPerformanceAssessmentStageRespVO> reviewStageRespVOs =
                filterList(stageRespVOs, stage -> HrmPerformanceStageTypeEnum.REVIEW_TYPES.contains(stage.getType()));
        respVO.setReviewStages(reviewStageRespVOs);
        respVO.setCurrentReviewStage(respVO.getCurrentStage() != null
                && HrmPerformanceStageTypeEnum.REVIEW_TYPES.contains(respVO.getCurrentStage().getType())
                ? respVO.getCurrentStage() : null);
        HrmPerformanceAssessmentStageRespVO targetStage = CollUtil.findOne(stageRespVOs,
                stage -> Objects.equals(stage.getType(),
                        HrmPerformanceStageTypeEnum.TARGET_CONFIRM.getType()));
        respVO.setTargetConfirmationEmployeeName(targetStage == null ? null : targetStage.getHandlerName());
        respVO.setCurrentHandlerName(resolveCurrentHandlerName(respVO, assessment, employeeMap));
    }

    /**
     * 标记员工绩效流程的当前处理权限
     *
     * @param respVO 绩效考核响应
     * @param employeeId 员工编号
     * @param appealRecords 待处理申诉记录
     */
    private void markStageAccess(HrmPerformanceAssessmentRespVO respVO, Long employeeId,
                                 List<HrmPerformanceAssessmentAppealRecordDO> appealRecords) {
        // 1. 标记当前用户可处理的阶段
        HrmPerformanceAssessmentStageRespVO currentStage = null;
        for (HrmPerformanceAssessmentStageRespVO stage : respVO.getStages()) {
            boolean canHandle = Objects.equals(stage.getStatus(),
                    HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus())
                    && employeeId != null
                    && Objects.equals(stage.getHandlerEmployeeId(), employeeId);
            stage.setCanHandle(canHandle).setCanScore(canHandle && HrmPerformanceStageTypeEnum.REVIEW_TYPES.contains(stage.getType()));
            if (canHandle) {
                currentStage = stage;
            }
        }
        // 2. 设置当前阶段和对应操作权限
        respVO.setCurrentStage(currentStage);
        respVO.setCurrentReviewStage(currentStage != null && HrmPerformanceStageTypeEnum.REVIEW_TYPES.contains(currentStage.getType())
                ? currentStage : null);
        respVO.setCanConfirmTarget(currentStage != null
                && Objects.equals(currentStage.getType(), HrmPerformanceStageTypeEnum.TARGET_CONFIRM.getType()));
        if (currentStage != null
                && Objects.equals(currentStage.getType(), HrmPerformanceStageTypeEnum.APPEAL_CONFIRM.getType())) {
            respVO.setAppealReviewStageIds(convertList(
                    appealRecords, HrmPerformanceAssessmentAppealRecordDO::getStageId));
        }
    }

    /**
     * 标记指定绩效任务的处理权限
     *
     * @param respVO 绩效考核响应
     * @param employeeId 员工编号
     * @param stageId 任务阶段编号
     * @param appealRecords 待处理申诉记录
     * @return 指定的任务阶段
     */
    private HrmPerformanceAssessmentStageRespVO markTaskStageAccess(
            HrmPerformanceAssessmentRespVO respVO, Long employeeId, Long stageId,
            List<HrmPerformanceAssessmentAppealRecordDO> appealRecords) {
        // 1. 标记各阶段的处理和评分权限，并定位当前任务阶段
        HrmPerformanceAssessmentStageRespVO selectedStage = null;
        for (HrmPerformanceAssessmentStageRespVO stage : respVO.getStages()) {
            boolean canHandle = Objects.equals(stage.getStatus(),
                    HrmPerformanceAssessmentStageStatusEnum.PENDING.getStatus())
                    && employeeId != null
                    && Objects.equals(stage.getHandlerEmployeeId(), employeeId);
            stage.setCanHandle(canHandle).setCanScore(canHandle && HrmPerformanceStageTypeEnum.REVIEW_TYPES.contains(stage.getType()));
            if (Objects.equals(stage.getId(), stageId)) {
                selectedStage = stage;
            }
        }
        respVO.setCurrentStage(selectedStage);

        // 2. 设置评分、目标确认和申诉处理信息
        respVO.setCurrentReviewStage(selectedStage != null
                && HrmPerformanceStageTypeEnum.REVIEW_TYPES.contains(selectedStage.getType()) ? selectedStage : null);
        respVO.setCanConfirmTarget(selectedStage != null && Boolean.TRUE.equals(selectedStage.getCanHandle())
                && Objects.equals(selectedStage.getType(), HrmPerformanceStageTypeEnum.TARGET_CONFIRM.getType()));
        if (selectedStage != null && Objects.equals(
                selectedStage.getType(), HrmPerformanceStageTypeEnum.APPEAL_CONFIRM.getType())) {
            respVO.setAppealReviewStageIds(convertList(
                    appealRecords, HrmPerformanceAssessmentAppealRecordDO::getStageId));
        }
        return selectedStage;
    }

    /**
     * 根据评分阶段的可见范围过滤其他评分结果
     *
     * @param respVO 绩效考核响应
     * @param selectedStage 评分阶段
     */
    private void filterReviewVisibility(HrmPerformanceAssessmentRespVO respVO,
                                        HrmPerformanceAssessmentStageRespVO selectedStage) {
        if (Objects.equals(selectedStage.getVisibleContent(),
                HrmPerformanceReviewVisibleContentEnum.ALL.getContent())) {
            return;
        }
        respVO.setReviewStages(Collections.singletonList(selectedStage));
        respVO.setSelfComment(null).setReviewerComment(null);
        respVO.getQuotas().forEach(quota -> quota.setSelfScore(null).setReviewerScore(null)
                .setFinalScore(null).setComment(null));
    }

    private HrmPerformanceAssessmentStageDO findReviewStageDOByHandler(
            List<HrmPerformanceAssessmentStageDO> stages, Long employeeId) {
        if (employeeId == null) {
            return null;
        }
        return CollUtil.findOne(stages, stage -> HrmPerformanceStageTypeEnum.REVIEW_TYPES.contains(stage.getType())
                && Objects.equals(stage.getHandlerEmployeeId(), employeeId));
    }

    private HrmPerformanceAssessmentStageRespVO findReviewStageRespVOByHandler(
            List<HrmPerformanceAssessmentStageRespVO> stages, Long employeeId) {
        if (employeeId == null) {
            return null;
        }
        return CollUtil.findOne(stages, stage -> HrmPerformanceStageTypeEnum.REVIEW_TYPES.contains(stage.getType())
                && Objects.equals(stage.getHandlerEmployeeId(), employeeId));
    }

    private boolean isReviewContentRestricted(HrmPerformanceAssessmentStageDO stage) {
        return stage != null && Objects.equals(stage.getVisibleContent(),
                HrmPerformanceReviewVisibleContentEnum.SELF.getContent());
    }

    private String resolveCurrentHandlerName(HrmPerformanceAssessmentRespVO respVO,
                                             HrmPerformanceAssessmentDO assessment,
                                             Map<Long, HrmEmployeeDO> employeeMap) {
        if (respVO.getCurrentStage() != null) {
            return respVO.getCurrentStage().getHandlerName();
        }
        if (ObjectUtils.notEqualsAny(assessment.getStageType(),
                HrmPerformanceStageTypeEnum.FILL_QUOTA.getType(),
                HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType())) {
            return null;
        }
        HrmEmployeeDO employee = employeeMap.get(assessment.getEmployeeId());
        return employee == null ? null : employee.getName();
    }

}
