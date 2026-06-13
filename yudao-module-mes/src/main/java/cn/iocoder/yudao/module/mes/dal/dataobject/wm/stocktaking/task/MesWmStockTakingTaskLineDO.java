package cn.iocoder.yudao.module.mes.dal.dataobject.wm.stocktaking.task;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseAreaDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseLocationDO;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmStockTakingTaskLineStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;

/**
 * MES 盘点任务行 DO
 *
 * @author 芋道源码
 */
@TableName("mes_wm_stock_taking_task_line")
@KeySequence("mes_wm_stock_taking_task_line_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmStockTakingTaskLineDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 盘点任务编号
     *
     * 关联 {@link MesWmStockTakingTaskDO#getId()}
     */
    private Long taskId;

    /**
     * 库存编号
     */
    private Long materialStockId;
    /**
     * 物料编号
     *
     * 关联 {@link MesMdItemDO#getId()}
     */
    private Long itemId;

    /**
     * 批次编号
     */
    private Long batchId;
    /**
     * 批次编码
     */
    private String batchCode;

    /**
     * 在库数量
     */
    private BigDecimal quantity;
    /**
     * 盘点数量
     */
    private BigDecimal takingQuantity;

    /**
     * 仓库编号
     *
     * 关联 {@link MesWmWarehouseDO#getId()}
     */
    private Long warehouseId;
    /**
     * 库位编号
     *
     * 关联 {@link MesWmWarehouseLocationDO#getId()}
     */
    private Long locationId;
    /**
     * 库区编号
     *
     * 关联 {@link MesWmWarehouseAreaDO#getId()} ()}
     */
    private Long areaId;

    /**
     * 盘点状态
     *
     * 字典 {@link DictTypeConstants#MES_WM_STOCK_TAKING_TASK_LINE_STATUS}
     * 枚举 {@link MesWmStockTakingTaskLineStatusEnum}
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

}
