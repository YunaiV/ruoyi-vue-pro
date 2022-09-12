package cn.iocoder.yudao.module.trade.dal.dataobject.cart;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 购物车的商品信息 DO
 *
 * @author 芋道源码
 */
@TableName("trade_cart_item")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TradeCartItemDO extends BaseDO {

    // ========= 基础字段 BEGIN =========

    /**
     * 编号，唯一自增
     */
    private Long id;
    /**
     * 是否选中
     */
    private Boolean selected;

    // ========= 基础字段 END =========

    // ========= 买家信息 BEGIN =========

    /**
     * 用户编号
     *
     * 关联 MemberUserDO 的 id 编号
     */
    private Long userId;

    // ========= 买家信息 END =========

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

//    /**
//     * 商品营销活动编号
//     */
//    private Long activityId; // discount_id
//    /**
//     * 商品营销活动类型
//     */
//    private Integer activityType;
    // TODO 芋艿：combination_id 拼团 ID
    // TODO 芋艿：seckill_id 秒杀产品 ID
    // TODO 芋艿：bargain_id 砍价 ID

    // ========= 优惠信息 END =========

    // TODO 待确定字段：mf
    // TODO 芋艿：distribution_card_no 推广员
    // TODO 芋艿：is_pay 未购买、已购买
    // TODO 芋艿：is_new 是否立即购买

    // TODO 待确定字段: yv
    // TODO isPay: 是否购买
    // TODO isNew：是否立即购买

}
