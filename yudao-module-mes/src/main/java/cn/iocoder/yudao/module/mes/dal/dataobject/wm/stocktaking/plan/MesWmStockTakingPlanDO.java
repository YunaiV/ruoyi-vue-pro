package cn.iocoder.yudao.module.mes.dal.dataobject.wm.stocktaking.plan;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmStockTakingTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * MES 盘点方案 DO
 *
 * @author 芋道源码
 */
@TableName("mes_wm_stock_taking_plan")
@KeySequence("mes_wm_stock_taking_plan_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesWmStockTakingPlanDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 方案编码
     */
    private String code;

    /**
     * 方案名称
     */
    private String name;

    /**
     * 盘点类型
     *
     * 枚举 {@link MesWmStockTakingTypeEnum}
     */
    private Integer type;
    /**
     * 计划开始时间
     */
    private LocalDateTime startTime;
    /**
     * 计划结束时间
     */
    private LocalDateTime endTime;

    /**
     * 是否盲盘
     */
    private Boolean blindFlag;
    /**
     * 是否冻结库存
     */
    private Boolean frozen;

    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

}
