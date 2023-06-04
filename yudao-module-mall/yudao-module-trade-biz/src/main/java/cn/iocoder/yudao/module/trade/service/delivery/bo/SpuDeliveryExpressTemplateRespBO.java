package cn.iocoder.yudao.module.trade.service.delivery.bo;

import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressTemplateChargeDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressTemplateFreeDO;
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
     * <p>
     * 枚举 {@link DeliveryExpressChargeModeEnum}
     */
    private Integer chargeMode;

    /**
     * 运费模板快递运费设置
     */
    private DeliveryExpressTemplateChargeDO templateCharge;

    /**
     * 运费模板包邮设置
     */
    private DeliveryExpressTemplateFreeDO templateFree;

    /**
     * SPU 编号
     * <p>
     * 关联  ProductSpuDO 的 id 编号
     */
    private Long spuId;

    /**
     * 区域编号
     */
    private Integer areaId;

}
