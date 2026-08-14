package cn.iocoder.yudao.module.hrm.controller.admin.insurance;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.monthrecord.employee.HrmInsuranceMonthEmployeeProjectRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.monthrecord.employee.HrmInsuranceMonthEmployeeRecordCreateListReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.monthrecord.employee.HrmInsuranceMonthEmployeeRecordPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.monthrecord.employee.HrmInsuranceMonthEmployeeRecordRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.monthrecord.employee.HrmInsuranceMonthEmployeeRecordStopListReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.monthrecord.employee.HrmInsuranceMonthEmployeeRecordUpdateReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.employee.HrmInsuranceEmployeeInfoDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.monthrecord.HrmInsuranceMonthEmployeeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.config.HrmInsuranceSchemeDO;
import cn.iocoder.yudao.module.hrm.enums.insurance.config.HrmInsuranceProjectTypeEnum;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.hrm.service.insurance.config.HrmInsuranceSchemeService;
import cn.iocoder.yudao.module.hrm.service.insurance.employee.HrmInsuranceEmployeeInfoService;
import cn.iocoder.yudao.module.hrm.service.insurance.monthrecord.HrmInsuranceMonthEmployeeRecordService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - HRM 员工月度社保")
@RestController
@RequestMapping("/hrm/insurance/month-employee-record")
@Validated
public class HrmInsuranceMonthEmployeeRecordController {

    @Resource
    private HrmInsuranceMonthEmployeeRecordService monthEmployeeRecordService;
    @Resource
    private HrmEmployeeService employeeService;
    @Resource
    private HrmInsuranceEmployeeInfoService insuranceEmployeeInfoService;
    @Resource
    private HrmInsuranceSchemeService insuranceSchemeService;

    @Resource
    private DeptApi deptApi;

    @GetMapping("/page")
    @Operation(summary = "获得员工月度社保分页")
    @PreAuthorize("@ss.hasPermission('hrm:insurance:month-record:query')")
    public CommonResult<PageResult<HrmInsuranceMonthEmployeeRecordRespVO>> getMonthEmployeeRecordPage(
            @Valid HrmInsuranceMonthEmployeeRecordPageReqVO reqVO) {
        PageResult<HrmInsuranceMonthEmployeeRecordDO> pageResult =
                monthEmployeeRecordService.getMonthEmployeeRecordPage(reqVO);
        return success(new PageResult<>(buildMonthEmployeeRecordRespVOList(pageResult.getList()),
                pageResult.getTotal()));
    }

    @GetMapping("/get")
    @Operation(summary = "获得员工月度社保详情")
    @Parameter(name = "id", description = "员工月度社保记录编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:insurance:month-record:query')")
    public CommonResult<HrmInsuranceMonthEmployeeRecordRespVO> getMonthEmployeeRecord(@RequestParam("id") Long id) {
        HrmInsuranceMonthEmployeeRecordDO employeeRecord =
                monthEmployeeRecordService.getMonthEmployeeRecord(id);
        if (employeeRecord == null) {
            return success(null);
        }
        return success(CollUtil.getFirst(buildMonthEmployeeRecordRespVOList(
                Collections.singletonList(employeeRecord))));
    }

    @PutMapping("/update")
    @Operation(summary = "修改员工月度参保项目")
    @PreAuthorize("@ss.hasPermission('hrm:insurance:month-record:update')")
    public CommonResult<Boolean> updateMonthEmployeeRecord(
            @Valid @RequestBody HrmInsuranceMonthEmployeeRecordUpdateReqVO reqVO) {
        monthEmployeeRecordService.updateMonthEmployeeRecord(reqVO);
        return success(true);
    }

    @PutMapping("/stop-list")
    @Operation(summary = "批量停止员工参保")
    @PreAuthorize("@ss.hasPermission('hrm:insurance:month-record:update')")
    public CommonResult<Boolean> stopMonthEmployeeRecordList(
            @Valid @RequestBody HrmInsuranceMonthEmployeeRecordStopListReqVO reqVO) {
        monthEmployeeRecordService.stopMonthEmployeeRecordList(reqVO.getIds());
        return success(true);
    }

    @PostMapping("/create-list")
    @Operation(summary = "添加参保人员")
    @PreAuthorize("@ss.hasPermission('hrm:insurance:month-record:update')")
    public CommonResult<Boolean> createMonthEmployeeRecordList(
            @Valid @RequestBody HrmInsuranceMonthEmployeeRecordCreateListReqVO reqVO) {
        monthEmployeeRecordService.createMonthEmployeeRecordList(reqVO);
        return success(true);
    }

