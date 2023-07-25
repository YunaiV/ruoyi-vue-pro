package cn.iocoder.yudao.module.trade.dal.dataobject.cart;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 购物车的商品信息 DO
 *
 * 每个商品，对应一条记录，通过 {@link #spuId} 和 {@link #skuId} 关联
 *
 * @author 芋道源码
 */
@TableName("trade_cart")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TradeCartDO extends BaseDO {

    // ========= 基础字段 BEGIN =========

    /**
     * 编号，唯一自增
     */
    private Long id;

    /**
     * 用户编号
     *
     * 关联 MemberUserDO 的 id 编号
     */
    private Long userId;

    /**
     * 是否添加到购物车
     *
     * false - 未添加：用户点击【立即购买】
     * true - 已添加：用户点击【添加购物车】
     *
     * 为什么要设计这个字段？
     *      配合 orderStatus 字段，可以知道有多少商品，用户点击了【立即购买】，最终多少【确认下单】
     */
    private Boolean addStatus;
    /**
     * 是否提交订单
     *
     * false - 未下单：立即购买，或者添加到购物车，此时设置为 false
     * true - 已下单：确认下单，此时设置为 true
     */
    private Boolean orderStatus;

    // ========= 基础字段 END =========

    // ========= 商品信息 BEGIN =========

    /**
     * 商品 SPU 编号
     *
     * 关联 ProductSpuDO 的 id 编号
     */
    private Long spuId;
    /**
     * 商品 SKU 编号
     *
     * 关联 ProductSkuDO 的 id 编号
     */
    private Long skuId;
    /**
     * 商品购买数量
     */
    private Integer count;

    // ========= 商品信息 END =========

    // ========= 优惠信息 BEGIN =========

    // TODO 芋艿：combination_id 拼团 ID
    // TODO 芋艿：seckill_id 秒杀产品 ID
    // TODO 芋艿：bargain_id 砍价 ID
    // TODO 芋艿：pinkId 团长拼团 ID

    // ========= 优惠信息 END =========

}
