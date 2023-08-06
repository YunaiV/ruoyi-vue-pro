package cn.iocoder.yudao.module.promotion.dal.dataobject.bargain;

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
     */
    private Integer status;

    /**
     * 商品 SPU 编号
     */
    private Long spuId;

    /**
     * 达到该人数，才能砍到低价
     */
    private Integer userSize;

    /**
     * 最大帮砍次数
     */
    private Integer bargainCount;

    /**
     * 总限购数量
     */
    private Integer totalLimitCount;

    /**
     * 砍价库存
     */
    private Integer stock;

    /**
     * 用户每次砍价的最小金额，单位：分
     */
    private Integer randomMinPrice;

    /**
     * 用户每次砍价的最大金额，单位：分
     */
    private Integer randomMaxPrice;

    /**
     * 砍价成功数量
     */
    private Integer successCount;

}
