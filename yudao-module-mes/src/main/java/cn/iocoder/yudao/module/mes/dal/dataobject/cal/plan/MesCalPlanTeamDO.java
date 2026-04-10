package cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.team.MesCalTeamDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * MES 计划班组关联 DO
 *
 * @author 芋道源码
 */
@TableName("mes_cal_plan_team")
@KeySequence("mes_cal_plan_team_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesCalPlanTeamDO extends BaseDO {

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
     * 备注
     */
    private String remark;

}
