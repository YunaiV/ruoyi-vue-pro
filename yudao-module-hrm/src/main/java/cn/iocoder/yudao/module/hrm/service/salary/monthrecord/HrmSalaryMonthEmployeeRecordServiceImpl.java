package cn.iocoder.yudao.module.hrm.service.salary.monthrecord;

import cn.iocoder.yudao.module.hrm.service.salary.config.HrmSalaryGroupService;
import cn.iocoder.yudao.module.hrm.service.salary.config.HrmSalaryOptionService;
import cn.iocoder.yudao.module.hrm.service.salary.config.HrmSalaryTaxRuleService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.option.HrmSalaryOptionValueVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.employee.HrmSalaryEmployeeMonthRecordPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.employee.HrmSalaryMonthEmployeeRecordListReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.employee.HrmSalaryMonthEmployeeRecordPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.employee.HrmSalaryMonthEmployeeRecordUpdateReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.employee.HrmSalaryPerformanceCoefficientReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment.HrmEmployeeChangeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.performance.plan.HrmPerformancePlanDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.monthrecord.HrmSalaryMonthEmployeeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.monthrecord.HrmSalaryMonthRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryGroupDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryOptionDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryTaxRuleDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.salary.monthrecord.HrmSalaryMonthEmployeeRecordMapper;
import cn.iocoder.yudao.module.hrm.enums.employee.employment.HrmEmployeeChangeTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.employee.HrmSalaryEmployeeChangeTypeEnum;
import cn.iocoder.yudao.module.hrm.service.employee.employment.HrmEmployeeChangeRecordService;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.hrm.service.performance.assessment.HrmPerformanceAssessmentService;
import cn.iocoder.yudao.module.hrm.service.performance.plan.HrmPerformancePlanService;
import cn.iocoder.yudao.module.hrm.enums.salary.config.HrmSalaryOptionCodeEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.config.HrmSalaryOptionTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.config.HrmSalaryTaxCycleTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.config.HrmSalaryTaxRateEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.config.HrmSalaryTaxTypeEnum;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.isBetween;
import static cn.iocoder.yudao.framework.common.util.number.MoneyUtils.priceScale;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_DATA_ILLEGAL;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_MONTH_EMP_RECORD_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_SALARY_MONTH_EMPLOYEE_UPDATE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_SALARY_MONTH_EMPLOYEE_UPDATE_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_SALARY_MONTH_TYPE;

