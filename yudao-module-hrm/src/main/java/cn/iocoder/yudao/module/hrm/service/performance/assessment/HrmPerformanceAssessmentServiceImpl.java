package cn.iocoder.yudao.module.hrm.service.performance.assessment;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeListReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceArchiveEmployeePageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceArchivePageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceAssessmentPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.assessment.HrmPortalPerformanceAssessmentPageReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentAppealRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentDimensionDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentQuotaDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentQuotaScoreDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.assessment.HrmPerformanceAssessmentStageDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.plan.HrmPerformancePlanDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.assessment.HrmPerformanceAssessmentAppealRecordMapper;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.assessment.HrmPerformanceAssessmentDimensionMapper;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.assessment.HrmPerformanceAssessmentMapper;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.assessment.HrmPerformanceAssessmentQuotaMapper;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.assessment.HrmPerformanceAssessmentQuotaScoreMapper;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.assessment.HrmPerformanceAssessmentStageMapper;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAppealStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAssessmentActionTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAssessmentProcessStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceAssessmentStageStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformancePlanOperationTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformancePlanStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceQuotaSettingTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceResultAuditStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceStageTypeEnum;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import javax.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_ARCHIVE_NOT_READY;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_ASSESSMENT_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_DATA_ILLEGAL;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_INTERVIEW_NOT_READY;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_SCORING_NOT_READY;

