package cn.iocoder.yudao.module.trade.service.delivery.bo;

import lombok.Data;

/**
 * 快递运费模板包邮配置 BO
 *
 * @author jason
 */
@Data
public class DeliveryExpressTemplateFreeBO {

    /**
     * 包邮金额，单位：分
     *
     * 订单总金额 > 包邮金额时，才免运费
     */
    private Integer freePrice;

    /**
     * 包邮件数
     *
     * 订单总件数 > 包邮件数时，才免运费
     */
    private Integer freeCount;
}
