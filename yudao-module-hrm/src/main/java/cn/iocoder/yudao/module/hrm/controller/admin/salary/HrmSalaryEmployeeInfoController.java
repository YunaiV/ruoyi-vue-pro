package cn.iocoder.yudao.module.hrm.controller.admin.salary;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeListReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeStatusCountRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.employeeinfo.HrmSalaryEmployeeInfoUpdateListReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.employeeinfo.HrmSalaryEmployeeInfoUpdateListRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.employeeinfo.HrmSalaryEmployeeInfoPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.employeeinfo.HrmSalaryEmployeeInfoUpdateReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.employeeinfo.HrmSalaryEmployeeInfoRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.employeeinfo.HrmSalaryEmployeeInfoImportRespVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.employee.HrmSalaryEmployeeInfoDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.monthrecord.HrmSalaryMonthRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryOptionDO;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeStatusTabEnum;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.hrm.service.salary.employee.HrmSalaryEmployeeInfoService;
import cn.iocoder.yudao.module.hrm.service.salary.monthrecord.HrmSalaryMonthRecordService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.employee.HrmSalaryEmployeeInfoChangeTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - HRM 员工薪资信息")
@RestController
@RequestMapping("/hrm/salary/employee-info")
@Validated
public class HrmSalaryEmployeeInfoController {

    @Resource
    private HrmSalaryEmployeeInfoService salaryEmployeeInfoService;
    @Resource
    private HrmEmployeeService employeeService;
    @Resource
    private HrmSalaryMonthRecordService salaryMonthRecordService;

    @Resource
    private DeptApi deptApi;

    @GetMapping("/page")
    @Operation(summary = "获得员工薪资信息分页")
    @PreAuthorize("@ss.hasPermission('hrm:salary:employee-info:query')")
    public CommonResult<PageResult<HrmSalaryEmployeeInfoRespVO>> getSalaryEmployeeInfoPage(
            @Validated HrmSalaryEmployeeInfoPageReqVO reqVO) {
        PageResult<HrmEmployeeDO> pageResult = salaryEmployeeInfoService.getSalaryEmployeeInfoPage(reqVO);
        return success(buildSalaryEmployeeInfoRespVOPageResult(pageResult));
    }

    @GetMapping("/status-count")
    @Operation(summary = "获得员工薪资信息状态数量")
    @PreAuthorize("@ss.hasPermission('hrm:salary:employee-info:query')")
    public CommonResult<List<HrmEmployeeStatusCountRespVO>> getSalaryEmployeeInfoStatusCount(
            @Validated HrmSalaryEmployeeInfoPageReqVO reqVO) {
        Map<Integer, Long> countMap = salaryEmployeeInfoService.getSalaryEmployeeInfoStatusCount(reqVO);
        return success(convertList(Arrays.asList(HrmEmployeeStatusTabEnum.values()),
                status -> new HrmEmployeeStatusCountRespVO(status.getStatus(),
                        countMap.getOrDefault(status.getStatus(), 0L))));
    }

    @GetMapping("/get")
    @Operation(summary = "获得员工薪资信息")
    @Parameter(name = "employeeId", description = "员工编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:salary:employee-info:query')")
    public CommonResult<HrmSalaryEmployeeInfoRespVO> getSalaryEmployeeInfo(
            @RequestParam("employeeId") Long employeeId) {
        HrmEmployeeDO employee = employeeService.getEmployee(employeeId);
        if (employee == null) {
            return success(null);
        }
        HrmSalaryEmployeeInfoDO salaryEmployeeInfo =
                salaryEmployeeInfoService.getSalaryEmployeeInfoByEmployeeId(employeeId);
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(Collections.singleton(employee.getDeptId()));
        return success(buildSalaryEmployeeInfoRespVO(employee, salaryEmployeeInfo, deptMap));
    }

