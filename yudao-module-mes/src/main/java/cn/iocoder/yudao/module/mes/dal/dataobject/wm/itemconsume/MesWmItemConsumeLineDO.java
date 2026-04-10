package cn.iocoder.yudao.module.mes.dal.dataobject.wm.itemconsume;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * MES 物料消耗记录行 DO
 *
 * @author 芋道源码
 */
@TableName("mes_wm_item_consume_line")
@KeySequence("mes_wm_item_consume_line_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmItemConsumeLineDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 消耗记录编号
     *
     * 关联 {@link MesWmItemConsumeDO#getId()}
     */
    private Long consumeId;
    /**
     * 物料编号
     *
     * 关联 {@link MesMdItemDO#getId()}
     */
    private Long itemId;
    /**
     * 消耗数量
     */
    private BigDecimal quantity;
    /**
     * 批次编号
     *
     * 关联 {@link cn.iocoder.yudao.module.mes.dal.dataobject.wm.batch.MesWmBatchDO#getId()}
     */
    private Long batchId;
    /**
     * 批次号
     *
     * 冗余 {@link cn.iocoder.yudao.module.mes.dal.dataobject.wm.batch.MesWmBatchDO#getCode()}
     */
    private String batchCode;
    /**
     * 备注
     */
    private String remark;

}
