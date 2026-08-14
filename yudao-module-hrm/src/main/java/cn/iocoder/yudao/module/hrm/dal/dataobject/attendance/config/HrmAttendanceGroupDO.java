package cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.config;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.type.LongListTypeHandler;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.enums.attendance.config.HrmAttendanceAbsenteeismDeductMethodEnum;
import cn.iocoder.yudao.module.hrm.enums.attendance.config.HrmAttendanceLateEarlyDeductMethodEnum;
import cn.iocoder.yudao.module.hrm.enums.attendance.config.HrmAttendanceHolidayTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.attendance.config.HrmAttendanceMisscardDeductMethodEnum;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * HRM 考勤组 DO
 *
 * @author 芋道源码
 */
@TableName(value = "hrm_attendance_group", autoResultMap = true)
@KeySequence("hrm_attendance_group_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmAttendanceGroupDO extends BaseDO {

    /**
     * 考勤组编号
     */
    @TableId
    private Long id;
    /**
     * 考勤组名称
     */
    private String name;
    /**
     * 适用部门编号
     *
     * 关联 {@link DeptDO#getId()}
     */
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> deptIds;
    /**
     * 适用员工编号
     *
     * 关联 {@link HrmEmployeeDO#getId()}
     */
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> employeeIds;
    /**
     * 是否启用 WiFi 打卡
     */
    private Boolean openWifiCard;
    /**
     * 是否启用定位打卡
     */
    private Boolean openPointCard;
    /**
     * 班次配置
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Shift> shifts;
    /**
     * 是否法定节假日休息
     */
    private Boolean rest;
    /**
     * 特殊日期设置数组
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<SpecialDate> specialDates;
    /**
     * 打卡地点配置
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Point> points;
    /**
     * WiFi 打卡配置
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Wifi> wifis;
    /**
     * 扣款规则
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private DeductRule deductRule;
    /**
     * 是否默认考勤组
     */
    private Boolean defaultStatus;

    /**
     * 特殊日期设置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecialDate {

        /**
         * 日期类型
         *
         * 枚举 {@link HrmAttendanceHolidayTypeEnum}
         */
        private Integer type;
        /**
         * 特殊日期
         */
        private LocalDateTime date;
    }

    /**
     * 考勤班次配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Shift {

        /**
         * 工作日数组
         */
        private List<Integer> weeks;
        /**
         * 上班时间
         */
        @JsonFormat(pattern = "HH:mm")
        private LocalTime startTime;
        /**
         * 下班时间
         */
        @JsonFormat(pattern = "HH:mm")
        private LocalTime endTime;
        /**
         * 上班打卡开始时间
         */
        @JsonFormat(pattern = "HH:mm")
        private LocalTime clockInStartTime;
        /**
         * 上班打卡结束时间
         */
        @JsonFormat(pattern = "HH:mm")
        private LocalTime clockInEndTime;
        /**
         * 下班打卡开始时间
         */
        @JsonFormat(pattern = "HH:mm")
        private LocalTime clockOutStartTime;
        /**
         * 下班打卡结束时间
         */
        @JsonFormat(pattern = "HH:mm")
        private LocalTime clockOutEndTime;
        /**
         * 休息开始时间
         */
        @JsonFormat(pattern = "HH:mm")
        private LocalTime restStartTime;
        /**
         * 休息结束时间
         */
        @JsonFormat(pattern = "HH:mm")
        private LocalTime restEndTime;
        /**
         * 休息时间是否不计入工作时长
         */
        private Boolean excludeRestTime;
    }

    /**
     * 打卡地点配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Point {

        /**
         * 地点名称
         */
        private String name;
        /**
         * 定位地址
         */
        private String address;
        /**
         * 纬度
         */
        private BigDecimal latitude;
        /**
         * 经度
         */
        private BigDecimal longitude;
        /**
         * 有效打卡半径，单位：米
         */
        private Integer radius;
    }

    /**
     * WiFi 打卡配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Wifi {

        /**
         * WiFi 名称
         */
        private String ssid;
        /**
         * MAC 地址
         */
        private String mac;
    }

    /**
     * 考勤扣款规则
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeductRule {

        /**
         * 迟到扣款方式
         *
         * 枚举 {@link HrmAttendanceLateEarlyDeductMethodEnum}
         */
        private Integer lateMethod;
        /**
         * 迟到扣款金额
         */
        private BigDecimal lateDeductMoney;
        /**
         * 早退扣款方式
         *
         * 枚举 {@link HrmAttendanceLateEarlyDeductMethodEnum}
         */
        private Integer earlyMethod;
        /**
         * 早退扣款金额
         */
        private BigDecimal earlyDeductMoney;
        /**
         * 旷工扣款方式
         *
         * 枚举 {@link HrmAttendanceAbsenteeismDeductMethodEnum}
         */
        private Integer absenteeismMethod;
        /**
         * 旷工扣款金额
         */
        private BigDecimal absenteeismDeductMoney;
        /**
         * 缺卡扣款方式
         *
         * 枚举 {@link HrmAttendanceMisscardDeductMethodEnum}
         */
        private Integer misscardMethod;
        /**
         * 缺卡扣款金额
         */
        private BigDecimal misscardDeductMoney;
    }

}
