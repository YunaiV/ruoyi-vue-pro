package cn.iocoder.yudao.module.promotion.dal.dataobject.bargain;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 砍价记录 DO
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
     * 砍价活动编号
     */
    private Long activityId;

    /**
     * 用户编号
     */
    private Long userId;

    /**
     * 商品 SPU 编号
     */
    private Long spuId;

    /**
     * 商品 SKU 编号
     */
    private Long skuId;

    /**
     * 砍价底价，单位分
     */
    private Integer bargainPrice;

    /**
     * 商品原价，单位分
     */
    private Integer price;

    /**
     * 应付金额，单位分
     */
    private Integer payPrice;

    /**
     * 状态1 - 砍价中；2- 砍价成功；3 - 砍价失败
     */
    private Integer status;

    /**
     * 订单编号
     */
    private Long orderId;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 过期时间
     */
    private Data expireTime;

}
