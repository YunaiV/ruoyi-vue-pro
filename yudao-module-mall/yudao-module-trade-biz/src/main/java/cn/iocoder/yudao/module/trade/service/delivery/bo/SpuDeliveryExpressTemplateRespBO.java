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

    // TODO @jason：是不是可以简单一点；是否包邮；然后直接把  templateCharge、templateFree 需要的字段平铺开

    /**
     * 配送计费方式
     *
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

    // TODO @jason：下面两个字段不用返回也可以呀
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
