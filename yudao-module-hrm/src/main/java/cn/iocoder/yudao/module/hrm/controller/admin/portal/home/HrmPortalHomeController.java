package cn.iocoder.yudao.module.hrm.controller.admin.portal.home;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.hrm.controller.admin.home.vo.HrmHomeCalendarItemRespVO;
import cn.iocoder.yudao.module.hrm.service.home.HrmHomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - HRM 员工端首页")
@RestController
@RequestMapping("/hrm/portal/home")
@Validated
public class HrmPortalHomeController {

    @Resource
    private HrmHomeService homeService;

    @GetMapping("/calendar")
    @Operation(summary = "获得员工端首页日历")
    @Parameters({
            @Parameter(name = "startDate", description = "开始日期", required = true, example = "2026-08-01"),
            @Parameter(name = "endDate", description = "结束日期", required = true, example = "2026-08-31")
    })
    @PreAuthorize("@ss.hasPermission('hrm:portal:query')")
    public CommonResult<List<HrmHomeCalendarItemRespVO>> getEmployeeHomeCalendar(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return success(homeService.getEmployeeCalendar(getLoginUserId(), startDate, endDate));
    }

}