/**
 * HRM 员工月度工资 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmSalaryMonthEmployeeRecordServiceImpl implements HrmSalaryMonthEmployeeRecordService {

    private static final Set<Integer> EXPECTED_PAY_PARENT_CODES = new HashSet<>(Arrays.asList(
            10, 20, 30, 40, 50, 60, 70, 80, 90, 130, 140, 180, 200));
    private static final Integer WITHHOLDING_PARENT_CODE = 100;
    private static final Integer AFTER_TAX_ADD_PARENT_CODE = 150;
    private static final Integer AFTER_TAX_DEDUCT_PARENT_CODE = 160;
    private static final Integer SPECIAL_TAX_PARENT_CODE = 170;

    @Resource
    private HrmSalaryMonthEmployeeRecordMapper monthEmployeeRecordMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private HrmSalaryMonthRecordService monthRecordService;
    @Resource
    private HrmEmployeeService employeeService;
    @Resource
    private HrmEmployeeChangeRecordService employeeChangeRecordService;
    @Resource
    private HrmPerformanceAssessmentService performanceAssessmentService;
    @Resource
    private HrmPerformancePlanService performancePlanService;
    @Resource
    private HrmSalaryOptionService salaryOptionService;
    @Resource
    private HrmSalaryGroupService salaryGroupService;
    @Resource
    private HrmSalaryTaxRuleService salaryTaxRuleService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMonthEmployeeRecordList(
            Long monthRecordId, Collection<HrmSalaryMonthEmployeeRecordDO> employeeRecords) {
        // 1. 锁定月度工资表，避免并发核算生成重复的员工记录
        monthRecordService.validateMonthRecordEditableForUpdate(monthRecordId);
        // 2. 加载已有员工记录
        List<HrmSalaryMonthEmployeeRecordDO> oldRecords =
                monthEmployeeRecordMapper.selectListByMonthRecordId(monthRecordId);
        Map<Long, HrmSalaryMonthEmployeeRecordDO> oldRecordMap = convertMap(oldRecords,
                HrmSalaryMonthEmployeeRecordDO::getEmployeeId, record -> record,
                (firstRecord, lastRecord) -> lastRecord);

        // 3. 新增或更新员工记录
        Set<Long> retainedIds = new HashSet<>();
        for (HrmSalaryMonthEmployeeRecordDO employeeRecord : employeeRecords) {
            HrmSalaryMonthEmployeeRecordDO oldRecord = oldRecordMap.get(employeeRecord.getEmployeeId());
            if (oldRecord == null) {
                monthEmployeeRecordMapper.insert(employeeRecord);
                retainedIds.add(employeeRecord.getId());
                continue;
            }
            employeeRecord.setId(oldRecord.getId());
            monthEmployeeRecordMapper.updateById(employeeRecord);
            retainedIds.add(oldRecord.getId());
        }

        // 4. 删除不在工资表范围的员工记录
        Set<Long> removedIds = convertSet(oldRecords, HrmSalaryMonthEmployeeRecordDO::getId);
        removedIds.removeAll(retainedIds);
        if (CollUtil.isNotEmpty(removedIds)) {
            monthEmployeeRecordMapper.deleteByIds(removedIds);
        }
    }

    @Override
    public List<HrmSalaryMonthEmployeeRecordDO> calculateMonthEmployeeRecordList(
            List<HrmSalaryMonthEmployeeRecordDO> employeeRecords,
            Map<Long, List<HrmSalaryOptionValueVO>> employeeOptionValueMap,
            Map<Long, HrmSalaryTaxRuleDO> employeeTaxRuleMap) {
        if (CollUtil.isEmpty(employeeRecords)) {
            return Collections.emptyList();
        }
        Map<Integer, HrmSalaryOptionDO> optionMap = getSalaryOptionMap();
        for (HrmSalaryMonthEmployeeRecordDO employeeRecord : employeeRecords) {
            calculateMonthEmployeeRecord(employeeRecord,
                    employeeOptionValueMap.getOrDefault(employeeRecord.getEmployeeId(), Collections.emptyList()),
                    employeeTaxRuleMap.get(employeeRecord.getEmployeeId()), optionMap);
        }
        return employeeRecords;
    }

    @Override
    public HrmSalaryMonthEmployeeRecordDO calculateMonthEmployeeRecord(
            HrmSalaryMonthEmployeeRecordDO employeeRecord, List<HrmSalaryOptionValueVO> optionValues) {
        Map<Integer, HrmSalaryOptionDO> optionMap = getSalaryOptionMap();
        HrmSalaryTaxRuleDO taxRule = getEmployeeTaxRuleMap(
                Collections.singleton(employeeRecord.getEmployeeId())).get(employeeRecord.getEmployeeId());
        return calculateMonthEmployeeRecord(employeeRecord,
                mergeEditableOptionValues(employeeRecord.getOptionValues(), optionValues, optionMap),
                taxRule, optionMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_SALARY_MONTH_TYPE, subType = HRM_SALARY_MONTH_EMPLOYEE_UPDATE_SUB_TYPE,
            bizNo = "{{#salaryMonthRecord.id}}", success = HRM_SALARY_MONTH_EMPLOYEE_UPDATE_SUCCESS,
            condition = "{{#reqVOs != null && !#reqVOs.isEmpty()}}")
    public void updateMonthEmployeeRecordList(List<HrmSalaryMonthEmployeeRecordUpdateReqVO> reqVOs) {
        // 1. 校验员工月度工资记录属于同一张可编辑工资表
        if (CollUtil.isEmpty(reqVOs)) {
            return;
        }
        Set<Long> employeeRecordIds = convertSet(reqVOs, HrmSalaryMonthEmployeeRecordUpdateReqVO::getId);
        if (employeeRecordIds.size() != reqVOs.size()) {
            throw exception(SALARY_DATA_ILLEGAL);
        }
        List<HrmSalaryMonthEmployeeRecordDO> employeeRecords =
                monthEmployeeRecordMapper.selectByIds(employeeRecordIds);
        if (employeeRecords.size() != employeeRecordIds.size()) {
            throw exception(SALARY_MONTH_EMP_RECORD_NOT_EXISTS);
        }
        Set<Long> monthRecordIds = convertSet(
                employeeRecords, HrmSalaryMonthEmployeeRecordDO::getMonthRecordId);
        if (monthRecordIds.size() != 1) {
            throw exception(SALARY_DATA_ILLEGAL);
        }
        Long monthRecordId = CollUtil.getFirst(monthRecordIds);
        HrmSalaryMonthRecordDO salaryMonthRecord =
                monthRecordService.validateMonthRecordEditableForUpdate(monthRecordId);

        // 2.1 批量加载工资项和员工计税规则
        Map<Integer, HrmSalaryOptionDO> optionMap = getSalaryOptionMap();
        Map<Long, HrmSalaryTaxRuleDO> employeeTaxRuleMap = getEmployeeTaxRuleMap(
                convertSet(employeeRecords, HrmSalaryMonthEmployeeRecordDO::getEmployeeId));
        // 2.2 重新计算并批量更新员工月度工资
        Map<Long, HrmSalaryMonthEmployeeRecordDO> employeeRecordMap = employeeRecords.stream()
                .collect(Collectors.toMap(HrmSalaryMonthEmployeeRecordDO::getId, Function.identity()));
        for (HrmSalaryMonthEmployeeRecordUpdateReqVO reqVO : reqVOs) {
            HrmSalaryMonthEmployeeRecordDO employeeRecord = employeeRecordMap.get(reqVO.getId());
            monthEmployeeRecordMapper.updateById(calculateMonthEmployeeRecord(employeeRecord,
                    mergeEditableOptionValues(employeeRecord.getOptionValues(), reqVO.getOptionValues(), optionMap),
                    employeeTaxRuleMap.get(employeeRecord.getEmployeeId()), optionMap));
        }

        // 3. 统一更新月度工资表汇总
        monthRecordService.updateMonthRecordSummary(monthRecordId);

        // 4.1 构建操作日志上下文。只记录影响范围和薪资项名称，不记录任何薪资金额
        List<Long> affectedEmployeeIds = employeeRecords.stream()
                .map(HrmSalaryMonthEmployeeRecordDO::getEmployeeId).filter(Objects::nonNull)
                .distinct().sorted().collect(Collectors.toList());
        List<Long> displayedEmployeeIds = affectedEmployeeIds.size() > 20
                ? affectedEmployeeIds.subList(0, 20) : affectedEmployeeIds;
        String employeeScope = StrUtil.join("、", displayedEmployeeIds);
        if (affectedEmployeeIds.size() > displayedEmployeeIds.size()) {
            employeeScope += " 等 " + affectedEmployeeIds.size() + " 人";
        }
        List<String> changedOptionNames = reqVOs.stream()
                .flatMap(reqVO -> CollUtil.emptyIfNull(reqVO.getOptionValues()).stream())
                .map(HrmSalaryOptionValueVO::getCode).filter(Objects::nonNull)
                .distinct().sorted().map(optionMap::get).filter(Objects::nonNull)
                .map(HrmSalaryOptionDO::getName).filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
        List<String> displayedOptionNames = changedOptionNames.size() > 20
                ? changedOptionNames.subList(0, 20) : changedOptionNames;
        String changedOptionSummary = CollUtil.isEmpty(displayedOptionNames)
                ? "未指定可编辑薪资项" : StrUtil.join("、", displayedOptionNames);
        if (changedOptionNames.size() > displayedOptionNames.size()) {
            changedOptionSummary += " 等 " + changedOptionNames.size() + " 项";
        }

        // 4.2 记录操作日志上下文
        LogRecordContext.putVariable("salaryMonthRecord", salaryMonthRecord);
        LogRecordContext.putVariable("employeeCount", affectedEmployeeIds.size());
        LogRecordContext.putVariable("employeeScope", employeeScope);
        LogRecordContext.putVariable("changedOptionSummary", changedOptionSummary);
    }

    @Override
    public void deleteMonthEmployeeRecordListByMonthRecordId(Long monthRecordId) {
        monthEmployeeRecordMapper.deleteByMonthRecordId(monthRecordId);
    }

    @Override
    public PageResult<HrmSalaryMonthEmployeeRecordDO> getMonthEmployeeRecordPage(
            HrmSalaryMonthEmployeeRecordPageReqVO reqVO) {
        // 1.1 获得月度工资表
        HrmSalaryMonthRecordDO monthRecord = monthRecordService.getMonthRecord(reqVO.getMonthRecordId());
        if (monthRecord == null) {
            return PageResult.empty();
        }
        // 1.2 获得符合员工信息和异动条件的员工编号
        Set<Long> employeeIds = reqVO.getEmployeeIds() == null
                ? resolveMonthEmployeeRecordEmployeeIds(reqVO, monthRecord, true)
                : new HashSet<>(reqVO.getEmployeeIds());
        if (employeeIds != null && CollUtil.isEmpty(employeeIds)) {
            return PageResult.empty();
        }
        HrmSalaryMonthEmployeeRecordPageReqVO queryReqVO = BeanUtils.toBean(
                reqVO, HrmSalaryMonthEmployeeRecordPageReqVO.class);
        if (employeeIds != null) {
            queryReqVO.setEmployeeIds(new ArrayList<>(employeeIds));
        }

        // 2. 查询员工月度工资分页
        return monthEmployeeRecordMapper.selectPage(queryReqVO);
    }

    @Override
    public List<HrmSalaryMonthEmployeeRecordDO> getMonthEmployeeRecordList(
            HrmSalaryMonthEmployeeRecordListReqVO reqVO) {
        // 1.1 获得月度工资表
        HrmSalaryMonthRecordDO monthRecord = monthRecordService.getMonthRecord(reqVO.getMonthRecordId());
        if (monthRecord == null) {
            return Collections.emptyList();
        }
        // 1.2 获得符合员工信息和异动条件的员工编号
        Set<Long> employeeIds = reqVO.getEmployeeIds() == null
                ? resolveMonthEmployeeRecordEmployeeIds(reqVO, monthRecord)
                : new HashSet<>(reqVO.getEmployeeIds());
        if (employeeIds != null && CollUtil.isEmpty(employeeIds)) {
            return Collections.emptyList();
        }
        HrmSalaryMonthEmployeeRecordListReqVO queryReqVO = BeanUtils.toBean(
                reqVO, HrmSalaryMonthEmployeeRecordListReqVO.class);
        if (employeeIds != null) {
            queryReqVO.setEmployeeIds(new ArrayList<>(employeeIds));
        }

        // 2. 查询员工月度工资列表
        return monthEmployeeRecordMapper.selectList(queryReqVO);
    }

    @Override
    public List<HrmSalaryMonthEmployeeRecordDO> getMonthEmployeeRecordListByMonthRecordId(Long monthRecordId) {
        return monthEmployeeRecordMapper.selectListByMonthRecordId(monthRecordId);
    }

    @Override
    public HrmSalaryMonthEmployeeRecordDO getMonthEmployeeRecordByEmployeeIdAndYearMonth(
            Long employeeId, Integer year, Integer month) {
        return monthEmployeeRecordMapper.selectByEmployeeIdAndYearMonth(employeeId, year, month);
    }

    @Override
    public List<HrmSalaryMonthEmployeeRecordDO.OptionValue> getMonthOptionSummary(
            List<HrmSalaryMonthEmployeeRecordDO> employeeRecords) {
        Map<Integer, HrmSalaryMonthEmployeeRecordDO.OptionValue> summaryMap = new LinkedHashMap<>();
        for (HrmSalaryMonthEmployeeRecordDO employeeRecord : employeeRecords) {
            for (HrmSalaryMonthEmployeeRecordDO.OptionValue optionValue :
                    CollUtil.emptyIfNull(employeeRecord.getOptionValues())) {
                HrmSalaryMonthEmployeeRecordDO.OptionValue summary = summaryMap.computeIfAbsent(
                        optionValue.getCode(), code -> HrmSalaryMonthEmployeeRecordDO.OptionValue.builder()
                                .code(code).name(optionValue.getName()).value(BigDecimal.ZERO).build());
                summary.setValue(summary.getValue().add(nvl(optionValue.getValue())));
            }
        }
        return summaryMap.values().stream()
                .sorted(Comparator.comparing(HrmSalaryMonthEmployeeRecordDO.OptionValue::getCode))
                .collect(Collectors.toList());
    }

    @Override
    public Map<Integer, Long> getMonthEmployeeChangeCount(HrmSalaryMonthEmployeeRecordPageReqVO reqVO) {
        // 1.1 获得月度工资表
        HrmSalaryMonthRecordDO monthRecord = monthRecordService.getMonthRecord(reqVO.getMonthRecordId());
        if (monthRecord == null) {
            return Collections.emptyMap();
        }
        // 1.2 按其他查询条件筛选员工
        Set<Long> employeeIds = resolveMonthEmployeeRecordEmployeeIds(reqVO, monthRecord, false);
        if (employeeIds == null) {
            employeeIds = convertSet(monthEmployeeRecordMapper.selectListByMonthRecordId(reqVO.getMonthRecordId()),
                    HrmSalaryMonthEmployeeRecordDO::getEmployeeId);
        }
        Set<Long> filteredEmployeeIds = employeeIds;

        // 2. 统计各异动分类数量
        return convertMap(Arrays.asList(HrmSalaryEmployeeChangeTypeEnum.values()),
                HrmSalaryEmployeeChangeTypeEnum::getType,
                type -> (long) filterEmployeeIdsByChangeType(filteredEmployeeIds, monthRecord, type).size(),
                (first, second) -> first, LinkedHashMap::new);
    }

    @Override
    public Map<Long, BigDecimal> getPerformanceCoefficientMap(HrmSalaryPerformanceCoefficientReqVO reqVO) {
        return getPerformanceCoefficientMap(reqVO.getYear(), reqVO.getMonth(), reqVO.getEmployeeIds());
    }

    @Override
    public PageResult<HrmSalaryMonthEmployeeRecordDO> getEmployeeMonthRecordPage(
            HrmSalaryEmployeeMonthRecordPageReqVO reqVO) {
        // 1. 获得符合状态条件的月度工资表编号
        List<HrmSalaryMonthRecordDO> monthRecords =
                monthRecordService.getMonthRecordListByStatus(reqVO.getMonthRecordStatus());
        if (CollUtil.isEmpty(monthRecords)) {
            return PageResult.empty();
        }
        List<Long> monthRecordIds = convertList(monthRecords, HrmSalaryMonthRecordDO::getId);

        // 2. 查询指定员工的月度工资分页
        return monthEmployeeRecordMapper.selectPageByEmployeeId(reqVO, monthRecordIds);
    }

    private HrmSalaryMonthEmployeeRecordDO calculateMonthEmployeeRecord(
            HrmSalaryMonthEmployeeRecordDO employeeRecord, List<HrmSalaryOptionValueVO> optionValues,
            HrmSalaryTaxRuleDO taxRule, Map<Integer, HrmSalaryOptionDO> optionMap) {
        List<HrmSalaryOptionValueVO> values = fillLastMonthCumulativeValues(
                new ArrayList<>(optionValues), employeeRecord.getEmployeeId(),
                employeeRecord.getYear(), employeeRecord.getMonth(), taxRule, optionMap);
        SalaryComputeResult result = computeSalary(values, optionMap, taxRule);
        return employeeRecord.setExpectedPaySalary(result.getExpectedPaySalary())
                .setTaxableSalary(result.getTaxableSalary()).setPersonalTax(result.getPersonalTax())
                .setRealPaySalary(result.getRealPaySalary())
                .setOptionValues(buildMonthOptionValues(result.getOptionValues()));
    }

    private List<HrmSalaryMonthEmployeeRecordDO.OptionValue> buildMonthOptionValues(
            List<HrmSalaryOptionValueVO> optionValues) {
        return convertList(optionValues, optionValue -> HrmSalaryMonthEmployeeRecordDO.OptionValue.builder()
                .code(optionValue.getCode()).name(optionValue.getName()).value(nvl(optionValue.getValue()))
                .build());
    }

    private SalaryComputeResult computeSalary(List<HrmSalaryOptionValueVO> sourceValues,
                                              Map<Integer, HrmSalaryOptionDO> optionMap,
                                              HrmSalaryTaxRuleDO taxRule) {
        List<HrmSalaryOptionValueVO> values = normalizeComputeSourceValues(sourceValues);
        SalaryBaseAmounts baseAmounts = calculateSalaryBaseAmounts(values, optionMap);
        BigDecimal expected = baseAmounts.getExpectedPaySalary();
        BigDecimal taxable;
        BigDecimal personalTax;
        if (taxRule == null || Boolean.FALSE.equals(taxRule.getTaxEnabled())
                || Objects.equals(taxRule.getType(), HrmSalaryTaxTypeEnum.NONE.getType())) {
            taxable = BigDecimal.ZERO;
            personalTax = BigDecimal.ZERO;
        } else if (Objects.equals(taxRule.getType(), HrmSalaryTaxTypeEnum.REMUNERATION.getType())) {
            taxable = calculateRemunerationTaxable(
                    baseAmounts.getTaxableIncome(), nvl(taxRule.getThreshold()), taxRule.getDecimalScale());
            personalTax = calculateRemunerationTax(taxable, taxRule.getDecimalScale());
        } else {
            BigDecimal threshold = nvl(taxRule.getThreshold());
            taxable = baseAmounts.getTaxableBeforeThreshold().subtract(threshold).max(BigDecimal.ZERO);
            personalTax = appendCumulativeSalaryTaxValues(values, optionMap, baseAmounts, threshold,
                    taxRule.getDecimalScale());
        }
        BigDecimal realPay;
        if (taxRule == null || Boolean.FALSE.equals(taxRule.getTaxEnabled())
                || Objects.equals(taxRule.getType(), HrmSalaryTaxTypeEnum.NONE.getType())) {
            realPay = expected.subtract(baseAmounts.getCurrentSpecialDeduction())
                    .add(baseAmounts.getAfterTaxAdjustment());
        } else if (Objects.equals(taxRule.getType(), HrmSalaryTaxTypeEnum.REMUNERATION.getType())) {
            realPay = expected.subtract(personalTax).add(baseAmounts.getAfterTaxAdjustment());
        } else {
            realPay = expected.subtract(baseAmounts.getCurrentSpecialDeduction()).subtract(personalTax)
                    .add(baseAmounts.getAfterTaxAdjustment());
        }
        appendComputedValue(values, HrmSalaryOptionCodeEnum.EXPECTED_PAY.getCode(), "应发工资", expected);
        appendComputedValue(values, HrmSalaryOptionCodeEnum.TAXABLE.getCode(), "应税工资", taxable);
        appendComputedValue(values, HrmSalaryOptionCodeEnum.PERSONAL_TAX.getCode(), "个人所得税", personalTax);
        appendComputedValue(values, HrmSalaryOptionCodeEnum.REAL_PAY.getCode(), "实发工资", realPay);
        return new SalaryComputeResult(expected, taxable, personalTax, realPay, values);
    }

    private SalaryBaseAmounts calculateSalaryBaseAmounts(List<HrmSalaryOptionValueVO> values,
                                                         Map<Integer, HrmSalaryOptionDO> optionMap) {
        BigDecimal expected = BigDecimal.ZERO;
        BigDecimal taxableExpected = BigDecimal.ZERO;
        BigDecimal currentSpecialDeduction = BigDecimal.ZERO;
        BigDecimal specialTaxSalary = BigDecimal.ZERO;
        BigDecimal afterTaxAdjustment = BigDecimal.ZERO;
        for (HrmSalaryOptionValueVO value : values) {
            HrmSalaryOptionDO option = optionMap.get(value.getCode());
            if (option == null) {
                continue;
            }
            BigDecimal amount = nvl(value.getValue());
            if (Objects.equals(option.getParentCode(), WITHHOLDING_PARENT_CODE)) {
                currentSpecialDeduction = currentSpecialDeduction.add(amount);
            }
            if (Objects.equals(option.getParentCode(), SPECIAL_TAX_PARENT_CODE)
                    && Boolean.TRUE.equals(option.getTaxEnabled())) {
                specialTaxSalary = specialTaxSalary.add(amount);
            }
            if (Objects.equals(option.getParentCode(), AFTER_TAX_ADD_PARENT_CODE)) {
                afterTaxAdjustment = afterTaxAdjustment.add(amount);
            }
            if (Objects.equals(option.getParentCode(), AFTER_TAX_DEDUCT_PARENT_CODE)) {
                afterTaxAdjustment = afterTaxAdjustment.subtract(amount);
            }
            if (EXPECTED_PAY_PARENT_CODES.contains(option.getParentCode())) {
                boolean minus = Objects.equals(option.getType(), HrmSalaryOptionTypeEnum.MINUS.getType());
                expected = minus ? expected.subtract(amount) : expected.add(amount);
                if (Boolean.TRUE.equals(option.getTaxEnabled())) {
                    taxableExpected = minus ? taxableExpected.subtract(amount) : taxableExpected.add(amount);
                }
            }
        }
        BigDecimal taxableIncome = taxableExpected.add(specialTaxSalary);
        BigDecimal taxableBeforeThreshold = taxableIncome
                .subtract(currentSpecialDeduction);
        return new SalaryBaseAmounts(expected, taxableIncome, taxableBeforeThreshold,
                currentSpecialDeduction, afterTaxAdjustment);
    }

    private List<HrmSalaryOptionValueVO> normalizeComputeSourceValues(
            List<HrmSalaryOptionValueVO> sourceValues) {
        Map<Integer, HrmSalaryOptionValueVO> valueMap = new LinkedHashMap<>();
        for (HrmSalaryOptionValueVO sourceValue : sourceValues) {
            if (sourceValue.getCode() == null || HrmSalaryOptionCodeEnum.COMPUTED_CODES.contains(sourceValue.getCode())) {
                continue;
            }
            valueMap.put(sourceValue.getCode(), sourceValue);
        }
        return new ArrayList<>(valueMap.values());
    }

    private BigDecimal appendCumulativeSalaryTaxValues(List<HrmSalaryOptionValueVO> values,
                                                       Map<Integer, HrmSalaryOptionDO> optionMap,
                                                       SalaryBaseAmounts baseAmounts, BigDecimal threshold,
                                                       Integer decimalScale) {
        BigDecimal cumulativeIncome = getOptionValue(values, HrmSalaryOptionCodeEnum.LAST_MONTH_CUMULATIVE_INCOME.getCode())
                .add(baseAmounts.getTaxableIncome());
        BigDecimal cumulativeDeductExpense = getOptionValue(values, HrmSalaryOptionCodeEnum.LAST_MONTH_CUMULATIVE_DEDUCT_EXPENSE.getCode())
                .add(threshold);
        BigDecimal cumulativeSpecialDeduction = getOptionValue(values, HrmSalaryOptionCodeEnum.LAST_MONTH_CUMULATIVE_SPECIAL_DEDUCTION.getCode())
                .add(baseAmounts.getCurrentSpecialDeduction());
        BigDecimal cumulativeAdditionalDeduction = calculateCumulativeAdditionalDeduction(values, optionMap);
        BigDecimal cumulativeTaxable = cumulativeIncome.subtract(cumulativeDeductExpense)
                .subtract(cumulativeSpecialDeduction).subtract(cumulativeAdditionalDeduction);
        BigDecimal cumulativeTax = calculateCumulativeSalaryTax(cumulativeTaxable, decimalScale);
        BigDecimal personalTax = cumulativeTax.subtract(
                getOptionValue(values, HrmSalaryOptionCodeEnum.LAST_MONTH_PREPAID_TAX.getCode()));

        appendComputedValue(values, HrmSalaryOptionCodeEnum.CURRENT_CUMULATIVE_INCOME.getCode(), "累计收入额", cumulativeIncome);
        appendComputedValue(values, HrmSalaryOptionCodeEnum.CURRENT_CUMULATIVE_DEDUCT_EXPENSE.getCode(), "累计减除费用", cumulativeDeductExpense);
        appendComputedValue(values, HrmSalaryOptionCodeEnum.CURRENT_CUMULATIVE_SPECIAL_DEDUCTION.getCode(), "累计专项扣除",
                cumulativeSpecialDeduction);
        appendComputedValue(values, HrmSalaryOptionCodeEnum.CURRENT_CUMULATIVE_ADDITIONAL_DEDUCTION.getCode(),
                "累计专项附加扣除", cumulativeAdditionalDeduction);
        appendComputedValue(values, HrmSalaryOptionCodeEnum.CURRENT_CUMULATIVE_TAXABLE.getCode(), "累计应纳税所得额", cumulativeTaxable);
        appendComputedValue(values, HrmSalaryOptionCodeEnum.CURRENT_CUMULATIVE_TAX.getCode(), "累计应纳税额", cumulativeTax);
        return personalTax.setScale(decimalScale == null ? 2 : decimalScale, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateCumulativeAdditionalDeduction(List<HrmSalaryOptionValueVO> values,
                                                               Map<Integer, HrmSalaryOptionDO> optionMap) {
        return values.stream()
                .filter(value -> {
                    HrmSalaryOptionDO option = optionMap.get(value.getCode());
                    return option != null && Objects.equals(option.getParentCode(), 260);
                })
                .map(value -> nvl(value.getValue()).max(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getOptionValue(List<HrmSalaryOptionValueVO> values, Integer code) {
        return values.stream().filter(value -> Objects.equals(value.getCode(), code)).findFirst()
                .map(HrmSalaryOptionValueVO::getValue).map(HrmSalaryMonthEmployeeRecordServiceImpl::nvl)
                .orElse(BigDecimal.ZERO).max(BigDecimal.ZERO);
    }

    private void appendComputedValue(List<HrmSalaryOptionValueVO> values, Integer code,
                                     String name, BigDecimal value) {
        values.removeIf(item -> Objects.equals(item.getCode(), code));
        values.add(new HrmSalaryOptionValueVO().setCode(code).setName(name).setValue(priceScale(value)));
    }

    private BigDecimal calculateCumulativeSalaryTax(BigDecimal taxable, Integer decimalScale) {
        if (taxable.signum() < 0) {
            return BigDecimal.ZERO.setScale(getTaxDecimalScale(decimalScale), RoundingMode.HALF_UP);
        }
        HrmSalaryTaxRateEnum taxRate = HrmSalaryTaxRateEnum.valueOf(
                HrmSalaryTaxTypeEnum.SALARY.getType(), taxable);
        return calculateTax(taxable, taxRate, decimalScale);
    }

    private BigDecimal calculateRemunerationTaxable(
            BigDecimal expected, BigDecimal threshold, Integer decimalScale) {
        int scale = getTaxDecimalScale(decimalScale);
        if (expected.compareTo(BigDecimal.valueOf(4000)) <= 0) {
            return expected.subtract(threshold).max(BigDecimal.ZERO)
                    .setScale(scale, RoundingMode.HALF_UP);
        }
        return expected.multiply(BigDecimal.valueOf(0.8)).max(BigDecimal.ZERO)
                .setScale(scale, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateRemunerationTax(BigDecimal taxable, Integer decimalScale) {
        HrmSalaryTaxRateEnum taxRate = HrmSalaryTaxRateEnum.valueOf(
                HrmSalaryTaxTypeEnum.REMUNERATION.getType(), taxable);
        return calculateTax(taxable, taxRate, decimalScale);
    }

    private BigDecimal calculateTax(BigDecimal taxable, HrmSalaryTaxRateEnum taxRate,
                                    Integer decimalScale) {
        int scale = getTaxDecimalScale(decimalScale);
        return taxable.multiply(BigDecimal.valueOf(taxRate.getRate()))
                .divide(BigDecimal.valueOf(100), scale, RoundingMode.UP)
                .subtract(taxRate.getQuickDeduction()).setScale(scale, RoundingMode.HALF_UP);
    }

    private int getTaxDecimalScale(Integer decimalScale) {
        return decimalScale == null ? 2 : decimalScale;
    }

    private List<HrmSalaryOptionValueVO> fillLastMonthCumulativeValues(
            List<HrmSalaryOptionValueVO> sourceValues, Long employeeId, Integer year, Integer month,
            HrmSalaryTaxRuleDO taxRule, Map<Integer, HrmSalaryOptionDO> optionMap) {
        if (!isSalaryTaxRule(taxRule)) {
            return sourceValues;
        }
        Set<Integer> existingCodes = convertSet(sourceValues, HrmSalaryOptionValueVO::getCode);
        Map<Integer, BigDecimal> lastMonthValues = getLastMonthCumulativeValues(
                employeeId, year, month, taxRule, optionMap);
        List<HrmSalaryOptionValueVO> missingValues = HrmSalaryOptionCodeEnum.LAST_MONTH_CUMULATIVE_CODES.stream()
                .filter(code -> !existingCodes.contains(code))
                .map(code -> buildOptionValue(code, lastMonthValues.getOrDefault(code, BigDecimal.ZERO), optionMap))
                .collect(Collectors.toList());
        return mergeOptionValues(sourceValues, missingValues);
    }

    private boolean isSalaryTaxRule(HrmSalaryTaxRuleDO taxRule) {
        return taxRule != null && ObjectUtil.notEqual(Boolean.FALSE, taxRule.getTaxEnabled())
                && (taxRule.getType() == null || Objects.equals(taxRule.getType(), HrmSalaryTaxTypeEnum.SALARY.getType()));
    }

    private Map<Integer, BigDecimal> getLastMonthCumulativeValues(
            Long employeeId, Integer year, Integer month, HrmSalaryTaxRuleDO taxRule,
            Map<Integer, HrmSalaryOptionDO> optionMap) {
        Map<Integer, BigDecimal> result = HrmSalaryOptionCodeEnum.LAST_MONTH_CUMULATIVE_CODES.stream()
                .collect(Collectors.toMap(Function.identity(), ignored -> BigDecimal.ZERO,
                        (first, second) -> first, LinkedHashMap::new));
        if (employeeId == null || year == null || month == null) {
            return result;
        }
        int cycleStartMonth = Objects.equals(taxRule.getCycleType(), HrmSalaryTaxCycleTypeEnum.DECEMBER_TO_NOVEMBER.getType()) ? 12 : 1;
        if (month == cycleStartMonth) {
            return result;
        }
        YearMonth lastYearMonth = YearMonth.of(year, month).minusMonths(1);
        HrmSalaryMonthEmployeeRecordDO lastRecord = monthEmployeeRecordMapper
                .selectByEmployeeIdAndYearMonth(employeeId, lastYearMonth.getYear(), lastYearMonth.getMonthValue());
        if (lastRecord == null) {
            return result;
        }
        List<HrmSalaryOptionValueVO> lastOptionValues = BeanUtils.toBean(
                CollUtil.emptyIfNull(lastRecord.getOptionValues()), HrmSalaryOptionValueVO.class);
        Map<Integer, BigDecimal> lastOptionMap = lastOptionValues.stream()
                .filter(value -> value.getCode() != null)
                .collect(Collectors.toMap(HrmSalaryOptionValueVO::getCode,
                        value -> nvl(value.getValue()), (first, second) -> second));

        SalaryBaseAmounts lastBaseAmounts = calculateSalaryBaseAmounts(lastOptionValues, optionMap);
        BigDecimal legacyIncome = lastOptionMap
                .getOrDefault(HrmSalaryOptionCodeEnum.LAST_MONTH_CUMULATIVE_INCOME.getCode(), BigDecimal.ZERO)
                .add(lastBaseAmounts.getExpectedPaySalary());
        BigDecimal legacyDeductExpense = lastOptionMap
                .getOrDefault(HrmSalaryOptionCodeEnum.LAST_MONTH_CUMULATIVE_DEDUCT_EXPENSE.getCode(), BigDecimal.ZERO)
                .add(nvl(taxRule.getThreshold()));
        BigDecimal legacySpecialDeduction = lastOptionMap
                .getOrDefault(HrmSalaryOptionCodeEnum.LAST_MONTH_CUMULATIVE_SPECIAL_DEDUCTION.getCode(), BigDecimal.ZERO)
                .add(lastBaseAmounts.getCurrentSpecialDeduction());
        BigDecimal legacyPrepaidTax = lastOptionMap
                .getOrDefault(HrmSalaryOptionCodeEnum.LAST_MONTH_PREPAID_TAX.getCode(), BigDecimal.ZERO)
                .add(nvl(lastRecord.getPersonalTax()));

        result.put(HrmSalaryOptionCodeEnum.LAST_MONTH_CUMULATIVE_INCOME.getCode(),
                lastOptionMap.getOrDefault(HrmSalaryOptionCodeEnum.CURRENT_CUMULATIVE_INCOME.getCode(), legacyIncome));
        result.put(HrmSalaryOptionCodeEnum.LAST_MONTH_CUMULATIVE_DEDUCT_EXPENSE.getCode(),
                lastOptionMap.getOrDefault(HrmSalaryOptionCodeEnum.CURRENT_CUMULATIVE_DEDUCT_EXPENSE.getCode(), legacyDeductExpense));
        result.put(HrmSalaryOptionCodeEnum.LAST_MONTH_CUMULATIVE_SPECIAL_DEDUCTION.getCode(),
                lastOptionMap.getOrDefault(HrmSalaryOptionCodeEnum.CURRENT_CUMULATIVE_SPECIAL_DEDUCTION.getCode(), legacySpecialDeduction));
        result.put(HrmSalaryOptionCodeEnum.LAST_MONTH_PREPAID_TAX.getCode(),
                lastOptionMap.getOrDefault(HrmSalaryOptionCodeEnum.CURRENT_CUMULATIVE_TAX.getCode(), legacyPrepaidTax));
        return result;
    }

    private List<HrmSalaryOptionValueVO> mergeEditableOptionValues(
            List<HrmSalaryMonthEmployeeRecordDO.OptionValue> currentValues,
            List<HrmSalaryOptionValueVO> updateValues, Map<Integer, HrmSalaryOptionDO> optionMap) {
        Map<Integer, HrmSalaryOptionValueVO> valueMap = new LinkedHashMap<>();
        for (HrmSalaryMonthEmployeeRecordDO.OptionValue currentValue : CollUtil.emptyIfNull(currentValues)) {
            HrmSalaryOptionValueVO value = new HrmSalaryOptionValueVO();
            value.setCode(currentValue.getCode()).setName(currentValue.getName()).setValue(currentValue.getValue());
            valueMap.put(value.getCode(), value);
        }
        for (HrmSalaryOptionValueVO updateValue : CollUtil.emptyIfNull(updateValues)) {
            HrmSalaryOptionDO option = optionMap.get(updateValue.getCode());
            if (option == null || HrmSalaryOptionCodeEnum.COMPUTED_CODES.contains(option.getCode())) {
                continue;
            }
            HrmSalaryOptionValueVO value = new HrmSalaryOptionValueVO();
            value.setCode(option.getCode()).setName(option.getName()).setValue(nvl(updateValue.getValue()));
            valueMap.put(value.getCode(), value);
        }
        return valueMap.values().stream()
                .sorted(Comparator.comparing(HrmSalaryOptionValueVO::getCode))
                .collect(Collectors.toList());
    }

    private List<HrmSalaryOptionValueVO> mergeOptionValues(
            List<HrmSalaryOptionValueVO> baseValues, List<HrmSalaryOptionValueVO> addedValues) {
        if (CollUtil.isEmpty(addedValues)) {
            return baseValues;
        }
        Map<Integer, HrmSalaryOptionValueVO> valueMap = convertMap(
                baseValues, HrmSalaryOptionValueVO::getCode, Function.identity(), (first, second) -> second,
                LinkedHashMap::new);
        valueMap.putAll(convertMap(addedValues, HrmSalaryOptionValueVO::getCode, Function.identity()));
        return new ArrayList<>(valueMap.values());
    }

    private HrmSalaryOptionValueVO buildOptionValue(
            Integer code, BigDecimal value, Map<Integer, HrmSalaryOptionDO> optionMap) {
        HrmSalaryOptionDO option = optionMap.get(code);
        return new HrmSalaryOptionValueVO().setCode(code)
                .setName(option == null ? "薪资项" + code : option.getName()).setValue(value);
    }

    private Map<Integer, HrmSalaryOptionDO> getSalaryOptionMap() {
        return convertMap(salaryOptionService.getSalaryOptionList(false),
                HrmSalaryOptionDO::getCode, Function.identity());
    }

    private Map<Long, HrmSalaryTaxRuleDO> getEmployeeTaxRuleMap(Collection<Long> employeeIds) {
        if (CollUtil.isEmpty(employeeIds)) {
            return Collections.emptyMap();
        }
        Map<Long, HrmEmployeeDO> employeeMap = employeeService.getEmployeeMap(employeeIds);
        Collection<HrmEmployeeDO> employees = employeeMap == null
                ? Collections.emptyList() : employeeMap.values();
        Map<Long, HrmSalaryGroupDO> salaryGroupMap = salaryGroupService.getEmployeeSalaryGroupMap(employees);
        salaryGroupMap = salaryGroupMap == null ? Collections.emptyMap() : salaryGroupMap;
        Map<Long, HrmSalaryTaxRuleDO> taxRuleMap = salaryTaxRuleService.getSalaryTaxRuleMap(
                convertSet(salaryGroupMap.values(), HrmSalaryGroupDO::getTaxRuleId));
        HrmSalaryTaxRuleDO defaultTaxRule = CollUtil.getFirst(salaryTaxRuleService.getSalaryTaxRuleList());
        Map<Long, HrmSalaryTaxRuleDO> employeeTaxRuleMap = new LinkedHashMap<>();
        for (Long employeeId : employeeIds) {
            HrmSalaryGroupDO salaryGroup = salaryGroupMap.get(employeeId);
            HrmSalaryTaxRuleDO taxRule = salaryGroup == null || salaryGroup.getTaxRuleId() == null
                    ? defaultTaxRule : taxRuleMap.getOrDefault(salaryGroup.getTaxRuleId(), defaultTaxRule);
            employeeTaxRuleMap.put(employeeId, taxRule);
        }
        return employeeTaxRuleMap;
    }

    private Set<Long> resolveMonthEmployeeRecordEmployeeIds(
            HrmSalaryMonthEmployeeRecordPageReqVO reqVO, HrmSalaryMonthRecordDO monthRecord,
            boolean filterChangeType) {
        return resolveMonthEmployeeRecordEmployeeIds(reqVO.getMonthRecordId(), reqVO.getEmployeeId(),
                reqVO.getEmployeeName(), reqVO.getJobNumber(), reqVO.getDeptId(), reqVO.getEmployeeChangeType(),
                monthRecord, filterChangeType);
    }

    private Set<Long> resolveMonthEmployeeRecordEmployeeIds(
            HrmSalaryMonthEmployeeRecordListReqVO reqVO, HrmSalaryMonthRecordDO monthRecord) {
        return resolveMonthEmployeeRecordEmployeeIds(reqVO.getMonthRecordId(), reqVO.getEmployeeId(),
                reqVO.getEmployeeName(), reqVO.getJobNumber(), reqVO.getDeptId(), reqVO.getEmployeeChangeType(),
                monthRecord, true);
    }

    /**
     * 获得符合工资表、员工信息和异动类型条件的员工编号
     *
     * @param monthRecordId 月度工资表编号
     * @param employeeId 员工编号
     * @param employeeName 员工姓名
     * @param jobNumber 工号
     * @param deptId 部门编号
     * @param employeeChangeType 员工异动类型
     * @param monthRecord 月度工资表
     * @param filterChangeType 是否筛选员工异动类型
     * @return 员工编号集合
     */
    private Set<Long> resolveMonthEmployeeRecordEmployeeIds(
            Long monthRecordId, Long employeeId, String employeeName, String jobNumber, Long deptId,
            Integer employeeChangeType, HrmSalaryMonthRecordDO monthRecord, boolean filterChangeType) {
        // 1. 判断是否需要按员工或异动类型筛选
        HrmSalaryEmployeeChangeTypeEnum changeType = HrmSalaryEmployeeChangeTypeEnum.valueOf(employeeChangeType);
        boolean hasEmployeeFilter = employeeId != null || StrUtil.isNotBlank(employeeName)
                || StrUtil.isNotBlank(jobNumber) || deptId != null;
        boolean hasChangeTypeFilter = filterChangeType && changeType != null
                && changeType != HrmSalaryEmployeeChangeTypeEnum.ALL;
        if (!hasEmployeeFilter && !hasChangeTypeFilter) {
            return null;
        }
        if (employeeId != null && StrUtil.isBlank(employeeName) && StrUtil.isBlank(jobNumber)
                && deptId == null && !hasChangeTypeFilter) {
            return Collections.singleton(employeeId);
        }

        // 2.1 获得工资表员工编号
        List<HrmSalaryMonthEmployeeRecordDO> employeeRecords =
                monthEmployeeRecordMapper.selectListByMonthRecordId(monthRecordId);
        if (CollUtil.isEmpty(employeeRecords)) {
            return Collections.emptySet();
        }
        Set<Long> employeeIds = convertSet(employeeRecords, HrmSalaryMonthEmployeeRecordDO::getEmployeeId);
        // 2.2 按指定员工编号筛选
        if (employeeId != null) {
            employeeIds.removeIf(id -> ObjectUtil.notEqual(id, employeeId));
        }

        // 3. 按员工档案信息筛选
        if (StrUtil.isNotBlank(employeeName) || StrUtil.isNotBlank(jobNumber) || deptId != null) {
            Map<Long, HrmEmployeeDO> employeeMap = employeeService.getEmployeeMap(employeeIds);
            employeeIds.removeIf(candidateEmployeeId -> {
                HrmEmployeeDO employee = employeeMap.get(candidateEmployeeId);
                return employee == null
                        || (StrUtil.isNotBlank(employeeName)
                            && !StrUtil.contains(employee.getName(), employeeName.trim()))
                        || (StrUtil.isNotBlank(jobNumber)
                            && !StrUtil.contains(employee.getJobNumber(), jobNumber.trim()))
                        || (deptId != null && ObjectUtil.notEqual(deptId, employee.getDeptId()));
            });
        }

        // 4. 按员工异动类型筛选
        if (!filterChangeType) {
            return employeeIds;
        }
        return filterEmployeeIdsByChangeType(employeeIds, monthRecord, changeType);
    }

    /**
     * 按工资周期内的员工异动类型筛选员工编号
     *
     * @param employeeIds 员工编号集合
     * @param monthRecord 月度工资表
     * @param changeType 员工异动类型
     * @return 员工编号集合
     */
    private Set<Long> filterEmployeeIdsByChangeType(
            Set<Long> employeeIds, HrmSalaryMonthRecordDO monthRecord,
            HrmSalaryEmployeeChangeTypeEnum changeType) {
        // 1. 未指定异动类型时返回全部员工
        if (CollUtil.isEmpty(employeeIds) || changeType == null
                || changeType == HrmSalaryEmployeeChangeTypeEnum.ALL) {
            return new HashSet<>(employeeIds);
        }

        // 2. 按员工入职、离职时间筛选
        LocalDate beginDate = monthRecord.getStartTime().toLocalDate();
        LocalDate endDate = monthRecord.getEndTime().toLocalDate();
        if (ObjectUtils.equalsAny(changeType, HrmSalaryEmployeeChangeTypeEnum.ENTRY,
                HrmSalaryEmployeeChangeTypeEnum.LEAVE)) {
            Map<Long, HrmEmployeeDO> employeeMap = employeeService.getEmployeeMap(employeeIds);
            return convertSet(employeeIds, employeeId -> employeeId, employeeId -> {
                HrmEmployeeDO employee = employeeMap.get(employeeId);
                if (employee == null) {
                    return false;
                }
                LocalDate effectDate = changeType == HrmSalaryEmployeeChangeTypeEnum.ENTRY
                        ? employee.getEntryTime() == null ? null : employee.getEntryTime().toLocalDate()
                        : employee.getLeaveTime() == null ? null : employee.getLeaveTime().toLocalDate();
                return isBetween(beginDate, endDate, effectDate);
            });
        }

        // 3. 按员工转正、调岗记录筛选
        Integer employeeChangeType = changeType == HrmSalaryEmployeeChangeTypeEnum.REGULAR
                ? HrmEmployeeChangeTypeEnum.REGULAR.getType() : HrmEmployeeChangeTypeEnum.TRANSFER.getType();
        List<HrmEmployeeChangeRecordDO> changeRecords = employeeChangeRecordService
                .getEmployeeChangeRecordListByEmployeeIdsAndEffectTimeBetween(employeeIds,
                        LocalDateTimeUtils.getDateTimeRange(beginDate, endDate));
        return convertSet(changeRecords, HrmEmployeeChangeRecordDO::getEmployeeId,
                record -> ObjectUtil.equal(record.getType(), employeeChangeType));
    }

    private Map<Long, BigDecimal> getPerformanceCoefficientMap(
            Integer year, Integer month, Collection<Long> employeeIds) {
        if (year == null || month == null || CollUtil.isEmpty(employeeIds)) {
            return Collections.emptyMap();
        }
        List<HrmPerformancePlanDO> performancePlans = performancePlanService.getPerformancePlanListByPaidForMonth(
                String.format("%d-%02d", year, month));
        return performanceAssessmentService.getPerformanceArchiveEmployeeCoefficientMap(
                convertSet(performancePlans, HrmPerformancePlanDO::getId,
                        plan -> Boolean.TRUE.equals(plan.getSyncToSalary())), employeeIds);
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    @Getter
    @AllArgsConstructor
    private static class SalaryBaseAmounts {

        private final BigDecimal expectedPaySalary;
        private final BigDecimal taxableIncome;
        private final BigDecimal taxableBeforeThreshold;
        private final BigDecimal currentSpecialDeduction;
        private final BigDecimal afterTaxAdjustment;

    }

    @Getter
    @AllArgsConstructor
    private static class SalaryComputeResult {

        private final BigDecimal expectedPaySalary;
        private final BigDecimal taxableSalary;
        private final BigDecimal personalTax;
        private final BigDecimal realPaySalary;
        private final List<HrmSalaryOptionValueVO> optionValues;

    }

}