    @GetMapping("/uninsured-employee-list")
    @Operation(summary = "获得本月未参保员工")
    @Parameter(name = "monthRecordId", description = "社保表编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:insurance:month-record:query')")
    public CommonResult<List<HrmEmployeeRespVO>> getUninsuredEmployeeList(
            @RequestParam("monthRecordId") Long monthRecordId) {
        List<HrmEmployeeDO> employees = monthEmployeeRecordService.getUninsuredEmployeeList(monthRecordId);
        return success(BeanUtils.toBean(employees, HrmEmployeeRespVO.class));
    }

    // ==================== 拼接 VO ====================

    private List<HrmInsuranceMonthEmployeeRecordRespVO> buildMonthEmployeeRecordRespVOList(
            List<HrmInsuranceMonthEmployeeRecordDO> employeeRecords) {
        if (CollUtil.isEmpty(employeeRecords)) {
            return Collections.emptyList();
        }

        // 1.1 批量查询员工和部门
        Set<Long> employeeIds = convertSet(employeeRecords, HrmInsuranceMonthEmployeeRecordDO::getEmployeeId);
        Map<Long, HrmEmployeeDO> employeeMap = employeeService.getEmployeeMap(employeeIds);
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(employeeMap.values(), HrmEmployeeDO::getDeptId));
        // 1.2 批量查询社保方案和员工社保资料
        Map<Long, HrmInsuranceSchemeDO> schemeMap = convertMap(insuranceSchemeService.getSchemeListByIds(
                convertSet(employeeRecords, HrmInsuranceMonthEmployeeRecordDO::getSchemeId)),
                HrmInsuranceSchemeDO::getId);
        Map<Long, HrmInsuranceEmployeeInfoDO> insuranceEmployeeInfoMap =
                insuranceEmployeeInfoService.getInsuranceEmployeeInfoMap(employeeIds);
        Map<Long, List<HrmInsuranceMonthEmployeeRecordDO.Project>> projectMap =
                convertMap(employeeRecords, HrmInsuranceMonthEmployeeRecordDO::getId,
                        record -> record.getProjects() == null ? Collections.emptyList() : record.getProjects());

        // 2. 拼接响应
        List<HrmInsuranceMonthEmployeeRecordRespVO> respVOs =
                BeanUtils.toBean(employeeRecords, HrmInsuranceMonthEmployeeRecordRespVO.class);
        for (HrmInsuranceMonthEmployeeRecordRespVO respVO : respVOs) {
            MapUtils.findAndThen(employeeMap, respVO.getEmployeeId(), employee -> {
                respVO.setEmployeeName(employee.getName()).setJobNumber(employee.getJobNumber())
                        .setMobile(employee.getMobile()).setIdNumber(employee.getIdNumber())
                        .setSex(employee.getSex()).setAge(employee.getAge())
                        .setDeptId(employee.getDeptId()).setPostName(employee.getPostName())
                        .setEntryStatus(employee.getEntryStatus()).setEmployeeStatus(employee.getStatus())
                        .setEntryTime(employee.getEntryTime());
                MapUtils.findAndThen(deptMap, employee.getDeptId(),
                        dept -> respVO.setDeptName(dept.getName()));
            });
            MapUtils.findAndThen(schemeMap, respVO.getSchemeId(), scheme -> {
                respVO.setSchemeName(scheme.getName()).setAreaId(scheme.getAreaId())
                        .setAreaName(AreaUtils.format(scheme.getAreaId()))
                        .setHouseType(scheme.getHouseholdType()).setSchemeType(scheme.getType());
            });
            MapUtils.findAndThen(insuranceEmployeeInfoMap, respVO.getEmployeeId(), insuranceEmployeeInfo -> {
                respVO.setSocialSecurityNumber(insuranceEmployeeInfo.getSocialSecurityNumber())
                        .setAccumulationFundNumber(insuranceEmployeeInfo.getAccumulationFundNumber());
            });
            List<HrmInsuranceMonthEmployeeProjectRespVO> projects = BeanUtils.toBean(
                    projectMap.getOrDefault(respVO.getId(), Collections.emptyList()),
                    HrmInsuranceMonthEmployeeProjectRespVO.class);
            respVO.setSocialSecurityProjectList(convertList(projects, project -> project,
                    project -> HrmInsuranceProjectTypeEnum.isSocialSecurity(project.getType())));
            respVO.setProvidentFundProjectList(convertList(projects, project -> project,
                    project -> HrmInsuranceProjectTypeEnum.isProvidentFund(project.getType())));
        }
        return respVOs;
    }

}
