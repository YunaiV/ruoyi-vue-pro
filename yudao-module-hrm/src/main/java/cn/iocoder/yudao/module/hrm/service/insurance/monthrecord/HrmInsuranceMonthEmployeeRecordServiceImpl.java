package cn.iocoder.yudao.module.hrm.service.insurance.monthrecord;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeListReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.monthrecord.employee.HrmInsuranceMonthEmployeeProjectUpdateReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.monthrecord.employee.HrmInsuranceMonthEmployeeRecordCreateListReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.monthrecord.employee.HrmInsuranceMonthEmployeeRecordPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.monthrecord.employee.HrmInsuranceMonthEmployeeRecordUpdateReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.config.HrmInsuranceSchemeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.config.HrmInsuranceSchemeProjectDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.employee.HrmInsuranceEmployeeInfoDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.monthrecord.HrmInsuranceMonthEmployeeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.monthrecord.HrmInsuranceMonthEmployeeRecordDO.Project;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.monthrecord.HrmInsuranceMonthRecordDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.insurance.monthrecord.HrmInsuranceMonthEmployeeRecordMapper;
import cn.iocoder.yudao.module.hrm.enums.MessageTemplateConstants;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeEntryStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.insurance.config.HrmInsuranceProjectTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.insurance.config.HrmInsuranceSchemeTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.insurance.employee.HrmInsuranceEmployeeStatusEnum;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.hrm.service.insurance.config.HrmInsuranceSchemeService;
import cn.iocoder.yudao.module.hrm.service.insurance.employee.HrmInsuranceEmployeeInfoService;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import javax.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.number.MoneyUtils.priceMultiplyPercent;
import static cn.iocoder.yudao.framework.common.util.number.MoneyUtils.priceScale;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.INSURANCE_DATA_ILLEGAL;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.INSURANCE_EMPLOYEE_SCHEME_NOT_CONFIGURED;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.INSURANCE_MONTH_EMPLOYEE_NOT_ELIGIBLE;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.INSURANCE_MONTH_EMP_RECORD_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_INSURANCE_MONTH_EMPLOYEE_CREATE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_INSURANCE_MONTH_EMPLOYEE_CREATE_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_INSURANCE_MONTH_EMPLOYEE_STOP_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_INSURANCE_MONTH_EMPLOYEE_STOP_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_INSURANCE_MONTH_EMPLOYEE_UPDATE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_INSURANCE_MONTH_EMPLOYEE_UPDATE_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_INSURANCE_MONTH_TYPE;

