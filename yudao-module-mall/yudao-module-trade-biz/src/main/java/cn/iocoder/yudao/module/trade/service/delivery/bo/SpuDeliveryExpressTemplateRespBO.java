package cn.iocoder.yudao.module.trade.service.delivery.bo;

import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryExpressChargeModeEnum;
import lombok.Data;

/**
 * SPU 运费模板配置 Resp BO
 *
 * @author jason
 */
@Data
public class SpuDeliveryExpressTemplateRespBO {

    /**
     * 配送计费方式
     *
     * 枚举 {@link DeliveryExpressChargeModeEnum}
     */
    private Integer chargeMode;

    /**
     * 运费模板快递运费设置
     */
    private DeliveryExpressTemplateChargeBO templateCharge;

    /**
     * 运费模板包邮设置
     */
    private DeliveryExpressTemplateFreeBO templateFree;

}
