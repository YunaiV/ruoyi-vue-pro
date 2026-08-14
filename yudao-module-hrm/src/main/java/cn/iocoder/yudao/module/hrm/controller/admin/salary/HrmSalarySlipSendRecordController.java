package cn.iocoder.yudao.module.hrm.controller.admin.salary;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.sendrecord.HrmSalarySlipSendEmployeeReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.sendrecord.HrmSalarySlipSendEmployeeRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.sendrecord.HrmSalarySlipSendRecordPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.sendrecord.HrmSalarySlipSendRecordRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.sendrecord.HrmSalarySlipSendReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.monthrecord.HrmSalaryMonthEmployeeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.slip.HrmSalarySlipSendRecordDO;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.hrm.service.salary.slip.HrmSalarySlipService;
import cn.iocoder.yudao.module.hrm.service.salary.slip.HrmSalarySlipSendRecordService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - HRM 工资条发放记录")
@RestController
@RequestMapping("/hrm/salary/slip-send-record")
@Validated
public class HrmSalarySlipSendRecordController {

    @Resource
    private HrmSalarySlipSendRecordService salarySlipSendRecordService;
    @Resource
    private HrmSalarySlipService salarySlipService;
    @Resource
    private HrmEmployeeService employeeService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @PostMapping("/create")
    @Operation(summary = "发放工资条")
    @PreAuthorize("@ss.hasPermission('hrm:salary:slip:create')")
    public CommonResult<Long> sendSalarySlip(@Valid @RequestBody HrmSalarySlipSendReqVO reqVO) {
        return success(salarySlipSendRecordService.sendSalarySlip(reqVO));
    }

    @GetMapping("/employee-page")
    @Operation(summary = "获得工资条待发员工分页")
    @PreAuthorize("@ss.hasPermission('hrm:salary:slip:query')")
    public CommonResult<PageResult<HrmSalarySlipSendEmployeeRespVO>> getSalarySlipSendEmployeePage(
            @Valid HrmSalarySlipSendEmployeeReqVO reqVO) {
        // 1. 获得员工月度工资分页
        PageResult<HrmSalaryMonthEmployeeRecordDO> pageResult =
                salarySlipSendRecordService.getSalarySlipSendEmployeePage(reqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }

        // 2.1 获取员工和发放状态
        Map<Long, HrmEmployeeDO> employeeMap = employeeService.getEmployeeMap(
                convertSet(pageResult.getList(), HrmSalaryMonthEmployeeRecordDO::getEmployeeId));
        Set<Long> sentMonthEmployeeRecordIds = salarySlipService.getSentMonthEmployeeRecordIdSet(
                convertSet(pageResult.getList(), HrmSalaryMonthEmployeeRecordDO::getId));
        // 2.2 拼接响应
        return success(new PageResult<>(buildSalarySlipSendEmployeeRespVOList(
                pageResult.getList(), employeeMap, sentMonthEmployeeRecordIds), pageResult.getTotal()));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除工资条发放记录")
    @Parameter(name = "id", description = "发放记录编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:salary:slip:delete')")
    public CommonResult<Boolean> deleteSalarySlipSendRecord(@RequestParam("id") Long id) {
        salarySlipSendRecordService.deleteSalarySlipSendRecord(id);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得工资条发放记录分页")
    @PreAuthorize("@ss.hasPermission('hrm:salary:slip:query')")
    public CommonResult<PageResult<HrmSalarySlipSendRecordRespVO>> getSalarySlipSendRecordPage(
            @Validated HrmSalarySlipSendRecordPageReqVO reqVO) {
        PageResult<HrmSalarySlipSendRecordDO> pageResult =
                salarySlipSendRecordService.getSalarySlipSendRecordPage(reqVO);
        return success(new PageResult<>(buildSalarySlipSendRecordRespVOList(pageResult.getList()),
                pageResult.getTotal()));
    }

    @GetMapping("/get")
    @Operation(summary = "获得工资条发放记录")
    @Parameter(name = "id", description = "发放记录编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:salary:slip:query')")
    public CommonResult<HrmSalarySlipSendRecordRespVO> getSalarySlipSendRecord(@RequestParam("id") Long id) {
        HrmSalarySlipSendRecordDO slipSendRecord =
                salarySlipSendRecordService.getSalarySlipSendRecord(id);
        if (slipSendRecord == null) {
            return success(null);
        }
        return success(CollUtil.getFirst(buildSalarySlipSendRecordRespVOList(
                Collections.singletonList(slipSendRecord))));
    }

    // ==================== 拼接 VO ====================

    private List<HrmSalarySlipSendRecordRespVO> buildSalarySlipSendRecordRespVOList(
            List<HrmSalarySlipSendRecordDO> slipSendRecords) {
        if (CollUtil.isEmpty(slipSendRecords)) {
            return Collections.emptyList();
        }
        // 1. 获取关联数据
        Map<Long, Long> readCountMap = salarySlipService.getSalarySlipReadCountMap(
                convertSet(slipSendRecords, HrmSalarySlipSendRecordDO::getId));
        Map<Long, AdminUserRespDTO> creatorMap = adminUserApi.getUserMap(
                convertSet(slipSendRecords, record -> NumberUtil.isNumber(record.getCreator())
                        ? NumberUtils.parseLong(record.getCreator()) : null));

        // 2. 拼接响应
        return BeanUtils.toBean(slipSendRecords, HrmSalarySlipSendRecordRespVO.class, record -> {
            record.setReadCount(readCountMap.getOrDefault(record.getId(), 0L).intValue())
                    .setCreatorName(record.getCreator());
            MapUtils.findAndThen(creatorMap, NumberUtil.isNumber(record.getCreator())
                            ? NumberUtils.parseLong(record.getCreator()) : null,
                    creator -> record.setCreatorName(creator.getNickname()));
        });
    }

    private List<HrmSalarySlipSendEmployeeRespVO> buildSalarySlipSendEmployeeRespVOList(
            List<HrmSalaryMonthEmployeeRecordDO> employeeRecords,
            Map<Long, HrmEmployeeDO> employeeMap, Set<Long> sentMonthEmployeeRecordIds) {
        if (CollUtil.isEmpty(employeeRecords)) {
            return Collections.emptyList();
        }
        // 1. 获取部门信息
        Set<Long> deptIds = convertSet(employeeMap.values(), HrmEmployeeDO::getDeptId);
        Map<Long, DeptRespDTO> deptMap = CollUtil.isEmpty(deptIds)
                ? Collections.emptyMap() : deptApi.getDeptMap(deptIds);

        // 2. 拼接响应
        return convertList(employeeRecords, employeeRecord -> {
            HrmSalarySlipSendEmployeeRespVO respVO =
                    BeanUtils.toBean(employeeRecord, HrmSalarySlipSendEmployeeRespVO.class);
            respVO.setMonthEmployeeRecordId(employeeRecord.getId());
            respVO.setSent(sentMonthEmployeeRecordIds.contains(employeeRecord.getId()));
            MapUtils.findAndThen(employeeMap, employeeRecord.getEmployeeId(), employee -> {
                respVO.setEmployeeName(employee.getName()).setJobNumber(employee.getJobNumber())
                        .setMobile(employee.getMobile()).setDeptId(employee.getDeptId())
                        .setPostName(employee.getPostName());
                MapUtils.findAndThen(deptMap, employee.getDeptId(),
                        dept -> respVO.setDeptName(dept.getName()));
            });
            return respVO;
        });
    }

}
