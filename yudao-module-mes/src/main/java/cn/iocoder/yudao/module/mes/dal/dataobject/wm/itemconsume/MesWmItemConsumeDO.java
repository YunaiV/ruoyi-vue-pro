package cn.iocoder.yudao.module.mes.dal.dataobject.wm.itemconsume;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkstationDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.feedback.MesProFeedbackDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.process.MesProProcessDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.task.MesProTaskDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.workorder.MesProWorkOrderDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;

/**
 * MES 物料消耗记录 DO
 *
 * @author 芋道源码
 */
@TableName("mes_wm_item_consume")
@KeySequence("mes_wm_item_consume_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmItemConsumeDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 生产工单编号
     *
     * 关联 {@link MesProWorkOrderDO#getId()}
     */
    private Long workOrderId;
    /**
     * 生产任务编号
     *
     * 关联 {@link MesProTaskDO#getId()}
     */
    private Long taskId;
    /**
     * 工作站编号
     *
     * 关联 {@link MesMdWorkstationDO#getId()}
     */
    private Long workstationId;
    /**
     * 工序编号
     *
     * 关联 {@link MesProProcessDO#getId()}
     */
    private Long processId;
    /**
     * 报工记录编号
     *
     * 关联 {@link MesProFeedbackDO#getId()}
     */
    private Long feedbackId;
    /**
     * 消耗日期
     */
    private LocalDateTime consumeDate;
    // DONE @AI：搞个独立的枚举类；
    /**
     * 状态
     *
     * 字典 {@link DictTypeConstants#MES_WM_ITEM_CONSUME_STATUS}
     * 枚举 {@link cn.iocoder.yudao.module.mes.enums.wm.MesWmItemConsumeStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
