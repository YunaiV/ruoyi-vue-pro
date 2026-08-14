package cn.iocoder.yudao.module.hrm.controller.admin.portal.salary;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.salary.vo.HrmPortalSalarySlipListReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.salary.vo.HrmPortalSalarySlipRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.salary.vo.HrmPortalSalarySlipUnreadSummaryRespVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.slip.HrmSalarySlipDO;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.hrm.service.salary.slip.HrmSalarySlipService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import cn.iocoder.yudao.module.hrm.enums.salary.slip.HrmSalarySlipReadStatusEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - HRM 员工端工资条")
@RestController
@RequestMapping("/hrm/portal/salary/slip")
@Validated
public class HrmPortalSalarySlipController {

    @Resource
    private HrmEmployeeService employeeService;
    @Resource
    private HrmSalarySlipService salarySlipService;
    @Resource
    private AdminUserApi adminUserApi;

    @GetMapping("/unread-summary")
    @Operation(summary = "获得我的未读工资条概况")
    @PreAuthorize("@ss.hasPermission('hrm:portal:query')")
    public CommonResult<HrmPortalSalarySlipUnreadSummaryRespVO> getUnreadSalarySlipSummary() {
        // 1. 获得当前员工的未读工资条
        HrmEmployeeDO employee = employeeService.validateEmployeeBySelf(getLoginUserId());
        List<HrmSalarySlipDO> unreadSalarySlips = filterList(
                salarySlipService.getSalarySlipListByEmployeeId(
                        employee.getId(), null, null, null, null),
                salarySlip -> HrmSalarySlipReadStatusEnum.UNREAD.getStatus().equals(salarySlip.getReadStatus()));

        // 2. 构建未读工资条概况
        return success(new HrmPortalSalarySlipUnreadSummaryRespVO(
                (long) unreadSalarySlips.size(),
                buildSalarySlipReminder(CollUtil.getFirst(unreadSalarySlips))));
    }

    @GetMapping("/list")
    @Operation(summary = "获得我的工资条列表")
    @PreAuthorize("@ss.hasPermission('hrm:portal:query')")
    public CommonResult<List<HrmPortalSalarySlipRespVO>> getSalarySlipList(
            @Valid HrmPortalSalarySlipListReqVO reqVO) {
        // 1.1 获得当前员工和月份范围
        HrmEmployeeDO employee = employeeService.validateEmployeeBySelf(getLoginUserId());
        YearMonth startMonth = reqVO.getStartMonth() == null ? null : YearMonth.parse(reqVO.getStartMonth());
        YearMonth endMonth = reqVO.getEndMonth() == null ? null : YearMonth.parse(reqVO.getEndMonth());
        // 1.2 查询工资条列表
        List<HrmSalarySlipDO> salarySlips = salarySlipService.getSalarySlipListByEmployeeId(
                employee.getId(), startMonth, endMonth, reqVO.getOrderType(), reqVO.getOrder());

        // 2. 构建工资条响应
        return success(BeanUtils.toBean(salarySlips, HrmPortalSalarySlipRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得我的工资条详情")
    @Parameter(name = "id", description = "工资条编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hrm:portal:query')")
    public CommonResult<HrmPortalSalarySlipRespVO> getSalarySlip(@RequestParam("id") Long id) {
        // 1. 获得当前员工，并校验工资条归属本人
        HrmEmployeeDO employee = employeeService.validateEmployeeBySelf(getLoginUserId());
        HrmSalarySlipDO salarySlip = salarySlipService.getSalarySlipByIdAndEmployeeId(id, employee.getId());
        // 2. 构建工资条响应
        return success(BeanUtils.toBean(salarySlip, HrmPortalSalarySlipRespVO.class));
    }

    @PutMapping("/read")
    @Operation(summary = "标记我的工资条为已读")
    @Parameter(name = "ids", description = "工资条编号列表", required = true, example = "1024,1025")
    @PreAuthorize("@ss.hasPermission('hrm:portal:query')")
    public CommonResult<Boolean> markSalarySlipRead(
            @RequestParam("ids") @NotEmpty(message = "工资条编号列表不能为空") List<Long> ids) {
        HrmEmployeeDO employee = employeeService.validateEmployeeBySelf(getLoginUserId());
        salarySlipService.markSalarySlipListRead(employee.getId(), ids);
        return success(true);
    }

    // ==================== 拼接 VO ====================

    /**
     * 构建最新未读工资条提醒
     *
     * @param salarySlip 工资条
     * @return 工资条提醒
     */
    private String buildSalarySlipReminder(HrmSalarySlipDO salarySlip) {
        if (salarySlip == null) {
            return null;
        }
        String creator = salarySlip.getCreator();
        String publisherName = StrUtil.blankToDefault(creator, "HR");
        if (StrUtil.isNotBlank(creator) && StrUtil.isNumeric(creator)) {
            AdminUserRespDTO user = adminUserApi.getUser(Long.valueOf(creator));
            publisherName = user == null ? "HR" : user.getNickname();
        }
        return publisherName + "更新了您" + salarySlip.getMonth() + "月的工资条，前往查看";
    }

}
