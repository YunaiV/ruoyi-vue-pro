package cn.iocoder.yudao.module.promotion.dal.dataobject.bargain;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.promotion.enums.bargain.BargainRecordStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 砍价记录 DO TODO
 *
 * @author HUIHUI
 */
@TableName("promotion_bargain_record")
@KeySequence("promotion_bargain_record_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BargainRecordDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 用户编号
     */
    private Long userId;

    /**
     * 砍价活动编号
     *
     * 关联 {@link BargainActivityDO#getId()} 字段
     */
    private Long activityId;
    /**
     * 商品 SPU 编号
     */
    private Long spuId;
    /**
     * 商品 SKU 编号
     */
    private Long skuId;

    /**
     * 砍价起始价格，单位：分
     */
    private Integer bargainFirstPrice;
    /**
     * 当前砍价，单位：分
     */
    private Integer bargainPrice;

    /**
     * 砍价状态
     *
     * 砍价成功的条件是：（2 选 1）
     *  1. 砍价到 {@link BargainActivityDO#getBargainMinPrice()} 底价
     *  2. 助力人数到达 {@link BargainActivityDO#getHelpMaxCount()} 人
     *
     * 枚举 {@link BargainRecordStatusEnum}
     */
    private Integer status;
    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 订单编号
     */
    private Long orderId;

}
