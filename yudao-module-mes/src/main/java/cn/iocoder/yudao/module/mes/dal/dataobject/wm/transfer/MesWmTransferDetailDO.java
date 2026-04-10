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
 * MES 调拨明细 DO
 *
 * @author 芋道源码
 */
@TableName("mes_wm_transfer_detail")
@KeySequence("mes_wm_transfer_detail_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmTransferDetailDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 转移单行编号
     *
     * 关联 {@link MesWmTransferLineDO#getId()}
     */
    private Long lineId;
    /**
     * 转移单编号
     *
     * 关联 {@link MesWmTransferDO#getId()}
     */
    private Long transferId;
    /**
     * 物料编号
     *
     * 关联 {@link MesMdItemDO#getId()}
     */
    private Long itemId;
    /**
     * 上架数量
     */
    private BigDecimal quantity;
    /**
     * 批次编号
     */
    private Long batchId;

    /**
     * 移入仓库编号
     *
     * 关联 {@link MesWmWarehouseDO#getId()}
     */
    private Long toWarehouseId;
    /**
     * 移入库区编号
     *
     * 关联 {@link MesWmWarehouseLocationDO#getId()}
     */
    private Long toLocationId;
    /**
     * 移入库位编号
     *
     * 关联 {@link MesWmWarehouseAreaDO#getId()}
     */
    private Long toAreaId;

    /**
     * 备注
     */
    private String remark;

}
