package cn.iocoder.yudao.module.mes.dal.dataobject.pro.task;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkstationDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.workorder.MesProWorkOrderDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * MES 生产任务投料 DO
 *
 * @author 芋道源码
 */
@TableName("mes_pro_task_issue")
@KeySequence("mes_pro_task_issue_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesProTaskIssueDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 生产任务编号
     *
     * 关联 {@link MesProTaskDO#getId()}
     */
    private Long taskId;
    /**
     * 生产工单编号
     *
     * 关联 {@link MesProWorkOrderDO#getId()}
     */
    private Long workOrderId;
    /**
     * 工作站编号
     *
     * 关联 {@link MesMdWorkstationDO#getId()}
     */
    private Long workstationId;
    /**
     * 来源单据编号
     */
    private Long sourceDocId;
    /**
     * 来源单据编码
     */
    private String sourceDocCode;
    /**
     * 来源单据类型
     */
    private String sourceDocType;
    /**
     * 投料批次
     */
    private String batchCode;
    /**
     * 来源单据行编号
     */
    private Long sourceLineId;
    /**
     * 产品物料编号
     *
     * 关联 {@link MesMdItemDO#getId()}
     */
    private Long itemId;
    /**
     * 单位编号
     *
     * 关联 {@link MesMdUnitMeasureDO#getId()}
     */
    private Long unitMeasureId;
    /**
     * 总投料数量
     */
    private BigDecimal issuedQuantity;
    /**
     * 当前可用数量
     */
    private BigDecimal availableQuantity;
    /**
     * 当前使用数量
     */
    private BigDecimal usedQuantity;
    /**
     * 备注
     */
    private String remark;

}
