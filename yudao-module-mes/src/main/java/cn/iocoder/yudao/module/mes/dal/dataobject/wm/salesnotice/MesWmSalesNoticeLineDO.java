package cn.iocoder.yudao.module.mes.dal.dataobject.wm.salesnotice;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.batch.MesWmBatchDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * MES 发货通知单行 DO
 */
@TableName("mes_wm_sales_notice_line")
@KeySequence("mes_wm_sales_notice_line_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmSalesNoticeLineDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 发货通知单编号
     *
     * 关联 {@link MesWmSalesNoticeDO#getId()}
     */
    private Long noticeId;
    /**
     * 物料编号
     *
     * 关联 {@link MesMdItemDO#getId()}
     */
    private Long itemId;
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
     * 发货数量
     */
    private BigDecimal quantity;
    /**
     * 是否检验
     */
    private Boolean oqcCheckFlag;
    /**
     * 备注
     */
    private String remark;

}
