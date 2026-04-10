package cn.iocoder.yudao.module.mes.dal.dataobject.wm.itemreceipt;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.arrivalnotice.MesWmArrivalNoticeLineDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.batch.MesWmBatchDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MES 采购入库单行 DO
 */
@TableName("mes_wm_item_receipt_line")
@KeySequence("mes_wm_item_receipt_line_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmItemReceiptLineDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 入库单编号
     *
     * 关联 {@link MesWmItemReceiptDO#getId()}
     */
    private Long receiptId;
    /**
     * 到货通知单行编号
     *
     * 关联 {@link MesWmArrivalNoticeLineDO#getId()}
     */
    private Long arrivalNoticeLineId;
    /**
     * 物料编号
     *
     * 关联 {@link MesMdItemDO#getId()}
     */
    private Long itemId;
    /**
     * 入库数量
     */
    private BigDecimal receivedQuantity;
    /**
     * 批次编号
     *
     * 关联 {@link MesWmBatchDO#getId()}
     */
    private Long batchId;
    /**
     * 批次编码
     *
     * 关联 {@link MesWmBatchDO#getCode()}
     */
    private String batchCode;
    /**
     * 生产日期
     */
    private LocalDateTime productionDate;
    /**
     * 有效期
     */
    private LocalDateTime expireDate;
    /**
     * 生产批号
     */
    private String lotNumber;
    /**
     * 备注
     */
    private String remark;

}
