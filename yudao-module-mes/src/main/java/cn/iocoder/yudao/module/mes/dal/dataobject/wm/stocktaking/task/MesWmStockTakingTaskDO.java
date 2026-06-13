package cn.iocoder.yudao.module.mes.dal.dataobject.wm.stocktaking.task;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.stocktaking.plan.MesWmStockTakingPlanDO;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmStockTakingTaskStatusEnum;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmStockTakingTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;

/**
 * MES 盘点任务 DO
 *
 * @author 芋道源码
 */
@TableName("mes_wm_stock_taking_task")
@KeySequence("mes_wm_stock_taking_task_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmStockTakingTaskDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 任务编码
     */
    private String code;
    /**
     * 任务名称
     */
    private String name;
    /**
     * 盘点日期
     */
    private LocalDateTime takingDate;

    /**
     * 盘点类型
     *
     * 字典 {@link DictTypeConstants#MES_WM_STOCK_TAKING_TYPE}
     * 枚举 {@link MesWmStockTakingTypeEnum}
     */
    private Integer type;
    /**
     * 盘点人编号
     *
     * 关联 AdminUserDO 的 id 字段
     */
    private Long userId;

    /**
     * 盘点计划编号
     *
     * 关联 {@link MesWmStockTakingPlanDO#getId()}
     */
    private Long planId;

    /**
     * 是否盲盘
     */
    private Boolean blindFlag;
    /**
     * 是否冻结库存
     */
    private Boolean frozen;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 任务状态
     *
     * 字典 {@link DictTypeConstants#MES_WM_STOCK_TAKING_TASK_STATUS}
     * 枚举 {@link MesWmStockTakingTaskStatusEnum}
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

}
