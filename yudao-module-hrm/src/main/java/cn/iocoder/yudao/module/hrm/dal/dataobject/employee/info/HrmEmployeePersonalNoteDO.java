package cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
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
 * HRM 员工个人备忘 DO
 *
 * @author 芋道源码
 */
@TableName("hrm_employee_personal_note")
@KeySequence("hrm_employee_personal_note_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmEmployeePersonalNoteDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 所属员工编号
     *
     * 关联 {@link HrmEmployeeDO#getId()}
     */
    private Long employeeId;

    /**
     * 备忘内容
     */
    private String content;

    /**
     * 提醒时间
     */
    private LocalDateTime reminderTime;

}
