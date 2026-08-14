package cn.iocoder.yudao.module.hrm.controller.admin.salary;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.statistics.HrmAttendanceMonthRecordRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.option.HrmSalaryOptionValueVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.employee.HrmSalaryMonthEmployeeRecordListReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.HrmSalaryMonthRecordPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.HrmSalaryMonthRecordRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.HrmSalaryPayrollReadinessRespVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryGroupDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.monthrecord.HrmSalaryMonthEmployeeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.monthrecord.HrmSalaryMonthRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryTaxRuleDO;
import cn.iocoder.yudao.module.hrm.service.attendance.statistics.HrmAttendanceStatisticsService;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.hrm.service.salary.config.HrmSalaryGroupService;
import cn.iocoder.yudao.module.hrm.service.salary.monthrecord.HrmSalaryMonthEmployeeRecordService;
import cn.iocoder.yudao.module.hrm.service.salary.monthrecord.HrmSalaryMonthRecordService;
import cn.iocoder.yudao.module.hrm.service.salary.config.HrmSalaryTaxRuleService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.config.HrmSalaryTaxTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - HRM 月度工资表")
@RestController
@RequestMapping("/hrm/salary/month-record")
@Validated
public class HrmSalaryMonthRecordController {

    @Resource
    private HrmSalaryMonthRecordService salaryMonthRecordService;
    @Resource
    private HrmSalaryMonthEmployeeRecordService monthEmployeeRecordService;
    @Resource
    private HrmSalaryGroupService salaryGroupService;
    @Resource
    private HrmSalaryTaxRuleService salaryTaxRuleService;
    @Resource
    private HrmEmployeeService employeeService;
    @Resource
    private HrmAttendanceStatisticsService attendanceStatisticsService;

    @Resource
    private DeptApi deptApi;

    @PostMapping("/create-next")
    @Operation(summary = "创建下月工资表")
    @PreAuthorize("@ss.hasPermission('hrm:salary:month-record:create')")
    public CommonResult<Long> createNextMonthRecord() {
        return success(salaryMonthRecordService.createNextMonthRecord());
    }

    @PostMapping("/compute")
    @Operation(summary = "核算工资表")
    @Parameter(name = "id", description = "工资表编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:salary:month-record:compute')")
    public CommonResult<Boolean> computeMonthRecord(@RequestParam("id") Long id) {
        salaryMonthRecordService.computeMonthRecord(id);
        return success(true);
    }

