package cn.iocoder.yudao.module.trade.dal.dataobject.order;

/**
 * 订单优惠明细
 *
 * 可参考 https://jos.jd.com/apilist?apiGroupId=55&apiId=16757&apiName=jingdong.pop.order.coupondetail
 */
// TODO 芋艿 后续在完善
@Deprecated
public class OrderPreferentialDO {

    /**
     * 编号
     */
    private Integer id;
    /**
     * 类型
     *
     * 1 - 促销活动
     * 2 - 优惠劵
     */
    private Integer type;
    // TODO 芋艿 优惠劵编号 or 促销活动编号
    /**
     * 订单编号
     */
    private Integer orderId;
    /**
     * 商品 SPU 编号
     */
    private Integer spuId;
    /**
     * 商品 SKU 编号
     */
    private Integer skuId;
    /**
     * 商品数量
     */
    private Integer quantity;
    /**
     * 传入时的价格
     */
    private Integer originTotal;
    /**
     * 总优惠价格
     */
    private Integer discountTotal;

}
