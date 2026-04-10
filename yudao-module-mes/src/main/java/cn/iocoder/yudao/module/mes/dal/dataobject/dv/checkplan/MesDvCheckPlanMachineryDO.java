package cn.iocoder.yudao.module.mes.dal.dataobject.dv.checkplan;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.machinery.MesDvMachineryDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * MES 点检保养方案设备 DO
 *
 * @author 芋道源码
 */
@TableName("mes_dv_check_plan_machinery")
@KeySequence("mes_dv_check_plan_machinery_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesDvCheckPlanMachineryDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 方案编号
     *
     * 关联 {@link MesDvCheckPlanDO#getId()}
     */
    private Long planId;
    /**
     * 设备编号
     *
     * 关联 {@link MesDvMachineryDO#getId()}
     */
    private Long machineryId;
    /**
     * 备注
     */
    private String remark;

}