/**
 * HRM 员工月度社保 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmInsuranceMonthEmployeeRecordServiceImpl implements HrmInsuranceMonthEmployeeRecordService {

    @Resource
    private HrmInsuranceMonthEmployeeRecordMapper monthEmployeeRecordMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private HrmInsuranceMonthRecordService monthRecordService;
    @Resource
    private HrmInsuranceSchemeService insuranceSchemeService;
    @Resource
    private HrmInsuranceEmployeeInfoService insuranceEmployeeInfoService;
    @Resource
    private HrmEmployeeService employeeService;

    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createMonthEmployeeRecordList(HrmInsuranceMonthRecordDO monthRecord) {
        // 1. 批量加载已配置参保方案的员工社保资料
        List<HrmInsuranceEmployeeInfoDO> insuranceEmployeeInfos = convertList(
                insuranceEmployeeInfoService.getInsuranceEmployeeInfoList(), info -> info,
                info -> info.getSchemeId() != null);
        Set<Long> schemeIds = convertSet(insuranceEmployeeInfos, HrmInsuranceEmployeeInfoDO::getSchemeId);
        Map<Long, HrmInsuranceSchemeDO> schemeMap = convertMap(
                insuranceSchemeService.getSchemeListByIds(schemeIds), HrmInsuranceSchemeDO::getId);
        Map<Long, List<HrmInsuranceSchemeProjectDO>> schemeProjectMap =
                insuranceSchemeService.getSchemeProjectListMap(schemeIds);
        Map<Long, HrmEmployeeDO> employeeMap = employeeService.getEmployeeMap(
                convertSet(insuranceEmployeeInfos, HrmInsuranceEmployeeInfoDO::getEmployeeId));

        // 2. 生成员工月记录
        List<HrmInsuranceMonthEmployeeRecordDO> employeeRecords = new ArrayList<>();
        for (HrmInsuranceEmployeeInfoDO insuranceEmployeeInfo : insuranceEmployeeInfos) {
            HrmInsuranceSchemeDO scheme = schemeMap.get(insuranceEmployeeInfo.getSchemeId());
            HrmEmployeeDO employee = employeeMap.get(insuranceEmployeeInfo.getEmployeeId());
            List<HrmInsuranceSchemeProjectDO> schemeProjects = scheme == null ? Collections.emptyList()
                    : schemeProjectMap.getOrDefault(scheme.getId(), Collections.emptyList());
            if (scheme == null || employee == null || CollUtil.isEmpty(schemeProjects)
                    || !isAvailableForInsuranceMonth(employee, monthRecord)) {
                continue;
            }
            List<Project> projects = buildProjectsFromScheme(schemeProjects, scheme.getType());
            employeeRecords.add(buildMonthEmployeeRecord(monthRecord, employee.getId(), scheme, projects,
                    HrmInsuranceEmployeeStatusEnum.NORMAL.getStatus()));
        }
        if (CollUtil.isNotEmpty(employeeRecords)) {
            monthEmployeeRecordMapper.insertBatch(employeeRecords);
        }

        // 3. 回填员工社保起缴月份
        LocalDateTime startMonth = YearMonth.of(monthRecord.getYear(), monthRecord.getMonth())
                .atDay(1).atStartOfDay();
        for (HrmInsuranceMonthEmployeeRecordDO employeeRecord : employeeRecords) {
            insuranceEmployeeInfoService.updateSocialSecurityStartMonthIfAbsent(
                    employeeRecord.getEmployeeId(), startMonth);
            sendInsuranceMonthRecordMessage(employeeMap.get(employeeRecord.getEmployeeId()), monthRecord);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_INSURANCE_MONTH_TYPE, subType = HRM_INSURANCE_MONTH_EMPLOYEE_CREATE_SUB_TYPE,
            bizNo = "{{#reqVO.monthRecordId}}", success = HRM_INSURANCE_MONTH_EMPLOYEE_CREATE_SUCCESS)
    public void createMonthEmployeeRecordList(HrmInsuranceMonthEmployeeRecordCreateListReqVO reqVO) {
        // 1. 校验月表和员工
        HrmInsuranceMonthRecordDO monthRecord = monthRecordService.validateMonthRecordEditableForUpdate(
                reqVO.getMonthRecordId());
        employeeService.validateEmployeeListExists(reqVO.getEmployeeIds());
        Map<Long, HrmEmployeeDO> employeeMap = employeeService.getEmployeeMap(reqVO.getEmployeeIds());

        // 2. 批量加载员工社保资料、方案和项目
        Map<Long, HrmInsuranceEmployeeInfoDO> insuranceEmployeeInfoMap =
                insuranceEmployeeInfoService.getInsuranceEmployeeInfoMap(reqVO.getEmployeeIds());
        Set<Long> schemeIds = convertSet(insuranceEmployeeInfoMap.values(),
                HrmInsuranceEmployeeInfoDO::getSchemeId);
        Map<Long, HrmInsuranceSchemeDO> schemeMap = convertMap(
                insuranceSchemeService.getSchemeListByIds(schemeIds), HrmInsuranceSchemeDO::getId);
        Map<Long, List<HrmInsuranceSchemeProjectDO>> schemeProjectMap =
                insuranceSchemeService.getSchemeProjectListMap(schemeIds);

        // 3. 新增或更新员工月记录
        for (Long employeeId : reqVO.getEmployeeIds()) {
            HrmInsuranceEmployeeInfoDO insuranceEmployeeInfo = insuranceEmployeeInfoMap.get(employeeId);
            HrmInsuranceSchemeDO scheme = insuranceEmployeeInfo == null
                    ? null : schemeMap.get(insuranceEmployeeInfo.getSchemeId());
            List<HrmInsuranceSchemeProjectDO> schemeProjects = scheme == null ? Collections.emptyList()
                    : schemeProjectMap.getOrDefault(scheme.getId(), Collections.emptyList());
            if (scheme == null || CollUtil.isEmpty(schemeProjects)) {
                throw exception(INSURANCE_EMPLOYEE_SCHEME_NOT_CONFIGURED);
            }
            if (!isAvailableForInsuranceMonth(employeeMap.get(employeeId), monthRecord)) {
                throw exception(INSURANCE_MONTH_EMPLOYEE_NOT_ELIGIBLE);
            }
            List<Project> projects = buildProjectsFromScheme(schemeProjects, scheme.getType());
            createOrUpdateMonthEmployeeRecord(monthRecord, employeeId, scheme, projects,
                    HrmInsuranceEmployeeStatusEnum.NORMAL.getStatus());
        }

        // 4. 更新月表汇总
        monthRecordService.updateMonthRecordSummary(monthRecord.getId());

        // 5. 记录操作日志上下文
        LogRecordContext.putVariable("monthRecord", monthRecord);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_INSURANCE_MONTH_TYPE, subType = HRM_INSURANCE_MONTH_EMPLOYEE_UPDATE_SUB_TYPE,
            bizNo = "{{#employeeRecord.monthRecordId}}", success = HRM_INSURANCE_MONTH_EMPLOYEE_UPDATE_SUCCESS)
    public void updateMonthEmployeeRecord(HrmInsuranceMonthEmployeeRecordUpdateReqVO reqVO) {
        // 1. 校验员工月记录和社保方案
        HrmInsuranceMonthEmployeeRecordDO employeeRecord = validateMonthEmployeeRecordEditable(reqVO.getId());
        HrmInsuranceSchemeDO scheme = insuranceSchemeService.validateSchemeExists(reqVO.getSchemeId());

        // 2. 更新员工月记录和当前参保方案
        updateSingleMonthEmployeeRecord(employeeRecord, scheme,
                buildProjectsFromUpdateReqVO(employeeRecord, scheme, reqVO.getProjects()));

        // 3. 更新月表汇总
        monthRecordService.updateMonthRecordSummary(employeeRecord.getMonthRecordId());

        // 4. 记录操作日志上下文
        LogRecordContext.putVariable("employeeRecord", employeeRecord);
        LogRecordContext.putVariable("scheme", scheme);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_INSURANCE_MONTH_TYPE, subType = HRM_INSURANCE_MONTH_EMPLOYEE_STOP_SUB_TYPE,
            bizNo = "{{#monthRecord.id}}", success = HRM_INSURANCE_MONTH_EMPLOYEE_STOP_SUCCESS)
    public void stopMonthEmployeeRecordList(Collection<Long> ids) {
        // 1. 校验员工月记录
        List<HrmInsuranceMonthEmployeeRecordDO> employeeRecords =
                convertList(ids, this::validateMonthEmployeeRecordEditable);

        // 2. 批量停止参保
        for (HrmInsuranceMonthEmployeeRecordDO employeeRecord : employeeRecords) {
            monthEmployeeRecordMapper.updateById(new HrmInsuranceMonthEmployeeRecordDO()
                    .setId(employeeRecord.getId())
                    .setStatus(HrmInsuranceEmployeeStatusEnum.STOPPED.getStatus()));
        }

        // 3. 更新涉及的月表汇总
        for (Long monthRecordId : convertSet(employeeRecords,
                HrmInsuranceMonthEmployeeRecordDO::getMonthRecordId)) {
            monthRecordService.updateMonthRecordSummary(monthRecordId);
        }

        // 4. 记录操作日志上下文
        HrmInsuranceMonthRecordDO monthRecord = monthRecordService.validateMonthRecordExists(
                CollUtil.getFirst(employeeRecords).getMonthRecordId());
        LogRecordContext.putVariable("monthRecord", monthRecord);
    }

    @Override
    public void deleteMonthEmployeeRecordListByMonthRecordId(Long monthRecordId) {
        monthEmployeeRecordMapper.deleteByMonthRecordId(monthRecordId);
    }

    @Override
    public PageResult<HrmInsuranceMonthEmployeeRecordDO> getMonthEmployeeRecordPage(
            HrmInsuranceMonthEmployeeRecordPageReqVO reqVO) {
        // 1.1 获得员工姓名关联的员工编号
        Collection<Long> employeeIds = null;
        if (StrUtil.isNotBlank(reqVO.getEmployeeName())) {
            employeeIds = convertList(employeeService.getEmployeeList(
                    new HrmEmployeeListReqVO().setName(reqVO.getEmployeeName())), HrmEmployeeDO::getId);
            if (CollUtil.isEmpty(employeeIds)) {
                return PageResult.empty();
            }
        }
        // 1.2 获得参保地区关联的方案编号
        Collection<Long> schemeIds = null;
        if (reqVO.getAreaId() != null) {
            schemeIds = convertList(insuranceSchemeService.getSchemeListByAreaId(reqVO.getAreaId()),
                    HrmInsuranceSchemeDO::getId);
            if (CollUtil.isEmpty(schemeIds)) {
                return PageResult.empty();
            }
        }

        // 2. 查询员工月记录
        return monthEmployeeRecordMapper.selectPage(reqVO, employeeIds, schemeIds);
    }

    @Override
    public HrmInsuranceMonthEmployeeRecordDO getMonthEmployeeRecord(Long id) {
        return monthEmployeeRecordMapper.selectById(id);
    }

    @Override
    public List<HrmInsuranceMonthEmployeeRecordDO> getMonthEmployeeRecordListByEmployeeIdAndYear(
            Long employeeId, Integer year) {
        return monthEmployeeRecordMapper.selectListByEmployeeIdAndYear(employeeId, year);
    }

    @Override
    public List<HrmInsuranceMonthEmployeeRecordDO> getMonthEmployeeRecordListByMonthRecordId(Long monthRecordId) {
        return monthEmployeeRecordMapper.selectListByMonthRecordId(monthRecordId);
    }

    @Override
    public long getMonthEmployeeRecordCountBySchemeId(Long schemeId) {
        return monthEmployeeRecordMapper.selectCountBySchemeId(schemeId);
    }

    @Override
    public Map<Long, Long> getMonthEmployeeRecordCountMapBySchemeIds(Collection<Long> schemeIds) {
        if (CollUtil.isEmpty(schemeIds)) {
            return Collections.emptyMap();
        }
        return monthEmployeeRecordMapper.selectCountMapBySchemeIds(schemeIds);
    }

    @Override
    public void updateInsuranceMonthEmployeeRecordSchemeIdBySchemeId(Long schemeId, Long newSchemeId) {
        monthEmployeeRecordMapper.updateBySchemeId(schemeId,
                new HrmInsuranceMonthEmployeeRecordDO().setSchemeId(newSchemeId));
    }

    @Override
    public List<HrmEmployeeDO> getUninsuredEmployeeList(Long monthRecordId) {
        // 1. 获得月表和当前正常参保员工
        HrmInsuranceMonthRecordDO monthRecord = monthRecordService.getMonthRecord(monthRecordId);
        if (monthRecord == null) {
            return Collections.emptyList();
        }
        Set<Long> insuredEmployeeIds = convertSet(
                monthEmployeeRecordMapper.selectListByMonthRecordIdAndStatus(
                        monthRecordId, HrmInsuranceEmployeeStatusEnum.NORMAL.getStatus()),
                HrmInsuranceMonthEmployeeRecordDO::getEmployeeId);

        // 2. 加载员工当前参保方案
        List<HrmEmployeeDO> employees = employeeService.getEmployeeList(new HrmEmployeeListReqVO());
        Map<Long, HrmInsuranceEmployeeInfoDO> insuranceEmployeeInfoMap = insuranceEmployeeInfoService
                .getInsuranceEmployeeInfoMap(convertSet(employees, HrmEmployeeDO::getId));
        Set<Long> schemeIds = convertSet(insuranceEmployeeInfoMap.values(), HrmInsuranceEmployeeInfoDO::getSchemeId);
        Set<Long> validSchemeIds = convertSet(insuranceSchemeService.getSchemeListByIds(schemeIds),
                HrmInsuranceSchemeDO::getId);

        // 3. 筛选本月可参保员工
        return convertList(employees, employee -> employee, employee -> {
            HrmInsuranceEmployeeInfoDO employeeInfo = insuranceEmployeeInfoMap.get(employee.getId());
            return !insuredEmployeeIds.contains(employee.getId())
                    && employeeInfo != null && validSchemeIds.contains(employeeInfo.getSchemeId())
                    && isAvailableForInsuranceMonth(employee, monthRecord);
        });
    }

    @Override
    public Map<Long, HrmInsuranceMonthEmployeeRecordDO> getNormalMonthEmployeeRecordMap(Integer year, Integer month) {
        List<HrmInsuranceMonthEmployeeRecordDO> list = monthEmployeeRecordMapper.selectListByYearAndMonthAndStatus(
                year, month, HrmInsuranceEmployeeStatusEnum.NORMAL.getStatus());
        return convertMap(list, HrmInsuranceMonthEmployeeRecordDO::getEmployeeId);
    }

    private HrmInsuranceMonthEmployeeRecordDO validateMonthEmployeeRecordExists(Long id) {
        HrmInsuranceMonthEmployeeRecordDO employeeRecord = monthEmployeeRecordMapper.selectById(id);
        if (employeeRecord == null) {
            throw exception(INSURANCE_MONTH_EMP_RECORD_NOT_EXISTS);
        }
        return employeeRecord;
    }

    private HrmInsuranceMonthEmployeeRecordDO validateMonthEmployeeRecordEditable(Long id) {
        // 1. 校验员工月记录存在
        HrmInsuranceMonthEmployeeRecordDO employeeRecord = validateMonthEmployeeRecordExists(id);
        // 2. 校验月表可编辑
        monthRecordService.validateMonthRecordEditableForUpdate(employeeRecord.getMonthRecordId());
        return employeeRecord;
    }

    private boolean isAvailableForInsuranceMonth(HrmEmployeeDO employee,
                                                  HrmInsuranceMonthRecordDO monthRecord) {
        if (employee == null
                || Objects.equals(employee.getEntryStatus(), HrmEmployeeEntryStatusEnum.PENDING_ENTRY.getStatus())) {
            return false;
        }
        YearMonth yearMonth = YearMonth.of(monthRecord.getYear(), monthRecord.getMonth());
        LocalDateTime monthBeginTime = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEndTime = yearMonth.atEndOfMonth().atTime(LocalTime.MAX);
        if (employee.getEntryTime() != null && employee.getEntryTime().isAfter(monthEndTime)) {
            return false;
        }
        if (employee.getLeaveTime() != null) {
            return !employee.getLeaveTime().isBefore(monthBeginTime);
        }
        return ObjUtil.notEqual(employee.getEntryStatus(), HrmEmployeeEntryStatusEnum.LEFT.getStatus());
    }

    private List<Project> buildProjectsFromScheme(List<HrmInsuranceSchemeProjectDO> schemeProjects, Integer schemeType) {
        return convertList(schemeProjects, schemeProject -> buildProject(schemeProject, schemeType));
    }

    private Project buildProject(HrmInsuranceSchemeProjectDO schemeProject, Integer schemeType) {
        Project project = Project.builder()
                .schemeProjectId(schemeProject.getId())
                .type(schemeProject.getType()).name(schemeProject.getName())
                .baseAmount(schemeProject.getBaseAmount())
                .corporateRate(schemeProject.getCorporateRate()).personalRate(schemeProject.getPersonalRate())
                .corporateAmount(schemeProject.getCorporateAmount()).personalAmount(schemeProject.getPersonalAmount())
                .build();
        calculateProjectAmount(schemeType, project);
        return project;
    }

    private List<Project> buildProjectsFromUpdateReqVO(
            HrmInsuranceMonthEmployeeRecordDO employeeRecord, HrmInsuranceSchemeDO scheme,
            List<HrmInsuranceMonthEmployeeProjectUpdateReqVO> projectUpdates) {
        // 1. 校验请求项目编号不重复
        Map<Long, HrmInsuranceMonthEmployeeProjectUpdateReqVO> updateMap = new LinkedHashMap<>();
        for (HrmInsuranceMonthEmployeeProjectUpdateReqVO projectUpdate : projectUpdates) {
            if (updateMap.put(projectUpdate.getSchemeProjectId(), projectUpdate) != null) {
                throw exception(INSURANCE_DATA_ILLEGAL);
            }
        }
        // 2. 未切换方案时沿用月度快照；切换方案时加载新方案项目
        List<Project> projects = Objects.equals(employeeRecord.getSchemeId(), scheme.getId())
                && CollUtil.isNotEmpty(employeeRecord.getProjects())
                ? convertList(employeeRecord.getProjects(), project -> BeanUtils.toBean(project, Project.class))
                : buildProjectsFromScheme(insuranceSchemeService.getSchemeProjectList(scheme.getId()),
                        scheme.getType());
        // 3. 校验请求项目均属于月度快照或新方案
        if (!convertSet(projects, Project::getSchemeProjectId).containsAll(updateMap.keySet())) {
            throw exception(INSURANCE_DATA_ILLEGAL);
        }
        // 4. 使用请求数据覆盖可编辑金额并重新计算
        for (Project project : projects) {
            HrmInsuranceMonthEmployeeProjectUpdateReqVO projectUpdate =
                    updateMap.get(project.getSchemeProjectId());
            if (projectUpdate == null) {
                continue;
            }
            if (Objects.equals(scheme.getType(), HrmInsuranceSchemeTypeEnum.PROPORTION.getType())) {
                project.setBaseAmount(projectUpdate.getBaseAmount());
            } else {
                project.setCorporateAmount(projectUpdate.getCorporateAmount())
                        .setPersonalAmount(projectUpdate.getPersonalAmount());
            }
            calculateProjectAmount(scheme.getType(), project);
        }
        return projects;
    }

    private HrmInsuranceMonthEmployeeRecordDO buildMonthEmployeeRecord(
            HrmInsuranceMonthRecordDO monthRecord, Long employeeId, HrmInsuranceSchemeDO scheme,
            List<Project> projects, Integer status) {
        HrmInsuranceMonthEmployeeRecordDO employeeRecord = new HrmInsuranceMonthEmployeeRecordDO()
                .setMonthRecordId(monthRecord.getId()).setEmployeeId(employeeId)
                .setSchemeId(scheme == null ? null : scheme.getId())
                .setYear(monthRecord.getYear()).setMonth(monthRecord.getMonth())
                .setStatus(status).setProjects(projects);
        fillEmployeeRecordAmounts(employeeRecord, projects);
        return employeeRecord;
    }

    @SuppressWarnings("UnusedReturnValue")
    private Long createOrUpdateMonthEmployeeRecord(
            HrmInsuranceMonthRecordDO monthRecord, Long employeeId, HrmInsuranceSchemeDO scheme,
            List<Project> projects, Integer status) {
        HrmInsuranceMonthEmployeeRecordDO employeeRecord = monthEmployeeRecordMapper
                .selectByMonthRecordIdAndEmployeeId(monthRecord.getId(), employeeId);
        if (employeeRecord == null) {
            employeeRecord = buildMonthEmployeeRecord(monthRecord, employeeId, scheme, projects, status);
            monthEmployeeRecordMapper.insert(employeeRecord);
        } else {
            employeeRecord.setSchemeId(scheme == null ? null : scheme.getId())
                    .setStatus(status).setProjects(projects);
            fillEmployeeRecordAmounts(employeeRecord, projects);
            monthEmployeeRecordMapper.updateById(employeeRecord);
        }
        insuranceEmployeeInfoService.updateSocialSecurityStartMonthIfAbsent(employeeId,
                YearMonth.of(monthRecord.getYear(), monthRecord.getMonth()).atDay(1).atStartOfDay());
        return employeeRecord.getId();
    }

    /**
     * 更新单个员工月度社保记录，并同步员工当前参保方案
     *
     * @param employeeRecord 员工月度社保记录
     * @param scheme 社保方案
     * @param projects 月度参保项目
     */
    private void updateSingleMonthEmployeeRecord(HrmInsuranceMonthEmployeeRecordDO employeeRecord,
                                                 HrmInsuranceSchemeDO scheme, List<Project> projects) {
        // 1. 更新员工月度社保记录
        employeeRecord.setSchemeId(scheme.getId()).setProjects(projects);
        fillEmployeeRecordAmounts(employeeRecord, projects);
        monthEmployeeRecordMapper.updateById(employeeRecord);

        // 2. 同步员工当前参保方案
        HrmEmployeeDO employee = employeeService.getEmployee(employeeRecord.getEmployeeId());
        if (employee != null && HrmEmployeeStatusEnum.FULL_TIME_STATUSES.contains(employee.getStatus())
                && HrmEmployeeEntryStatusEnum.ACTIVE_STATUSES.contains(employee.getEntryStatus())) {
            insuranceEmployeeInfoService.updateEmployeeScheme(employeeRecord.getEmployeeId(), scheme.getId());
        }
    }

    private void fillEmployeeRecordAmounts(HrmInsuranceMonthEmployeeRecordDO employeeRecord, List<Project> projects) {
        BigDecimal personalInsuranceAmount = BigDecimal.ZERO;
        BigDecimal personalProvidentFundAmount = BigDecimal.ZERO;
        BigDecimal corporateInsuranceAmount = BigDecimal.ZERO;
        BigDecimal corporateProvidentFundAmount = BigDecimal.ZERO;
        for (Project project : projects) {
            HrmInsuranceProjectTypeEnum projectType = HrmInsuranceProjectTypeEnum.valueOf(project.getType());
            if (projectType == null) {
                continue;
            }
            if (projectType.isProvidentFund()) {
                personalProvidentFundAmount = personalProvidentFundAmount.add(
                        ObjUtil.defaultIfNull(project.getPersonalAmount(), BigDecimal.ZERO));
                corporateProvidentFundAmount = corporateProvidentFundAmount.add(
                        ObjUtil.defaultIfNull(project.getCorporateAmount(), BigDecimal.ZERO));
            } else {
                personalInsuranceAmount = personalInsuranceAmount.add(
                        ObjUtil.defaultIfNull(project.getPersonalAmount(), BigDecimal.ZERO));
                corporateInsuranceAmount = corporateInsuranceAmount.add(
                        ObjUtil.defaultIfNull(project.getCorporateAmount(), BigDecimal.ZERO));
            }
        }
        employeeRecord.setPersonalInsuranceAmount(priceScale(personalInsuranceAmount))
                .setPersonalProvidentFundAmount(priceScale(personalProvidentFundAmount))
                .setCorporateInsuranceAmount(priceScale(corporateInsuranceAmount))
                .setCorporateProvidentFundAmount(priceScale(corporateProvidentFundAmount));
    }

    private void calculateProjectAmount(Integer type, Project project) {
        project.setBaseAmount(priceScale(project.getBaseAmount()))
                .setCorporateRate(priceScale(project.getCorporateRate()))
                .setPersonalRate(priceScale(project.getPersonalRate()));
        if (Objects.equals(type, HrmInsuranceSchemeTypeEnum.PROPORTION.getType())) {
            project.setCorporateAmount(priceMultiplyPercent(project.getBaseAmount(), project.getCorporateRate()))
                    .setPersonalAmount(priceMultiplyPercent(project.getBaseAmount(), project.getPersonalRate()));
        } else {
            project.setCorporateAmount(priceScale(project.getCorporateAmount()))
                    .setPersonalAmount(priceScale(project.getPersonalAmount()));
        }
    }

    /**
     * 发送月度社保表生成通知
     *
     * @param employee 员工
     * @param monthRecord 月度社保表
     */
    private void sendInsuranceMonthRecordMessage(HrmEmployeeDO employee, HrmInsuranceMonthRecordDO monthRecord) {
        if (employee == null || employee.getUserId() == null) {
            return;
        }
        Map<String, Object> templateParams = new HashMap<>(4);
        templateParams.put("employeeName", employee.getName());
        templateParams.put("year", monthRecord.getYear());
        templateParams.put("month", monthRecord.getMonth());
        templateParams.put("route", "/hrm/portal/insurance/record");
        notifyMessageSendApi.sendSingleMessageToAdmin(new NotifySendSingleToUserReqDTO()
                .setUserId(employee.getUserId())
                .setTemplateCode(MessageTemplateConstants.INSURANCE_MONTH_RECORD_CREATED)
                .setTemplateParams(templateParams));
    }

}
