package cn.iocoder.yudao.module.hrm.controller.admin.salary;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.group.HrmSalaryGroupPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.group.HrmSalaryGroupRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.group.HrmSalaryGroupSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryGroupDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryTaxRuleDO;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.hrm.service.salary.config.HrmSalaryGroupService;
import cn.iocoder.yudao.module.hrm.service.salary.config.HrmSalaryTaxRuleService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSetByFlatMap;

@Tag(name = "管理后台 - HRM 薪资组")
@RestController
@RequestMapping("/hrm/salary/group")
@Validated
public class HrmSalaryGroupController {

    @Resource
    private HrmSalaryGroupService salaryGroupService;
    @Resource
    private HrmSalaryTaxRuleService salaryTaxRuleService;
    @Resource
    private HrmEmployeeService employeeService;
    @Resource
    private DeptApi deptApi;

    @PostMapping("/create")
    @Operation(summary = "创建薪资组")
    @PreAuthorize("@ss.hasPermission('hrm:salary:group:create')")
    public CommonResult<Long> createSalaryGroup(@Valid @RequestBody HrmSalaryGroupSaveReqVO reqVO) {
        return success(salaryGroupService.createSalaryGroup(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改薪资组")
    @PreAuthorize("@ss.hasPermission('hrm:salary:group:update')")
    public CommonResult<Boolean> updateSalaryGroup(@Valid @RequestBody HrmSalaryGroupSaveReqVO reqVO) {
        salaryGroupService.updateSalaryGroup(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除薪资组")
    @Parameter(name = "id", description = "薪资组编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:salary:group:delete')")
    public CommonResult<Boolean> deleteSalaryGroup(@RequestParam("id") Long id) {
        salaryGroupService.deleteSalaryGroup(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得薪资组")
    @Parameter(name = "id", description = "薪资组编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:salary:group:query')")
    public CommonResult<HrmSalaryGroupRespVO> getSalaryGroup(@RequestParam("id") Long id) {
        return success(buildSalaryGroupRespVO(salaryGroupService.getSalaryGroup(id)));
    }

    @GetMapping("/page")
    @Operation(summary = "获得薪资组分页")
    @PreAuthorize("@ss.hasPermission('hrm:salary:group:query')")
    public CommonResult<PageResult<HrmSalaryGroupRespVO>> getSalaryGroupPage(
            @Validated HrmSalaryGroupPageReqVO reqVO) {
        PageResult<HrmSalaryGroupDO> pageResult = salaryGroupService.getSalaryGroupPage(reqVO);
        return success(new PageResult<>(buildSalaryGroupRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得薪资组精简列表")
    @PreAuthorize("@ss.hasPermission('hrm:salary:group:query')")
    public CommonResult<List<HrmSalaryGroupRespVO>> getSalaryGroupSimpleList() {
        return success(convertList(salaryGroupService.getSalaryGroupList(),
                salaryGroup -> new HrmSalaryGroupRespVO().setId(salaryGroup.getId())
                        .setName(salaryGroup.getName()).setTaxRuleId(salaryGroup.getTaxRuleId())));
    }

    // ==================== 拼接 VO ====================

    private HrmSalaryGroupRespVO buildSalaryGroupRespVO(HrmSalaryGroupDO group) {
        if (group == null) {
            return null;
        }
        return CollUtil.getFirst(buildSalaryGroupRespVOList(Collections.singletonList(group)));
    }

    private List<HrmSalaryGroupRespVO> buildSalaryGroupRespVOList(List<HrmSalaryGroupDO> salaryGroups) {
        if (CollUtil.isEmpty(salaryGroups)) {
            return Collections.emptyList();
        }
        // 1.1 批量查询计税规则
        Map<Long, HrmSalaryTaxRuleDO> taxRuleMap = salaryTaxRuleService.getSalaryTaxRuleMap(
                convertSet(salaryGroups, HrmSalaryGroupDO::getTaxRuleId));
        // 1.2 批量查询部门和员工
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
                convertSetByFlatMap(salaryGroups, HrmSalaryGroupDO::getDeptIds, Collection::stream));
        Map<Long, HrmEmployeeDO> employeeMap = employeeService.getEmployeeMap(
                convertSetByFlatMap(salaryGroups, HrmSalaryGroupDO::getEmployeeIds, Collection::stream));

        // 2. 拼接响应
        return BeanUtils.toBean(salaryGroups, HrmSalaryGroupRespVO.class, vo -> {
            MapUtils.findAndThen(taxRuleMap, vo.getTaxRuleId(), taxRule -> vo.setTaxRuleName(taxRule.getName()));
            vo.setDeptNames(convertList(convertList(vo.getDeptIds(), deptMap::get), DeptRespDTO::getName));
            vo.setEmployeeNames(convertList(convertList(vo.getEmployeeIds(), employeeMap::get),
                    HrmEmployeeDO::getName));
        });
    }

}
