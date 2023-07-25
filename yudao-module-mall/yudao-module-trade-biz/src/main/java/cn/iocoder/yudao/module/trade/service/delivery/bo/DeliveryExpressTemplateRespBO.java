package cn.iocoder.yudao.module.trade.service.delivery.bo;

import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryExpressChargeModeEnum;
import lombok.Data;

/**
 * 运费模板配置 Resp BO
 *
 * @author jason
 */
@Data
public class DeliveryExpressTemplateRespBO {

    /**
     * 配送计费方式
     *
     * 枚举 {@link DeliveryExpressChargeModeEnum}
     */
    private Integer chargeMode;

    /**
     * 运费模板快递运费设置
     */
    private Charge charge;

    /**
     * 运费模板包邮设置
     */
    private Free free;

    /**
     * 快递运费模板费用配置 BO
     *
     * @author jason
     */
    @Data
    public static class Charge {

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

    /**
     * 快递运费模板包邮配置 BO
     *
     * @author jason
     */
    @Data
    public static class Free {

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

}
