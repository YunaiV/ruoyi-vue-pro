package cn.iocoder.yudao.module.mes.dal.dataobject.wm.itemconsume;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.batch.MesWmBatchDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.materialstock.MesWmMaterialStockDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseAreaDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseLocationDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * MES 物料消耗记录明细 DO
 *
 * 记录 line 级别的消耗按线边库 FIFO 分配到具体批次的明细。
 * 一条 line 可能拆分为多条 detail（当一个批次库存不够，需要从下一个批次继续分配时）。
 *
 * @author 芋道源码
 */
@TableName("mes_wm_item_consume_detail")
@KeySequence("mes_wm_item_consume_detail_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmItemConsumeDetailDO extends BaseDO {

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
     * 消耗记录行编号
     *
     * 关联 {@link MesWmItemConsumeLineDO#getId()}
     */
    private Long lineId;
    /**
     * 库存台账编号
     *
     * 关联 {@link MesWmMaterialStockDO#getId()}
     * 当线边库中无可用库存时为 null
     */
    private Long materialStockId;
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
     * 关联 {@link MesWmBatchDO#getId()}
     */
    private Long batchId;
    /**
     * 批次号
     *
     * 冗余 {@link MesWmBatchDO#getCode()}
     */
    private String batchCode;
    /**
     * 仓库编号
     *
     * 关联 {@link MesWmWarehouseDO#getId()}
     */
    private Long warehouseId;
    /**
     * 库区编号
     *
     * 关联 {@link MesWmWarehouseLocationDO#getId()}
     */
    private Long locationId;
    /**
     * 库位编号
     *
     * 关联 {@link MesWmWarehouseAreaDO#getId()}
     */
    private Long areaId;
    /**
     * 备注
     */
    private String remark;

}
