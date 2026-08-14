package cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.group;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Schema(description = "管理后台 - HRM 考勤组 Response VO")
@Data
public class HrmAttendanceGroupRespVO {

    @Schema(description = "考勤组编号", example = "1024")
    private Long id;

    @Schema(description = "名称", example = "总部考勤组")
    private String name;

    @Schema(description = "是否启用 WiFi 打卡")
    private Boolean openWifiCard;

    @Schema(description = "是否启用定位打卡")
    private Boolean openPointCard;

    @Schema(description = "是否法定节假日休息")
    private Boolean rest;

    @Schema(description = "是否默认配置")
    private Boolean defaultStatus;

    @Schema(description = "特殊日期设置数组")
    private List<SpecialDate> specialDates;

    @Schema(description = "部门范围")
    private List<Long> deptIds;

    @Schema(description = "适用部门名称")
    private List<String> deptNames;

    @Schema(description = "员工范围")
    private List<Long> employeeIds;

    @Schema(description = "适用员工名称")
    private List<String> employeeNames;

    @Schema(description = "班次配置")
    private List<Shift> shifts;

    @Schema(description = "打卡地点")
    private List<Point> points;

    @Schema(description = "打卡 WiFi")
    private List<Wifi> wifis;

    @Schema(description = "扣款规则")
    private DeductRule deductRule;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "管理后台 - HRM 考勤组特殊日期 Response VO")
    @Data
    public static class SpecialDate {

        @Schema(description = "日期类型", example = "1")
        private Integer type;

        @Schema(description = "特殊日期")
        private LocalDateTime date;
    }

    @Schema(description = "管理后台 - HRM 考勤组班次配置 Response VO")
    @Data
    public static class Shift {

        @Schema(description = "工作日数组", example = "[1, 2, 3, 4, 5]")
        private List<Integer> weeks;

        @Schema(description = "上班时间", example = "09:00")
        @JsonFormat(pattern = "HH:mm")
        private LocalTime startTime;

        @Schema(description = "下班时间", example = "18:00")
        @JsonFormat(pattern = "HH:mm")
        private LocalTime endTime;

        @Schema(description = "上班打卡开始时间", example = "08:00")
        @JsonFormat(pattern = "HH:mm")
        private LocalTime clockInStartTime;

        @Schema(description = "上班打卡结束时间", example = "10:00")
        @JsonFormat(pattern = "HH:mm")
        private LocalTime clockInEndTime;

        @Schema(description = "下班打卡开始时间", example = "17:00")
        @JsonFormat(pattern = "HH:mm")
        private LocalTime clockOutStartTime;

        @Schema(description = "下班打卡结束时间", example = "20:00")
        @JsonFormat(pattern = "HH:mm")
        private LocalTime clockOutEndTime;

        @Schema(description = "休息开始时间", example = "12:00")
        @JsonFormat(pattern = "HH:mm")
        private LocalTime restStartTime;

        @Schema(description = "休息结束时间", example = "13:00")
        @JsonFormat(pattern = "HH:mm")
        private LocalTime restEndTime;

        @Schema(description = "休息时间是否不计入工作时长")
        private Boolean excludeRestTime;
    }

    @Schema(description = "管理后台 - HRM 考勤组打卡地点 Response VO")
    @Data
    public static class Point {

        @Schema(description = "地点名称", example = "总部办公区")
        private String name;

        @Schema(description = "定位地址", example = "北京市海淀区中关村大街")
        private String address;

        @Schema(description = "纬度", example = "39.983424")
        private BigDecimal latitude;

        @Schema(description = "经度", example = "116.322987")
        private BigDecimal longitude;

        @Schema(description = "有效打卡半径，单位：米", example = "300")
        private Integer radius;
    }

    @Schema(description = "管理后台 - HRM 考勤组 WiFi 配置 Response VO")
    @Data
    public static class Wifi {

        @Schema(description = "WiFi 名称", example = "Office-WiFi")
        private String ssid;

        @Schema(description = "MAC 地址", example = "00:11:22:33:44:55")
        private String mac;
    }

    @Schema(description = "管理后台 - HRM 考勤组扣款规则 Response VO")
    @Data
    public static class DeductRule {

        @Schema(description = "迟到扣款方式", example = "1")
        private Integer lateMethod;

        @Schema(description = "迟到扣款金额", example = "10.00")
        private BigDecimal lateDeductMoney;

        @Schema(description = "早退扣款方式", example = "1")
        private Integer earlyMethod;

        @Schema(description = "早退扣款金额", example = "10.00")
        private BigDecimal earlyDeductMoney;

        @Schema(description = "旷工扣款方式", example = "1")
        private Integer absenteeismMethod;

        @Schema(description = "旷工扣款金额", example = "100.00")
        private BigDecimal absenteeismDeductMoney;

        @Schema(description = "缺卡扣款方式", example = "1")
        private Integer misscardMethod;

        @Schema(description = "缺卡扣款金额", example = "20.00")
        private BigDecimal misscardDeductMoney;
    }

}
