package cn.iocoder.yudao.module.trade.service.delivery.bo;

import lombok.Data;

/**
 * 快递运费模板费用配置 BO
 *
 * @author jason
 */
@Data
public class DeliveryExpressTemplateChargeBO {

    /**
     * 首件数量(件数,重量，或体积)
     */
    private Double startCount;
    /**
     * 起步价，单位：分
     */
    private Integer startPrice;
    /**
     * 续件数量(件, 重量，或体积)
     */
    private Double extraCount;
    /**
     * 额外价，单位：分
     */
    private Integer extraPrice;
}