    @GetMapping("/get-adjustment-min-effect-date")
    @Operation(summary = "获得最早调薪生效日期")
    @PreAuthorize("@ss.hasPermission('hrm:salary:employee-info:update')")
    public CommonResult<LocalDate> getSalaryAdjustmentMinEffectDate() {
        return success(getSalaryAdjustmentMinEffectDateValue());
    }

    private LocalDate getSalaryAdjustmentMinEffectDateValue() {
        HrmSalaryMonthRecordDO monthRecord = salaryMonthRecordService.getLastMonthRecord();
        if (monthRecord == null || monthRecord.getYear() == null || monthRecord.getMonth() == null) {
            return null;
        }
        return YearMonth.of(monthRecord.getYear(), monthRecord.getMonth()).atDay(1);
    }

    @PutMapping("/update")
    @Operation(summary = "修改员工薪资信息")
    @PreAuthorize("@ss.hasPermission('hrm:salary:employee-info:update')")
    public CommonResult<Long> updateSalaryEmployeeInfo(
            @Valid @RequestBody HrmSalaryEmployeeInfoUpdateReqVO reqVO) {
        return success(salaryEmployeeInfoService.updateSalaryEmployeeInfo(reqVO));
    }

    @PutMapping("/update-list")
    @Operation(summary = "批量调薪")
    @PreAuthorize("@ss.hasPermission('hrm:salary:employee-info:update')")
    public CommonResult<HrmSalaryEmployeeInfoUpdateListRespVO> updateSalaryEmployeeInfoList(
            @Valid @RequestBody HrmSalaryEmployeeInfoUpdateListReqVO reqVO) {
        return success(salaryEmployeeInfoService.updateSalaryEmployeeInfoList(reqVO));
    }

    @GetMapping("/get-fix-import-template")
    @Operation(summary = "获得薪资档案定薪导入模板")
    @PreAuthorize("@ss.hasPermission('hrm:salary:employee-info:import')")
    @ApiAccessLog(operateType = OperateTypeEnum.EXPORT)
    public void getFixImportTemplate(HttpServletResponse response) throws IOException {
        List<HrmSalaryOptionDO> options = salaryEmployeeInfoService.getSalaryImportOptionList();
        List<List<String>> head = buildFixSalaryImportHead(options);
        List<List<Object>> data = buildFixSalaryImportTemplateData(options);
        ExcelUtils.write(response, "薪资档案定薪导入模板.xls", "薪资档案", head, data);
    }

    @PostMapping("/import-fix")
    @Operation(summary = "导入薪资档案定薪")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('hrm:salary:employee-info:import')")
    @ApiAccessLog(operateType = OperateTypeEnum.IMPORT)
    public CommonResult<HrmSalaryEmployeeInfoImportRespVO> importFixExcel(@RequestParam("file") MultipartFile file)
            throws IOException {
        return success(salaryEmployeeInfoService.importFixSalaryList(ExcelUtils.read(file)));
    }

    @GetMapping("/get-change-import-template")
    @Operation(summary = "获得薪资档案调薪导入模板")
    @PreAuthorize("@ss.hasPermission('hrm:salary:employee-info:import')")
    @ApiAccessLog(operateType = OperateTypeEnum.EXPORT)
    public void getChangeImportTemplate(HttpServletResponse response) throws IOException {
        List<HrmSalaryOptionDO> options = salaryEmployeeInfoService.getSalaryImportOptionList();
        List<List<String>> head = buildChangeSalaryImportHead(options);
        List<List<Object>> data = buildChangeSalaryImportTemplateData(options);
        ExcelUtils.write(response, "薪资档案调薪导入模板.xls", "薪资档案", head, data);
    }

    @PostMapping("/import-change")
    @Operation(summary = "导入薪资档案调薪")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('hrm:salary:employee-info:import')")
    @ApiAccessLog(operateType = OperateTypeEnum.IMPORT)
    public CommonResult<HrmSalaryEmployeeInfoImportRespVO> importChangeExcel(@RequestParam("file") MultipartFile file)
            throws IOException {
        return success(salaryEmployeeInfoService.importChangeSalaryList(ExcelUtils.read(file)));
    }

