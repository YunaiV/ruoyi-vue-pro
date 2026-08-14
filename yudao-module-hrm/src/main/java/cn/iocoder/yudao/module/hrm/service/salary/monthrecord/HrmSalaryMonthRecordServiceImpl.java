package cn.iocoder.yudao.module.hrm.service.salary.monthrecord;

import cn.iocoder.yudao.module.hrm.service.salary.config.HrmSalaryConfigService;
import cn.iocoder.yudao.module.hrm.service.salary.config.HrmSalaryGroupService;
import cn.iocoder.yudao.module.hrm.service.salary.config.HrmSalaryOptionService;
import cn.iocoder.yudao.module.hrm.service.salary.config.HrmSalaryTaxRuleService;
import cn.iocoder.yudao.module.hrm.service.salary.employee.HrmSalaryEmployeeInfoService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.statistics.HrmAttendanceMonthRecordRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeListReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.option.HrmSalaryOptionValueVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.HrmSalaryMonthRecordCreateReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.HrmSalaryMonthRecordPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.HrmSalaryPayrollReadinessRespVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment.HrmEmployeeChangeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.monthrecord.HrmInsuranceMonthEmployeeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryConfigDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryGroupDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.monthrecord.HrmSalaryMonthEmployeeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.monthrecord.HrmSalaryMonthRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryOptionDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryTaxRuleDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.salary.monthrecord.HrmSalaryMonthRecordMapper;
import cn.iocoder.yudao.module.hrm.enums.employee.employment.HrmEmployeeChangeTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeEntryStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.config.HrmSalaryOptionCodeEnum;
import cn.iocoder.yudao.module.hrm.service.attendance.statistics.HrmAttendanceStatisticsService;
import cn.iocoder.yudao.module.hrm.service.employee.employment.HrmEmployeeChangeRecordService;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.hrm.service.insurance.monthrecord.HrmInsuranceMonthEmployeeRecordService;
import cn.iocoder.yudao.module.hrm.service.insurance.monthrecord.HrmInsuranceMonthRecordService;
import cn.iocoder.yudao.module.hrm.enums.salary.config.HrmSalarySocialSecurityMonthTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.monthrecord.HrmSalaryMonthRecordStatusEnum;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import javax.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.isAfterOrEqual;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.isBeforeOrEqual;
import static cn.iocoder.yudao.framework.common.util.number.MoneyUtils.priceAdd;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_CONFIG_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_MONTH_RECORD_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_MONTH_RECORD_CANNOT_DELETE_ONLY;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_MONTH_RECORD_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_MONTH_RECORD_NOT_LATEST;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_MONTH_RECORD_STATUS_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.*;
import static cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryOptionDO.ROOT_PARENT_CODE;
import static cn.iocoder.yudao.module.hrm.util.HrmExcelUtils.getCell;
import static cn.iocoder.yudao.module.hrm.util.HrmExcelUtils.parseAmount;

