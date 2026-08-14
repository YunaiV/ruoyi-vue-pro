package cn.iocoder.yudao.module.hrm.controller.admin.home;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.hrm.controller.admin.home.vo.HrmHomeCalendarItemRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.home.vo.HrmHrHomeStatisticsRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.home.vo.HrmTeamHomeStatisticsRespVO;
import cn.iocoder.yudao.module.hrm.service.home.HrmHomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
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

@Tag(name = "管理后台 - HRM 首页")
@RestController
@RequestMapping("/hrm/home")
@Validated
public class HrmHomeController {

    @Resource
    private HrmHomeService homeService;

    @GetMapping("/hr-statistics-summary")
    @Operation(summary = "获得 HR 工作台统计")
    @PreAuthorize("@ss.hasPermission('hrm:home:hr-query')")
    public CommonResult<HrmHrHomeStatisticsRespVO> getHrHomeStatisticsSummary() {
        return success(homeService.getHrHomeStatisticsSummary());
    }

    @GetMapping("/hr-calendar")
    @Operation(summary = "获得 HR 工作台日历")
    @Parameters({
            @Parameter(name = "startDate", description = "开始日期", required = true, example = "2026-08-01"),
            @Parameter(name = "endDate", description = "结束日期", required = true, example = "2026-08-31")
    })
    @PreAuthorize("@ss.hasPermission('hrm:home:hr-query')")
    public CommonResult<List<HrmHomeCalendarItemRespVO>> getHrHomeCalendar(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return success(homeService.getHrHomeCalendar(getLoginUserId(), startDate, endDate));
    }

    @GetMapping("/team-statistics-summary")
    @Operation(summary = "获得团队工作台统计")
    @PreAuthorize("@ss.hasPermission('hrm:home:team-query')")
    public CommonResult<HrmTeamHomeStatisticsRespVO> getTeamHomeStatisticsSummary() {
        return success(homeService.getTeamHomeStatisticsSummary(getLoginUserId()));
    }

    @GetMapping("/team-calendar")
    @Operation(summary = "获得团队工作台日历")
    @Parameters({
            @Parameter(name = "startDate", description = "开始日期", required = true, example = "2026-08-01"),
            @Parameter(name = "endDate", description = "结束日期", required = true, example = "2026-08-31")
    })
    @PreAuthorize("@ss.hasPermission('hrm:home:team-query')")
    public CommonResult<List<HrmHomeCalendarItemRespVO>> getTeamHomeCalendar(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return success(homeService.getTeamHomeCalendar(getLoginUserId(), startDate, endDate));
    }

}
