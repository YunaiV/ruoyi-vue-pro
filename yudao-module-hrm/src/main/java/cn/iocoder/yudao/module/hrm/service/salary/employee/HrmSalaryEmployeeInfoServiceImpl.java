package cn.iocoder.yudao.module.hrm.service.salary.employee;

import cn.iocoder.yudao.module.hrm.service.salary.config.HrmSalaryOptionService;
import cn.iocoder.yudao.module.hrm.service.salary.monthrecord.HrmSalaryMonthRecordService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeePageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.employeeinfo.HrmSalaryEmployeeInfoUpdateListReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.employeeinfo.HrmSalaryEmployeeInfoUpdateListRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.employeeinfo.HrmSalaryEmployeeInfoPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.employeeinfo.HrmSalaryEmployeeInfoUpdateReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.employeeinfo.HrmSalaryEmployeeInfoImportRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.option.HrmSalaryOptionValueVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.employee.HrmSalaryChangeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.employee.HrmSalaryEmployeeInfoDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.monthrecord.HrmSalaryMonthRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryOptionDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.salary.employee.HrmSalaryEmployeeInfoMapper;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.employee.HrmSalaryBatchAdjustTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.employee.HrmSalaryChangeReasonEnum;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.hrm.enums.salary.config.HrmSalaryOptionCodeEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.config.HrmSalaryOptionTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.employee.HrmSalaryChangeRecordStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.employee.HrmSalaryChangeRecordTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.employee.HrmSalaryEmployeeInfoChangeTypeEnum;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.getDayBeginTime;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.isBeforeOrEqual;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.defaultIfNull;
import static cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryOptionDO.ROOT_PARENT_CODE;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_CHANGE_RECORD_STATUS_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_CHANGE_EFFECT_DATE_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_DATA_ILLEGAL;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_EMPLOYEE_INFO_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_IMPORT_LIST_IS_EMPTY;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_PENDING_CHANGE_RECORD_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.*;
import static cn.iocoder.yudao.module.hrm.util.HrmExcelUtils.getCell;
import static cn.iocoder.yudao.module.hrm.util.HrmExcelUtils.parseAmount;
import static cn.iocoder.yudao.module.hrm.util.HrmExcelUtils.parseDate;

