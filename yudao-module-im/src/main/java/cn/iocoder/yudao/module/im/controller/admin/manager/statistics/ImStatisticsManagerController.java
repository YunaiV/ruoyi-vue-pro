package cn.iocoder.yudao.module.im.controller.admin.manager.statistics;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.module.im.controller.admin.manager.statistics.vo.*;
import cn.iocoder.yudao.module.im.service.statistics.ImStatisticsManagerService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - IM 数据看板")
@RestController
@RequestMapping("/im/manager/statistics")
@Validated
public class ImStatisticsManagerController {

    /**
     * 群规模分桶名称的展示顺序
     */
    private static final List<String> GROUP_SIZE_BUCKETS = Arrays.asList("1-9 人", "10-49 人", "50-199 人", "200+ 人");
    /**
     * 看板分布默认时间窗口（天）
     */
    private static final int DISTRIBUTION_WINDOW_DAYS = 30;
    /**
     * TOP 发送者数量
     */
    private static final int TOP_SENDER_LIMIT = 10;

    @Resource
    private ImStatisticsManagerService statisticsService;
    @Resource
    private AdminUserApi adminUserApi;

    @GetMapping("/overview")
    @Operation(summary = "获得数据概览")
    @PreAuthorize("@ss.hasPermission('im:manager:statistics:query')")
    public CommonResult<ImStatisticsManagerOverviewRespVO> getOverview() {
        LocalDateTime todayBegin = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrowBegin = todayBegin.plusDays(1);
        LocalDateTime yesterdayBegin = todayBegin.minusDays(1);
        // 周活/月活定义为 N 天滚动窗口（含今天）
        LocalDateTime weekBegin = todayBegin.minusDays(6);
        LocalDateTime monthBegin = todayBegin.minusDays(29);
        return success(new ImStatisticsManagerOverviewRespVO()
                .setTotalUser(statisticsService.getTotalUserCount())
                .setNewUserToday(statisticsService.getNewUserCount(todayBegin, tomorrowBegin))
                .setTotalGroup(statisticsService.getTotalGroupCount())
                .setNewGroupToday(statisticsService.getNewGroupCount(todayBegin, tomorrowBegin))
                .setActiveUserDaily(statisticsService.getActiveUserCount(todayBegin, tomorrowBegin))
                .setActiveUserWeekly(statisticsService.getActiveUserCount(weekBegin, tomorrowBegin))
                .setActiveUserMonthly(statisticsService.getActiveUserCount(monthBegin, tomorrowBegin))
                .setPrivateMessageToday(statisticsService.getPrivateMessageCount(todayBegin, tomorrowBegin))
                .setGroupMessageToday(statisticsService.getGroupMessageCount(todayBegin, tomorrowBegin))
                .setPrivateMessageYesterday(statisticsService.getPrivateMessageCount(yesterdayBegin, todayBegin))
                .setGroupMessageYesterday(statisticsService.getGroupMessageCount(yesterdayBegin, todayBegin)));
    }

    @GetMapping("/message-trend")
    @Operation(summary = "获得消息趋势（私聊 / 群聊双线）")
    @Parameter(name = "days", description = "回看天数（含今日）", example = "7")
    @PreAuthorize("@ss.hasPermission('im:manager:statistics:query')")
    public CommonResult<ImStatisticsManagerTrendRespVO> getMessageTrend(
            @RequestParam(value = "days", defaultValue = "7") @Min(1) @Max(90) int days) {
        List<LocalDateTime> dates = LocalDateTimeUtils.getLatestDays(days);
        LocalDateTime beginTime = dates.get(0);
        LocalDateTime endTime = dates.get(days - 1).plusDays(1);
        Map<LocalDateTime, Long> privateMap = statisticsService.getPrivateMessageDailyCountMap(beginTime, endTime);
        Map<LocalDateTime, Long> groupMap = statisticsService.getGroupMessageDailyCountMap(beginTime, endTime);
        // 转换格式
        Map<String, List<Long>> series = new LinkedHashMap<>();
        series.put("private", alignSeries(dates, privateMap));
        series.put("group", alignSeries(dates, groupMap));
        return success(new ImStatisticsManagerTrendRespVO().setDates(dates).setSeries(series));
    }

