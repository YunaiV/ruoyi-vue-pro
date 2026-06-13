package cn.iocoder.yudao.module.mes.dal.dataobject.wm.materialstock;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemTypeDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.vendor.MesMdVendorDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.batch.MesWmBatchDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseAreaDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseLocationDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MES 库存台账（仓库现有量）DO
 */
@TableName("mes_wm_material_stock")
@KeySequence("mes_wm_material_stock_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmMaterialStockDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    // ==================== 物料维度 ====================

    /**
     * 物料分类编号
     *
     * 关联 {@link MesMdItemTypeDO#getId()}
     */
    private Long itemTypeId;
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

    // ==================== 库存位置 ====================

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

    // ==================== 供应商 & 数量 ====================

    /**
     * 供应商编号
     *
     * 关联 {@link MesMdVendorDO#getId()}
     */
    private Long vendorId;
    /**
     * 在库数量
     */
    private BigDecimal quantity;
    /**
     * 入库时间
     */
    private LocalDateTime receiptTime;

    // ==================== 状态 ====================

    /**
     * 是否冻结
     */
    private Boolean frozen;

}
