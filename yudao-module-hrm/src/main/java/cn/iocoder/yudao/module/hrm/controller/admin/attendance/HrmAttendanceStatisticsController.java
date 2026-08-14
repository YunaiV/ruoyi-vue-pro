package cn.iocoder.yudao.module.hrm.controller.admin.attendance;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.clock.HrmAttendanceClockRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.statistics.HrmAttendanceDailyDetailReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.statistics.HrmAttendanceDailyDetailRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.statistics.HrmAttendanceDailyOverviewRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.statistics.HrmAttendanceMonthDailyOverviewRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.statistics.HrmAttendanceMonthDetailRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.statistics.HrmAttendanceMonthRecordPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.statistics.HrmAttendanceMonthRecordRespVO;
import cn.iocoder.yudao.module.hrm.enums.DictTypeConstants;
import cn.iocoder.yudao.module.hrm.service.attendance.statistics.HrmAttendanceStatisticsService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - HRM 考勤统计")
@RestController
@RequestMapping("/hrm/attendance/statistics")
@Validated
public class HrmAttendanceStatisticsController {

    @Resource
    private HrmAttendanceStatisticsService attendanceStatisticsService;

    @Resource
    private DeptApi deptApi;

    @GetMapping("/daily-detail")
    @Operation(summary = "查询员工每日打卡明细")
    @PreAuthorize("@ss.hasPermission('hrm:attendance:clock:query')")
    public CommonResult<HrmAttendanceDailyDetailRespVO> getAttendanceDailyDetail(
            @Valid HrmAttendanceDailyDetailReqVO reqVO) {
        return success(attendanceStatisticsService.getAttendanceDailyDetail(reqVO));
    }

    @GetMapping("/month-record-page")
    @Operation(summary = "查询员工月度考勤汇总")
    @PreAuthorize("@ss.hasPermission('hrm:attendance:statistics:query')")
    public CommonResult<PageResult<HrmAttendanceMonthRecordRespVO>> getAttendanceMonthRecordPage(
            @Valid HrmAttendanceMonthRecordPageReqVO reqVO) {
        PageResult<HrmAttendanceMonthRecordRespVO> pageResult =
                attendanceStatisticsService.getAttendanceMonthRecordPage(reqVO);
        fillAttendanceMonthRecordDeptName(pageResult.getList());
        return success(pageResult);
    }

    @GetMapping("/month-daily-page")
    @Operation(summary = "查询员工月度打卡概况")
    @PreAuthorize("@ss.hasPermission('hrm:attendance:clock:query')")
    public CommonResult<PageResult<HrmAttendanceMonthDailyOverviewRespVO>> getAttendanceMonthDailyOverviewPage(
            @Valid HrmAttendanceMonthRecordPageReqVO reqVO) {
        PageResult<HrmAttendanceMonthDailyOverviewRespVO> pageResult =
                attendanceStatisticsService.getAttendanceMonthDailyOverviewPage(reqVO);
        fillAttendanceMonthDailyOverview(pageResult.getList());
        return success(pageResult);
    }

    @GetMapping("/month-record-export-excel")
    @Operation(summary = "导出员工月度考勤汇总")
    @PreAuthorize("@ss.hasPermission('hrm:attendance:statistics:export')")
    @ApiAccessLog(operateType = OperateTypeEnum.EXPORT)
    public void exportAttendanceMonthRecord(@Validated HrmAttendanceMonthRecordPageReqVO exportReqVO,
                                            HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<HrmAttendanceMonthRecordRespVO> list =
                attendanceStatisticsService.getAttendanceMonthRecordList(exportReqVO);
        fillAttendanceMonthRecordDeptName(list);
        ExcelUtils.write(response, "员工月度考勤汇总.xls", "数据", HrmAttendanceMonthRecordRespVO.class, list);
    }

    @GetMapping("/month-daily-export-excel")
    @Operation(summary = "导出员工月度打卡概况")
    @PreAuthorize("@ss.hasPermission('hrm:attendance:clock:export')")
    @ApiAccessLog(operateType = OperateTypeEnum.EXPORT)
    public void exportAttendanceMonthDailyOverview(@Validated HrmAttendanceMonthRecordPageReqVO exportReqVO,
                                                   HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<HrmAttendanceMonthDailyOverviewRespVO> list =
                attendanceStatisticsService.getAttendanceMonthDailyOverviewList(exportReqVO);
        fillAttendanceMonthDailyOverview(list);
        writeAttendanceMonthDailyOverview(response,
                YearMonth.of(exportReqVO.getYear(), exportReqVO.getMonth()), list);
    }

