package cn.iocoder.yudao.module.hrm.service.performance.plan;

import cn.iocoder.yudao.module.hrm.service.performance.assessment.HrmPerformanceAssessmentService;
import cn.iocoder.yudao.module.hrm.service.performance.config.HrmPerformanceAssessmentTemplateService;
import cn.iocoder.yudao.module.hrm.service.performance.config.HrmPerformanceResultTemplateService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeListReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment.HrmPerformanceAssessmentBatchReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.plan.HrmPerformancePlanPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.plan.HrmPerformancePlanSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.config.HrmPerformanceAssessmentTemplateDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.plan.HrmPerformancePlanDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.plan.HrmPerformancePlanDO.Scope;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.config.HrmPerformanceResultTemplateDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.performance.plan.HrmPerformancePlanMapper;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformancePlanOperationTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformancePlanScopeTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformancePlanStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceRaterTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.plan.HrmPerformanceStageTypeEnum;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.notEqualsAny;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_PLAN_NAME_DUPLICATE;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_PLAN_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_PLAN_STARTED_CANNOT_MODIFY;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_PLAN_STATUS_NOT_ALLOW_DELETE;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_DATA_ILLEGAL;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.PERFORMANCE_STAGE_ACTION_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_PLAN_ARCHIVE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_PLAN_ARCHIVE_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_PLAN_CREATE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_PLAN_CREATE_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_PLAN_DELETE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_PLAN_DELETE_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_PLAN_INTERVIEW_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_PLAN_INTERVIEW_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_PLAN_OPEN_SCORING_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_PLAN_OPEN_SCORING_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_PLAN_START_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_PLAN_START_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_PLAN_TERMINATE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_PLAN_TERMINATE_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_PLAN_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_PLAN_UPDATE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_PERFORMANCE_PLAN_UPDATE_SUCCESS;