    @GetMapping("/user-trend")
    @Operation(summary = "获得用户趋势（新增注册 / 日活双线）")
    @Parameter(name = "days", description = "回看天数（含今日）", example = "7")
    @PreAuthorize("@ss.hasPermission('im:manager:statistics:query')")
    public CommonResult<ImStatisticsManagerTrendRespVO> getUserTrend(
            @RequestParam(value = "days", defaultValue = "7") @Min(1) @Max(90) int days) {
        List<LocalDateTime> dates = LocalDateTimeUtils.getLatestDays(days);
        LocalDateTime beginTime = dates.get(0);
        LocalDateTime endTime = dates.get(days - 1).plusDays(1);
        Map<LocalDateTime, Long> registerMap = statisticsService.getNewUserDailyCountMap(beginTime, endTime);
        Map<LocalDateTime, Long> activeMap = statisticsService.getActiveUserDailyCountMap(beginTime, endTime);
        // 转换格式
        Map<String, List<Long>> series = new LinkedHashMap<>();
        series.put("register", alignSeries(dates, registerMap));
        series.put("active", alignSeries(dates, activeMap));
        return success(new ImStatisticsManagerTrendRespVO().setDates(dates).setSeries(series));
    }

    @GetMapping("/message-type-distribution")
    @Operation(summary = "获得内容类型分布（最近 30 天）")
    @PreAuthorize("@ss.hasPermission('im:manager:statistics:query')")
    public CommonResult<List<ImStatisticsManagerMessageTypeRespVO>> getMessageTypeDistribution() {
        LocalDateTime endTime = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime beginTime = endTime.minusDays(DISTRIBUTION_WINDOW_DAYS);
        Map<Integer, Long> typeCountMap = statisticsService.getMessageTypeCountMap(beginTime, endTime);
        // 转换格式
        return success(convertList(typeCountMap.entrySet(), entry -> new ImStatisticsManagerMessageTypeRespVO()
                .setType(entry.getKey()).setValue(entry.getValue())));
    }

    @GetMapping("/group-size-distribution")
    @Operation(summary = "获得群规模分布")
    @PreAuthorize("@ss.hasPermission('im:manager:statistics:query')")
    public CommonResult<List<ImStatisticsManagerGroupSizeRespVO>> getGroupSizeDistribution() {
        Map<String, Long> groupSizeMap = statisticsService.getGroupSizeCountMap();
        // 转换格式
        return success(convertList(GROUP_SIZE_BUCKETS, bucket -> new ImStatisticsManagerGroupSizeRespVO()
                .setRange(bucket).setCount(groupSizeMap.getOrDefault(bucket, 0L))));
    }

    @GetMapping("/top-senders")
    @Operation(summary = "获得消息 TOP 发送者（最近 30 天）")
    @PreAuthorize("@ss.hasPermission('im:manager:statistics:query')")
    public CommonResult<List<ImStatisticsManagerTopSenderRespVO>> getTopSenders() {
        LocalDateTime endTime = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime beginTime = endTime.minusDays(DISTRIBUTION_WINDOW_DAYS);
        Map<Long, Long> topSenderMap = statisticsService.getTopSenderCountMap(beginTime, endTime, TOP_SENDER_LIMIT);
        // TOP 发送者：批量回填昵称
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(topSenderMap.keySet());
        return success(convertList(topSenderMap.entrySet(), entry -> {
            ImStatisticsManagerTopSenderRespVO item = new ImStatisticsManagerTopSenderRespVO()
                    .setUserId(entry.getKey()).setMessageCount(entry.getValue());
            MapUtils.findAndThen(userMap, entry.getKey(), user -> item.setNickname(user.getNickname()));
            return item;
        }));
    }

    /**
     * 把每日聚合 Map 对齐到 dates 序列；缺失天补 0
     */
    private static List<Long> alignSeries(List<LocalDateTime> dates, Map<LocalDateTime, Long> dailyMap) {
        return convertList(dates, date -> dailyMap.getOrDefault(date, 0L));
    }

}
