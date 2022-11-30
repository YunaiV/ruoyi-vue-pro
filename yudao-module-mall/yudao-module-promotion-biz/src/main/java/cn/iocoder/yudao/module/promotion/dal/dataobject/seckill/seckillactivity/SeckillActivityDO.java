package cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckillactivity;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionActivityStatusEnum;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀活动 DO
 *
 * @author 芋道源码
 */
@TableName("promotion_seckill_activity")
@KeySequence("promotion_seckill_activity_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillActivityDO extends BaseDO {

    /**
     * 秒杀活动编号
     */
    @TableId
    private Long id;
    /**
     * 秒杀活动名称
     */
    private String name;
    /**
     * 活动状态
     *
     * 枚举 {@link PromotionActivityStatusEnum 对应的类}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;
    /**
     * 活动开始时间
     */
    private LocalDateTime startTime;
    /**
     * 活动结束时间
     */
    private LocalDateTime endTime;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 秒杀时段 id
     */
    // TODO @halfninety 可以使用 List 存储；看下别的模块怎么做的哈
    private String timeId;
    /**
     * 付款订单数
     */
    private Integer orderCount;
    /**
     * 付款人数
     */
    private Integer userCount;
    // TODO @halfninety 使用 Long 哈。单位是分
    /**
     * 订单实付金额，单位：分
     */
    private BigDecimal totalPrice;

}