/**
 * HRM 绩效计划 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmPerformancePlanServiceImpl implements HrmPerformancePlanService {

    @Resource
    private HrmPerformancePlanMapper performancePlanMapper;

    @Resource
    private HrmPerformanceAssessmentService performanceAssessmentService;
    @Resource
    private HrmPerformanceAssessmentTemplateService performanceAssessmentTemplateService;
    @Resource
    private HrmPerformanceResultTemplateService performanceResultTemplateService;
    @Resource
    private HrmEmployeeService employeeService;

    @Resource
    private DeptApi deptApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_PERFORMANCE_PLAN_TYPE, subType = HRM_PERFORMANCE_PLAN_CREATE_SUB_TYPE,
            bizNo = "{{#performancePlan.id}}", success = HRM_PERFORMANCE_PLAN_CREATE_SUCCESS)
    public Long createPerformancePlan(HrmPerformancePlanSaveReqVO reqVO) {
        // 1. 校验计划配置
        validateNameUnique(null, reqVO.getName());
        HrmPerformanceAssessmentTemplateDO assessmentTemplate = performanceAssessmentTemplateService
                .validatePerformanceAssessmentTemplateEnabled(reqVO.getAssessmentTemplateId());
        HrmPerformanceResultTemplateDO resultTemplate =
                performanceResultTemplateService.validatePerformanceResultTemplateEnabled(reqVO.getResultTemplateId());
        validatePlanReferences(reqVO);
        validatePlanScoreRange(assessmentTemplate);

        // 2. 创建绩效计划
        HrmPerformancePlanDO plan = BeanUtils.toBean(reqVO, HrmPerformancePlanDO.class)
                .setAssessmentConfig(BeanUtils.toBean(assessmentTemplate,
                        HrmPerformanceAssessmentTemplateDO.AssessmentConfig.class))
                .setResultConfig(BeanUtils.toBean(resultTemplate, HrmPerformancePlanDO.ResultConfig.class))
                .setStatus(HrmPerformancePlanStatusEnum.NOT_STARTED.getStatus())
                .setStageType(HrmPerformanceStageTypeEnum.NOT_STARTED.getType())
                .setOperationType(null);
        if (!Boolean.TRUE.equals(plan.getSyncToSalary())) {
            plan.setPaidForMonth(null);
        }
        performancePlanMapper.insert(plan);

        // 3. 创建员工绩效考核
        performanceAssessmentService.addPerformanceAssessmentList(plan, resolvePlanScopeEmployeeIds(plan));

        // 4. 记录操作日志上下文
        LogRecordContext.putVariable("performancePlan", plan);
        return plan.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_PERFORMANCE_PLAN_TYPE, subType = HRM_PERFORMANCE_PLAN_UPDATE_SUB_TYPE,
            bizNo = "{{#performancePlan.id}}", success = HRM_PERFORMANCE_PLAN_UPDATE_SUCCESS)
    public void updatePerformancePlan(HrmPerformancePlanSaveReqVO reqVO) {
        // 1. 校验计划配置
        HrmPerformancePlanDO oldPlan = performancePlanMapper.selectOneForUpdate(
                HrmPerformancePlanDO::getId, reqVO.getId());
        validatePlanEditable(oldPlan);
        validateNameUnique(reqVO.getId(), reqVO.getName());
        HrmPerformanceAssessmentTemplateDO assessmentTemplate;
        if (Objects.equals(oldPlan.getAssessmentTemplateId(), reqVO.getAssessmentTemplateId())) {
            assessmentTemplate = performanceAssessmentTemplateService
                    .validatePerformanceAssessmentTemplateExists(reqVO.getAssessmentTemplateId());
        } else {
            assessmentTemplate = performanceAssessmentTemplateService
                    .validatePerformanceAssessmentTemplateEnabled(reqVO.getAssessmentTemplateId());
        }
        HrmPerformanceResultTemplateDO resultTemplate;
        if (Objects.equals(oldPlan.getResultTemplateId(), reqVO.getResultTemplateId())) {
            resultTemplate = performanceResultTemplateService
                    .validatePerformanceResultTemplateExists(reqVO.getResultTemplateId());
        } else {
            resultTemplate = performanceResultTemplateService
                    .validatePerformanceResultTemplateEnabled(reqVO.getResultTemplateId());
        }
        validatePlanReferences(reqVO);
        validatePlanScoreRange(assessmentTemplate);

        // 2. 更新绩效计划，并保留内部维护的排除员工范围
        HrmPerformancePlanDO plan = BeanUtils.toBean(reqVO, HrmPerformancePlanDO.class)
                .setAssessmentConfig(BeanUtils.toBean(assessmentTemplate,
                        HrmPerformanceAssessmentTemplateDO.AssessmentConfig.class))
                .setResultConfig(BeanUtils.toBean(resultTemplate, HrmPerformancePlanDO.ResultConfig.class))
                .setStatus(oldPlan.getStatus()).setStageType(oldPlan.getStageType())
                .setOperationType(oldPlan.getOperationType());
        if (!Boolean.TRUE.equals(plan.getSyncToSalary())) {
            plan.setPaidForMonth(null);
        }
        // 排除员工范围不由计划表单提交，需要从原计划合并到本次更新数据
        List<Scope> scopes = new ArrayList<>(plan.getScopes());
        scopes.addAll(filterList(oldPlan.getScopes(), scope -> Objects.equals(scope.getType(),
                HrmPerformancePlanScopeTypeEnum.EXCLUDED_EMPLOYEE.getType())));
        plan.setScopes(scopes);
        performancePlanMapper.updateById(plan);

        // 3. 同步员工绩效考核
        performanceAssessmentService.syncPerformanceAssessmentList(plan, resolvePlanScopeEmployeeIds(plan));

        // 4. 记录操作日志上下文
        LogRecordContext.putVariable("performancePlan", plan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_PERFORMANCE_PLAN_TYPE, subType = HRM_PERFORMANCE_PLAN_DELETE_SUB_TYPE,
            bizNo = "{{#performancePlan.id}}", success = HRM_PERFORMANCE_PLAN_DELETE_SUCCESS)
    public void deletePerformancePlan(Long id) {
        // 1. 校验计划可以删除
        HrmPerformancePlanDO plan = validatePerformancePlanExists(id);
        // 仅未开始或已归档的计划允许删除
        if (notEqualsAny(plan.getStatus(),
                HrmPerformancePlanStatusEnum.DRAFT.getStatus(),
                HrmPerformancePlanStatusEnum.NOT_STARTED.getStatus(),
                HrmPerformancePlanStatusEnum.ARCHIVED.getStatus())) {
            throw exception(PERFORMANCE_PLAN_STATUS_NOT_ALLOW_DELETE);
        }

        // 2. 删除员工绩效考核和计划
        performanceAssessmentService.deletePerformanceAssessmentListByPlanId(id);
        performancePlanMapper.deleteById(id);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("performancePlan", plan);
    }

    @Override
    public HrmPerformancePlanDO getPerformancePlan(Long id) {
        return performancePlanMapper.selectById(id);
    }

    @Override
    public PageResult<HrmPerformancePlanDO> getPerformancePlanPage(HrmPerformancePlanPageReqVO reqVO) {
        return performancePlanMapper.selectPage(reqVO);
    }

    @Override
    public List<HrmPerformancePlanDO> getPerformancePlanList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return performancePlanMapper.selectByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPerformancePlanEmployees(HrmPerformanceAssessmentBatchReqVO reqVO) {
        // 1. 校验计划和员工
        HrmPerformancePlanDO plan = performancePlanMapper.selectOneForUpdate(
                HrmPerformancePlanDO::getId, reqVO.getPlanId());
        validatePlanEditable(plan);
        employeeService.validateEmployeeListExists(reqVO.getEmployeeIds());

        // 2. 将手工加入员工写入普通员工范围，并从排除范围移除
        updatePlanEmployeeScope(plan, reqVO.getEmployeeIds(), true);

        // 3. 添加员工绩效考核
        performanceAssessmentService.addPerformanceAssessmentList(plan, reqVO.getEmployeeIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removePerformancePlanEmployees(HrmPerformanceAssessmentBatchReqVO reqVO) {
        // 1. 校验计划和员工
        HrmPerformancePlanDO plan = performancePlanMapper.selectOneForUpdate(
                HrmPerformancePlanDO::getId, reqVO.getPlanId());
        validatePlanEditable(plan);
        if (CollUtil.isEmpty(reqVO.getEmployeeIds())) {
            return;
        }
        employeeService.validateEmployeeListExists(reqVO.getEmployeeIds());

        // 2. 从普通员工范围移除，并记录排除员工
        updatePlanEmployeeScope(plan, reqVO.getEmployeeIds(), false);

        // 3. 删除员工绩效考核
        performanceAssessmentService.deletePerformanceAssessmentList(plan.getId(), reqVO.getEmployeeIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_PERFORMANCE_PLAN_TYPE, subType = HRM_PERFORMANCE_PLAN_START_SUB_TYPE,
            bizNo = "{{#performancePlan.id}}", success = HRM_PERFORMANCE_PLAN_START_SUCCESS)
    public void startPerformancePlan(Long id) {
        // 1. 校验计划并补齐参评员工
        HrmPerformancePlanDO plan = performancePlanMapper.selectOneForUpdate(
                HrmPerformancePlanDO::getId, id);
        validatePlanEditable(plan);
        performanceAssessmentService.addPerformanceAssessmentList(plan, resolvePlanScopeEmployeeIds(plan));

        // 2. 启动员工绩效考核
        Integer initialStage = performanceAssessmentService.startPerformanceAssessmentList(plan);

        // 3. 更新计划状态
        performancePlanMapper.updateById(new HrmPerformancePlanDO().setId(id)
                .setStatus(HrmPerformancePlanStatusEnum.RUNNING.getStatus()));
        performancePlanMapper.updateStageTypeAndOperationTypeById(id, initialStage,
                Objects.equals(initialStage, HrmPerformanceStageTypeEnum.EXECUTING.getType())
                        ? HrmPerformancePlanOperationTypeEnum.START_SCORING.getType() : null);

        // 4. 记录操作日志上下文
        LogRecordContext.putVariable("performancePlan", plan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_PERFORMANCE_PLAN_TYPE, subType = HRM_PERFORMANCE_PLAN_OPEN_SCORING_SUB_TYPE,
            bizNo = "{{#performancePlan.id}}", success = HRM_PERFORMANCE_PLAN_OPEN_SCORING_SUCCESS)
    public void openPerformancePlanScoring(Long id) {
        // 1. 开启员工绩效评分
        HrmPerformancePlanDO plan = validatePerformancePlanExists(id);
        Integer stageType = performanceAssessmentService.openPerformanceAssessmentScoring(plan);

        // 2. 更新计划阶段
        performancePlanMapper.updateStageTypeAndOperationTypeById(id, stageType, null);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("performancePlan", plan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_PERFORMANCE_PLAN_TYPE, subType = HRM_PERFORMANCE_PLAN_INTERVIEW_SUB_TYPE,
            bizNo = "{{#performancePlan.id}}", success = HRM_PERFORMANCE_PLAN_INTERVIEW_SUCCESS)
    public void startPerformancePlanInterview(Long id) {
        // 1. 发起员工绩效面谈
        HrmPerformancePlanDO plan = validatePerformancePlanExists(id);
        Integer nextStage = performanceAssessmentService.startPerformanceAssessmentInterview(plan);

        // 2. 更新计划阶段
        performancePlanMapper.updateStageTypeAndOperationTypeById(id, nextStage,
                Boolean.TRUE.equals(plan.getResultConfirmation())
                        ? null : HrmPerformancePlanOperationTypeEnum.ARCHIVE.getType());

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("performancePlan", plan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_PERFORMANCE_PLAN_TYPE, subType = HRM_PERFORMANCE_PLAN_ARCHIVE_SUB_TYPE,
            bizNo = "{{#performancePlan.id}}", success = HRM_PERFORMANCE_PLAN_ARCHIVE_SUCCESS)
    public void archivePerformancePlan(Long id) {
        // 1. 归档员工绩效考核
        HrmPerformancePlanDO plan = validatePerformancePlanExists(id);
        performanceAssessmentService.archivePerformanceAssessmentList(plan);

        // 2. 归档绩效计划
        performancePlanMapper.updateById(new HrmPerformancePlanDO().setId(id)
                .setStatus(HrmPerformancePlanStatusEnum.ARCHIVED.getStatus())
                .setStageType(HrmPerformanceStageTypeEnum.ARCHIVED.getType()));
        performancePlanMapper.updateStageTypeAndOperationTypeById(
                id, HrmPerformanceStageTypeEnum.ARCHIVED.getType(), null);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("performancePlan", plan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_PERFORMANCE_PLAN_TYPE, subType = HRM_PERFORMANCE_PLAN_TERMINATE_SUB_TYPE,
            bizNo = "{{#performancePlan.id}}", success = HRM_PERFORMANCE_PLAN_TERMINATE_SUCCESS)
    public void terminatePerformancePlan(Long userId, Long id) {
        // 1. 校验计划运行中
        HrmPerformancePlanDO plan = validatePerformancePlanExists(id);
        // 只有运行中的计划允许终止
        if (ObjUtil.notEqual(plan.getStatus(), HrmPerformancePlanStatusEnum.RUNNING.getStatus())) {
            throw exception(PERFORMANCE_STAGE_ACTION_INVALID);
        }

        // 2. 终止员工绩效考核和计划，并保留流程历史
        HrmEmployeeDO operator = employeeService.getEmployeeByUserId(userId);
        performanceAssessmentService.terminatePerformanceAssessmentListByPlanId(
                id, operator == null ? null : operator.getId());
        performancePlanMapper.updateById(new HrmPerformancePlanDO().setId(id)
                .setStatus(HrmPerformancePlanStatusEnum.TERMINATED.getStatus())
                .setOperationType(null).setTerminateTime(LocalDateTime.now()));

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("performancePlan", plan);
    }

    @Override
    public Map<Integer, Long> getPerformancePlanStatusCount(HrmPerformancePlanPageReqVO reqVO) {
        return performancePlanMapper.selectCountMapByStatus(reqVO);
    }

    @Override
    public long getPerformancePlanCountByAssessmentTemplateId(Long assessmentTemplateId) {
        return performancePlanMapper.selectCount(
                HrmPerformancePlanDO::getAssessmentTemplateId, assessmentTemplateId);
    }

    @Override
    public long getPerformancePlanCountByResultTemplateId(Long resultTemplateId) {
        return performancePlanMapper.selectCount(
                HrmPerformancePlanDO::getResultTemplateId, resultTemplateId);
    }

    @Override
    public List<HrmPerformancePlanDO> getPerformancePlanListByPaidForMonth(String paidForMonth) {
        return performancePlanMapper.selectListByPaidForMonth(paidForMonth);
    }

    @Override
    public HrmPerformancePlanDO validatePerformancePlanExists(Long id) {
        HrmPerformancePlanDO plan = performancePlanMapper.selectById(id);
        if (plan == null) {
            throw exception(PERFORMANCE_PLAN_NOT_EXISTS);
        }
        return plan;
    }

    @Override
    public void updatePerformancePlanStageTypeAndOperationType(
            Long id, Integer stageType, Integer operationType) {
        performancePlanMapper.updateStageTypeAndOperationTypeById(id, stageType, operationType);
    }

    /**
     * 根据计划考评范围解析参评员工
     *
     * @param plan 绩效计划
     * @return 参评员工编号列表
     */
    private List<Long> resolvePlanScopeEmployeeIds(HrmPerformancePlanDO plan) {
        Set<Long> directEmployeeIds = new LinkedHashSet<>();
        Set<Long> employmentEmployeeIds = new LinkedHashSet<>();
        Set<Long> excludedEmployeeIds = new HashSet<>();
        boolean directScopeConfigured = false; // 是否配置员工或部门范围
        boolean employmentScopeConfigured = false; // 是否配置聘用形式范围
        for (Scope scope : plan.getScopes()) {
            if (Objects.equals(scope.getType(),
                    HrmPerformancePlanScopeTypeEnum.EXCLUDED_EMPLOYEE.getType())) {
                if (CollUtil.isNotEmpty(scope.getEmployeeIds())) {
                    excludedEmployeeIds.addAll(scope.getEmployeeIds());
                }
                continue;
            }
            if (Objects.equals(scope.getType(),
                    HrmPerformancePlanScopeTypeEnum.EMPLOYEE_DEPT.getType())) {
                directScopeConfigured = true;
                if (CollUtil.isNotEmpty(scope.getEmployeeIds())) {
                    directEmployeeIds.addAll(scope.getEmployeeIds());
                }
                if (CollUtil.isNotEmpty(scope.getDeptIds())) {
                    // 部门范围按当前部门员工展开
                    List<HrmEmployeeDO> employees = employeeService.getEmployeeListByDeptIds(scope.getDeptIds());
                    directEmployeeIds.addAll(convertSet(employees, HrmEmployeeDO::getId));
                }
                continue;
            }
            employmentScopeConfigured = true;
            for (Integer status : scope.getEmployeeStatuses()) {
                // 聘用形式范围按每个员工状态分别查询
                HrmEmployeeListReqVO employeeReqVO = new HrmEmployeeListReqVO()
                        .setType(scope.getEmployeeType()).setStatus(status);
                List<HrmEmployeeDO> employees = employeeService.getEmployeeList(employeeReqVO);
                employmentEmployeeIds.addAll(convertSet(employees, HrmEmployeeDO::getId));
            }
        }
        Set<Long> resultEmployeeIds;
        if (directScopeConfigured && employmentScopeConfigured) {
            directEmployeeIds.retainAll(employmentEmployeeIds);
            resultEmployeeIds = directEmployeeIds;
        } else if (employmentScopeConfigured) {
            resultEmployeeIds = employmentEmployeeIds;
        } else {
            resultEmployeeIds = directEmployeeIds;
        }
        resultEmployeeIds.removeAll(excludedEmployeeIds);
        return new ArrayList<>(resultEmployeeIds);
    }

    /**
     * 更新计划手工加入、排除的员工范围
     *
     * @param plan 绩效计划
     * @param employeeIds 员工编号集合
     * @param include 是否额外加入计划
     */
    private void updatePlanEmployeeScope(
            HrmPerformancePlanDO plan, Collection<Long> employeeIds, boolean include) {
        if (CollUtil.isEmpty(employeeIds)) {
            return;
        }

        // 1. 获得当前普通员工范围和排除员工范围
        List<Scope> scopes = new ArrayList<>(plan.getScopes());
        Scope employeeScope = CollUtil.findOne(scopes,
                scope -> Objects.equals(scope.getType(), HrmPerformancePlanScopeTypeEnum.EMPLOYEE_DEPT.getType()));
        Scope excludedScope = CollUtil.findOne(scopes,
                scope -> Objects.equals(scope.getType(), HrmPerformancePlanScopeTypeEnum.EXCLUDED_EMPLOYEE.getType()));
        Set<Long> directEmployeeIds = new LinkedHashSet<>();
        Set<Long> excludedEmployeeIds = new LinkedHashSet<>();
        if (employeeScope != null && CollUtil.isNotEmpty(employeeScope.getEmployeeIds())) {
            directEmployeeIds.addAll(employeeScope.getEmployeeIds());
        }
        if (excludedScope != null && CollUtil.isNotEmpty(excludedScope.getEmployeeIds())) {
            excludedEmployeeIds.addAll(excludedScope.getEmployeeIds());
        }

        // 2. 更新普通员工范围和排除员工范围
        if (include) {
            directEmployeeIds.addAll(employeeIds);
            excludedEmployeeIds.removeAll(employeeIds);
        } else {
            directEmployeeIds.removeAll(employeeIds);
            excludedEmployeeIds.addAll(employeeIds);
        }
        if (employeeScope == null && CollUtil.isNotEmpty(directEmployeeIds)) {
            employeeScope = new Scope().setType(HrmPerformancePlanScopeTypeEnum.EMPLOYEE_DEPT.getType());
            scopes.add(employeeScope);
        }
        if (employeeScope != null) {
            employeeScope.setEmployeeIds(new ArrayList<>(directEmployeeIds));
            if (CollUtil.isEmpty(directEmployeeIds) && CollUtil.isEmpty(employeeScope.getDeptIds())) {
                scopes.remove(employeeScope);
            }
        }
        if (excludedScope == null && CollUtil.isNotEmpty(excludedEmployeeIds)) {
            excludedScope = new Scope().setType(HrmPerformancePlanScopeTypeEnum.EXCLUDED_EMPLOYEE.getType());
            scopes.add(excludedScope);
        }
        if (excludedScope != null) {
            excludedScope.setEmployeeIds(new ArrayList<>(excludedEmployeeIds));
            if (CollUtil.isEmpty(excludedEmployeeIds)) {
                scopes.remove(excludedScope);
            }
        }

        // 3. 保存排除范围
        performancePlanMapper.updateById(
                new HrmPerformancePlanDO().setId(plan.getId()).setScopes(scopes));
        plan.setScopes(scopes);
    }

    /**
     * 校验计划关联的员工和部门
     *
     * @param reqVO 计划保存参数
     */
    private void validatePlanReferences(HrmPerformancePlanSaveReqVO reqVO) {
        // 1. 收集关联的员工和部门编号
        Set<Long> employeeIds = new LinkedHashSet<>();
        Set<Long> deptIds = new LinkedHashSet<>();
        for (HrmPerformancePlanSaveReqVO.Scope scope : reqVO.getScopes()) {
            if (ObjUtil.notEqual(scope.getType(),
                    HrmPerformancePlanScopeTypeEnum.EMPLOYEE_DEPT.getType())) {
                continue;
            }
            if (CollUtil.isNotEmpty(scope.getEmployeeIds())) {
                employeeIds.addAll(scope.getEmployeeIds());
            }
            if (CollUtil.isNotEmpty(scope.getDeptIds())) {
                deptIds.addAll(scope.getDeptIds());
            }
        }
        for (HrmPerformancePlanSaveReqVO.ReviewStage reviewStage : reqVO.getReviewStages()) {
            addSpecifiedEmployeeId(employeeIds, reviewStage.getRater());
        }
        if (Boolean.TRUE.equals(reqVO.getTargetConfirmation())) {
            addSpecifiedEmployeeId(employeeIds, reqVO.getTargetConfirmationStage());
        }
        if (Boolean.TRUE.equals(reqVO.getResultAudit())) {
            reqVO.getResultAuditStages().forEach(stage -> addSpecifiedEmployeeId(employeeIds, stage));
        }
        if (Boolean.TRUE.equals(reqVO.getResultConfirmation())) {
            reqVO.getAppealStages().forEach(stage -> addSpecifiedEmployeeId(employeeIds, stage));
        }
        // 2. 校验员工和部门存在
        if (CollUtil.isNotEmpty(employeeIds)) {
            employeeService.validateEmployeeListExists(employeeIds);
        }
        if (CollUtil.isNotEmpty(deptIds)) {
            deptApi.validateDeptList(deptIds);
        }
    }

    private void addSpecifiedEmployeeId(
            Set<Long> employeeIds, HrmPerformancePlanSaveReqVO.HandlerStage stage) {
        if (Objects.equals(stage.getType(), HrmPerformanceRaterTypeEnum.SPECIFIED.getType())) {
            employeeIds.add(stage.getEmployeeId());
        }
    }

    /**
     * 校验绩效计划可以编辑
     *
     * @param plan 绩效计划
     */
    private void validatePlanEditable(HrmPerformancePlanDO plan) {
        if (plan == null) {
            throw exception(PERFORMANCE_PLAN_NOT_EXISTS);
        }
        if (notEqualsAny(plan.getStatus(),
                HrmPerformancePlanStatusEnum.DRAFT.getStatus(), HrmPerformancePlanStatusEnum.NOT_STARTED.getStatus())) {
            throw exception(PERFORMANCE_PLAN_STARTED_CANNOT_MODIFY);
        }
    }

    private void validatePlanScoreRange(HrmPerformanceAssessmentTemplateDO assessmentTemplate) {
        if (assessmentTemplate.getUpperLimitScore() == null
                || assessmentTemplate.getUpperLimitScore().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw exception(PERFORMANCE_DATA_ILLEGAL);
        }
    }

    private void validateNameUnique(Long id, String name) {
        HrmPerformancePlanDO plan = performancePlanMapper.selectByName(name);
        if (plan != null && ObjUtil.notEqual(plan.getId(), id)) {
            throw exception(PERFORMANCE_PLAN_NAME_DUPLICATE);
        }
    }

}
