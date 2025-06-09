package cn.iocoder.yudao.module.srm.enums;

/**
 * 消息通道枚举
 */
public interface SrmChannelEnum {

    /**
     * 供应商同步通道
     */
    String SUPPLIER = "srm_channel_supplier";

    /**
     * 采购订单同步通道
     */
    String PURCHASE_ORDER = "srm_channel_purchase_order";

    /**
     * 采购入库单同步通道
     */
    String PURCHASE_IN = "srm_channel_purchase_in";

    /**
     * 采购退货单同步通道
     */
    String PURCHASE_RETURN = "srm_channel_purchase_return";
}
