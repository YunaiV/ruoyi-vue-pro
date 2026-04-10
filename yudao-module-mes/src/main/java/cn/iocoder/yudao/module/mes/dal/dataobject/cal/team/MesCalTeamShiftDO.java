package cn.iocoder.yudao.module.mes.dal.dataobject.cal.team;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan.MesCalPlanDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan.MesCalPlanShiftDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * MES 班组排班 DO
 *
 * @author 芋道源码
 */
@TableName("mes_cal_team_shift")
@KeySequence("mes_cal_team_shift_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesCalTeamShiftDO extends BaseDO {

    /**
     * 编号
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
     * 班组编号
     *
     * 关联 {@link MesCalTeamDO#getId()}
     */
    private Long teamId;
    /**
     * 班次编号
     *
     * 关联 {@link MesCalPlanShiftDO#getId()}
     */
    private Long shiftId;
    /**
     * 日期
     */
    private LocalDateTime day;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 备注
     */
    private String remark;

}
