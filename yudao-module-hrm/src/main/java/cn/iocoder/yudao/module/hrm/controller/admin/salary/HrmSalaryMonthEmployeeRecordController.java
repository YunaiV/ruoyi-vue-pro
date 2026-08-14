package cn.iocoder.yudao.module.hrm.controller.admin.salary;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.employee.HrmSalaryEmployeeMonthRecordPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.employee.HrmSalaryMonthEmployeeRecordListReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.employee.HrmSalaryMonthEmployeeRecordPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.employee.HrmSalaryMonthEmployeeRecordRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.employee.HrmSalaryMonthEmployeeRecordUpdateReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.employee.HrmSalaryPerformanceCoefficientReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.monthrecord.HrmSalaryMonthEmployeeRecordDO;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.hrm.service.salary.monthrecord.HrmSalaryMonthEmployeeRecordService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - HRM 员工月度工资")
@RestController
@RequestMapping("/hrm/salary/month-employee-record")
@Validated
public class HrmSalaryMonthEmployeeRecordController {

    @Resource
    private HrmSalaryMonthEmployeeRecordService monthEmployeeRecordService;
    @Resource
    private HrmEmployeeService employeeService;
    @Resource
    private DeptApi deptApi;

    @PutMapping("/update-list")
    @Operation(summary = "批量修改员工工资项")
    @PreAuthorize("@ss.hasPermission('hrm:salary:month-record:update')")
    public CommonResult<Boolean> updateMonthEmployeeRecordList(
            @NotEmpty(message = "员工工资记录不能为空")
            @RequestBody List<@Valid HrmSalaryMonthEmployeeRecordUpdateReqVO> reqVOs) {
        monthEmployeeRecordService.updateMonthEmployeeRecordList(reqVOs);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得员工月度工资分页")
    @PreAuthorize("@ss.hasPermission('hrm:salary:month-record:query')")
    public CommonResult<PageResult<HrmSalaryMonthEmployeeRecordRespVO>> getMonthEmployeeRecordPage(
            @Valid HrmSalaryMonthEmployeeRecordPageReqVO reqVO) {
        PageResult<HrmSalaryMonthEmployeeRecordDO> pageResult =
                monthEmployeeRecordService.getMonthEmployeeRecordPage(reqVO);
        return success(new PageResult<>(buildMonthEmployeeRecordRespVOList(pageResult.getList()),
                pageResult.getTotal()));
    }

    @GetMapping("/employee-page")
    @Operation(summary = "获得指定员工的月度工资分页")
    @PreAuthorize("@ss.hasPermission('hrm:salary:month-record:query')")
    public CommonResult<PageResult<HrmSalaryMonthEmployeeRecordRespVO>> getEmployeeMonthRecordPage(
            @Valid HrmSalaryEmployeeMonthRecordPageReqVO reqVO) {
        PageResult<HrmSalaryMonthEmployeeRecordDO> pageResult =
                monthEmployeeRecordService.getEmployeeMonthRecordPage(reqVO);
        return success(new PageResult<>(buildMonthEmployeeRecordRespVOList(pageResult.getList()),
                pageResult.getTotal()));
    }

    @GetMapping("/list")
    @Operation(summary = "获得员工月度工资列表")
    @PreAuthorize("@ss.hasPermission('hrm:salary:month-record:query')")
    public CommonResult<List<HrmSalaryMonthEmployeeRecordRespVO>> getMonthEmployeeRecordList(
            @Valid HrmSalaryMonthEmployeeRecordListReqVO reqVO) {
        return success(buildMonthEmployeeRecordRespVOList(
                monthEmployeeRecordService.getMonthEmployeeRecordList(reqVO)));
    }

    @GetMapping("/change-count")
    @Operation(summary = "获得工资表员工异动分类数量")
    @PreAuthorize("@ss.hasPermission('hrm:salary:month-record:query')")
    public CommonResult<Map<Integer, Long>> getMonthEmployeeChangeCount(
            @Valid HrmSalaryMonthEmployeeRecordPageReqVO reqVO) {
        return success(monthEmployeeRecordService.getMonthEmployeeChangeCount(reqVO));
    }

    @PostMapping("/performance-coefficients")
    @Operation(summary = "获得员工绩效系数")
    @PreAuthorize("@ss.hasPermission('hrm:salary:month-record:query')")
    public CommonResult<Map<Long, BigDecimal>> getPerformanceCoefficientMap(
            @Valid @RequestBody HrmSalaryPerformanceCoefficientReqVO reqVO) {
        return success(monthEmployeeRecordService.getPerformanceCoefficientMap(reqVO));
    }

    // ==================== 拼接 VO ====================

    private List<HrmSalaryMonthEmployeeRecordRespVO> buildMonthEmployeeRecordRespVOList(
            List<HrmSalaryMonthEmployeeRecordDO> employeeRecords) {
        if (CollUtil.isEmpty(employeeRecords)) {
            return Collections.emptyList();
        }

        // 1.1 批量查询员工和部门
        Set<Long> employeeIds = convertSet(employeeRecords, HrmSalaryMonthEmployeeRecordDO::getEmployeeId);
        Map<Long, HrmEmployeeDO> employeeMap = employeeService.getEmployeeMap(employeeIds);
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
                convertSet(employeeMap.values(), HrmEmployeeDO::getDeptId));
        // 1.2 批量查询绩效系数和薪资项
        HrmSalaryMonthEmployeeRecordDO firstRecord = CollUtil.getFirst(employeeRecords);
        boolean sameMonth = employeeRecords.stream().allMatch(record ->
                Objects.equals(record.getYear(), firstRecord.getYear())
                        && Objects.equals(record.getMonth(), firstRecord.getMonth()));
        Map<Long, BigDecimal> coefficientMap = sameMonth
                ? monthEmployeeRecordService.getPerformanceCoefficientMap(
                        new HrmSalaryPerformanceCoefficientReqVO().setYear(firstRecord.getYear())
                                .setMonth(firstRecord.getMonth()).setEmployeeIds(new ArrayList<>(employeeIds)))
                : Collections.emptyMap();
        // 2. 拼接响应
        List<HrmSalaryMonthEmployeeRecordRespVO> respVOs = BeanUtils.toBean(
                employeeRecords, HrmSalaryMonthEmployeeRecordRespVO.class);
        for (HrmSalaryMonthEmployeeRecordRespVO respVO : respVOs) {
            MapUtils.findAndThen(employeeMap, respVO.getEmployeeId(), employee -> {
                respVO.setEmployeeName(employee.getName()).setJobNumber(employee.getJobNumber())
                        .setDeptId(employee.getDeptId()).setPostName(employee.getPostName());
                MapUtils.findAndThen(deptMap, employee.getDeptId(),
                        dept -> respVO.setDeptName(dept.getName()));
            });
            respVO.setPerformanceCoefficient(coefficientMap.get(respVO.getEmployeeId()));
        }
        return respVOs;
    }

}