/**
 * HRM 员工绩效考核 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmPerformanceAssessmentServiceImpl implements HrmPerformanceAssessmentService {

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
    private HrmPerformanceAssessmentAppealRecordMapper assessmentAppealRecordMapper;
    @Resource
    private HrmPerformanceAssessmentActionRecordService assessmentActionRecordService;

    @Resource
    private HrmEmployeeService employeeService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private HrmPerformanceAssessmentProcessService assessmentProcessService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private HrmPerformanceAssessmentReviewService assessmentReviewService;

    @Override
    public HrmPerformanceAssessmentDO getPerformanceAssessment(Long id) {
        return assessmentMapper.selectById(id);
    }

    @Override
    public List<HrmPerformanceAssessmentDO> getPerformanceAssessmentList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return assessmentMapper.selectByIds(ids);
    }

    @Override
    public List<HrmPerformanceAssessmentDO> getPerformanceAssessmentListByPlanId(Long planId) {
        return assessmentMapper.selectListByPlanId(planId);
    }

    @Override
    public List<HrmPerformanceAssessmentDO> getPerformanceAssessmentListByPlanIds(Collection<Long> planIds) {
        if (CollUtil.isEmpty(planIds)) {
            return Collections.emptyList();
        }
        return assessmentMapper.selectListByPlanIds(planIds);
    }

    @Override
    public HrmPerformanceAssessmentDO validatePerformanceAssessmentExists(Long id) {
        HrmPerformanceAssessmentDO assessment = assessmentMapper.selectById(id);
        if (assessment == null) {
            throw exception(PERFORMANCE_ASSESSMENT_NOT_EXISTS);
        }
        return assessment;
    }

    @Override
    public HrmPerformanceAssessmentDO validatePerformanceAssessmentByEmployeeId(Long id, Long employeeId) {
        HrmPerformanceAssessmentDO assessment = assessmentMapper.selectById(id);
        if (assessment == null || ObjUtil.notEqual(assessment.getEmployeeId(), employeeId)) {
            throw exception(PERFORMANCE_ASSESSMENT_NOT_EXISTS);
        }
        return assessment;
    }

    @Override
    public PageResult<HrmPerformanceAssessmentDO> getPerformanceAssessmentPage(
            HrmPerformanceAssessmentPageReqVO reqVO) {
        if (StringUtils.hasText(reqVO.getSearch()) || reqVO.getDeptId() != null
                || reqVO.getEmployeeType() != null || reqVO.getEmployeeStatus() != null) {
            HrmEmployeeListReqVO employeeReqVO = new HrmEmployeeListReqVO()
                    .setSearch(reqVO.getSearch()).setDeptId(reqVO.getDeptId())
                    .setType(reqVO.getEmployeeType()).setStatus(reqVO.getEmployeeStatus());
            List<HrmEmployeeDO> employees = employeeService.getEmployeeList(employeeReqVO);
            List<Long> employeeIds = convertList(employees, HrmEmployeeDO::getId);
            if (CollUtil.isEmpty(employeeIds)) {
                return PageResult.empty();
            }
            reqVO.setEmployeeIds(employeeIds);
        }
        return assessmentMapper.selectPage(reqVO);
    }

    @Override
    public PageResult<HrmPerformanceAssessmentDO> getPortalPerformanceAssessmentPage(
            HrmPortalPerformanceAssessmentPageReqVO reqVO, Long employeeId) {
        return assessmentMapper.selectPortalPage(reqVO, employeeId);
    }

    @Override
    public Map<Integer, Long> getPerformanceAssessmentStatusCountMapByEmployeeId(
            Long employeeId, String planName) {
        return assessmentMapper.selectCountMapByEmployeeIdAndPlanName(employeeId, planName);
    }

    @Override
    public PageResult<HrmPerformanceAssessmentDO> getPerformanceAssessmentArchivePage(
            HrmPerformanceArchivePageReqVO reqVO) {
        return assessmentMapper.selectPageByStatus(
                reqVO, HrmPerformancePlanStatusEnum.ARCHIVED.getStatus());
    }

    @Override
    public PageResult<HrmEmployeeDO> getPerformanceArchiveEmployeePage(
            HrmPerformanceArchiveEmployeePageReqVO reqVO) {
        return assessmentMapper.selectArchiveEmployeePage(
                reqVO, HrmPerformancePlanStatusEnum.ARCHIVED.getStatus());
    }

    @Override
    public HrmPerformanceAssessmentDO getPerformanceAssessmentArchive(Long id) {
        HrmPerformanceAssessmentDO assessment = assessmentMapper.selectById(id);
        if (assessment == null
                || ObjUtil.notEqual(assessment.getStatus(), HrmPerformancePlanStatusEnum.ARCHIVED.getStatus())) {
            return null;
        }
        return assessment;
    }

    @Override
    public List<HrmPerformanceAssessmentDO> getPerformanceAssessmentListByEmployeeIdsAndStatus(
            Collection<Long> employeeIds, Integer status) {
        if (CollUtil.isEmpty(employeeIds)) {
            return Collections.emptyList();
        }
        return assessmentMapper.selectListByEmployeeIdsAndStatus(employeeIds, status);
    }

    @Override
    public List<HrmPerformanceAssessmentDimensionDO> getPerformanceAssessmentDimensionList(
            Collection<Long> assessmentIds) {
        if (CollUtil.isEmpty(assessmentIds)) {
            return Collections.emptyList();
        }
        return assessmentDimensionMapper.selectListByAssessmentIds(assessmentIds);
    }

    @Override
    public List<HrmPerformanceAssessmentQuotaDO> getPerformanceAssessmentQuotaList(
            Collection<Long> assessmentIds) {
        if (CollUtil.isEmpty(assessmentIds)) {
            return Collections.emptyList();
        }
        return assessmentQuotaMapper.selectListByAssessmentIds(assessmentIds);
    }

    @Override
    public List<HrmPerformanceAssessmentStageDO> getPerformanceAssessmentStageList(
            Collection<Long> assessmentIds) {
        if (CollUtil.isEmpty(assessmentIds)) {
            return Collections.emptyList();
        }
        return assessmentStageMapper.selectListByAssessmentIds(assessmentIds);
    }

    @Override
    public List<HrmPerformanceAssessmentQuotaScoreDO> getPerformanceAssessmentQuotaScoreList(
            Collection<Long> assessmentStageIds) {
        if (CollUtil.isEmpty(assessmentStageIds)) {
            return Collections.emptyList();
        }
        return assessmentQuotaScoreMapper.selectListByAssessmentStageIds(assessmentStageIds);
    }

    @Override
    public List<HrmPerformanceAssessmentAppealRecordDO> getPerformanceAssessmentAppealRecordList(
            Collection<Long> assessmentIds, Integer status) {
        if (CollUtil.isEmpty(assessmentIds)) {
            return Collections.emptyList();
        }
        return assessmentAppealRecordMapper.selectListByAssessmentIdsAndStatus(assessmentIds, status);
    }

    @Override
    public List<HrmPerformanceAssessmentDO> getPerformanceAssessmentListByStatus(Integer status) {
        return assessmentMapper.selectListByStatus(status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePerformanceArchiveRecords(Collection<Long> archiveIds) {
        if (CollUtil.isEmpty(archiveIds)) {
            return;
        }
        // 1. 只查询已归档的员工考核
        List<HrmPerformanceAssessmentDO> assessments = assessmentMapper.selectListByIdsAndStatus(
                archiveIds, HrmPerformancePlanStatusEnum.ARCHIVED.getStatus());
        // 2. 删除员工考核及其关联数据
        deleteAssessmentData(convertSet(assessments, HrmPerformanceAssessmentDO::getId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePerformanceArchiveRecordsByEmployeeIds(Collection<Long> employeeIds) {
        if (CollUtil.isEmpty(employeeIds)) {
            return;
        }
        // 1. 查询指定员工的已归档员工考核
        List<HrmPerformanceAssessmentDO> assessments = assessmentMapper.selectListByEmployeeIdsAndStatus(
                employeeIds, HrmPerformancePlanStatusEnum.ARCHIVED.getStatus());
        // 2. 删除员工考核及其关联数据
        deleteAssessmentData(convertSet(assessments, HrmPerformanceAssessmentDO::getId));
    }

    @Override
    public Map<Long, BigDecimal> getPerformanceArchiveEmployeeCoefficientMap(
            Collection<Long> planIds, Collection<Long> employeeIds) {
        if (CollUtil.isEmpty(planIds) || CollUtil.isEmpty(employeeIds)) {
            return Collections.emptyMap();
        }
        List<HrmPerformanceAssessmentDO> assessments =
                assessmentMapper.selectListByPlanIdsAndEmployeeIdsAndStatus(
                        planIds, employeeIds, HrmPerformancePlanStatusEnum.ARCHIVED.getStatus());
        Map<Long, BigDecimal> coefficientMap = new LinkedHashMap<>();
        for (HrmPerformanceAssessmentDO assessment : assessments) {
            coefficientMap.putIfAbsent(assessment.getEmployeeId(),
                    ObjUtil.defaultIfNull(assessment.getCoefficient(), BigDecimal.ONE));
        }
        return coefficientMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPerformanceAssessmentList(HrmPerformancePlanDO plan, Collection<Long> employeeIds) {
        if (CollUtil.isEmpty(employeeIds)) {
            return;
        }
        // 1. 查询已经参与计划的员工
        List<HrmPerformanceAssessmentDO> existingAssessments =
                assessmentMapper.selectListByPlanId(plan.getId());
        Set<Long> existingEmployeeIds = convertSet(
                existingAssessments, HrmPerformanceAssessmentDO::getEmployeeId);
        // 2. 为新增员工创建考核
        for (Long employeeId : employeeIds) {
            if (!existingEmployeeIds.add(employeeId)) {
                continue;
            }
            HrmPerformanceAssessmentDO assessment = new HrmPerformanceAssessmentDO()
                    .setPlanId(plan.getId()).setEmployeeId(employeeId)
                    .setStatus(plan.getStatus())
                    .setProcessStatus(HrmPerformanceAssessmentProcessStatusEnum.PROCESSING.getStatus())
                    .setStageType(plan.getStageType())
                    .setStageSort(0).setScore(BigDecimal.ZERO).setCoefficient(BigDecimal.ONE)
                    .setAppealStatus(HrmPerformanceAppealStatusEnum.NONE.getStatus());
            assessmentMapper.insert(assessment);

            // 3. 运行中的计划需要同步初始化指标
            if (Objects.equals(plan.getStatus(), HrmPerformancePlanStatusEnum.RUNNING.getStatus())) {
                assessmentReviewService.ensureAssessmentQuotaList(plan, assessment);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncPerformanceAssessmentList(HrmPerformancePlanDO plan, Collection<Long> employeeIds) {
        // 1. 删除不再参与计划的员工考核
        Set<Long> addedEmployeeIds = CollUtil.isEmpty(employeeIds)
                ? new LinkedHashSet<>() : new LinkedHashSet<>(employeeIds);
        List<Long> deletedAssessmentIds = new ArrayList<>();
        List<HrmPerformanceAssessmentDO> assessments = assessmentMapper.selectListByPlanId(plan.getId());
        for (HrmPerformanceAssessmentDO assessment : assessments) {
            if (addedEmployeeIds.remove(assessment.getEmployeeId())) {
                continue;
            }
            deletedAssessmentIds.add(assessment.getId());
        }
        deleteAssessmentData(deletedAssessmentIds);

        // 2. 添加新参与计划的员工考核
        addPerformanceAssessmentList(plan, addedEmployeeIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePerformanceAssessmentList(Long planId, Collection<Long> employeeIds) {
        if (CollUtil.isEmpty(employeeIds)) {
            return;
        }
        // 1. 查询指定员工的考核
        Set<Long> employeeIdSet = new LinkedHashSet<>(employeeIds);
        List<HrmPerformanceAssessmentDO> assessments = assessmentMapper.selectListByPlanId(planId);
        assessments = filterList(assessments,
                assessment -> employeeIdSet.contains(assessment.getEmployeeId()));

        // 2. 删除员工考核及其关联数据
        List<Long> assessmentIds = convertList(assessments, HrmPerformanceAssessmentDO::getId);
        deleteAssessmentData(assessmentIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePerformanceAssessmentListByPlanId(Long planId) {
        // 1. 查询计划的员工考核
        List<HrmPerformanceAssessmentDO> assessments = assessmentMapper.selectListByPlanId(planId);

        // 2. 删除员工考核及其关联数据
        List<Long> assessmentIds = convertList(assessments, HrmPerformanceAssessmentDO::getId);
        deleteAssessmentData(assessmentIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void terminatePerformanceAssessmentListByPlanId(Long planId, Long operatorEmployeeId) {
        // 1. 查询绩效计划的员工考核
        List<HrmPerformanceAssessmentDO> assessments = assessmentMapper.selectListByPlanId(planId);

        // 2. 终止员工考核并保留流程快照
        for (HrmPerformanceAssessmentDO assessment : assessments) {
            assessmentMapper.updateById(new HrmPerformanceAssessmentDO().setId(assessment.getId())
                    .setStatus(HrmPerformancePlanStatusEnum.TERMINATED.getStatus())
                    .setProcessStatus(HrmPerformanceAssessmentProcessStatusEnum.FINISHED.getStatus()));
            assessmentActionRecordService.createPerformanceAssessmentActionRecord(
                    operatorEmployeeId, assessment.getId(), null,
                    HrmPerformanceAssessmentActionTypeEnum.TERMINATE, null, null);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer startPerformanceAssessmentList(HrmPerformancePlanDO plan) {
        // 1. 校验计划存在员工考核
        List<HrmPerformanceAssessmentDO> assessments = assessmentMapper.selectListByPlanId(plan.getId());
        if (CollUtil.isEmpty(assessments)) {
            throw exception(PERFORMANCE_DATA_ILLEGAL);
        }
        Integer initialStage = Objects.equals(plan.getQuotaSettingType(), HrmPerformanceQuotaSettingTypeEnum.EMPLOYEE.getType())
                ? HrmPerformanceStageTypeEnum.FILL_QUOTA.getType() : HrmPerformanceStageTypeEnum.EXECUTING.getType();

        // 2. 初始化每个员工考核的指标和流程阶段
        for (HrmPerformanceAssessmentDO assessment : assessments) {
            assessmentReviewService.ensureAssessmentQuotaList(plan, assessment);
            assessmentProcessService.initializeAssessmentStages(plan, assessment);
            HrmPerformanceAssessmentDO updateObj = new HrmPerformanceAssessmentDO().setId(assessment.getId())
                    .setStatus(HrmPerformancePlanStatusEnum.RUNNING.getStatus())
                    .setProcessStatus(HrmPerformanceAssessmentProcessStatusEnum.PROCESSING.getStatus())
                    .setStageType(initialStage).setStageSort(0);
            if (Objects.equals(initialStage, HrmPerformanceStageTypeEnum.FILL_QUOTA.getType())) {
                updateObj.setStageSort(assessmentProcessService.activateAssessmentStage(assessment,
                        HrmPerformanceStageTypeEnum.FILL_QUOTA.getType()).getSort());
            }
            assessmentMapper.updateById(updateObj);
        }
        return initialStage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer openPerformanceAssessmentScoring(HrmPerformancePlanDO plan) {
        // 1. 校验全部员工考核可以开启评分
        List<HrmPerformanceAssessmentDO> assessments = assessmentMapper.selectListByPlanId(plan.getId());
        if (!isPerformanceAssessmentScoringReady(plan, assessments)) {
            throw exception(PERFORMANCE_SCORING_NOT_READY);
        }

        // 2. 初始化指标和阶段，并激活首个评分阶段
        Integer planStageType = null;
        for (HrmPerformanceAssessmentDO assessment : assessments) {
            assessmentReviewService.ensureAssessmentQuotaList(plan, assessment);
            assessmentProcessService.initializeAssessmentStages(plan, assessment);
            HrmPerformanceAssessmentStageDO firstReviewStage = assessmentProcessService.activateFirstReviewStage(assessment);
            HrmPerformanceAssessmentDO updateObj = new HrmPerformanceAssessmentDO().setId(assessment.getId())
                    .setStatus(HrmPerformancePlanStatusEnum.RUNNING.getStatus())
                    .setStageType(firstReviewStage.getType()).setStageSort(firstReviewStage.getSort());
            assessmentMapper.updateById(updateObj);
            if (planStageType == null) {
                planStageType = firstReviewStage.getType();
            }
        }
        return planStageType;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer startPerformanceAssessmentInterview(HrmPerformancePlanDO plan) {
        // 1. 校验全部员工考核可以发起面谈
        List<HrmPerformanceAssessmentDO> assessments = assessmentMapper.selectListByPlanId(plan.getId());
        List<HrmPerformanceAssessmentStageDO> stages = assessmentStageMapper.selectListByAssessmentIds(
                convertSet(assessments, HrmPerformanceAssessmentDO::getId));
        if (!isPerformanceAssessmentInterviewReady(plan, assessments, stages)) {
            throw exception(PERFORMANCE_INTERVIEW_NOT_READY);
        }

        // 2. 需要员工确认结果时，激活结果确认阶段
        boolean resultConfirmationEnabled = Boolean.TRUE.equals(plan.getResultConfirmation());
        Integer nextStage = resultConfirmationEnabled
                ? HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType()
                : HrmPerformanceStageTypeEnum.END.getType();
        if (resultConfirmationEnabled) {
            for (HrmPerformanceAssessmentDO assessment : assessments) {
                HrmPerformanceAssessmentStageDO assessmentStage =
                        assessmentProcessService.activateAssessmentStage(
                                assessment, HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType());
                HrmPerformanceAssessmentDO updateObj = new HrmPerformanceAssessmentDO().setId(assessment.getId())
                        .setProcessStatus(HrmPerformanceAssessmentProcessStatusEnum.PROCESSING.getStatus())
                        .setStageType(nextStage).setStageSort(assessmentStage.getSort());
                assessmentMapper.updateById(updateObj);
            }
        }
        return nextStage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archivePerformanceAssessmentList(HrmPerformancePlanDO plan) {
        // 1. 校验全部员工考核可以归档
        List<HrmPerformanceAssessmentDO> assessments = assessmentMapper.selectListByPlanId(plan.getId());
        if (!isPerformanceAssessmentArchiveReady(plan, assessments)) {
            throw exception(PERFORMANCE_ARCHIVE_NOT_READY);
        }

        // 2. 归档全部员工考核
        LocalDateTime archiveTime = LocalDateTime.now();
        for (HrmPerformanceAssessmentDO assessment : assessments) {
            HrmPerformanceAssessmentDO updateObj = new HrmPerformanceAssessmentDO().setId(assessment.getId())
                    .setStatus(HrmPerformancePlanStatusEnum.ARCHIVED.getStatus())
                    .setProcessStatus(HrmPerformanceAssessmentProcessStatusEnum.FINISHED.getStatus())
                    .setStageType(HrmPerformanceStageTypeEnum.ARCHIVED.getType())
                    .setArchiveTime(archiveTime);
            assessmentMapper.updateById(updateObj);
        }
    }

    private void deleteAssessmentData(Collection<Long> assessmentIds) {
        if (CollUtil.isEmpty(assessmentIds)) {
            return;
        }
        // 1. 删除阶段的指标评分
        List<HrmPerformanceAssessmentStageDO> stages =
                assessmentStageMapper.selectListByAssessmentIds(assessmentIds);
        Set<Long> stageIds = convertSet(stages, HrmPerformanceAssessmentStageDO::getId);
        if (CollUtil.isNotEmpty(stageIds)) {
            assessmentQuotaScoreMapper.deleteByAssessmentStageIds(stageIds);
        }

        // 2. 删除员工考核及其关联数据
        assessmentActionRecordService.deletePerformanceAssessmentActionRecordList(assessmentIds);
        assessmentAppealRecordMapper.deleteByAssessmentIds(assessmentIds);
        assessmentStageMapper.deleteByAssessmentIds(assessmentIds);
        assessmentQuotaMapper.deleteByAssessmentIds(assessmentIds);
        assessmentDimensionMapper.deleteByAssessmentIds(assessmentIds);
        assessmentMapper.deleteByIds(assessmentIds);
    }

    @Override
    public boolean isPerformanceAssessmentScoringReady(
            HrmPerformancePlanDO plan, List<HrmPerformanceAssessmentDO> assessments) {
        return Objects.equals(plan.getStatus(), HrmPerformancePlanStatusEnum.RUNNING.getStatus())
                && CollUtil.isNotEmpty(assessments)
                && assessments.stream().allMatch(assessment -> Objects.equals(
                        assessment.getStageType(), HrmPerformanceStageTypeEnum.EXECUTING.getType()));
    }

    @Override
    public boolean isPerformanceAssessmentInterviewReady(
            HrmPerformancePlanDO plan, List<HrmPerformanceAssessmentDO> assessments,
            List<HrmPerformanceAssessmentStageDO> stages) {
        // 1. 只有运行中且存在员工考核的计划，才允许发起绩效面谈
        if (ObjUtil.notEqual(plan.getStatus(), HrmPerformancePlanStatusEnum.RUNNING.getStatus())
                || CollUtil.isEmpty(assessments)) {
            return false;
        }
        // 2. 未启用结果确认时，全部员工考核进入结束阶段后即可发起面谈
        if (Boolean.FALSE.equals(plan.getResultConfirmation())) {
            return Objects.equals(plan.getOperationType(), HrmPerformancePlanOperationTypeEnum.START_INTERVIEW.getType())
                    && assessments.stream().allMatch(assessment -> Objects.equals(
                            assessment.getStageType(), HrmPerformanceStageTypeEnum.END.getType()));
        }
        // 3. 启用结果确认时，全部员工考核必须进入结果确认阶段，且启用的结果审核均已通过
        boolean resultAuditFinished = assessments.stream().allMatch(
                assessment -> Objects.equals(assessment.getStageType(), HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType())
                && (Boolean.FALSE.equals(plan.getResultAudit())
                        || Objects.equals(assessment.getResultAuditStatus(), HrmPerformanceResultAuditStatusEnum.PASS.getStatus())));
        if (!resultAuditFinished) {
            return false;
        }
        // 4. 每个员工考核都必须存在未处理的结果确认节点，避免遗漏或重复发起面谈
        Set<Long> assessmentIds = convertSet(assessments, HrmPerformanceAssessmentDO::getId);
        stages = filterList(stages, stage -> Objects.equals(stage.getType(),
                HrmPerformanceStageTypeEnum.RESULT_CONFIRM.getType())
                && Objects.equals(stage.getStatus(),
                HrmPerformanceAssessmentStageStatusEnum.NOT_PROCESSED.getStatus()));
        Set<Long> waitingAssessmentIds =
                convertSet(stages, HrmPerformanceAssessmentStageDO::getAssessmentId);
        return waitingAssessmentIds.containsAll(assessmentIds);
    }

    @Override
    public boolean isPerformanceAssessmentArchiveReady(
            HrmPerformancePlanDO plan, List<HrmPerformanceAssessmentDO> assessments) {
        return Objects.equals(plan.getStatus(), HrmPerformancePlanStatusEnum.RUNNING.getStatus())
                && CollUtil.isNotEmpty(assessments)
                && Objects.equals(plan.getOperationType(), HrmPerformancePlanOperationTypeEnum.ARCHIVE.getType())
                && assessments.stream().allMatch(assessment -> Objects.equals(
                        assessment.getStageType(), HrmPerformanceStageTypeEnum.END.getType()));
    }

}