/**
 * HRM 员工薪资信息 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmSalaryEmployeeInfoServiceImpl implements HrmSalaryEmployeeInfoService {

    @Resource
    private HrmSalaryOptionService salaryOptionService;
    @Resource
    private HrmSalaryEmployeeInfoMapper salaryEmployeeInfoMapper;
    @Resource
    private HrmSalaryChangeRecordService salaryChangeRecordService;
    @Resource
    @Lazy // 延迟加载
    private HrmSalaryMonthRecordService salaryMonthRecordService;
    @Resource
    private HrmEmployeeService employeeService;

    @Override
    public PageResult<HrmEmployeeDO> getSalaryEmployeeInfoPage(HrmSalaryEmployeeInfoPageReqVO reqVO) {
        // 1. 构建员工查询条件
        HrmEmployeePageReqVO employeeReqVO = buildSalaryEmployeePageReqVO(reqVO, false);
        if (reqVO.getChangeType() != null
                && ObjectUtil.notEqual(reqVO.getChangeType(), HrmSalaryEmployeeInfoChangeTypeEnum.UNSET.getType())
                && CollUtil.isEmpty(employeeReqVO.getIds())) {
            return PageResult.empty();
        }

        // 2. 查询员工分页
        return employeeService.getEmployeePage(employeeReqVO);
    }

    @Override
    public Map<Integer, Long> getSalaryEmployeeInfoStatusCount(HrmSalaryEmployeeInfoPageReqVO reqVO) {
        // 1. 转换员工查询条件，并忽略当前状态页签
        HrmEmployeePageReqVO employeeReqVO = buildSalaryEmployeePageReqVO(reqVO, true);
        if (reqVO.getChangeType() != null
                && ObjectUtil.notEqual(reqVO.getChangeType(), HrmSalaryEmployeeInfoChangeTypeEnum.UNSET.getType())
                && CollUtil.isEmpty(employeeReqVO.getIds())) {
            return Collections.emptyMap();
        }

        // 2. 统计员工状态数量
        return employeeService.getEmployeeStatusCount(employeeReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_SALARY_EMPLOYEE_TYPE, subType = HRM_SALARY_EMPLOYEE_SET_SUB_TYPE,
            bizNo = "{{#reqVO.employeeId}}", success = HRM_SALARY_EMPLOYEE_SET_SUCCESS)
    public Long updateSalaryEmployeeInfo(HrmSalaryEmployeeInfoUpdateReqVO reqVO) {
        // 1. 校验员工和定薪、调薪记录
        HrmEmployeeDO employee = employeeService.validateEmployeeExists(reqVO.getEmployeeId());
        HrmSalaryChangeRecordDO oldRecord = validateEditableChangeRecord(reqVO);
        HrmSalaryEmployeeInfoDO salaryEmployeeInfo =
                getSalaryEmployeeInfoByEmployeeId(reqVO.getEmployeeId());
        boolean salarySet = Objects.equals(reqVO.getRecordType(), HrmSalaryChangeRecordTypeEnum.SALARY_SET.getType());
        if (salarySet && oldRecord == null) {
            oldRecord = salaryChangeRecordService.getSalarySetRecordByEmployeeId(reqVO.getEmployeeId());
        }
        if (!salarySet && salaryEmployeeInfo == null) {
            throw exception(SALARY_EMPLOYEE_INFO_NOT_EXISTS);
        }
        if (salarySet && salaryChangeRecordService.hasUncancelledSalaryAdjustmentRecord(reqVO.getEmployeeId())) {
            throw exception(SALARY_CHANGE_RECORD_STATUS_INVALID);
        }
        if (salaryChangeRecordService.hasPendingSalaryChangeRecord(
                reqVO.getEmployeeId(), reqVO.getId())) {
            throw exception(SALARY_PENDING_CHANGE_RECORD_EXISTS);
        }
        if (!salarySet) {
            validateSalaryAdjustmentEffectTime(reqVO.getEffectTime());
        }

        // 2. 标准化并计算正式、试用期薪资
        Map<Integer, HrmSalaryOptionDO> optionMap = getSalaryOptionMap();
        List<HrmSalaryOptionValueVO> salaryOptions = normalizeOptionValues(reqVO.getSalaryOptions(), optionMap);
        List<HrmSalaryOptionValueVO> probationSalaryOptions =
                normalizeOptionValues(reqVO.getProbationSalaryOptions(), optionMap);
        if (Objects.equals(reqVO.getRecordType(), HrmSalaryChangeRecordTypeEnum.SALARY_ADJUSTMENT.getType())) {
            salaryOptions = mergeSalaryEmployeeInfoOptionValues(reqVO.getEmployeeId(), false, salaryOptions);
            probationSalaryOptions =
                    mergeSalaryEmployeeInfoOptionValues(reqVO.getEmployeeId(), true, probationSalaryOptions);
        }
        BigDecimal regularSalary = sumOptionValues(salaryOptions, optionMap);
        BigDecimal probationSalary = sumOptionValues(probationSalaryOptions, optionMap);
        LocalDate today = LocalDate.now();
        Integer changeReason = salarySet
                ? HrmSalaryChangeReasonEnum.ENTRY_SALARY.getReason() : reqVO.getChangeReason();
        // 首次定薪是月度工资基线；编辑定薪保留原生效日期，首次未指定时从当月首日生效。
        LocalDateTime effectTime = reqVO.getEffectTime();
        if (salarySet && oldRecord != null && oldRecord.getEffectTime() != null) {
            effectTime = oldRecord.getEffectTime();
        } else if (salarySet && effectTime == null) {
            effectTime = today.withDayOfMonth(1).atStartOfDay();
        } else if (!salarySet) {
            effectTime = getDayBeginTime(effectTime);
        }
        boolean pending = !salarySet && effectTime.toLocalDate().isAfter(today);
        BigDecimal beforeTotal = salarySet ? BigDecimal.ZERO
                : defaultIfNull(salaryEmployeeInfo.getRegularSalary(), BigDecimal.ZERO);
        BigDecimal probationBeforeTotal = salarySet ? BigDecimal.ZERO
                : defaultIfNull(salaryEmployeeInfo.getProbationSalary(), BigDecimal.ZERO);

        // 3. 非待生效记录立即更新员工当前薪资
        if (!pending) {
            saveEffectiveSalaryEmployeeInfo(salaryEmployeeInfo, reqVO.getEmployeeId(), reqVO.getRecordType(),
                    changeReason, effectTime, reqVO.getRemark(), salaryOptions,
                    probationSalaryOptions, optionMap);
        }

        // 4. 创建或更新定薪、调薪记录
        HrmSalaryChangeRecordDO changeRecord = HrmSalaryChangeRecordDO.builder()
                .id(oldRecord == null ? null : oldRecord.getId()).employeeId(reqVO.getEmployeeId())
                .type(reqVO.getRecordType()).reason(changeReason).effectTime(effectTime)
                .beforeTotal(beforeTotal).afterTotal(regularSalary)
                .probationBeforeTotal(probationBeforeTotal).probationAfterTotal(probationSalary)
                .salaryOptions(buildSalaryChangeRecordOptionList(salaryOptions))
                .probationSalaryOptions(buildSalaryChangeRecordOptionList(probationSalaryOptions))
                .status(pending ? HrmSalaryChangeRecordStatusEnum.PENDING.getStatus() : HrmSalaryChangeRecordStatusEnum.EFFECTIVE.getStatus()).remark(reqVO.getRemark()).build();
        if (oldRecord == null) {
            salaryChangeRecordService.createSalaryChangeRecord(changeRecord);
        } else {
            salaryChangeRecordService.updateSalaryChangeRecord(changeRecord);
        }

        // 5. 记录操作日志上下文
        LogRecordContext.putVariable("employee", employee);
        return changeRecord.getId();
    }

    @Override
    @LogRecord(type = HRM_SALARY_EMPLOYEE_TYPE, subType = HRM_SALARY_EMPLOYEE_BATCH_SET_SUB_TYPE,
            bizNo = "{{#employeeIds.iterator().next()}}", success = HRM_SALARY_EMPLOYEE_BATCH_SET_SUCCESS)
    public HrmSalaryEmployeeInfoUpdateListRespVO updateSalaryEmployeeInfoList(
            HrmSalaryEmployeeInfoUpdateListReqVO reqVO) {
        // 1. 合并部门和指定员工范围
        Set<Long> employeeIds = new LinkedHashSet<>(CollUtil.emptyIfNull(reqVO.getEmployeeIds()));
        List<HrmEmployeeDO> deptEmployees = employeeService.getEmployeeListByDeptIds(reqVO.getDeptIds());
        employeeIds.addAll(convertList(deptEmployees, HrmEmployeeDO::getId));
        if (CollUtil.isEmpty(employeeIds)) {
            throw exception(SALARY_DATA_ILLEGAL);
        }

        // 2. 逐个调薪，单个员工失败不影响其他员工
        HrmSalaryEmployeeInfoUpdateListRespVO result = new HrmSalaryEmployeeInfoUpdateListRespVO();
        for (Long employeeId : employeeIds) {
            try {
                // 2.1 校验员工已定薪
                HrmEmployeeDO employee = employeeService.validateEmployeeExists(employeeId);
                if (getSalaryEmployeeInfoByEmployeeId(employeeId) == null) {
                    throw exception(SALARY_EMPLOYEE_INFO_NOT_EXISTS);
                }

                // 2.2 根据员工当前工资计算调整后的薪资项
                List<HrmSalaryOptionValueVO> salaryOptions = adjustSalaryOptions(
                        employeeId, false, reqVO.getSalaryOptions(), reqVO.getType());
                List<HrmSalaryOptionValueVO> probationSalaryOptions =
                        Objects.equals(employee.getStatus(), HrmEmployeeStatusEnum.PROBATION.getStatus())
                                ? adjustSalaryOptions(employeeId, true, reqVO.getSalaryOptions(), reqVO.getType())
                                : Collections.emptyList();

                // 2.3 创建调薪记录
                HrmSalaryEmployeeInfoUpdateReqVO updateReqVO = new HrmSalaryEmployeeInfoUpdateReqVO()
                        .setEmployeeId(employeeId).setRecordType(HrmSalaryChangeRecordTypeEnum.SALARY_ADJUSTMENT.getType())
                        .setChangeReason(reqVO.getChangeReason()).setEffectTime(reqVO.getEffectTime())
                        .setRemark(reqVO.getRemark()).setSalaryOptions(salaryOptions)
                        .setProbationSalaryOptions(probationSalaryOptions);
                getSelf().updateSalaryEmployeeInfo(updateReqVO);
                result.getSuccessEmployeeIds().add(employeeId);
            } catch (ServiceException ex) {
                result.getFailureEmployeeReasons().put(employeeId, ex.getMessage());
            }
        }

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("employeeIds", employeeIds);
        LogRecordContext.putVariable("batchResult", result);
        return result;
    }

    // ==================== 导入 ====================

    @Override
    public List<HrmSalaryOptionDO> getSalaryImportOptionList() {
        List<HrmSalaryOptionDO> options = salaryOptionService.getSalaryOptionList(false);
        return filterList(options, option -> ObjectUtil.notEqual(option.getParentCode(), ROOT_PARENT_CODE)
                && Boolean.TRUE.equals(option.getCalculateEnabled())
                && !HrmSalaryOptionCodeEnum.EMPLOYEE_INFO_IMPORT_EXCLUDED_PARENT_CODES.contains(option.getParentCode()));
    }

    @Override
    public HrmSalaryEmployeeInfoImportRespVO importFixSalaryList(List<Map<Integer, String>> rows) {
        // 1. 校验导入数据并加载薪资项
        validateImportRows(rows);
        List<HrmSalaryOptionDO> options = getSalaryImportOptionList();

        // 2. 逐行解析并保存定薪结果
        HrmSalaryEmployeeInfoImportRespVO respVO = buildSalaryEmployeeInfoImportRespVO();
        for (int i = 0; i < rows.size(); i++) {
            Map<Integer, String> row = rows.get(i);
            String jobNumber = getCell(row, 1);
            String key = getSalaryImportKey(jobNumber, i + 1);
            try {
                HrmEmployeeDO employee = validateImportEmployee(jobNumber);
                int probationStartIndex = 4;
                int regularStartIndex = probationStartIndex + options.size();
                HrmSalaryEmployeeInfoUpdateReqVO updateReqVO = new HrmSalaryEmployeeInfoUpdateReqVO()
                        .setEmployeeId(employee.getId()).setRecordType(HrmSalaryChangeRecordTypeEnum.SALARY_SET.getType())
                        .setChangeReason(HrmSalaryChangeReasonEnum.ENTRY_SALARY.getReason())
                        .setSalaryOptions(readOptionValues(
                                row, regularStartIndex, options, false, Collections.emptyMap()))
                        .setProbationSalaryOptions(readOptionValues(
                                row, probationStartIndex, options, false, Collections.emptyMap()))
                        .setRemark(getCell(row, regularStartIndex + options.size()));
                getSelf().updateSalaryEmployeeInfo(updateReqVO);
                respVO.getSuccessJobNumbers().add(key);
            } catch (Exception ex) {
                respVO.getFailureJobNumbers().put(key, ex.getMessage());
            }
        }
        return respVO;
    }

    @Override
    public HrmSalaryEmployeeInfoImportRespVO importChangeSalaryList(List<Map<Integer, String>> rows) {
        // 1. 校验导入数据并加载薪资项
        validateImportRows(rows);
        List<HrmSalaryOptionDO> options = getSalaryImportOptionList();

        // 2. 逐行解析并保存调薪结果
        HrmSalaryEmployeeInfoImportRespVO respVO = buildSalaryEmployeeInfoImportRespVO();
        for (int i = 0; i < rows.size(); i++) {
            Map<Integer, String> row = rows.get(i);
            String jobNumber = getCell(row, 1);
            String key = getSalaryImportKey(jobNumber, i + 1);
            try {
                HrmEmployeeDO employee = validateImportEmployee(jobNumber);
                HrmSalaryEmployeeInfoDO salaryEmployeeInfo =
                        getSalaryEmployeeInfoByEmployeeId(employee.getId());
                if (salaryEmployeeInfo == null) {
                    throw exception(SALARY_EMPLOYEE_INFO_NOT_EXISTS);
                }
                int probationStartIndex = 6;
                int regularStartIndex = probationStartIndex + options.size() * 2;
                Map<Integer, BigDecimal> probationBeforeValueMap =
                        getSalaryEmployeeInfoOptionValueMap(employee.getId(), true);
                Map<Integer, BigDecimal> regularBeforeValueMap =
                        getSalaryEmployeeInfoOptionValueMap(employee.getId(), false);
                List<HrmSalaryOptionValueVO> probationSalaryOptions = readOptionValues(
                        row, probationStartIndex, options, true, probationBeforeValueMap);
                List<HrmSalaryOptionValueVO> salaryOptions = readOptionValues(
                        row, regularStartIndex, options, true, regularBeforeValueMap);
                HrmSalaryEmployeeInfoUpdateReqVO updateReqVO = new HrmSalaryEmployeeInfoUpdateReqVO()
                        .setEmployeeId(employee.getId()).setRecordType(HrmSalaryChangeRecordTypeEnum.SALARY_ADJUSTMENT.getType())
                        .setChangeReason(parseSalaryChangeReason(getCell(row, 5)))
                        .setEffectTime(parseDate(getCell(row, 4), null, true, "生效日期").atStartOfDay())
                        .setSalaryOptions(salaryOptions).setProbationSalaryOptions(probationSalaryOptions)
                        .setRemark(getCell(row, regularStartIndex + options.size() * 2));
                getSelf().updateSalaryEmployeeInfo(updateReqVO);
                respVO.getSuccessJobNumbers().add(key);
            } catch (Exception ex) {
                respVO.getFailureJobNumbers().put(key, ex.getMessage());
            }
        }
        return respVO;
    }

    // ==================== 查询 ====================

    @Override
    public HrmSalaryEmployeeInfoDO getSalaryEmployeeInfoByEmployeeId(Long employeeId) {
        return salaryEmployeeInfoMapper.selectByEmployeeId(employeeId);
    }

    @Override
    public List<HrmSalaryEmployeeInfoDO> getSalaryEmployeeInfoList(Collection<Long> employeeIds) {
        if (CollUtil.isEmpty(employeeIds)) {
            return Collections.emptyList();
        }
        return salaryEmployeeInfoMapper.selectListByEmployeeIds(new ArrayList<>(employeeIds));
    }

    // ==================== 内部调用 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> applyDueSalaryChanges(LocalDate targetDate) {
        // 1. 加载薪资项配置和待生效记录
        List<Long> employeeIds = new ArrayList<>();
        Map<Integer, HrmSalaryOptionDO> optionMap = getSalaryOptionMap();
        List<HrmSalaryChangeRecordDO> changeRecords =
                salaryChangeRecordService.getDueSalaryChangeRecordList(targetDate);

        // 2. 逐条抢占待生效记录，并更新员工当前薪资
        for (HrmSalaryChangeRecordDO changeRecord : changeRecords) {
            if (!salaryChangeRecordService.updateSalaryChangeRecordStatus(changeRecord.getId(),
                    HrmSalaryChangeRecordStatusEnum.PENDING.getStatus(),
                    HrmSalaryChangeRecordStatusEnum.EFFECTIVE.getStatus())) {
                continue;
            }
            List<HrmSalaryOptionValueVO> salaryOptions = buildSalaryChangeRecordOptionValueVOList(
                    changeRecord.getSalaryOptions());
            List<HrmSalaryOptionValueVO> probationSalaryOptions =
                    buildSalaryChangeRecordOptionValueVOList(changeRecord.getProbationSalaryOptions());
            HrmSalaryEmployeeInfoDO salaryEmployeeInfo =
                    getSalaryEmployeeInfoByEmployeeId(changeRecord.getEmployeeId());
            saveEffectiveSalaryEmployeeInfo(salaryEmployeeInfo, changeRecord.getEmployeeId(), changeRecord.getType(),
                    changeRecord.getReason(), changeRecord.getEffectTime(), changeRecord.getRemark(),
                    salaryOptions, probationSalaryOptions, optionMap);
            employeeIds.add(changeRecord.getEmployeeId());
        }
        return employeeIds;
    }

    @Override
    public List<HrmSalaryEmployeeInfoDO.SalaryOption> getEffectiveSalaryOptionList(
            HrmEmployeeDO employee, LocalDateTime[] salaryTimes) {
        // 1. 校验计算参数
        LocalDateTime beginTime = ArrayUtils.get(salaryTimes, 0);
        LocalDateTime endTime = ArrayUtils.get(salaryTimes, 1);
        LocalDate beginDate = beginTime == null ? null : beginTime.toLocalDate();
        LocalDate endDate = endTime == null ? null : endTime.toLocalDate();
        if (employee == null || beginDate == null || endDate == null || endDate.isBefore(beginDate)) {
            return Collections.emptyList();
        }
        // 2. 查询员工全部未取消的定薪、调薪记录
        List<HrmSalaryChangeRecordDO> changeRecords = salaryChangeRecordService
                .getSalaryChangeRecordList(employee.getId()).stream()
                .filter(record -> ObjectUtil.notEqual(record.getStatus(), HrmSalaryChangeRecordStatusEnum.CANCELLED.getStatus()))
                .sorted(Comparator.comparing(HrmSalaryChangeRecordDO::getEffectTime,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(HrmSalaryChangeRecordDO::getId))
                .collect(Collectors.toList());
        HrmSalaryEmployeeInfoDO salaryEmployeeInfo =
                getSalaryEmployeeInfoByEmployeeId(employee.getId());

        // 3. 按周期内每天适用的试用期、正式工资及调薪记录进行加权
        long periodDays = ChronoUnit.DAYS.between(beginDate, endDate) + 1;
        Map<Integer, HrmSalaryEmployeeInfoDO.SalaryOption> optionMap = new LinkedHashMap<>();
        Map<Integer, BigDecimal> amountMap = new LinkedHashMap<>();
        for (LocalDate date = beginDate; isBeforeOrEqual(date, endDate); date = date.plusDays(1)) {
            for (HrmSalaryEmployeeInfoDO.SalaryOption option :
                    getEffectiveSalaryOptions(employee, date, changeRecords, salaryEmployeeInfo)) {
                if (option.getCode() == null) {
                    continue;
                }
                optionMap.putIfAbsent(option.getCode(), option);
                amountMap.merge(option.getCode(), defaultIfNull(option.getValue(), BigDecimal.ZERO), BigDecimal::add);
            }
        }
        return amountMap.entrySet().stream().map(entry -> {
            HrmSalaryEmployeeInfoDO.SalaryOption source = optionMap.get(entry.getKey());
            return HrmSalaryEmployeeInfoDO.SalaryOption.builder().code(entry.getKey())
                    .name(source == null ? null : source.getName())
                    .value(entry.getValue().divide(
                            BigDecimal.valueOf(periodDays), 2, RoundingMode.HALF_UP))
                    .build();
        }).sorted(Comparator.comparing(HrmSalaryEmployeeInfoDO.SalaryOption::getCode)).collect(Collectors.toList());
    }

    private List<HrmSalaryEmployeeInfoDO.SalaryOption> getEffectiveSalaryOptions(
            HrmEmployeeDO employee, LocalDate date, List<HrmSalaryChangeRecordDO> changeRecords,
            HrmSalaryEmployeeInfoDO salaryEmployeeInfo) {
        // 1. 查找指定日期前最后一条生效记录
        HrmSalaryChangeRecordDO effectiveRecord = null;
        for (HrmSalaryChangeRecordDO changeRecord : changeRecords) {
            if (changeRecord.getEffectTime() == null || changeRecord.getEffectTime().toLocalDate().isAfter(date)) {
                break;
            }
            effectiveRecord = changeRecord;
        }
        if (effectiveRecord != null) {
            return convertSalaryChangeRecordOptionList(isProbation(employee, date)
                    ? effectiveRecord.getProbationSalaryOptions() : effectiveRecord.getSalaryOptions());
        }

        // 2. 无历史记录时回退员工当前薪资
        if (salaryEmployeeInfo == null || salaryEmployeeInfo.getEffectTime() == null
                || salaryEmployeeInfo.getEffectTime().toLocalDate().isAfter(date)) {
            return Collections.emptyList();
        }
        return CollUtil.emptyIfNull(isProbation(employee, date)
                ? salaryEmployeeInfo.getProbationSalaryOptions() : salaryEmployeeInfo.getSalaryOptions());
    }

    private boolean isProbation(HrmEmployeeDO employee, LocalDate date) {
        if (employee.getRegularTime() != null) {
            return date.isBefore(employee.getRegularTime().toLocalDate());
        }
        return Objects.equals(employee.getStatus(), HrmEmployeeStatusEnum.PROBATION.getStatus());
    }

    /**
     * 构建员工薪资分页使用的员工查询条件
     *
     * @param reqVO 员工薪资查询条件
     * @param ignoreStatusCategory 是否忽略员工状态分类
     * @return 员工查询条件
     */
    private HrmEmployeePageReqVO buildSalaryEmployeePageReqVO(
            HrmSalaryEmployeeInfoPageReqVO reqVO, boolean ignoreStatusCategory) {
        HrmEmployeePageReqVO employeeReqVO = BeanUtils.toBean(reqVO, HrmEmployeePageReqVO.class)
                .setStatusCategory(ignoreStatusCategory ? null : reqVO.getStatusCategory());
        Set<Long> requestedEmployeeIds = new LinkedHashSet<>(CollUtil.emptyIfNull(reqVO.getEmployeeIds()));
        if (reqVO.getEmployeeId() != null) {
            requestedEmployeeIds.add(reqVO.getEmployeeId());
        }

        // 1. 未指定薪资状态时，仅使用请求中的员工范围
        if (reqVO.getChangeType() == null) {
            employeeReqVO.setIds(new ArrayList<>(requestedEmployeeIds));
            return employeeReqVO;
        }

        // 2. 未定薪员工使用排除条件
        List<Long> salaryEmployeeIds = salaryEmployeeInfoMapper.selectEmployeeIdListByChangeType(
                Objects.equals(reqVO.getChangeType(), HrmSalaryEmployeeInfoChangeTypeEnum.UNSET.getType()) ? null : reqVO.getChangeType());
        if (Objects.equals(reqVO.getChangeType(), HrmSalaryEmployeeInfoChangeTypeEnum.UNSET.getType())) {
            employeeReqVO.setIds(new ArrayList<>(requestedEmployeeIds));
            employeeReqVO.setExcludeIds(salaryEmployeeIds);
            return employeeReqVO;
        }

        // 3. 其他薪资状态使用已定薪员工与请求员工范围的交集
        Set<Long> matchedEmployeeIds = new LinkedHashSet<>(salaryEmployeeIds);
        if (CollUtil.isNotEmpty(requestedEmployeeIds)) {
            matchedEmployeeIds.retainAll(requestedEmployeeIds);
        }
        employeeReqVO.setIds(new ArrayList<>(matchedEmployeeIds));
        return employeeReqVO;
    }

    /**
     * 校验待编辑的定薪、调薪记录
     *
     * @param reqVO 薪资信息修改参数
     * @return 原定薪、调薪记录；新增时返回 {@code null}
     */
    private HrmSalaryChangeRecordDO validateEditableChangeRecord(HrmSalaryEmployeeInfoUpdateReqVO reqVO) {
        if (reqVO.getId() == null) {
            return null;
        }
        HrmSalaryChangeRecordDO changeRecord =
                salaryChangeRecordService.validateSalaryChangeRecordExists(reqVO.getId());
        if (ObjectUtil.notEqual(changeRecord.getEmployeeId(), reqVO.getEmployeeId())
                || ObjectUtil.notEqual(changeRecord.getType(), reqVO.getRecordType())) {
            throw exception(SALARY_CHANGE_RECORD_STATUS_INVALID);
        }
        if (ObjectUtil.equal(changeRecord.getType(), HrmSalaryChangeRecordTypeEnum.SALARY_ADJUSTMENT.getType())
                && ObjectUtil.equal(changeRecord.getStatus(), HrmSalaryChangeRecordStatusEnum.EFFECTIVE.getStatus())) {
            throw exception(SALARY_CHANGE_RECORD_STATUS_INVALID);
        }
        return changeRecord;
    }

    /**
     * 校验调薪生效时间
     *
     * @param effectTime 生效时间
     */
    private void validateSalaryAdjustmentEffectTime(LocalDateTime effectTime) {
        if (effectTime == null) {
            throw exception(SALARY_CHANGE_EFFECT_DATE_INVALID);
        }
        HrmSalaryMonthRecordDO lastMonthRecord = salaryMonthRecordService.getLastMonthRecord();
        if (lastMonthRecord == null || lastMonthRecord.getYear() == null || lastMonthRecord.getMonth() == null) {
            return;
        }
        LocalDate latestSalaryMonthBeginDate = YearMonth.of(
                lastMonthRecord.getYear(), lastMonthRecord.getMonth()).atDay(1);
        if (effectTime.toLocalDate().isBefore(latestSalaryMonthBeginDate)) {
            throw exception(SALARY_CHANGE_EFFECT_DATE_INVALID);
        }
    }

    /**
     * 保存已生效的员工当前薪资信息
     *
     * @param salaryEmployeeInfo 原员工薪资信息
     * @param employeeId 员工编号
     * @param recordType 记录类型
     * @param changeReason 变更原因
     * @param effectTime 生效时间
     * @param remark 备注
     * @param salaryOptions 正式薪资项
     * @param probationSalaryOptions 试用期薪资项
     * @param optionMap 薪资项配置 Map
     */
    private void saveEffectiveSalaryEmployeeInfo(
            HrmSalaryEmployeeInfoDO salaryEmployeeInfo, Long employeeId, Integer recordType,
            Integer changeReason, LocalDateTime effectTime, String remark,
            List<HrmSalaryOptionValueVO> salaryOptions,
            List<HrmSalaryOptionValueVO> probationSalaryOptions,
            Map<Integer, HrmSalaryOptionDO> optionMap) {
        if (salaryEmployeeInfo == null) {
            salaryEmployeeInfo = HrmSalaryEmployeeInfoDO.builder().employeeId(employeeId).build();
        }
        salaryEmployeeInfo.setChangeReason(changeReason).setEffectTime(effectTime)
                .setChangeType(Objects.equals(recordType, HrmSalaryChangeRecordTypeEnum.SALARY_SET.getType()) ? HrmSalaryEmployeeInfoChangeTypeEnum.SET.getType() : HrmSalaryEmployeeInfoChangeTypeEnum.ADJUSTED.getType())
                .setRegularSalary(sumOptionValues(salaryOptions, optionMap))
                .setProbationSalary(sumOptionValues(probationSalaryOptions, optionMap))
                .setRemark(remark).setSalaryOptions(buildSalaryOptionList(salaryOptions))
                .setProbationSalaryOptions(buildSalaryOptionList(probationSalaryOptions));
        salaryEmployeeInfoMapper.insertOrUpdate(salaryEmployeeInfo);
    }

    /**
     * 合并员工原薪资项和本次变更薪资项
     *
     * @param employeeId 员工编号
     * @param probation 是否为试用期薪资
     * @param changedOptions 本次变更薪资项
     * @return 合并后的薪资项
     */
    private List<HrmSalaryOptionValueVO> mergeSalaryEmployeeInfoOptionValues(
            Long employeeId, boolean probation, List<HrmSalaryOptionValueVO> changedOptions) {
        Map<Integer, HrmSalaryOptionValueVO> optionMap = new LinkedHashMap<>();
        getSalaryEmployeeInfoOptionList(employeeId, probation).stream()
                .map(this::buildSalaryOptionValueVO)
                .forEach(option -> optionMap.put(option.getCode(), option));
        changedOptions.forEach(option -> optionMap.put(option.getCode(), option));
        return optionMap.values().stream()
                .sorted(Comparator.comparing(HrmSalaryOptionValueVO::getCode))
                .collect(Collectors.toList());
    }

    /**
     * 根据批量调薪规则计算员工调整后的薪资项
     *
     * @param employeeId 员工编号
     * @param probation 是否为试用期薪资
     * @param adjustmentOptions 调整薪资项
     * @param adjustmentType 调整类型
     * @return 调整后的薪资项
     */
    @SuppressWarnings("DataFlowIssue")
    private List<HrmSalaryOptionValueVO> adjustSalaryOptions(
            Long employeeId, boolean probation, List<HrmSalaryOptionValueVO> adjustmentOptions,
            Integer adjustmentType) {
        Map<Integer, HrmSalaryOptionValueVO> optionMap = new LinkedHashMap<>();
        getSalaryEmployeeInfoOptionList(employeeId, probation).stream()
                .map(this::buildSalaryOptionValueVO)
                .forEach(option -> optionMap.put(option.getCode(), option));
        for (HrmSalaryOptionValueVO adjustmentOption : adjustmentOptions) {
            if (adjustmentOption.getCode() == null) {
                continue;
            }
            HrmSalaryOptionValueVO oldOption = optionMap.get(adjustmentOption.getCode());
            BigDecimal oldValue = oldOption == null ? BigDecimal.ZERO
                    : defaultIfNull(oldOption.getValue(), BigDecimal.ZERO);
            BigDecimal adjustmentValue = defaultIfNull(adjustmentOption.getValue(), BigDecimal.ZERO);
            BigDecimal changeValue = Objects.equals(adjustmentType,
                    HrmSalaryBatchAdjustTypeEnum.PERCENT.getType())
                    ? oldValue.multiply(adjustmentValue)
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                    : adjustmentValue;
            BigDecimal newValue = oldValue.add(changeValue).setScale(2, RoundingMode.HALF_UP);
            validateSalaryOptionValue(newValue);
            HrmSalaryOptionValueVO newOption = new HrmSalaryOptionValueVO()
                    .setCode(adjustmentOption.getCode())
                    .setName(adjustmentOption.getName() != null ? adjustmentOption.getName()
                            : oldOption == null ? null : oldOption.getName())
                    .setValue(newValue);
            optionMap.put(newOption.getCode(), newOption);
        }
        return new ArrayList<>(optionMap.values());
    }

    private HrmSalaryEmployeeInfoImportRespVO buildSalaryEmployeeInfoImportRespVO() {
        return HrmSalaryEmployeeInfoImportRespVO.builder()
                .successJobNumbers(new ArrayList<>())
                .failureJobNumbers(new LinkedHashMap<>())
                .build();
    }

    private void validateImportRows(List<Map<Integer, String>> rows) {
        if (CollUtil.isEmpty(rows)) {
            throw exception(SALARY_IMPORT_LIST_IS_EMPTY);
        }
    }

    private HrmEmployeeDO validateImportEmployee(String jobNumber) {
        if (StrUtil.isBlank(jobNumber)) {
            throw new IllegalArgumentException("请填写工号");
        }
        HrmEmployeeDO employee = employeeService.getEmployeeByJobNumber(jobNumber);
        if (employee == null) {
            throw new IllegalArgumentException("工号对应的员工不存在");
        }
        return employee;
    }

    private String getSalaryImportKey(String jobNumber, int rowIndex) {
        return StrUtil.isBlank(jobNumber) ? "第 " + rowIndex + " 行" : jobNumber;
    }

    private Integer parseSalaryChangeReason(String value) {
        if (StrUtil.isBlank(value)) {
            throw new IllegalArgumentException("请填写调薪原因");
        }
        String text = value.trim();
        if (text.matches("\\d+")) {
            HrmSalaryChangeReasonEnum reason = HrmSalaryChangeReasonEnum.valueOf(Integer.parseInt(text));
            if (reason != null) {
                return reason.getReason();
            }
        }
        HrmSalaryChangeReasonEnum reason = HrmSalaryChangeReasonEnum.valueOfName(text);
        if (reason == null) {
            throw new IllegalArgumentException("调薪原因填写不正确");
        }
        return reason.getReason();
    }

    private List<HrmSalaryOptionValueVO> readOptionValues(Map<Integer, String> row, int startIndex,
                                                          List<HrmSalaryOptionDO> options,
                                                          boolean changeImport,
                                                          Map<Integer, BigDecimal> defaultValueMap) {
        List<HrmSalaryOptionValueVO> values = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            HrmSalaryOptionDO option = options.get(i);
            int index = changeImport ? startIndex + i * 2 + 1 : startIndex + i;
            BigDecimal defaultValue = defaultValueMap.getOrDefault(option.getCode(), BigDecimal.ZERO);
            values.add(optionValue(option, parseAmount(getCell(row, index), defaultValue)));
        }
        return values;
    }

    private HrmSalaryOptionValueVO optionValue(HrmSalaryOptionDO option, BigDecimal value) {
        return new HrmSalaryOptionValueVO().setCode(option.getCode())
                .setName(option.getName()).setValue(value);
    }

    private Map<Integer, BigDecimal> getSalaryEmployeeInfoOptionValueMap(
            Long employeeId, boolean probation) {
        return convertMap(getSalaryEmployeeInfoOptionList(employeeId, probation),
                HrmSalaryEmployeeInfoDO.SalaryOption::getCode,
                option -> defaultIfNull(option.getValue(), BigDecimal.ZERO));
    }

    private List<HrmSalaryOptionValueVO> normalizeOptionValues(
            List<HrmSalaryOptionValueVO> options, Map<Integer, HrmSalaryOptionDO> optionMap) {
        if (options == null) {
            return Collections.emptyList();
        }
        return convertList(options, option -> {
                    HrmSalaryOptionDO optionDO = optionMap.get(option.getCode());
                    if (optionDO == null) {
                        throw exception(SALARY_DATA_ILLEGAL);
                    }
                    BigDecimal value = defaultIfNull(option.getValue(), BigDecimal.ZERO);
                    validateSalaryOptionValue(value);
                    return new HrmSalaryOptionValueVO().setCode(option.getCode())
                            .setName(option.getName() == null ? optionDO.getName() : option.getName())
                            .setValue(value.setScale(2, RoundingMode.HALF_UP));
                }, option -> option.getCode() != null);
    }

    private void validateSalaryOptionValue(BigDecimal value) {
        if (value.signum() < 0 || Math.max(value.stripTrailingZeros().scale(), 0) > 2) {
            throw exception(SALARY_DATA_ILLEGAL);
        }
    }

    @SuppressWarnings("DataFlowIssue")
    private BigDecimal sumOptionValues(
            List<HrmSalaryOptionValueVO> options, Map<Integer, HrmSalaryOptionDO> optionMap) {
        return options.stream()
                .filter(option -> {
                    HrmSalaryOptionDO optionDO = optionMap.get(option.getCode());
                    return optionDO != null && Boolean.TRUE.equals(optionDO.getCalculateEnabled());
                })
                .map(option -> {
                    HrmSalaryOptionDO optionDO = optionMap.get(option.getCode());
                    BigDecimal amount = defaultIfNull(option.getValue(), BigDecimal.ZERO);
                    return Objects.equals(optionDO.getType(), HrmSalaryOptionTypeEnum.MINUS.getType()) ? amount.negate() : amount;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<Integer, HrmSalaryOptionDO> getSalaryOptionMap() {
        return convertMap(salaryOptionService.getSalaryOptionList(false), HrmSalaryOptionDO::getCode);
    }

    private HrmSalaryOptionValueVO buildSalaryOptionValueVO(HrmSalaryEmployeeInfoDO.SalaryOption optionDO) {
        return new HrmSalaryOptionValueVO().setCode(optionDO.getCode())
                .setName(optionDO.getName()).setValue(optionDO.getValue());
    }

    private List<HrmSalaryEmployeeInfoDO.SalaryOption> buildSalaryOptionList(
            List<HrmSalaryOptionValueVO> options) {
        return convertList(options, option -> HrmSalaryEmployeeInfoDO.SalaryOption.builder()
                .code(option.getCode()).name(option.getName())
                .value(defaultIfNull(option.getValue(), BigDecimal.ZERO)).build());
    }

    private List<HrmSalaryChangeRecordDO.SalaryOption> buildSalaryChangeRecordOptionList(
            List<HrmSalaryOptionValueVO> options) {
        return convertList(options, option -> HrmSalaryChangeRecordDO.SalaryOption.builder()
                .code(option.getCode()).name(option.getName())
                .value(defaultIfNull(option.getValue(), BigDecimal.ZERO)).build());
    }

    private List<HrmSalaryOptionValueVO> buildSalaryChangeRecordOptionValueVOList(
            List<HrmSalaryChangeRecordDO.SalaryOption> options) {
        return convertList(options, option -> new HrmSalaryOptionValueVO().setCode(option.getCode())
                .setName(option.getName()).setValue(option.getValue()));
    }

    private List<HrmSalaryEmployeeInfoDO.SalaryOption> convertSalaryChangeRecordOptionList(
            List<HrmSalaryChangeRecordDO.SalaryOption> options) {
        return convertList(options, option -> HrmSalaryEmployeeInfoDO.SalaryOption.builder()
                .code(option.getCode()).name(option.getName()).value(option.getValue()).build());
    }

    /**
     * 获得员工当前薪资项列表
     *
     * @param employeeId 员工编号
     * @param probation 是否为试用期薪资
     * @return 薪资项列表
     */
    private List<HrmSalaryEmployeeInfoDO.SalaryOption> getSalaryEmployeeInfoOptionList(
            Long employeeId, boolean probation) {
        HrmSalaryEmployeeInfoDO salaryEmployeeInfo =
                getSalaryEmployeeInfoByEmployeeId(employeeId);
        if (salaryEmployeeInfo == null) {
            return Collections.emptyList();
        }
        return CollUtil.emptyIfNull(probation
                ? salaryEmployeeInfo.getProbationSalaryOptions() : salaryEmployeeInfo.getSalaryOptions());
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private HrmSalaryEmployeeInfoServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
