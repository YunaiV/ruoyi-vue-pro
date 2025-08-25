package cn.iocoder.yudao.module.coal.dal.dataobject.productionplan;

import com.sun.xml.internal.bind.v2.TODO;
import lombok.*;

import java.time.LocalDate;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.annotation.TableLogic;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 生产计划 DO
 *
 * @author 京京
 */
@TableName("coal_production_plan")
@KeySequence("coal_production_plan_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionPlanDO extends BaseDO {

    public static final Long PARENT_ID_ROOT = 0L;

    /**
     * 计划ID
     */
    @TableId
    private Long id;
    /**
     * 计划名称
     */
    private String name;
    /**
     * 父计划ID
     */
    private Long parentId;
    /**
     * 显示顺序
     */
    private Integer sort;
    /**
     * 计划编号
     */
    private String planCode;
    /**
     * 计划类型
     *
     * 枚举 {@link TODO plan_type 对应的类}
     */
    private Integer planType;
    /**
     * 计划年度
     */
    private Integer planYear;
    /**
     * 计划月份
     */
    private Integer planMonth;
    /**
     * 计划日期
     */
    private LocalDate planDate;
    /**
     * 班次ID
     */
    private Long shiftId;
    /**
     * 计划状态
     *
     * 枚举 {@link TODO plan_status 对应的类}
     */
    private Integer status;
    /**
     * 计划层级
     *
     * 枚举 {@link TODO plan_level 对应的类}
     */
    private String planPath;
    /**
     * 计划入洗原煤量(吨)
     */
    private BigDecimal rawCoalPlan;
    /**
     * 计划末煤产量(吨)
     */
    private BigDecimal fineCoalPlan;
    /**
     * 计划粒煤产量(吨)
     */
    private BigDecimal granularCoalPlan;
    /**
     * 计划大块煤产量(吨)
     */
    private BigDecimal largeBlockCoalPlan;
    /**
     * 计划中块煤产量(吨)
     */
    private BigDecimal mediumBlockCoalPlan;
    /**
     * 计划小块煤产量(吨)
     */
    private BigDecimal smallBlockCoalPlan;
    /**
     * 计划中煤产量(吨)
     */
    private BigDecimal middlingCoalPlan;
    /**
     * 计划煤泥产量(吨)
     */
    private BigDecimal slimePlan;
    /**
     * 计划矸石产量(吨)
     */
    private BigDecimal ganguePlan;
    /**
     * 预留计划产量字段1(吨)
     */
    private BigDecimal reservedProductPlan1;
    /**
     * 预留计划产量字段2(吨)
     */
    private BigDecimal reservedProductPlan2;
    /**
     * 末煤灰分(%)
     */
    private BigDecimal fineCoalAsh;
    /**
     * 粒煤灰分(%)
     */
    private BigDecimal granularCoalAsh;
    /**
     * 大块煤灰分(%)
     */
    private BigDecimal largeBlockCoalAsh;
    /**
     * 中块煤灰分(%)
     */
    private BigDecimal mediumBlockCoalAsh;
    /**
     * 小块煤灰分(%)
     */
    private BigDecimal smallBlockCoalAsh;
    /**
     * 中煤灰分(%)
     */
    private BigDecimal middlingCoalAsh;
    /**
     * 制定人ID
     */
    private Long creatorId;
    /**
     * 审批人ID
     */
    private Long approverId;
    /**
     * 审批时间
     */
    private LocalDateTime approveTime;


}
