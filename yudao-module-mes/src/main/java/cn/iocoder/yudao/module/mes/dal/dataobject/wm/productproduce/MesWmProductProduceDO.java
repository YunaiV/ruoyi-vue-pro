package cn.iocoder.yudao.module.mes.dal.dataobject.wm.productproduce;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkstationDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.feedback.MesProFeedbackDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.process.MesProProcessDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.task.MesProTaskDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.workorder.MesProWorkOrderDO;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmProductProduceStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * MES 生产入库单 DO
 *
 * @author 芋道源码
 */
@TableName("mes_wm_product_produce")
@KeySequence("mes_wm_product_produce_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmProductProduceDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 生产工单 ID
     *
     * 关联 {@link MesProWorkOrderDO#getId()}
     */
    private Long workOrderId;
    /**
     * 报工记录 ID
     *
     * 关联 {@link MesProFeedbackDO#getId()}
     */
    private Long feedbackId;
    /**
     * 生产任务 ID
     *
     * 关联 {@link MesProTaskDO#getId()}
     */
    private Long taskId;
    /**
     * 工作站 ID
     *
     * 关联 {@link MesMdWorkstationDO#getId()}
     */
    private Long workstationId;
    /**
     * 工序 ID
     *
     * 关联 {@link MesProProcessDO#getId()}
     */
    private Long processId;
    /**
     * 生产日期
     */
    private LocalDateTime produceDate;
    /**
     * 状态
     *
     * 枚举 {@link MesWmProductProduceStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
