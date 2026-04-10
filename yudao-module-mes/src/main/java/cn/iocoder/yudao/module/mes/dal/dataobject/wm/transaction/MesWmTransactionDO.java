package cn.iocoder.yudao.module.mes.dal.dataobject.wm.transaction;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.batch.MesWmBatchDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.materialstock.MesWmMaterialStockDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseAreaDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseLocationDO;
import cn.iocoder.yudao.module.mes.enums.MesBizTypeConstants;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmTransactionTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MES 库存事务流水 DO
 *
 * 记录每一笔库存增减事件，系统自动生成，只读查询，不允许人工维护。
 */
@TableName("mes_wm_transaction")
@KeySequence("mes_wm_transaction_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmTransactionDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    // ==================== 业务归属 ====================

    /**
     * 事务类型
     *
     * 枚举 {@link MesWmTransactionTypeEnum}
     */
    private Integer type;
    /**
     * 本次变动数量
     *
     * 正数=入库，负数=出库
     */
    private BigDecimal quantity;

    /**
     * 业务类型
     *
     * 对应 {@link MesBizTypeConstants} 中的常量
     */
    private Integer bizType;
    /**
     * 来源业务主单 ID
     */
    private Long bizId;
    /**
     * 来源业务单号
     */
    private String bizCode;
    /**
     * 来源业务行 ID
     */
    private Long bizLineId;

    /**
     * 库存记录 ID
     *
     * 关联 {@link MesWmMaterialStockDO#getId()}
     */
    private Long materialStockId;
    /**
     * 关联的事务 ID
     *
     * 关联 {@link #getId()}，用于调拨等成对流水关联
     */
    private Long relatedTransactionId;

    // ==================== 物料维度 ====================

    /**
     * 物料 ID
     *
     * 关联 {@link MesMdItemDO#getId()}
     */
    private Long itemId;
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

    // ==================== 库存位置 ====================

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

    // ==================== 时间 ====================

    /**
     * 事务发生时间
     */
    private LocalDateTime transactionTime;
    /**
     * ERP 账期
     */
    private LocalDateTime erpTime;
    /**
     * 入库时间
     */
    private LocalDateTime receiptTime;

}
