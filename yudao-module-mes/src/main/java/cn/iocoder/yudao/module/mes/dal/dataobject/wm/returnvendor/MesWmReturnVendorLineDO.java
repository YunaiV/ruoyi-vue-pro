package cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnvendor;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.batch.MesWmBatchDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * MES 供应商退货单行 DO
 */
@TableName("mes_wm_return_vendor_line")
@KeySequence("mes_wm_return_vendor_line_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmReturnVendorLineDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 退货单 ID
     *
     * 关联 {@link MesWmReturnVendorDO#getId()}
     */
    private Long returnId;
    /**
     * 物料 ID
     *
     * 关联 {@link MesMdItemDO#getId()}
     */
    private Long itemId;
    /**
     * 退货数量
     */
    private BigDecimal quantity;
    /**
     * 批次 ID
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
     * 备注
     */
    private String remark;

}
