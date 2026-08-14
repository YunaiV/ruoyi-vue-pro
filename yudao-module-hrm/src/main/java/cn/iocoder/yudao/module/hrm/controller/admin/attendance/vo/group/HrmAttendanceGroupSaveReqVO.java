package cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.group;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.TimeRange;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.hrm.enums.attendance.config.HrmAttendanceAbsenteeismDeductMethodEnum;
import cn.iocoder.yudao.module.hrm.enums.attendance.config.HrmAttendanceHolidayTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.attendance.config.HrmAttendanceLateEarlyDeductMethodEnum;
import cn.iocoder.yudao.module.hrm.enums.attendance.config.HrmAttendanceMisscardDeductMethodEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Schema(description = "管理后台 - HRM 考勤组保存 Request VO")
@Data
public class HrmAttendanceGroupSaveReqVO {

    private static final Set<Integer> POINT_RADIUS_VALUES = new HashSet<>(
            Arrays.asList(100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 2000, 3000));

    @Schema(description = "考勤组编号", example = "1024")
    private Long id;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "总部考勤组")
    @NotBlank(message = "考勤组名称不能为空")
    @Size(max = 50, message = "考勤组名称不能超过 50 个字符")
    @DiffLogField(name = "考勤组名称")
    private String name;

    @Schema(description = "是否启用 WiFi 打卡", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否启用 WiFi 打卡不能为空")
    @DiffLogField(name = "是否启用 WiFi 打卡")
    private Boolean openWifiCard;

    @Schema(description = "是否启用定位打卡", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否启用定位打卡不能为空")
    @DiffLogField(name = "是否启用定位打卡")
    private Boolean openPointCard;

    @Schema(description = "是否法定节假日休息", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否法定节假日休息不能为空")
    @DiffLogField(name = "是否法定节假日休息")
    private Boolean rest;

    @Schema(description = "特殊日期设置数组", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotNull(message = "特殊日期设置不能为空")
    @DiffLogField(name = "特殊日期设置")
    private List<SpecialDate> specialDates;

    @Schema(description = "部门范围", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1024, 2048]")
    @NotNull(message = "部门范围不能为空")
    @DiffLogField(name = "适用部门")
    private List<@NotNull(message = "部门编号不能为空") Long> deptIds;

    @Schema(description = "员工范围", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1024, 2048]")
    @NotNull(message = "员工范围不能为空")
    @DiffLogField(name = "适用员工")
    private List<@NotNull(message = "员工编号不能为空") Long> employeeIds;

    @Schema(description = "班次配置", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotEmpty(message = "班次配置不能为空")
    @DiffLogField(name = "班次配置")
    private List<Shift> shifts;

    @Schema(description = "打卡地点", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotNull(message = "打卡地点不能为空")
    @DiffLogField(name = "打卡地点")
    private List<Point> points;

    @Schema(description = "打卡 WiFi", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotNull(message = "打卡 WiFi 不能为空")
    @DiffLogField(name = "打卡 WiFi")
    private List<Wifi> wifis;

    @Schema(description = "扣款规则", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotNull(message = "扣款规则不能为空")
    @DiffLogField(name = "扣款规则")
    private DeductRule deductRule;

    @Schema(description = "管理后台 - HRM 考勤组特殊日期")
    @Data
    public static class SpecialDate {

        @Schema(description = "日期类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "特殊日期类型不能为空")
        @InEnum(value = HrmAttendanceHolidayTypeEnum.class, message = "特殊日期类型必须是 {value}")
        private Integer type;

        @Schema(description = "特殊日期", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "特殊日期不能为空")
        private LocalDateTime date;
    }

    @Schema(description = "管理后台 - HRM 考勤组班次配置")
    @Data
    public static class Shift {

        @Schema(description = "工作日数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1, 2, 3, 4, 5]")
        @NotEmpty(message = "班次工作日不能为空")
        private List<@NotNull(message = "班次工作日不能为空")
                @Min(value = 1, message = "班次工作日必须在 1 到 7 之间")
                @Max(value = 7, message = "班次工作日必须在 1 到 7 之间") Integer> weeks;

        @Schema(description = "上班时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "09:00")
        @JsonFormat(pattern = "HH:mm")
        @NotNull(message = "上班时间不能为空")
        private LocalTime startTime;

        @Schema(description = "下班时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "18:00")
        @JsonFormat(pattern = "HH:mm")
        @NotNull(message = "下班时间不能为空")
        private LocalTime endTime;

        @Schema(description = "上班打卡开始时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "08:00")
        @JsonFormat(pattern = "HH:mm")
        @NotNull(message = "上班打卡开始时间不能为空")
        private LocalTime clockInStartTime;

        @Schema(description = "上班打卡结束时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "10:00")
        @JsonFormat(pattern = "HH:mm")
        @NotNull(message = "上班打卡结束时间不能为空")
        private LocalTime clockInEndTime;

        @Schema(description = "下班打卡开始时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "17:00")
        @JsonFormat(pattern = "HH:mm")
        @NotNull(message = "下班打卡开始时间不能为空")
        private LocalTime clockOutStartTime;

        @Schema(description = "下班打卡结束时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "20:00")
        @JsonFormat(pattern = "HH:mm")
        @NotNull(message = "下班打卡结束时间不能为空")
        private LocalTime clockOutEndTime;

        @Schema(description = "休息开始时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "12:00")
        @JsonFormat(pattern = "HH:mm")
        @NotNull(message = "休息开始时间不能为空")
        private LocalTime restStartTime;

        @Schema(description = "休息结束时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "13:00")
        @JsonFormat(pattern = "HH:mm")
        @NotNull(message = "休息结束时间不能为空")
        private LocalTime restEndTime;

        @Schema(description = "休息时间是否不计入工作时长", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "休息时间计算方式不能为空")
        private Boolean excludeRestTime;
    }

    @Schema(description = "管理后台 - HRM 考勤组打卡地点")
    @Data
    public static class Point {

        @Schema(description = "地点名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "总部办公区")
        @NotBlank(message = "地点名称不能为空")
        @Size(max = 50, message = "地点名称不能超过 50 个字符")
        private String name;

        @Schema(description = "定位地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "北京市海淀区中关村大街")
        @NotBlank(message = "定位地址不能为空")
        @Size(max = 255, message = "定位地址不能超过 255 个字符")
        private String address;

        @Schema(description = "纬度", requiredMode = Schema.RequiredMode.REQUIRED, example = "39.983424")
        @NotNull(message = "纬度不能为空")
        @DecimalMin(value = "-90", message = "纬度必须在 -90 到 90 之间")
        @DecimalMax(value = "90", message = "纬度必须在 -90 到 90 之间")
        private BigDecimal latitude;

        @Schema(description = "经度", requiredMode = Schema.RequiredMode.REQUIRED, example = "116.322987")
        @NotNull(message = "经度不能为空")
        @DecimalMin(value = "-180", message = "经度必须在 -180 到 180 之间")
        @DecimalMax(value = "180", message = "经度必须在 -180 到 180 之间")
        private BigDecimal longitude;

        @Schema(description = "有效打卡半径，单位：米", requiredMode = Schema.RequiredMode.REQUIRED, example = "300")
        @NotNull(message = "有效打卡半径不能为空")
        @Positive(message = "有效打卡半径必须大于 0")
        private Integer radius;
    }

    @Schema(description = "管理后台 - HRM 考勤组 WiFi 配置")
    @Data
    public static class Wifi {

        @Schema(description = "WiFi 名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "Office-WiFi")
        @NotBlank(message = "WiFi 名称不能为空")
        @Size(max = 50, message = "WiFi 名称不能超过 50 个字符")
        private String ssid;

        @Schema(description = "MAC 地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "00:11:22:33:44:55")
        @NotBlank(message = "MAC 地址不能为空")
        @Pattern(regexp = "^((([0-9a-fA-F]{2}:){5})|(([0-9a-fA-F]{2}-){5}))[0-9a-fA-F]{2}$",
                message = "MAC 地址格式不正确")
        private String mac;
    }

    @Schema(description = "管理后台 - HRM 考勤组扣款规则")
    @Data
    public static class DeductRule {

        @Schema(description = "迟到扣款方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "迟到扣款方式不能为空")
        @InEnum(value = HrmAttendanceLateEarlyDeductMethodEnum.class, message = "迟到扣款方式必须是 {value}")
        private Integer lateMethod;

        @Schema(description = "迟到扣款金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "10.00")
        @NotNull(message = "迟到扣款金额不能为空")
        @DecimalMin(value = "0", message = "迟到扣款金额不能小于 0")
        @Digits(integer = 8, fraction = 2, message = "迟到扣款金额最多 8 位整数和 2 位小数")
        private BigDecimal lateDeductMoney;

        @Schema(description = "早退扣款方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "早退扣款方式不能为空")
        @InEnum(value = HrmAttendanceLateEarlyDeductMethodEnum.class, message = "早退扣款方式必须是 {value}")
        private Integer earlyMethod;

        @Schema(description = "早退扣款金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "10.00")
        @NotNull(message = "早退扣款金额不能为空")
        @DecimalMin(value = "0", message = "早退扣款金额不能小于 0")
        @Digits(integer = 8, fraction = 2, message = "早退扣款金额最多 8 位整数和 2 位小数")
        private BigDecimal earlyDeductMoney;

        @Schema(description = "旷工扣款方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "旷工扣款方式不能为空")
        @InEnum(value = HrmAttendanceAbsenteeismDeductMethodEnum.class, message = "旷工扣款方式必须是 {value}")
        private Integer absenteeismMethod;

        @Schema(description = "旷工扣款金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
        @NotNull(message = "旷工扣款金额不能为空")
        @DecimalMin(value = "0", message = "旷工扣款金额不能小于 0")
        @Digits(integer = 8, fraction = 2, message = "旷工扣款金额最多 8 位整数和 2 位小数")
        private BigDecimal absenteeismDeductMoney;

        @Schema(description = "缺卡扣款方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "缺卡扣款方式不能为空")
        @InEnum(value = HrmAttendanceMisscardDeductMethodEnum.class, message = "缺卡扣款方式必须是 {value}")
        private Integer misscardMethod;

        @Schema(description = "缺卡扣款金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "20.00")
        @NotNull(message = "缺卡扣款金额不能为空")
        @DecimalMin(value = "0", message = "缺卡扣款金额不能小于 0")
        @Digits(integer = 8, fraction = 2, message = "缺卡扣款金额最多 8 位整数和 2 位小数")
        private BigDecimal misscardDeductMoney;
    }

    @AssertTrue(message = "不同班次不能配置重复工作日")
    @JsonIgnore
    public boolean isShiftWeeksUnique() {
        if (CollUtil.isEmpty(shifts)) {
            return true;
        }
        Set<Integer> weeks = new HashSet<>();
        return shifts.stream().allMatch(shift -> shift != null && CollUtil.isNotEmpty(shift.getWeeks())
                && shift.getWeeks().stream().allMatch(weeks::add));
    }

    @AssertTrue(message = "班次上下班、打卡窗口或休息时间配置不正确")
    @JsonIgnore
    public boolean isShiftTimeValid() {
        if (CollUtil.isEmpty(shifts)) {
            return true;
        }
        return shifts.stream().allMatch(this::isShiftTimeValid);
    }

    private boolean isShiftTimeValid(Shift shift) {
        if (shift == null || shift.getStartTime() == null || shift.getEndTime() == null
                || shift.getClockInStartTime() == null || shift.getClockInEndTime() == null
                || shift.getClockOutStartTime() == null || shift.getClockOutEndTime() == null
                || shift.getRestStartTime() == null || shift.getRestEndTime() == null
                || shift.getExcludeRestTime() == null) {
            // 交由内嵌字段的 @NotNull 输出具体校验错误
            return true;
        }
        if (shift.getStartTime().equals(shift.getEndTime())
                || shift.getClockInStartTime().equals(shift.getClockInEndTime())
                || shift.getClockOutStartTime().equals(shift.getClockOutEndTime())) {
            return false;
        }
        LocalDate attendanceDate = LocalDate.of(2000, 1, 3);
        TimeRange shiftTimeRange = CollUtil.getFirst(LocalDateTimeUtils.buildDailyTimeRanges(
                attendanceDate, attendanceDate, shift.getStartTime(), shift.getEndTime()));
        return LocalDateTimeUtils.findDailyTimeRange(
                attendanceDate, shift.getClockInStartTime(), shift.getClockInEndTime(),
                shiftTimeRange.getStartTime()) != null
                && LocalDateTimeUtils.findDailyTimeRange(
                        attendanceDate, shift.getClockOutStartTime(), shift.getClockOutEndTime(),
                        shiftTimeRange.getEndTime()) != null
                && isRestTimeValid(attendanceDate, shift, shiftTimeRange);
    }

    private boolean isRestTimeValid(LocalDate attendanceDate, Shift shift,
                                    TimeRange shiftTimeRange) {
        if (Boolean.FALSE.equals(shift.getExcludeRestTime())) {
            return true;
        }
        if (shift.getRestStartTime().equals(shift.getRestEndTime())) {
            return false;
        }
        List<TimeRange> restTimeRanges = LocalDateTimeUtils.buildDailyTimeRanges(
                attendanceDate.minusDays(1), attendanceDate.plusDays(1),
                shift.getRestStartTime(), shift.getRestEndTime());
        return CollUtil.findOne(restTimeRanges, restTimeRange ->
                LocalDateTimeUtils.isAfterOrEqual(restTimeRange.getStartTime(), shiftTimeRange.getStartTime())
                        && LocalDateTimeUtils.isBeforeOrEqual(
                        restTimeRange.getEndTime(), shiftTimeRange.getEndTime())) != null;
    }

    @AssertTrue(message = "特殊日期不能重复")
    @JsonIgnore
    public boolean isSpecialDatesUnique() {
        if (CollUtil.isEmpty(specialDates)) {
            return true;
        }
        Set<LocalDate> dates = new HashSet<>();
        return specialDates.stream().allMatch(specialDate -> specialDate != null && specialDate.getDate() != null
                && dates.add(specialDate.getDate().toLocalDate()));
    }

    @AssertTrue(message = "考勤组至少需要选择一个适用部门或员工")
    @JsonIgnore
    public boolean isScopeValid() {
        if (deptIds == null || employeeIds == null) {
            return true;
        }
        return CollUtil.isNotEmpty(deptIds) || CollUtil.isNotEmpty(employeeIds);
    }

    @AssertTrue(message = "考勤组至少需要启用定位打卡或 WiFi 打卡")
    @JsonIgnore
    public boolean isClockMethodValid() {
        if (openPointCard == null || openWifiCard == null) {
            return true;
        }
        return openPointCard || openWifiCard;
    }

    @AssertTrue(message = "启用定位打卡时，打卡地点不能为空")
    @JsonIgnore
    public boolean isPointSettingValid() {
        if (Boolean.TRUE.equals(openPointCard)) {
            return CollUtil.isNotEmpty(points);
        }
        return true;
    }

    @AssertTrue(message = "启用 WiFi 打卡时，打卡 WiFi 不能为空")
    @JsonIgnore
    public boolean isWifiSettingValid() {
        if (Boolean.TRUE.equals(openWifiCard)) {
            return CollUtil.isNotEmpty(wifis);
        }
        return true;
    }

    @AssertTrue(message = "有效打卡半径必须是 100 至 1000 米（间隔 100 米），或 2000、3000 米")
    @JsonIgnore
    public boolean isPointRadiusValid() {
        if (CollUtil.isEmpty(points)) {
            return true;
        }
        return points.stream().allMatch(point -> point != null
                && point.getRadius() != null && POINT_RADIUS_VALUES.contains(point.getRadius()));
    }

}
