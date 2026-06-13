package cn.iocoder.yudao.module.mes.dal.dataobject.wm.outsourcereceipt;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.iqc.MesQcIqcDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.batch.MesWmBatchDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;

/**
 * MES 外协入库单行 DO
 */
@TableName("mes_wm_outsource_receipt_line")
@KeySequence("mes_wm_outsource_receipt_line_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmOutsourceReceiptLineDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 入库单编号
     *
     * 关联 {@link MesWmOutsourceReceiptDO#getId()}
     */
    private Long receiptId;
    /**
     * 物料编号
     *
     * 关联 {@link MesMdItemDO#getId()}
     */
    private Long itemId;
    /**
     * 入库数量
     */
    private BigDecimal quantity;
    /**
     * 批次编号
     *
     * 关联 {@link MesWmBatchDO#getId()}
     */
    private Long batchId;
    /**
     * 批次号
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
    /**
     * 来料检验单编号
     *
     * 关联 {@link MesQcIqcDO#getId()}
     */
    private Long iqcId;
    /**
     * 是否需要质检
     */
    private Boolean iqcCheckFlag;
    /**
     * 质量状态
     *
     * 字典 {@link DictTypeConstants#MES_WM_QUALITY_STATUS}
     * 枚举 {@link cn.iocoder.yudao.module.mes.enums.wm.MesWmQualityStatusEnum}
     */
    private Integer qualityStatus;

}