    // ==================== 拼接 VO ====================

    private PageResult<HrmSalaryEmployeeInfoRespVO> buildSalaryEmployeeInfoRespVOPageResult(
            PageResult<HrmEmployeeDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        // 1. 获取关联数据
        Map<Long, HrmSalaryEmployeeInfoDO> salaryEmployeeInfoMap =
                salaryEmployeeInfoService.getSalaryEmployeeInfoMap(
                        convertSet(pageResult.getList(), HrmEmployeeDO::getId));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
                convertSet(pageResult.getList(), HrmEmployeeDO::getDeptId));

        // 2. 拼接响应
        return new PageResult<>(convertList(pageResult.getList(), employee -> buildSalaryEmployeeInfoRespVO(
                employee, salaryEmployeeInfoMap.get(employee.getId()), deptMap)), pageResult.getTotal());
    }

    private HrmSalaryEmployeeInfoRespVO buildSalaryEmployeeInfoRespVO(
            HrmEmployeeDO employee, HrmSalaryEmployeeInfoDO salaryEmployeeInfo, Map<Long, DeptRespDTO> deptMap) {
        // 1. 拼接员工基础信息
        HrmSalaryEmployeeInfoRespVO respVO = salaryEmployeeInfo == null
                ? new HrmSalaryEmployeeInfoRespVO()
                : BeanUtils.toBean(salaryEmployeeInfo, HrmSalaryEmployeeInfoRespVO.class);
        respVO.setEmployeeId(employee.getId()).setEmployeeName(employee.getName())
                .setJobNumber(employee.getJobNumber()).setMobile(employee.getMobile())
                .setDeptId(employee.getDeptId()).setPostName(employee.getPostName())
                .setEntryStatus(employee.getEntryStatus()).setStatus(employee.getStatus())
                .setEntryTime(employee.getEntryTime()).setRegularTime(employee.getRegularTime());
        MapUtils.findAndThen(deptMap, employee.getDeptId(),
                dept -> respVO.setDeptName(dept.getName()));

        // 2. 拼接当前薪资信息
        if (salaryEmployeeInfo == null) {
            respVO.setChangeType(HrmSalaryEmployeeInfoChangeTypeEnum.UNSET.getType())
                    .setSalaryOptions(Collections.emptyList()).setProbationSalaryOptions(Collections.emptyList());
            return respVO;
        }
        return respVO;
    }

    // ==================== Excel 导入导出 ====================

    private List<List<String>> buildFixSalaryImportHead(List<HrmSalaryOptionDO> options) {
        List<List<String>> head = buildSalaryImportEmployeeHead();
        options.forEach(option -> head.add(Collections.singletonList("试用期-" + option.getName())));
        options.forEach(option -> head.add(Collections.singletonList("正式-" + option.getName())));
        head.add(Collections.singletonList("备注"));
        return head;
    }

    private List<List<Object>> buildFixSalaryImportTemplateData(List<HrmSalaryOptionDO> options) {
        List<HrmEmployeeDO> employees = getActiveEmployeeList();
        List<List<Object>> rows = convertList(employees, employee -> {
            List<Object> row = buildSalaryImportEmployeeRow(employee);
            options.forEach(option -> row.add(BigDecimal.ZERO));
            options.forEach(option -> row.add(BigDecimal.ZERO));
            row.add("");
            return row;
        });
        if (CollUtil.isEmpty(rows)) {
            List<Object> row = new ArrayList<>(Arrays.asList(
                    "张三", "HRM001", 100L, "Java 工程师"));
            options.forEach(option -> row.add(BigDecimal.ZERO));
            options.forEach(option -> row.add(BigDecimal.ZERO));
            row.add("示例定薪");
            rows.add(row);
        }
        return rows;
    }

    private List<List<String>> buildChangeSalaryImportHead(List<HrmSalaryOptionDO> options) {
        List<List<String>> head = buildSalaryImportBaseHead("调薪原因");
        options.forEach(option -> {
            head.add(Collections.singletonList("试用期-" + option.getName() + "-调整前"));
            head.add(Collections.singletonList("试用期-" + option.getName() + "-调整后"));
        });
        options.forEach(option -> {
            head.add(Collections.singletonList("正式-" + option.getName() + "-调整前"));
            head.add(Collections.singletonList("正式-" + option.getName() + "-调整后"));
        });
        head.add(Collections.singletonList("备注"));
        return head;
    }

    private List<List<Object>> buildChangeSalaryImportTemplateData(List<HrmSalaryOptionDO> options) {
        List<HrmEmployeeDO> employees = getActiveEmployeeList();
        Map<Long, HrmSalaryEmployeeInfoDO> salaryEmployeeInfoMap =
                salaryEmployeeInfoService.getSalaryEmployeeInfoMap(convertSet(employees, HrmEmployeeDO::getId));
        LocalDate effectDate = getSalaryAdjustmentMinEffectDateValue();
        if (effectDate == null || effectDate.isBefore(LocalDate.now())) {
            effectDate = LocalDate.now();
        }
        LocalDate finalEffectDate = effectDate;
        return convertList(employees, employee -> {
            HrmSalaryEmployeeInfoDO salaryEmployeeInfo = salaryEmployeeInfoMap.get(employee.getId());
            List<Object> row = buildSalaryImportBaseRow(employee, finalEffectDate, "转正");
            appendChangeOptionTemplateValues(row, options,
                    buildSalaryOptionValueMap(salaryEmployeeInfo == null
                            ? Collections.emptyList() : salaryEmployeeInfo.getProbationSalaryOptions()));
            appendChangeOptionTemplateValues(row, options,
                    buildSalaryOptionValueMap(salaryEmployeeInfo == null
                            ? Collections.emptyList() : salaryEmployeeInfo.getSalaryOptions()));
            row.add("");
            return row;
        });
    }

    private List<HrmEmployeeDO> getActiveEmployeeList() {
        return employeeService.getEmployeeList(new HrmEmployeeListReqVO()
                .setStatusCategory(HrmEmployeeStatusTabEnum.ACTIVE.getStatus()));
    }

    private List<List<String>> buildSalaryImportBaseHead(String reasonName) {
        List<List<String>> head = buildSalaryImportEmployeeHead();
        head.add(Collections.singletonList("生效日期"));
        head.add(Collections.singletonList(reasonName));
        return head;
    }

    private List<List<String>> buildSalaryImportEmployeeHead() {
        return new ArrayList<>(Arrays.asList(
                Collections.singletonList("员工姓名"),
                Collections.singletonList("工号"),
                Collections.singletonList("部门编号"),
                Collections.singletonList("岗位")));
    }

    private List<Object> buildSalaryImportEmployeeRow(HrmEmployeeDO employee) {
        return new ArrayList<>(Arrays.asList(
                employee.getName(), employee.getJobNumber(), employee.getDeptId(), employee.getPostName()));
    }

    private List<Object> buildSalaryImportBaseRow(HrmEmployeeDO employee, LocalDate effectDate, String changeReason) {
        List<Object> row = buildSalaryImportEmployeeRow(employee);
        row.add(effectDate.toString());
        row.add(changeReason);
        return row;
    }

    private Map<Integer, BigDecimal> buildSalaryOptionValueMap(
            List<HrmSalaryEmployeeInfoDO.SalaryOption> options) {
        return convertMap(options, HrmSalaryEmployeeInfoDO.SalaryOption::getCode,
                option -> option.getValue() == null ? BigDecimal.ZERO : option.getValue());
    }

    private void appendChangeOptionTemplateValues(List<Object> row, List<HrmSalaryOptionDO> options,
                                                  Map<Integer, BigDecimal> valueMap) {
        for (HrmSalaryOptionDO option : options) {
            BigDecimal value = valueMap.getOrDefault(option.getCode(), BigDecimal.ZERO);
            row.add(value);
            row.add(value);
        }
    }

}
