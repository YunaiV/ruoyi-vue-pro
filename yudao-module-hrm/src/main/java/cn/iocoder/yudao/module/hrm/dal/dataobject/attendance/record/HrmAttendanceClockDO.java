package cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.record;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.enums.DictTypeConstants;
import cn.iocoder.yudao.module.hrm.enums.attendance.record.HrmAttendanceClockSourceEnum;
import cn.iocoder.yudao.module.hrm.enums.attendance.record.HrmAttendanceClockStageEnum;
import cn.iocoder.yudao.module.hrm.enums.attendance.record.HrmAttendanceClockStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.attendance.record.HrmAttendanceClockTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * HRM 考勤打卡记录 DO
 *
 * @author 芋道源码
 */
@TableName("hrm_attendance_clock")
@KeySequence("hrm_attendance_clock_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmAttendanceClockDO extends BaseDO {

    /**
     * 打卡记录编号
     */
    @TableId
    private Long id;
    /**
     * 员工编号
     *
     * 关联 {@link HrmEmployeeDO#getId()}
     */
    private Long employeeId;
    /**
     * 打卡时间
     */
    private LocalDateTime clockTime;
    /**
     * 打卡类型
     *
     * 枚举 {@link HrmAttendanceClockTypeEnum}
     * 字典 {@link DictTypeConstants#HRM_ATTENDANCE_CLOCK_TYPE}
     */
    private Integer type;
    /**
     * 应打卡时间
     */
    private LocalDateTime attendanceTime;
    /**
     * 打卡来源
     *
     * 枚举 {@link HrmAttendanceClockSourceEnum}
     * 字典 {@link DictTypeConstants#HRM_ATTENDANCE_CLOCK_SOURCE}
     */
    private Integer sourceType;
    /**
     * 打卡状态
     *
     * 枚举 {@link HrmAttendanceClockStatusEnum}
     * 字典 {@link DictTypeConstants#HRM_ATTENDANCE_CLOCK_STATUS}
     */
    private Integer status;
    /**
     * 打卡阶段
     *
     * 枚举 {@link HrmAttendanceClockStageEnum}
     */
    private Integer stage;
    /**
     * 打卡地址
     */
    private String address;
    /**
     * 经度
     */
    private BigDecimal longitude;
    /**
     * 纬度
     */
    private BigDecimal latitude;
    /**
     * WiFi 名称
     */
    private String ssid;
    /**
     * WiFi MAC 地址
     */
    private String mac;
    /**
     * 备注
     */
    private String remark;

}
