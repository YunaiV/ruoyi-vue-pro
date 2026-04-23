package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 生产计划表 deepay_production_plan（Phase 5 — ProductionPlanner 输出）
 */
@TableName("deepay_production_plan")
@Data
public class DeepayProductionPlanDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联链码 */
    private String chainCode;

    /**
     * 计划生产量：
     * = predictedDemand - currentStock - inTransitStock
     */
    private Integer plannedQuantity;

    /** 预测需求量（来自 DemandAgent） */
    private Integer forecastDemand;

    /** 当前库存快照 */
    private Integer currentStock;

    /** 在途库存（已下单未入库） */
    private Integer inTransitStock;

    /**
     * 计划状态：
     * PENDING   — 待执行
     * IN_PRODUCTION — 生产中
     * COMPLETED — 已完成
     * CANCELLED — 已取消
     */
    private String status;

    /** 关联工厂 ID（可选） */
    private Long factoryId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
