package cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * MES 计划班次 DO
 *
 * @author 芋道源码
 */
@TableName("mes_cal_plan_shift")
@KeySequence("mes_cal_plan_shift_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesCalPlanShiftDO extends BaseDO {

    /**
     * 班次编号
     */
    @TableId
    private Long id;
    /**
     * 排班计划编号
     *
     * 关联 {@link MesCalPlanDO#getId()}
     */
    private Long planId;
    /**
     * 显示顺序
     */
    private Integer sort;
    /**
     * 班次名称
     */
    private String name;
    /**
     * 开始时间（HH:mm 格式）
     */
    private String startTime;
    /**
     * 结束时间（HH:mm 格式）
     */
    private String endTime;
    /**
     * 备注
     */
    private String remark;

}