    @PostMapping(value = "/compute-import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "带导入文件核算工资表")
    @Parameters({
            @Parameter(name = "id", description = "工资表编号", required = true, example = "1024"),
            @Parameter(name = "syncInsuranceData", description = "是否同步社保数据", example = "true"),
            @Parameter(name = "syncAttendanceData", description = "是否同步考勤数据", example = "false"),
            @Parameter(name = "attendanceFile", description = "考勤数据文件"),
            @Parameter(name = "additionalDeductionFile", description = "专项附加扣除数据文件"),
            @Parameter(name = "cumulativeTaxFile", description = "上月个税累计数据文件")
    })
    @PreAuthorize("@ss.hasPermission('hrm:salary:month-record:compute')")
    @ApiAccessLog(operateType = OperateTypeEnum.IMPORT)
    public CommonResult<Boolean> computeMonthRecordWithImport(
            @RequestParam("id") Long id,
            @RequestParam(value = "syncInsuranceData", defaultValue = "true") boolean syncInsuranceData,
            @RequestParam(value = "syncAttendanceData", defaultValue = "false") boolean syncAttendanceData,
            @RequestParam(value = "attendanceFile", required = false) MultipartFile attendanceFile,
            @RequestParam(value = "additionalDeductionFile", required = false) MultipartFile additionalDeductionFile,
            @RequestParam(value = "cumulativeTaxFile", required = false) MultipartFile cumulativeTaxFile)
            throws IOException {
        salaryMonthRecordService.computeMonthRecord(id, syncInsuranceData, syncAttendanceData,
                ExcelUtils.read(attendanceFile),
                ExcelUtils.read(additionalDeductionFile),
                ExcelUtils.read(cumulativeTaxFile));
        return success(true);
    }

    @GetMapping("/get-attendance-import-template")
    @Operation(summary = "获得工资表考勤导入模板")
    @Parameter(name = "monthRecordId", description = "月度工资表编号", example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:salary:month-record:compute')")
    @ApiAccessLog(operateType = OperateTypeEnum.EXPORT)
    public void getAttendanceImportTemplate(
            @RequestParam(value = "monthRecordId", required = false) Long monthRecordId,
            HttpServletResponse response) throws IOException {
        List<HrmEmployeeDO> employees = getPayrollImportEmployeeList(monthRecordId, false);
        List<List<Object>> data;
        if (monthRecordId == null) {
            data = buildImportTemplateData(employees, 8);
        } else {
            HrmSalaryMonthRecordDO monthRecord = salaryMonthRecordService.getMonthRecord(monthRecordId);
            if (monthRecord == null) {
                data = buildImportTemplateData(employees, 8);
            } else {
                Map<Long, HrmAttendanceMonthRecordRespVO> attendanceMap = convertMap(
                        attendanceStatisticsService.getAttendanceMonthRecordList(monthRecord.getYear(),
                                monthRecord.getMonth(), convertList(employees, HrmEmployeeDO::getId)),
                        HrmAttendanceMonthRecordRespVO::getEmployeeId);
                data = convertList(employees, employee -> {
                    List<Object> row = buildImportEmployeeRow(employee);
                    HrmAttendanceMonthRecordRespVO attendance = attendanceMap.get(employee.getId());
                    row.add(BigDecimal.ZERO);
                    row.add(attendance == null ? BigDecimal.ZERO : attendance.getLateDeductAmount());
                    row.add(attendance == null ? BigDecimal.ZERO : attendance.getEarlyDeductAmount());
                    row.add(attendance == null ? BigDecimal.ZERO : attendance.getAbsenteeismDeductAmount());
                    row.add(BigDecimal.ZERO);
                    row.add(attendance == null ? BigDecimal.ZERO : attendance.getMisscardDeductAmount());
                    row.add(BigDecimal.ZERO);
                    row.add(attendance == null ? BigDecimal.ZERO : attendance.getActualDays());
                    return row;
                });
            }
        }
        List<List<String>> head = buildImportHead(Arrays.asList(
                "加班工资", "迟到扣款", "早退扣款", "旷工扣款", "假期扣款",
                "缺卡扣款", "综合扣款", "实际计薪天数"));
        ExcelUtils.write(response, "月度工资考勤导入模板.xls", "月度工资", head, data);
    }

    @GetMapping("/get-cumulative-tax-import-template")
    @Operation(summary = "获得工资表上月个税累计导入模板")
    @Parameter(name = "monthRecordId", description = "月度工资表编号", example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:salary:month-record:compute')")
    @ApiAccessLog(operateType = OperateTypeEnum.EXPORT)
    public void getCumulativeTaxImportTemplate(
            @RequestParam(value = "monthRecordId", required = false) Long monthRecordId,
            HttpServletResponse response) throws IOException {
        List<List<String>> head = buildImportHead(Arrays.asList(
                "累计收入额（截至上月）", "累计减除费用（截至上月）",
                "累计专项扣除（截至上月）", "累计已预缴税额"));
        List<HrmEmployeeDO> employees = getPayrollImportEmployeeList(monthRecordId, true);
        List<List<Object>> data = buildImportTemplateData(employees, 4);
        ExcelUtils.write(response, "月度工资上月个税累计导入模板.xls", "月度工资", head, data);
    }

    @GetMapping("/get-additional-deduction-import-template")
    @Operation(summary = "获得工资表专项附加扣除导入模板")
    @Parameter(name = "monthRecordId", description = "月度工资表编号", example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:salary:month-record:compute')")
    @ApiAccessLog(operateType = OperateTypeEnum.EXPORT)
    public void getAdditionalDeductionImportTemplate(
            @RequestParam(value = "monthRecordId", required = false) Long monthRecordId,
            HttpServletResponse response) throws IOException {
        List<List<String>> head = buildImportHead(Arrays.asList(
                "累计子女教育", "累计住房租金", "累计住房贷款利息", "累计赡养老人", "累计继续教育"));
        List<HrmEmployeeDO> employees = getPayrollImportEmployeeList(monthRecordId, true);
        List<List<Object>> data = buildImportTemplateData(employees, 5);
        ExcelUtils.write(response, "月度工资专项附加扣除导入模板.xls", "月度工资", head, data);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除月度工资表")
    @Parameter(name = "id", description = "工资表编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:salary:month-record:delete')")
    public CommonResult<Boolean> deleteMonthRecord(@RequestParam("id") Long id) {
        salaryMonthRecordService.deleteMonthRecord(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得月度工资表")
    @Parameter(name = "id", description = "月度工资表编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:salary:month-record:query')")
    public CommonResult<HrmSalaryMonthRecordRespVO> getMonthRecord(@RequestParam("id") Long id) {
        HrmSalaryMonthRecordDO monthRecord = salaryMonthRecordService.getMonthRecord(id);
        return success(buildMonthRecordRespVO(monthRecord));
    }

    @GetMapping("/page")
    @Operation(summary = "获得月度工资表分页")
    @PreAuthorize("@ss.hasPermission('hrm:salary:month-record:query')")
    public CommonResult<PageResult<HrmSalaryMonthRecordRespVO>> getMonthRecordPage(
            @Valid HrmSalaryMonthRecordPageReqVO reqVO) {
        PageResult<HrmSalaryMonthRecordDO> pageResult = salaryMonthRecordService.getMonthRecordPage(reqVO);
        return success(BeanUtils.toBean(pageResult, HrmSalaryMonthRecordRespVO.class));
    }

    @GetMapping("/last")
    @Operation(summary = "获得最近月度工资表")
    @PreAuthorize("@ss.hasPermission('hrm:salary:month-record:query')")
    public CommonResult<HrmSalaryMonthRecordRespVO> getLastMonthRecord() {
        HrmSalaryMonthRecordDO monthRecord = salaryMonthRecordService.getLastMonthRecord();
        return success(buildMonthRecordRespVO(monthRecord));
    }

    @GetMapping("/payroll-readiness")
    @Operation(summary = "获得工资核算准备情况")
    @Parameter(name = "monthRecordId", description = "月度工资表编号", example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:salary:month-record:query')")
    public CommonResult<HrmSalaryPayrollReadinessRespVO> getPayrollReadiness(
            @RequestParam(value = "monthRecordId", required = false) Long monthRecordId) {
        HrmSalaryPayrollReadinessRespVO readiness =
                salaryMonthRecordService.getPayrollReadiness(monthRecordId);
        if (readiness == null) {
            return success(null);
        }
        fillPayrollReadinessEmployeeDeptName(readiness.getNoSalaryEmployees());
        fillPayrollReadinessEmployeeDeptName(readiness.getNoSalaryGroupEmployees());
        return success(readiness);
    }

    @GetMapping("/option-summary")
    @Operation(summary = "获得工资项汇总")
    @PreAuthorize("@ss.hasPermission('hrm:salary:month-record:query')")
    public CommonResult<List<HrmSalaryOptionValueVO>> getMonthOptionSummary(
            @Valid HrmSalaryMonthEmployeeRecordListReqVO reqVO) {
        List<HrmSalaryMonthEmployeeRecordDO> employeeRecords =
                monthEmployeeRecordService.getMonthEmployeeRecordList(reqVO);
        List<HrmSalaryMonthEmployeeRecordDO.OptionValue> optionSummary =
                monthEmployeeRecordService.getMonthOptionSummary(employeeRecords);
        return success(BeanUtils.toBean(optionSummary, HrmSalaryOptionValueVO.class));
    }

    // ==================== 拼接 VO ====================

    private HrmSalaryMonthRecordRespVO buildMonthRecordRespVO(HrmSalaryMonthRecordDO monthRecord) {
        if (monthRecord == null) {
            return null;
        }
        return BeanUtils.toBean(monthRecord, HrmSalaryMonthRecordRespVO.class);
    }

    private void fillPayrollReadinessEmployeeDeptName(
            List<HrmSalaryPayrollReadinessRespVO.Employee> employees) {
        if (CollUtil.isEmpty(employees)) {
            return;
        }
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
                convertSet(employees, HrmSalaryPayrollReadinessRespVO.Employee::getDeptId));
        employees.forEach(employee -> MapUtils.findAndThen(deptMap, employee.getDeptId(),
                dept -> employee.setDeptName(dept.getName())));
    }

    // ==================== Excel 导入导出 ====================

    private List<HrmEmployeeDO> getPayrollImportEmployeeList(Long monthRecordId, boolean salaryTaxOnly) {
        // 1. 获得指定或最近月度工资表
        HrmSalaryMonthRecordDO monthRecord = monthRecordId == null
                ? salaryMonthRecordService.getLastMonthRecord()
                : salaryMonthRecordService.getMonthRecord(monthRecordId);
        if (monthRecord == null) {
            return Collections.emptyList();
        }

        // 2. 获得指定工资表已有员工，或尚未核算时的计薪员工
        List<HrmEmployeeDO> employees;
        if (monthRecordId != null) {
            List<HrmSalaryMonthEmployeeRecordDO> employeeRecords =
                    monthEmployeeRecordService.getMonthEmployeeRecordListByMonthRecordId(monthRecordId);
            if (CollUtil.isNotEmpty(employeeRecords)) {
                Map<Long, HrmEmployeeDO> employeeMap = employeeService.getEmployeeMap(
                        convertSet(employeeRecords, HrmSalaryMonthEmployeeRecordDO::getEmployeeId));
                employees = convertList(employeeRecords, record -> employeeMap.get(record.getEmployeeId()),
                        Objects::nonNull);
            } else {
                employees = salaryMonthRecordService.getPayrollEmployeeList(monthRecord);
            }
        } else {
            employees = salaryMonthRecordService.getPayrollEmployeeList(monthRecord);
        }

        // 3. 筛选已配置薪资组的员工
        Map<Long, HrmSalaryGroupDO> salaryGroupMap = salaryGroupService.getEmployeeSalaryGroupMap(employees);
        if (!salaryTaxOnly) {
            return convertList(employees, employee -> employee,
                    employee -> salaryGroupMap.containsKey(employee.getId()));
        }

        // 4. 个税累计模板只包含使用工资薪金计税规则的员工
        Set<Long> taxRuleIds = convertSet(salaryGroupMap.values(), HrmSalaryGroupDO::getTaxRuleId,
                salaryGroup -> salaryGroup.getTaxRuleId() != null);
        Map<Long, HrmSalaryTaxRuleDO> taxRuleMap = salaryTaxRuleService.getSalaryTaxRuleMap(taxRuleIds);
        return convertList(employees, employee -> employee, employee -> {
            HrmSalaryGroupDO salaryGroup = salaryGroupMap.get(employee.getId());
            HrmSalaryTaxRuleDO taxRule = salaryGroup == null ? null
                    : taxRuleMap.get(salaryGroup.getTaxRuleId());
            return taxRule != null && Objects.equals(taxRule.getType(), HrmSalaryTaxTypeEnum.SALARY.getType());
        });
    }

    private List<List<String>> buildImportHead(List<String> businessHeaders) {
        List<String> headers = Arrays.asList("员工名称", "岗位", "工号", "部门编号");
        return convertList(CollUtil.union(headers, businessHeaders), Collections::singletonList);
    }

    private List<List<Object>> buildImportTemplateData(List<HrmEmployeeDO> employees, int blankColumnSize) {
        return convertList(employees, employee -> {
            List<Object> row = buildImportEmployeeRow(employee);
            for (int i = 0; i < blankColumnSize; i++) {
                row.add("");
            }
            return row;
        });
    }

    private List<Object> buildImportEmployeeRow(HrmEmployeeDO employee) {
        return new ArrayList<>(Arrays.asList(
                employee.getName(), employee.getPostName(), employee.getJobNumber(), employee.getDeptId()));
    }

}
