package cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnsales;

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
 * MES 销售退货明细 DO
 *
 * @author 芋道源码
 */
@TableName("mes_wm_return_sales_detail")
@KeySequence("mes_wm_return_sales_detail_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmReturnSalesDetailDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 退货单 ID
     *
     * 关联 {@link MesWmReturnSalesDO#getId()}
     */
    private Long returnId;
    /**
     * 行 ID
     *
     * 关联 {@link MesWmReturnSalesLineDO#getId()}
     */
    private Long lineId;
    /**
     * 物料 ID
     *
     * 关联 {@link MesMdItemDO#getId()}
     */
    private Long itemId;
    /**
     * 数量
     */
    private BigDecimal quantity;
    /**
     * 批次 ID
     */
    private Long batchId;
    /**
     * 批次号
     */
    private String batchCode;
    /**
     * 仓库 ID
     *
     * 关联 {@link MesWmWarehouseDO#getId()}
     */
    private Long warehouseId;
    /**
     * 库区 ID
     *
     * 关联 {@link MesWmWarehouseLocationDO#getId()}
     */
    private Long locationId;
    /**
     * 库位 ID
     *
     * 关联 {@link MesWmWarehouseAreaDO#getId()}
     */
    private Long areaId;
    /**
     * 备注
     */
    private String remark;

}
