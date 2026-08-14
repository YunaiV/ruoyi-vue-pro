package cn.iocoder.yudao.module.hrm.controller.admin.portal.attendance.vo.clock;

import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.TimeRange;
import cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.config.HrmAttendanceGroupDO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - HRM 员工端打卡详情 Response VO")
@Data
public class HrmPortalAttendanceClockDetailRespVO {

    @Schema(description = "考勤组名称")
    private String groupName;

    @Schema(description = "是否启用定位打卡")
    private Boolean openPointCard;

    @Schema(description = "是否启用 WiFi 打卡")
    private Boolean openWifiCard;

    @Schema(description = "班次所属考勤日")
    private LocalDate attendanceDate;

    @Schema(description = "班次标题，例如 09:00-18:00")
    private String shiftTitle;

    @Schema(description = "是否休息日")
    private Boolean restDay;

    @Schema(description = "上班应打卡时间")
    private LocalDateTime onDutyAttendanceTime;

    @Schema(description = "下班应打卡时间")
    private LocalDateTime offDutyAttendanceTime;

    @Schema(description = "打卡地点列表")
    private List<Point> points;

    @Schema(description = "WiFi 列表")
    private List<Wifi> wifis;

    @Schema(description = "下一次可打卡动作")
    private NextClock nextClock;

    @Schema(description = "当日打卡时间线")
    private List<TimelineItem> timeline;

    // ==================== 内部计算字段 ====================

    @JsonIgnore
    @Schema(hidden = true)
    private HrmAttendanceGroupDO group;

    @JsonIgnore
    @Schema(hidden = true)
    private HrmAttendanceGroupDO.Shift shift;

    @JsonIgnore
    @Schema(hidden = true)
    private TimeRange clockInRange;

    @JsonIgnore
    @Schema(hidden = true)
    private TimeRange clockOutRange;

    @Schema(description = "打卡地点")
    @Data
    public static class Point {

        @Schema(description = "地点名称")
        private String name;

        @Schema(description = "定位地址")
        private String address;

        @Schema(description = "纬度")
        private BigDecimal latitude;

        @Schema(description = "经度")
        private BigDecimal longitude;

        @Schema(description = "有效半径（米）")
        private Integer radius;

    }

    @Schema(description = "WiFi 配置")
    @Data
    public static class Wifi {

        @Schema(description = "WiFi 名称")
        private String ssid;

        @Schema(description = "MAC 地址")
        private String mac;

    }

    @Schema(description = "下一次打卡动作")
    @Data
    public static class NextClock {

        @Schema(description = "打卡类型")
        private Integer type;

        @Schema(description = "打卡阶段")
        private Integer stage;

        @Schema(description = "应打卡时间")
        private LocalDateTime attendanceTime;

        @Schema(description = "按钮状态：0 未到时间 1 正常 2 更新 3 迟到 4 早退")
        private Integer buttonStatus;

    }

    @Schema(description = "当日打卡时间线条目")
    @Data
    public static class TimelineItem {

        @Schema(description = "打卡类型")
        private Integer type;

        @Schema(description = "应打卡时间")
        private LocalDateTime attendanceTime;

        @Schema(description = "实际打卡时间；缺卡时为空")
        private LocalDateTime clockTime;

        @Schema(description = "打卡状态")
        private Integer status;

        @Schema(description = "是否缺卡")
        private Boolean missCard;

        @Schema(description = "打卡地址")
        private String address;

    }

}
