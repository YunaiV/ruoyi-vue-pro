package cn.iocoder.yudao.module.promotion.dal.dataobject.point;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 积分商城商品 DO
 *
 * @author HUIHUI
 */
@TableName("promotion_point_product")
@KeySequence("promotion_point_product_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointProductDO extends BaseDO {

    /**
     * 积分商城商品编号
     */
    @TableId
    private Long id;
    /**
     * 积分商城活动 id
     *
     * 关联 {@link PointActivityDO#getId()}
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
     * 可兑换次数
     */
    private Integer count;
    /**
     * 所需兑换积分
     */
    private Integer point;
    /**
     * 所需兑换金额，单位：分
     */
    private Integer price;
    /**
     * 积分商城商品库存
     */
    private Integer stock;
    /**
     * 积分商城商品状态
     *
     * 枚举 {@link CommonStatusEnum 对应的类}
     */
    private Integer activityStatus;

}
