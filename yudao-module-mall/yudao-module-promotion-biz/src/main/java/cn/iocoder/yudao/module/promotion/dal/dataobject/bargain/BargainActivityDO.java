package cn.iocoder.yudao.module.promotion.dal.dataobject.bargain;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 砍价活动 DO
 *
 * @author HUIHUI
 */
@TableName("promotion_bargain_activity")
@KeySequence("promotion_bargain_activity_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BargainActivityDO extends BaseDO {

    /**
     * 砍价活动编号
     */
    @TableId
    private Long id;

    /**
     * 砍价活动名称
     */
    private String name;

    /**
     * 活动开始时间
     */
    private LocalDateTime startTime;
    /**
     * 活动结束时间
     */
    private LocalDateTime endTime;

    /**
     * 活动状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

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
     * 砍价底价，单位：分
     */
    private Integer bargainMinPrice;

    /**
     * 砍价库存(剩余库存砍价时扣减)
     */
    private Integer stock;
    /**
     * 砍价总库存
     */
    private Integer totalStock;

    /**
     * 砍价人数
     *
     * 需要多少人，砍价才能成功，即 {@link BargainRecordDO#getStatus()} 更新为 {@link BargainRecordDO#getStatus()} 成功状态
     */
    private Integer helpMaxCount;
    /**
     * 帮砍次数
     *
     * 单个活动，用户可以帮砍的次数。
     * 例如说：帮砍次数为 1 时，A 和 B 同时将该活动链接发给 C，C 只能帮其中一个人砍价。
     */
    private Integer bargainCount;

    /**
     * 总限购数量
     */
    private Integer totalLimitCount;
    /**
     * 用户每次砍价的最小金额，单位：分
     */
    private Integer randomMinPrice;
    /**
     * 用户每次砍价的最大金额，单位：分
     */
    private Integer randomMaxPrice;

}
