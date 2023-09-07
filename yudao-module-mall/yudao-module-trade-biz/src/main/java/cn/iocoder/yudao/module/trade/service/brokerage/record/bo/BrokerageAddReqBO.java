package cn.iocoder.yudao.module.trade.service.brokerage.record.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// TODO @疯狂：要不要 service 还是拍平；就是都放在 brokerage 包下，然后 bo 里面，稍微分分；
/**
 * 佣金 增加 Request BO
 *
 * @author owen
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrokerageAddReqBO {

    // TODO @疯狂：bo 的话，也可以考虑加下 @Validated 注解，校验下参数；防御性下哈，虽然不一定用的到

    /**
     * 业务ID
     */
    private String bizId;
    // TODO @疯狂：不需要 payPrice 和 count，计算成 price 就好啦；
    /**
     * 商品支付价格
     */
    private Integer payPrice;
    // TODO @疯狂：可以去掉 sku 哈，更抽象一点；
    /**
     * SKU 一级佣金
     */
    private Integer skuFirstBrokeragePrice;
    /**
     * SKU 二级佣金
     */
    private Integer skuSecondBrokeragePrice;
    /**
     * 购买数量
     */
    private Integer count;

}
