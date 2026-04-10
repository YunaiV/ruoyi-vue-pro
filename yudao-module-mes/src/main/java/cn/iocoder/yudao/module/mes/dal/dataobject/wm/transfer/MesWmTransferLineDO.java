package cn.iocoder.yudao.module.mes.dal.dataobject.wm.transfer;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseAreaDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseLocationDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * MES 转移单行 DO
 *
 * @author 芋道源码
 */
@TableName("mes_wm_transfer_line")
@KeySequence("mes_wm_transfer_line_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmTransferLineDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 转移单编号
     *
     * 关联 {@link MesWmTransferDO#getId()}
     */
    private Long transferId;
    /**
     * 库存记录编号
     */
    private Long materialStockId;
    /**
     * 物料编号
     *
     * 关联 {@link MesMdItemDO#getId()}
     */
    private Long itemId;
    /**
     * 转移数量
     */
    private BigDecimal quantity;
    /**
     * 批次编号
     */
    private Long batchId;

    /**
     * 移出仓库编号
     *
     * 关联 {@link MesWmWarehouseDO#getId()}
     */
    private Long fromWarehouseId;
    /**
     * 移出库区编号
     *
     * 关联 {@link MesWmWarehouseLocationDO#getId()}
     */
    private Long fromLocationId;
    /**
     * 移出库位编号
     *
     * 关联 {@link MesWmWarehouseAreaDO#getId()}
     */
    private Long fromAreaId;

    /**
     * 备注
     */
    private String remark;

}
