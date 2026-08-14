package cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.config;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.hrm.enums.DictTypeConstants;
import cn.iocoder.yudao.module.hrm.enums.attendance.config.HrmAttendanceHolidayTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * HRM 考勤节假日 DO
 *
 * @author 芋道源码
 */
@TableName("hrm_attendance_holiday")
@KeySequence("hrm_attendance_holiday_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmAttendanceHolidayDO extends BaseDO {

    /**
     * 节假日编号
     */
    @TableId
    private Long id;
    /**
     * 日期
     */
    private LocalDateTime date;
    /**
     * 日期类型
     *
     * 枚举 {@link HrmAttendanceHolidayTypeEnum}
     * 字典 {@link DictTypeConstants#HRM_ATTENDANCE_HOLIDAY_TYPE}
     */
    private Integer type;

}