/**
 * HRM 月度工资 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmSalaryMonthRecordServiceImpl implements HrmSalaryMonthRecordService {

    private static final String MONTH_RECORD_TITLE_TEMPLATE = "%d年%d月工资表";

    @Resource
    private HrmSalaryMonthRecordMapper monthRecordMapper;

    @Resource
    private HrmSalaryOptionService salaryOptionService;
    @Resource
    private HrmSalaryConfigService salaryConfigService;
    @Resource
    private HrmEmployeeService employeeService;
    @Resource
    private HrmEmployeeChangeRecordService employeeChangeRecordService;
    @Resource
    private HrmAttendanceStatisticsService attendanceStatisticsService;
    @Resource
    private HrmInsuranceMonthEmployeeRecordService insuranceMonthEmployeeRecordService;
    @Resource
    private HrmInsuranceMonthRecordService insuranceMonthRecordService;
    @Resource
    private HrmSalaryMonthEmployeeRecordService monthEmployeeRecordService;
    @Resource
    private HrmSalaryEmployeeInfoService salaryEmployeeInfoService;
    @Resource
    private HrmSalaryGroupService salaryGroupService;
    @Resource
    private HrmSalaryTaxRuleService salaryTaxRuleService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_SALARY_MONTH_TYPE, subType = HRM_SALARY_MONTH_CREATE_SUB_TYPE,
            bizNo = "{{#salaryMonthRecord.id}}", success = HRM_SALARY_MONTH_CREATE_SUCCESS)
    public Long createMonthRecord(HrmSalaryMonthRecordCreateReqVO reqVO) {
        // 1. 校验年月唯一性
        if (monthRecordMapper.selectByYearMonth(reqVO.getYear(), reqVO.getMonth()) != null) {
            throw exception(SALARY_MONTH_RECORD_EXISTS);
        }

        // 2.1 根据工资配置计算计薪周期
        YearMonth yearMonth = YearMonth.of(reqVO.getYear(), reqVO.getMonth());
        HrmSalaryConfigDO salaryConfig = salaryConfigService.getSalaryConfig();
        LocalDate defaultStartDate = resolveMonthDay(yearMonth,
                salaryConfig == null ? null : salaryConfig.getCycleStartDay(), 1);
        YearMonth endYearMonth = salaryConfig != null && salaryConfig.getCycleStartDay() != null
                && salaryConfig.getCycleStartDay() > 1 ? yearMonth.plusMonths(1) : yearMonth;
        LocalDate defaultEndDate = resolveMonthDay(endYearMonth,
                salaryConfig == null ? null : salaryConfig.getCycleEndDay(), endYearMonth.lengthOfMonth());
        // 2.2 创建月度工资表
        HrmSalaryMonthRecordDO monthRecord = BeanUtils.toBean(reqVO, HrmSalaryMonthRecordDO.class)
                .setTitle(reqVO.getTitle() != null ? reqVO.getTitle()
                        : String.format(MONTH_RECORD_TITLE_TEMPLATE, reqVO.getYear(), reqVO.getMonth()))
                .setStartTime(reqVO.getStartTime() == null ? defaultStartDate.atStartOfDay() : reqVO.getStartTime())
                .setEndTime(reqVO.getEndTime() == null ? defaultEndDate.atStartOfDay() : reqVO.getEndTime())
                .setEmployeeCount(0).setStatus(HrmSalaryMonthRecordStatusEnum.UNCOMPUTED.getStatus())
                .setExpectedPaySalary(BigDecimal.ZERO).setPersonalTax(BigDecimal.ZERO).setRealPaySalary(BigDecimal.ZERO)
                .setOptionHeaders(buildOptionHeaderList(salaryOptionService.getSalaryOptionList(false, true)));
        monthRecordMapper.insert(monthRecord);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("salaryMonthRecord", monthRecord);
        return monthRecord.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_SALARY_MONTH_TYPE, subType = HRM_SALARY_MONTH_CREATE_SUB_TYPE,
            bizNo = "{{#salaryMonthRecord.id}}", success = HRM_SALARY_MONTH_CREATE_SUCCESS)
    public Long createNextMonthRecord() {
        // 1. 获得最近月度工资表
        HrmSalaryMonthRecordDO lastRecord = monthRecordMapper.selectLast();

        // 2.1 首次创建时，使用工资配置的开始年月
        YearMonth nextMonth;
        if (lastRecord == null) {
            HrmSalaryConfigDO salaryConfig = salaryConfigService.getSalaryConfig();
            if (salaryConfig == null || salaryConfig.getStartYear() == null || salaryConfig.getStartMonth() == null) {
                throw exception(SALARY_CONFIG_NOT_EXISTS);
            }
            nextMonth = YearMonth.of(salaryConfig.getStartYear(), salaryConfig.getStartMonth());
        } else {
            // 2.2 非首次创建时，归档最近月度工资表
            if (ObjectUtil.notEqual(lastRecord.getStatus(), HrmSalaryMonthRecordStatusEnum.HISTORY.getStatus())) {
                monthRecordMapper.updateById(new HrmSalaryMonthRecordDO()
                        .setId(lastRecord.getId()).setStatus(HrmSalaryMonthRecordStatusEnum.HISTORY.getStatus()));
            }
            nextMonth = YearMonth.of(lastRecord.getYear(), lastRecord.getMonth()).plusMonths(1);
        }

        // 3. 创建下月工资表
        return createMonthRecord(new HrmSalaryMonthRecordCreateReqVO()
                .setYear(nextMonth.getYear()).setMonth(nextMonth.getMonthValue()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_SALARY_MONTH_TYPE, subType = HRM_SALARY_MONTH_COMPUTE_SUB_TYPE,
            bizNo = "{{#salaryMonthRecord.id}}", success = HRM_SALARY_MONTH_COMPUTE_SUCCESS)
    public void computeMonthRecord(Long id) {
        computeMonthRecord(id, false, false,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void computeMonthRecord(Long id, List<Map<Integer, String>> attendanceRows,
                                   List<Map<Integer, String>> additionalDeductionRows,
                                   List<Map<Integer, String>> cumulativeTaxRows) {
        computeMonthRecord(id, false, false,
                attendanceRows, additionalDeductionRows, cumulativeTaxRows);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_SALARY_MONTH_TYPE, subType = HRM_SALARY_MONTH_COMPUTE_SUB_TYPE,
            bizNo = "{{#salaryMonthRecord.id}}", success = HRM_SALARY_MONTH_COMPUTE_SUCCESS)
    public void computeMonthRecord(Long id, boolean syncInsuranceData, boolean syncAttendanceData,
                                   List<Map<Integer, String>> attendanceRows,
                                   List<Map<Integer, String>> additionalDeductionRows,
                                   List<Map<Integer, String>> cumulativeTaxRows) {
        // 1.1 校验工资表并构造计薪周期
        HrmSalaryMonthRecordDO monthRecord = validateMonthRecordEditableForUpdate(id);
        LocalDateTime[] salaryTimes = LocalDateTimeUtils.getDateTimeRange(
                monthRecord.getStartTime().toLocalDate(), monthRecord.getEndTime().toLocalDate());
        // 1.2 获得计薪周期内、且已分配薪资组的员工
        List<HrmEmployeeDO> payrollEmployees = getPayrollEmployeeList(monthRecord);
        Map<Long, HrmSalaryGroupDO> salaryGroupMap = salaryGroupService.getEmployeeSalaryGroupMap(payrollEmployees);
        payrollEmployees = filterList(payrollEmployees, employee -> salaryGroupMap.containsKey(employee.getId()));

        // 2. 加载工资项、考勤、社保和导入数据
        List<HrmSalaryOptionDO> optionList = salaryOptionService.getSalaryOptionList(false);
        Map<Integer, HrmSalaryOptionDO> optionMap = convertMap(optionList, HrmSalaryOptionDO::getCode);
        Map<String, AttendanceImportData> attendanceImportMap = syncAttendanceData
                ? Collections.emptyMap() : parseAttendanceImportData(attendanceRows, optionMap);
        Map<Long, HrmAttendanceMonthRecordRespVO> attendanceMap = syncAttendanceData
                ? attendanceStatisticsService.getAttendanceMonthRecordMap(
                        monthRecord.getYear(), monthRecord.getMonth(),
                        convertList(payrollEmployees, HrmEmployeeDO::getId))
                : Collections.emptyMap();
        HrmSalaryConfigDO salaryConfig = salaryConfigService.getSalaryConfig();
        YearMonth socialSecurityYearMonth = resolveSocialSecurityYearMonth(
                YearMonth.of(monthRecord.getYear(), monthRecord.getMonth()), salaryConfig);
        if (syncInsuranceData) {
            insuranceMonthRecordService.validateMonthRecordExists(
                    socialSecurityYearMonth.getYear(), socialSecurityYearMonth.getMonthValue());
        }
        Map<Long, HrmInsuranceMonthEmployeeRecordDO> insuranceMap = syncInsuranceData
                ? insuranceMonthEmployeeRecordService.getNormalMonthEmployeeRecordMap(
                        socialSecurityYearMonth.getYear(), socialSecurityYearMonth.getMonthValue())
                : Collections.emptyMap();
        Map<String, List<HrmSalaryOptionValueVO>> additionalDeductionImportMap =
                parseOptionImportData(additionalDeductionRows, getAdditionalDeductionImportColumns(), optionMap);
        Map<String, List<HrmSalaryOptionValueVO>> cumulativeTaxImportMap =
                parseOptionImportData(cumulativeTaxRows, getCumulativeTaxImportColumns(), optionMap);
        Map<Long, HrmSalaryTaxRuleDO> taxRuleMap = salaryTaxRuleService.getSalaryTaxRuleMap(
                convertSet(salaryGroupMap.values(), HrmSalaryGroupDO::getTaxRuleId));
        HrmSalaryTaxRuleDO defaultTaxRule = CollUtil.getFirst(salaryTaxRuleService.getSalaryTaxRuleList());

        // 3. 逐个员工核算工资
        List<HrmSalaryMonthEmployeeRecordDO> employeeRecords = new ArrayList<>();
        Map<Long, List<HrmSalaryOptionValueVO>> employeeOptionValueMap = new LinkedHashMap<>();
        Map<Long, HrmSalaryTaxRuleDO> employeeTaxRuleMap = new LinkedHashMap<>();
        for (HrmEmployeeDO employee : payrollEmployees) {
            // 3.1 获得本周期生效薪资；没有薪资档案时继承上月可维护薪资项
            String jobNumber = employee.getJobNumber();
            List<HrmSalaryOptionValueVO> values = BeanUtils.toBean(
                    salaryEmployeeInfoService.getEffectiveSalaryOptionList(employee, salaryTimes),
                    HrmSalaryOptionValueVO.class);
            if (CollUtil.isEmpty(values)) {
                YearMonth previousMonth = YearMonth.of(monthRecord.getYear(), monthRecord.getMonth()).minusMonths(1);
                HrmSalaryMonthEmployeeRecordDO previousEmployeeRecord = monthEmployeeRecordService
                        .getMonthEmployeeRecordByEmployeeIdAndYearMonth(employee.getId(), previousMonth.getYear(), previousMonth.getMonthValue());
                if (previousEmployeeRecord != null) {
                    values = convertList(previousEmployeeRecord.getOptionValues(),
                            optionValue -> new HrmSalaryOptionValueVO().setCode(optionValue.getCode()).setName(optionValue.getName()).setValue(optionValue.getValue()),
                            optionValue -> {
                                HrmSalaryOptionDO option = optionMap.get(optionValue.getCode());
                                return option != null && option.getParentCode() != null
                                        && option.getParentCode() >= HrmSalaryOptionDO.ADJUSTABLE_CATEGORY_MIN_CODE
                                        && option.getParentCode() <= HrmSalaryOptionDO.ADJUSTABLE_CATEGORY_MAX_CODE;
                            });
                }
            }

            // 3.2 合并考勤、社保、专项附加扣除和累计个税数据
            AttendanceImportData attendanceData = syncAttendanceData
                    ? buildAttendanceData(attendanceMap.get(employee.getId()), optionMap)
                    : jobNumber == null ? null : attendanceImportMap.get(jobNumber);
            if (attendanceData != null) {
                values = mergeOptionValues(values, attendanceData.getOptionValues());
            }
            if (syncInsuranceData) {
                values = mergeOptionValues(values, buildInsuranceOptionValues(insuranceMap.get(employee.getId()), optionMap));
            }
            if (jobNumber != null) {
                values = mergeOptionValues(values, additionalDeductionImportMap.getOrDefault(jobNumber, Collections.emptyList()));
                values = mergeOptionValues(values, cumulativeTaxImportMap.getOrDefault(jobNumber, Collections.emptyList()));
            }

            // 3.3 构建员工月工资记录，并准备批量核算所需数据
            HrmSalaryGroupDO salaryGroup = salaryGroupMap.get(employee.getId());
            HrmSalaryTaxRuleDO taxRule = taxRuleMap.getOrDefault(salaryGroup.getTaxRuleId(), defaultTaxRule);
            BigDecimal salaryStandard = defaultIfNull(salaryGroup.getSalaryStandard(), HrmSalaryGroupDO.DEFAULT_SALARY_STANDARD);
            employeeRecords.add(HrmSalaryMonthEmployeeRecordDO.builder()
                    .monthRecordId(id).employeeId(employee.getId())
                    .actualWorkDay(attendanceData == null ? salaryStandard : attendanceData.getActualWorkDay())
                    .needWorkDay(salaryStandard).year(monthRecord.getYear()).month(monthRecord.getMonth()).build());
            employeeOptionValueMap.put(employee.getId(), values);
            employeeTaxRuleMap.put(employee.getId(), taxRule);
        }
        employeeRecords = monthEmployeeRecordService.calculateMonthEmployeeRecordList(
                employeeRecords, employeeOptionValueMap, employeeTaxRuleMap);
        monthEmployeeRecordService.saveMonthEmployeeRecordList(id, employeeRecords);

        // 4. 更新工资表状态、表头和汇总
        monthRecordMapper.updateById(new HrmSalaryMonthRecordDO().setId(id).setStatus(HrmSalaryMonthRecordStatusEnum.COMPUTED.getStatus())
                .setOptionHeaders(buildOptionHeaderList(salaryOptionService.getSalaryOptionList(false, true))));
        updateMonthRecordSummary(id);

        // 5. 记录操作日志上下文
        LogRecordContext.putVariable("salaryMonthRecord", monthRecord);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_SALARY_MONTH_TYPE, subType = HRM_SALARY_MONTH_DELETE_SUB_TYPE,
            bizNo = "{{#salaryMonthRecord.id}}", success = HRM_SALARY_MONTH_DELETE_SUCCESS)
    public void deleteMonthRecord(Long id) {
        // 1. 校验仅删除最新工资表，并至少保留一张工资表
        HrmSalaryMonthRecordDO salaryMonthRecord = validateMonthRecordEditableForUpdate(id);
        HrmSalaryMonthRecordDO lastMonthRecord = monthRecordMapper.selectLast();
        if (lastMonthRecord == null || ObjectUtil.notEqual(lastMonthRecord.getId(), salaryMonthRecord.getId())) {
            throw exception(SALARY_MONTH_RECORD_NOT_LATEST);
        }
        if (monthRecordMapper.selectCount() <= 1) {
            throw exception(SALARY_MONTH_RECORD_CANNOT_DELETE_ONLY);
        }

        // 2.1 删除当前月员工工资记录和工资表
        monthEmployeeRecordService.deleteMonthEmployeeRecordListByMonthRecordId(id);
        monthRecordMapper.deleteById(id);
        // 2.2 恢复上一月工资表为已核算状态
        HrmSalaryMonthRecordDO previousMonthRecord = monthRecordMapper.selectLast();
        monthRecordMapper.updateById(new HrmSalaryMonthRecordDO()
                .setId(previousMonthRecord.getId()).setStatus(HrmSalaryMonthRecordStatusEnum.COMPUTED.getStatus()));

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("salaryMonthRecord", salaryMonthRecord);
    }

    @Override
    public HrmSalaryMonthRecordDO getMonthRecord(Long id) {
        return monthRecordMapper.selectById(id);
    }

    @Override
    public HrmSalaryMonthRecordDO getMonthRecordByYearMonth(Integer year, Integer month) {
        return monthRecordMapper.selectByYearMonth(year, month);
    }

    @Override
    public HrmSalaryMonthRecordDO getLastMonthRecord() {
        return monthRecordMapper.selectLast();
    }

    @Override
    public HrmSalaryPayrollReadinessRespVO getPayrollReadiness(Long monthRecordId) {
        // 1. 获得月度工资表和计薪周期内的员工
        HrmSalaryMonthRecordDO monthRecord = monthRecordId == null ? monthRecordMapper.selectLast()
                : monthRecordMapper.selectById(monthRecordId);
        if (monthRecord == null) {
            return null;
        }
        List<HrmEmployeeDO> validEmployees = getPayrollEmployeeList(monthRecord);

        // 2. 筛选已配置薪资组和已定薪的员工
        Map<Long, HrmSalaryGroupDO> salaryGroupMap =
                salaryGroupService.getEmployeeSalaryGroupMap(validEmployees);
        List<HrmEmployeeDO> payrollCandidates =
                filterList(validEmployees, employee -> salaryGroupMap.containsKey(employee.getId()));
        List<HrmEmployeeDO> noSalaryGroupEmployees =
                filterList(validEmployees, employee -> !salaryGroupMap.containsKey(employee.getId()));
        Set<Long> candidateIds = convertSet(payrollCandidates, HrmEmployeeDO::getId);
        Set<Long> salaryEmployeeIds = salaryEmployeeInfoService.getSalaryEmployeeInfoMap(candidateIds).keySet();

        // 3. 获得实际计薪员工和工资周期内的员工异动
        List<HrmSalaryMonthEmployeeRecordDO> employeeRecords = monthEmployeeRecordService
                .getMonthEmployeeRecordListByMonthRecordId(monthRecord.getId());
        Set<Long> payrollEmployeeIds = CollUtil.isEmpty(employeeRecords) ? candidateIds
                : convertSet(employeeRecords, HrmSalaryMonthEmployeeRecordDO::getEmployeeId);
        List<HrmEmployeeDO> noSalaryEmployees = filterList(payrollCandidates,
                employee -> employee.getId() != null && !salaryEmployeeIds.contains(employee.getId()));
        List<HrmEmployeeChangeRecordDO> changeRecords = employeeChangeRecordService
                .getEmployeeChangeRecordListByEmployeeIdsAndEffectTimeBetween(payrollEmployeeIds,
                        LocalDateTimeUtils.getDateTimeRange(monthRecord.getStartTime().toLocalDate(),
                                monthRecord.getEndTime().toLocalDate()));

        // 4. 构建核算准备情况
        HrmSalaryConfigDO salaryConfig = salaryConfigService.getSalaryConfig();
        YearMonth salaryYearMonth = YearMonth.of(monthRecord.getYear(), monthRecord.getMonth());
        List<HrmSalaryPayrollReadinessRespVO.Employee> noSalaryEmployeeVOs =
                buildPayrollReadinessEmployeeList(noSalaryEmployees);
        List<HrmSalaryPayrollReadinessRespVO.Employee> noSalaryGroupEmployeeVOs =
                buildPayrollReadinessEmployeeList(noSalaryGroupEmployees);
        return new HrmSalaryPayrollReadinessRespVO()
                .setMonthRecordId(monthRecord.getId()).setTitle(monthRecord.getTitle())
                .setYear(monthRecord.getYear()).setMonth(monthRecord.getMonth())
                .setStartTime(monthRecord.getStartTime()).setEndTime(monthRecord.getEndTime())
                .setSocialSecurityYearMonth(resolveSocialSecurityYearMonth(salaryYearMonth, salaryConfig).toString())
                .setPayrollEmployeeCount((long) payrollEmployeeIds.size())
                .setSalaryEmployeeCount((long) salaryEmployeeIds.size())
                .setNoSalaryEmployeeCount((long) noSalaryEmployeeVOs.size())
                .setNoSalaryGroupEmployeeCount((long) noSalaryGroupEmployeeVOs.size())
                .setChangeEmployeeCount((long) changeRecords.size())
                .setChangeTypeCountMap(buildPayrollReadinessChangeTypeCountMap(changeRecords))
                .setNoSalaryEmployees(noSalaryEmployeeVOs)
                .setNoSalaryGroupEmployees(noSalaryGroupEmployeeVOs);
    }

    private List<HrmSalaryPayrollReadinessRespVO.Employee> buildPayrollReadinessEmployeeList(
            List<HrmEmployeeDO> employees) {
        return convertList(employees, employee ->
                BeanUtils.toBean(employee, HrmSalaryPayrollReadinessRespVO.Employee.class)
                        .setEmployeeId(employee.getId()).setEmployeeName(employee.getName()));
    }

    @Override
    public List<HrmEmployeeDO> getPayrollEmployeeList(HrmSalaryMonthRecordDO monthRecord) {
        HrmEmployeeListReqVO employeeReqVO = new HrmEmployeeListReqVO();
        List<HrmEmployeeDO> employees = employeeService.getEmployeeList(employeeReqVO);
        employees = filterList(employees, employee -> (employee.getEntryTime() == null
                        || isBeforeOrEqual(employee.getEntryTime().toLocalDate(), monthRecord.getEndTime().toLocalDate()))
                        && (HrmEmployeeEntryStatusEnum.ACTIVE_STATUSES.contains(employee.getEntryStatus())
                        || Objects.equals(employee.getEntryStatus(), HrmEmployeeEntryStatusEnum.LEFT.getStatus())
                        && employee.getLeaveTime() != null
                        && isAfterOrEqual(employee.getLeaveTime().toLocalDate(), monthRecord.getStartTime().toLocalDate())));
        employees.sort(Comparator.comparing(HrmEmployeeDO::getId));
        return employees;
    }

    @Override
    public PageResult<HrmSalaryMonthRecordDO> getMonthRecordPage(HrmSalaryMonthRecordPageReqVO reqVO) {
        return monthRecordMapper.selectPage(reqVO);
    }

    @Override
    public List<HrmSalaryMonthRecordDO> getMonthRecordListByStatus(Integer status) {
        return monthRecordMapper.selectListByStatus(status);
    }

    @Override
    public HrmSalaryMonthRecordDO validateMonthRecordExists(Long id) {
        HrmSalaryMonthRecordDO monthRecord = monthRecordMapper.selectById(id);
        if (monthRecord == null) {
            throw exception(SALARY_MONTH_RECORD_NOT_EXISTS);
        }
        return monthRecord;
    }

    @Override
    public HrmSalaryMonthRecordDO validateMonthRecordExistsForUpdate(Long id) {
        HrmSalaryMonthRecordDO monthRecord = monthRecordMapper.selectByIdForUpdate(id);
        if (monthRecord == null) {
            throw exception(SALARY_MONTH_RECORD_NOT_EXISTS);
        }
        return monthRecord;
    }

    @Override
    public HrmSalaryMonthRecordDO validateMonthRecordEditable(Long id) {
        HrmSalaryMonthRecordDO monthRecord = validateMonthRecordExists(id);
        if (Objects.equals(monthRecord.getStatus(), HrmSalaryMonthRecordStatusEnum.HISTORY.getStatus())) {
            throw exception(SALARY_MONTH_RECORD_STATUS_INVALID);
        }
        return monthRecord;
    }

    @Override
    public HrmSalaryMonthRecordDO validateMonthRecordEditableForUpdate(Long id) {
        HrmSalaryMonthRecordDO monthRecord = validateMonthRecordExistsForUpdate(id);
        if (Objects.equals(monthRecord.getStatus(), HrmSalaryMonthRecordStatusEnum.HISTORY.getStatus())) {
            throw exception(SALARY_MONTH_RECORD_STATUS_INVALID);
        }
        return monthRecord;
    }

    @Override
    public void updateMonthRecordSummary(Long id) {
        // 1.1 校验工资表可编辑
        HrmSalaryMonthRecordDO monthRecord = validateMonthRecordEditable(id);
        // 1.2 计算员工工资汇总
        List<HrmSalaryMonthEmployeeRecordDO> employeeRecords = monthEmployeeRecordService
                .getMonthEmployeeRecordListByMonthRecordId(monthRecord.getId());
        Map<Integer, BigDecimal> optionSummary = convertMap(
                monthEmployeeRecordService.getMonthOptionSummary(employeeRecords),
                HrmSalaryMonthEmployeeRecordDO.OptionValue::getCode,
                option -> defaultIfNull(option.getValue(), BigDecimal.ZERO));

        // 2. 更新工资表汇总数据
        HrmSalaryMonthRecordDO updateObj = new HrmSalaryMonthRecordDO()
                .setId(id).setEmployeeCount(employeeRecords.size());
        updateObj.setExpectedPaySalary(priceAdd(convertList(employeeRecords,
                HrmSalaryMonthEmployeeRecordDO::getExpectedPaySalary).toArray(new BigDecimal[0])));
        updateObj.setPersonalTax(priceAdd(convertList(employeeRecords,
                HrmSalaryMonthEmployeeRecordDO::getPersonalTax).toArray(new BigDecimal[0])));
        updateObj.setRealPaySalary(priceAdd(convertList(employeeRecords,
                HrmSalaryMonthEmployeeRecordDO::getRealPaySalary).toArray(new BigDecimal[0])));
        updateObj.setPersonalInsuranceAmount(
                optionSummary.getOrDefault(HrmSalaryOptionCodeEnum.PERSONAL_INSURANCE.getCode(), BigDecimal.ZERO));
        updateObj.setPersonalProvidentFundAmount(
                optionSummary.getOrDefault(HrmSalaryOptionCodeEnum.PERSONAL_PROVIDENT_FUND.getCode(), BigDecimal.ZERO));
        updateObj.setCorporateInsuranceAmount(
                optionSummary.getOrDefault(HrmSalaryOptionCodeEnum.CORPORATE_INSURANCE.getCode(), BigDecimal.ZERO));
        updateObj.setCorporateProvidentFundAmount(
                optionSummary.getOrDefault(HrmSalaryOptionCodeEnum.CORPORATE_PROVIDENT_FUND.getCode(), BigDecimal.ZERO));
        monthRecordMapper.updateById(updateObj);
    }

    private AttendanceImportData buildAttendanceData(
            HrmAttendanceMonthRecordRespVO attendance, Map<Integer, HrmSalaryOptionDO> optionMap) {
        if (attendance == null) {
            return null;
        }
        List<HrmSalaryOptionValueVO> optionValues = Arrays.asList(
                importOptionValue(HrmSalaryOptionCodeEnum.OVERTIME_PAY.getCode(), optionMap, BigDecimal.ZERO),
                importOptionValue(HrmSalaryOptionCodeEnum.LATE_DEDUCTION.getCode(), optionMap,
                        defaultIfNull(attendance.getLateDeductAmount(), BigDecimal.ZERO)),
                importOptionValue(HrmSalaryOptionCodeEnum.EARLY_DEDUCTION.getCode(), optionMap,
                        defaultIfNull(attendance.getEarlyDeductAmount(), BigDecimal.ZERO)),
                importOptionValue(HrmSalaryOptionCodeEnum.ABSENTEEISM_DEDUCTION.getCode(), optionMap,
                        defaultIfNull(attendance.getAbsenteeismDeductAmount(), BigDecimal.ZERO)),
                importOptionValue(HrmSalaryOptionCodeEnum.LEAVE_DEDUCTION.getCode(), optionMap, BigDecimal.ZERO),
                importOptionValue(HrmSalaryOptionCodeEnum.MISSING_CARD_DEDUCTION.getCode(), optionMap,
                        defaultIfNull(attendance.getMisscardDeductAmount(), BigDecimal.ZERO)),
                importOptionValue(HrmSalaryOptionCodeEnum.COMPREHENSIVE_DEDUCTION.getCode(), optionMap, BigDecimal.ZERO),
                importOptionValue(HrmSalaryOptionCodeEnum.ATTENDANCE_DEDUCTION_TOTAL.getCode(), optionMap,
                        defaultIfNull(attendance.getAttendanceDeductAmount(), BigDecimal.ZERO)));
        return new AttendanceImportData(
                defaultIfNull(attendance.getActualDays(), BigDecimal.ZERO), optionValues);
    }

    private List<HrmSalaryOptionValueVO> buildInsuranceOptionValues(
            HrmInsuranceMonthEmployeeRecordDO insurance, Map<Integer, HrmSalaryOptionDO> optionMap) {
        if (insurance == null) {
            return Arrays.asList(
                    importOptionValue(HrmSalaryOptionCodeEnum.PERSONAL_INSURANCE.getCode(), optionMap, BigDecimal.ZERO),
                    importOptionValue(HrmSalaryOptionCodeEnum.PERSONAL_PROVIDENT_FUND.getCode(), optionMap, BigDecimal.ZERO),
                    importOptionValue(HrmSalaryOptionCodeEnum.CORPORATE_INSURANCE.getCode(), optionMap, BigDecimal.ZERO),
                    importOptionValue(HrmSalaryOptionCodeEnum.CORPORATE_PROVIDENT_FUND.getCode(), optionMap, BigDecimal.ZERO));
        }
        return Arrays.asList(
                importOptionValue(HrmSalaryOptionCodeEnum.PERSONAL_INSURANCE.getCode(), optionMap,
                        defaultIfNull(insurance.getPersonalInsuranceAmount(), BigDecimal.ZERO)),
                importOptionValue(HrmSalaryOptionCodeEnum.PERSONAL_PROVIDENT_FUND.getCode(), optionMap,
                        defaultIfNull(insurance.getPersonalProvidentFundAmount(), BigDecimal.ZERO)),
                importOptionValue(HrmSalaryOptionCodeEnum.CORPORATE_INSURANCE.getCode(), optionMap,
                        defaultIfNull(insurance.getCorporateInsuranceAmount(), BigDecimal.ZERO)),
                importOptionValue(HrmSalaryOptionCodeEnum.CORPORATE_PROVIDENT_FUND.getCode(), optionMap,
                        defaultIfNull(insurance.getCorporateProvidentFundAmount(), BigDecimal.ZERO)));
    }

    private Map<String, AttendanceImportData> parseAttendanceImportData(List<Map<Integer, String>> rows,
                                                                        Map<Integer, HrmSalaryOptionDO> optionMap) {
        // 1. 无导入数据时直接返回
        if (CollUtil.isEmpty(rows)) {
            return Collections.emptyMap();
        }

        // 2. 按工号解析考勤金额和实际出勤天数
        List<ImportColumn> columns = getAttendanceImportColumns();
        Map<String, AttendanceImportData> dataMap = new HashMap<>();
        for (Map<Integer, String> row : rows) {
            String jobNumber = getCell(row, 2);
            if (StrUtil.isBlank(jobNumber)) {
                continue;
            }
            List<HrmSalaryOptionValueVO> values = new ArrayList<>();
            BigDecimal attendanceDeductionTotal = BigDecimal.ZERO;
            for (ImportColumn column : columns) {
                BigDecimal amount = parseAmount(getCell(row, column.getIndex()), BigDecimal.ZERO);
                values.add(importOptionValue(column.getCode(), optionMap, amount));
                if (ObjectUtil.notEqual(column.getCode(), HrmSalaryOptionCodeEnum.OVERTIME_PAY.getCode())) {
                    attendanceDeductionTotal = attendanceDeductionTotal.add(amount);
                }
            }
            values.add(importOptionValue(HrmSalaryOptionCodeEnum.ATTENDANCE_DEDUCTION_TOTAL.getCode(), optionMap, attendanceDeductionTotal));
            dataMap.put(jobNumber, new AttendanceImportData(parseAmount(getCell(row, 11), BigDecimal.ZERO), values));
        }
        return dataMap;
    }

    /**
     * 按员工工号解析导入的薪资项金额
     *
     * @param rows 导入行数据
     * @param columns 导入列定义
     * @param optionMap 薪资项 Map
     * @return 工号与薪资项金额列表的映射
     */
    private Map<String, List<HrmSalaryOptionValueVO>> parseOptionImportData(
            List<Map<Integer, String>> rows, List<ImportColumn> columns, Map<Integer, HrmSalaryOptionDO> optionMap) {
        // 1. 无导入数据时直接返回
        if (CollUtil.isEmpty(rows)) {
            return Collections.emptyMap();
        }

        // 2. 按工号解析各导入列对应的薪资项金额
        Map<String, List<HrmSalaryOptionValueVO>> dataMap = new HashMap<>();
        for (Map<Integer, String> row : rows) {
            String jobNumber = getCell(row, 2);
            if (StrUtil.isBlank(jobNumber)) {
                continue;
            }
            List<HrmSalaryOptionValueVO> values = new ArrayList<>();
            for (ImportColumn column : columns) {
                values.add(importOptionValue(column.getCode(), optionMap,
                        parseAmount(getCell(row, column.getIndex()), BigDecimal.ZERO)));
            }
            dataMap.put(jobNumber, values);
        }
        return dataMap;
    }

    private List<ImportColumn> getAttendanceImportColumns() {
        return Arrays.asList(
                new ImportColumn(4, HrmSalaryOptionCodeEnum.OVERTIME_PAY.getCode()),
                new ImportColumn(5, HrmSalaryOptionCodeEnum.LATE_DEDUCTION.getCode()),
                new ImportColumn(6, HrmSalaryOptionCodeEnum.EARLY_DEDUCTION.getCode()),
                new ImportColumn(7, HrmSalaryOptionCodeEnum.ABSENTEEISM_DEDUCTION.getCode()),
                new ImportColumn(8, HrmSalaryOptionCodeEnum.LEAVE_DEDUCTION.getCode()),
                new ImportColumn(9, HrmSalaryOptionCodeEnum.MISSING_CARD_DEDUCTION.getCode()),
                new ImportColumn(10, HrmSalaryOptionCodeEnum.COMPREHENSIVE_DEDUCTION.getCode())
        );
    }

    private List<ImportColumn> getCumulativeTaxImportColumns() {
        return Arrays.asList(
                new ImportColumn(4, HrmSalaryOptionCodeEnum.LAST_MONTH_CUMULATIVE_INCOME.getCode()),
                new ImportColumn(5, HrmSalaryOptionCodeEnum.LAST_MONTH_CUMULATIVE_DEDUCT_EXPENSE.getCode()),
                new ImportColumn(6, HrmSalaryOptionCodeEnum.LAST_MONTH_CUMULATIVE_SPECIAL_DEDUCTION.getCode()),
                new ImportColumn(7, HrmSalaryOptionCodeEnum.LAST_MONTH_PREPAID_TAX.getCode())
        );
    }

    private List<ImportColumn> getAdditionalDeductionImportColumns() {
        return Arrays.asList(
                new ImportColumn(4, HrmSalaryOptionCodeEnum.CHILD_EDUCATION_DEDUCTION.getCode()),
                new ImportColumn(5, HrmSalaryOptionCodeEnum.HOUSE_RENT_DEDUCTION.getCode()),
                new ImportColumn(6, HrmSalaryOptionCodeEnum.HOUSE_LOAN_DEDUCTION.getCode()),
                new ImportColumn(7, HrmSalaryOptionCodeEnum.ELDERLY_SUPPORT_DEDUCTION.getCode()),
                new ImportColumn(8, HrmSalaryOptionCodeEnum.CONTINUING_EDUCATION_DEDUCTION.getCode())
        );
    }

    private HrmSalaryOptionValueVO importOptionValue(Integer code, Map<Integer, HrmSalaryOptionDO> optionMap,
                                                      BigDecimal amount) {
        HrmSalaryOptionDO option = optionMap.get(code);
        HrmSalaryOptionCodeEnum optionCode = HrmSalaryOptionCodeEnum.valueOf(code);
        String name = option != null ? option.getName() : optionCode != null ? optionCode.getName() : "薪资项" + code;
        return new HrmSalaryOptionValueVO().setCode(code).setName(name).setValue(amount);
    }

    private List<HrmSalaryOptionValueVO> mergeOptionValues(
            List<HrmSalaryOptionValueVO> baseValues,
            List<HrmSalaryOptionValueVO> importValues) {
        if (CollUtil.isEmpty(importValues)) {
            return baseValues;
        }
        Map<Integer, HrmSalaryOptionValueVO> valueMap = new LinkedHashMap<>();
        for (HrmSalaryOptionValueVO value : baseValues) {
            if (value.getCode() != null) {
                valueMap.put(value.getCode(), value);
            }
        }
        for (HrmSalaryOptionValueVO value : importValues) {
            if (value.getCode() != null) {
                valueMap.put(value.getCode(), value);
            }
        }
        return new ArrayList<>(valueMap.values());
    }

    private Map<Integer, Long> buildPayrollReadinessChangeTypeCountMap(List<HrmEmployeeChangeRecordDO> changeRecords) {
        Map<Integer, Long> changeTypeCountMap = new LinkedHashMap<>();
        changeTypeCountMap.put(HrmEmployeeChangeTypeEnum.REGULAR.getType(), 0L);
        changeTypeCountMap.put(HrmEmployeeChangeTypeEnum.TRANSFER.getType(), 0L);
        changeTypeCountMap.put(HrmEmployeeChangeTypeEnum.PROMOTION.getType(), 0L);
        changeTypeCountMap.put(HrmEmployeeChangeTypeEnum.DEMOTION.getType(), 0L);
        changeTypeCountMap.put(HrmEmployeeChangeTypeEnum.FULL_TIME.getType(), 0L);
        for (HrmEmployeeChangeRecordDO changeRecord : changeRecords) {
            if (changeRecord.getType() == null) {
                continue;
            }
            changeTypeCountMap.merge(changeRecord.getType(), 1L, Long::sum);
        }
        return changeTypeCountMap;
    }

    private LocalDate resolveMonthDay(YearMonth yearMonth, Integer configuredDay, int defaultDay) {
        int day = configuredDay == null ? defaultDay : configuredDay;
        return yearMonth.atDay(Math.max(1, Math.min(day, yearMonth.lengthOfMonth())));
    }

    private YearMonth resolveSocialSecurityYearMonth(YearMonth salaryYearMonth, HrmSalaryConfigDO salaryConfig) {
        if (salaryConfig == null || salaryConfig.getSocialSecurityMonthType() == null) {
            return salaryYearMonth;
        }
        if (HrmSalarySocialSecurityMonthTypeEnum.PREVIOUS_MONTH.getType().equals(salaryConfig.getSocialSecurityMonthType())) {
            return salaryYearMonth.minusMonths(1);
        }
        if (HrmSalarySocialSecurityMonthTypeEnum.NEXT_MONTH.getType().equals(salaryConfig.getSocialSecurityMonthType())) {
            return salaryYearMonth.plusMonths(1);
        }
        return salaryYearMonth;
    }

    private List<HrmSalaryMonthRecordDO.OptionHeader> buildOptionHeaderList(List<HrmSalaryOptionDO> options) {
        Map<Integer, List<HrmSalaryOptionDO>> childrenMap =
                convertMultiMap(options, HrmSalaryOptionDO::getParentCode);
        List<HrmSalaryOptionDO> categories =
                childrenMap.getOrDefault(ROOT_PARENT_CODE, Collections.emptyList());
        return convertList(categories, category -> {
            List<HrmSalaryMonthRecordDO.OptionHeader> children = convertList(
                    childrenMap.getOrDefault(category.getCode(), Collections.emptyList()),
                    option -> HrmSalaryMonthRecordDO.OptionHeader.builder()
                            .code(option.getCode()).name(option.getName())
                            .children(Collections.emptyList()).build());
            return HrmSalaryMonthRecordDO.OptionHeader.builder()
                    .code(category.getCode()).name(category.getName()).children(children).build();
        });
    }

    /**
     * 薪资导入列定义
     */
    @Getter
    @AllArgsConstructor
    @SuppressWarnings("ClassCanBeRecord")
    private static class ImportColumn {

        /**
         * Excel 列索引
         */
        private final int index;
        /**
         * 薪资项编码
         */
        private final int code;

    }

    /**
     * 员工考勤导入数据
     */
    @Getter
    @AllArgsConstructor
    @SuppressWarnings("ClassCanBeRecord")
    private static class AttendanceImportData {

        /**
         * 实际出勤天数
         */
        private final BigDecimal actualWorkDay;
        /**
         * 薪资项金额列表
         */
        private final List<HrmSalaryOptionValueVO> optionValues;

    }

}
