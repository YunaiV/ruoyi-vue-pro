package cn.iocoder.yudao.module.statistics.dal.mysql.product;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 商品 SPU 统计 DO
 *
 * 以天为维度，统计商品 SPU 的数据
 *
 * @author 芋道源码
 */
@TableName("product_spu_statistics")
@KeySequence("product_spu_statistics_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSpuStatisticsDO extends BaseDO {

    /**
     * 编号，主键自增
     */
    @TableId
    private Long id;

    /**
     * 商品 SPU 编号
     *
     * 关联 ProductSpuDO 的 id 字段
     */
    private Long spuId;
    /**
     * 统计日期
     */
    private LocalDateTime time;

    /**
     * 浏览量
     */
    private Integer browseCount;
    /**
     * 收藏量
     */
    private Integer favoriteCount;

    /**
     * 添加购物车次数
     *
     * 以商品被添加到购物车的 createTime 计算，后续多次添加，不会增加该值。
     * 直到该次被下单、或者被删除，后续再次被添加到购物车。
     */
    private Integer addCartCount;
    /**
     * 创建订单商品数
     */
    private Integer createOrderCount;
    /**
     * 支付订单商品数
     */
    private Integer payOrderCount;
    /**
     * 总支付金额，单位：分
     */
    private Integer payPrice;

}