    @GetMapping("/month-detail")
    @Operation(summary = "查询员工月度考勤详情")
    @Parameters({
            @Parameter(name = "employeeId", description = "员工编号", required = true, example = "1024"),
            @Parameter(name = "year", description = "年份", required = true, example = "2026"),
            @Parameter(name = "month", description = "月份", required = true, example = "8")
    })
    @PreAuthorize("@ss.hasPermission('hrm:attendance:statistics:query')")
    public CommonResult<HrmAttendanceMonthDetailRespVO> getAttendanceMonthDetail(
            @RequestParam("employeeId") Long employeeId,
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month) {
        HrmAttendanceMonthDetailRespVO detail =
                attendanceStatisticsService.getAttendanceMonthDetail(employeeId, year, month);
        fillAttendanceMonthRecordDeptName(Collections.singletonList(detail.getSummary()));
        return success(detail);
    }

    private void writeAttendanceMonthDailyOverview(HttpServletResponse response, YearMonth month,
                                                   List<HrmAttendanceMonthDailyOverviewRespVO> list)
            throws IOException {
        List<List<String>> head = convertList(
                Arrays.asList("员工编号", "员工姓名", "工号", "部门", "岗位"), Collections::singletonList);
        for (int day = 1; day <= month.lengthOfMonth(); day++) {
            head.add(Collections.singletonList(month.atDay(day).toString()));
        }
        ExcelUtils.write(response, "员工月度打卡概况.xls", "打卡概况", head,
                buildAttendanceMonthDailyOverviewData(month, list));
    }

    private List<List<Object>> buildAttendanceMonthDailyOverviewData(
            YearMonth month, List<HrmAttendanceMonthDailyOverviewRespVO> list) {
        List<List<Object>> result = new ArrayList<>(list.size());
        for (HrmAttendanceMonthDailyOverviewRespVO row : list) {
            List<Object> data = new ArrayList<>(Arrays.asList(row.getEmployeeId(), row.getEmployeeName(),
                    row.getJobNumber(), row.getDeptName(), row.getPostName()));
            for (int day = 1; day <= month.lengthOfMonth(); day++) {
                data.add(formatDailyClockOverview(row.getDailyClockMap().get(month.atDay(day))));
            }
            result.add(data);
        }
        return result;
    }

    private List<HrmAttendanceDailyOverviewRespVO.OverviewItem> buildDailyClockOverviewList(
            HrmAttendanceDailyOverviewRespVO dailyOverview) {
        if (CollUtil.isEmpty(dailyOverview.getClocks())) {
            return Collections.singletonList(new HrmAttendanceDailyOverviewRespVO.OverviewItem()
                    .setText(StrUtil.emptyIfNull(dailyOverview.getAttendanceResult())));
        }
        // 按打卡时间升序展示每日打卡概况
        dailyOverview.getClocks().sort(Comparator.comparing(HrmAttendanceClockRespVO::getClockTime));
        return convertList(dailyOverview.getClocks(),
                clock -> new HrmAttendanceDailyOverviewRespVO.OverviewItem()
                        .setType(StrUtil.removeSuffix(StrUtil.emptyIfNull(DictFrameworkUtils.parseDictDataLabel(
                                DictTypeConstants.HRM_ATTENDANCE_CLOCK_TYPE, clock.getType())), "打卡"))
                        .setTime(clock.getClockTime() == null ? "-" : clock.getClockTime().toLocalTime().toString())
                        .setStatus(StrUtil.emptyIfNull(DictFrameworkUtils.parseDictDataLabel(
                                DictTypeConstants.HRM_ATTENDANCE_CLOCK_STATUS, clock.getStatus()))));
    }

    private String formatDailyClockOverview(HrmAttendanceDailyOverviewRespVO dailyOverview) {
        if (dailyOverview == null) {
            return "";
        }
        return dailyOverview.getOverviews().stream()
                .map(item -> StrUtil.isNotEmpty(item.getText()) ? item.getText()
                        : String.format("%s %s %s", item.getType(), item.getTime(), item.getStatus()))
                .collect(Collectors.joining("；"));
    }

    // ==================== 拼接 VO ====================

    private void fillAttendanceMonthDailyOverview(List<HrmAttendanceMonthDailyOverviewRespVO> list) {
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
                convertSet(list, HrmAttendanceMonthDailyOverviewRespVO::getDeptId));
        list.forEach(vo -> {
            MapUtils.findAndThen(deptMap, vo.getDeptId(), dept -> vo.setDeptName(dept.getName()));
            MapUtil.emptyIfNull(vo.getDailyClockMap()).values().forEach(
                    overview -> overview.setOverviews(buildDailyClockOverviewList(overview)));
        });
    }

    private void fillAttendanceMonthRecordDeptName(List<HrmAttendanceMonthRecordRespVO> list) {
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
                convertSet(list, HrmAttendanceMonthRecordRespVO::getDeptId));
        list.forEach(vo -> MapUtils.findAndThen(deptMap, vo.getDeptId(), dept -> vo.setDeptName(dept.getName())));
    }

}
