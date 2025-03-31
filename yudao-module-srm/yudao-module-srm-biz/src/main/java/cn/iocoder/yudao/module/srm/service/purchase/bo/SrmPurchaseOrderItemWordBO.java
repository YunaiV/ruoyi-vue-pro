package cn.iocoder.yudao.module.srm.service.purchase.bo;

import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderItemDO;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 返回给word的订单子项
 */
@Data
public class SrmPurchaseOrderItemWordBO extends SrmPurchaseOrderItemDO {
    //序号
    Integer index;

    //产品信息
//    ErpProductDTO product;

    private BigDecimal count;

    //不含税总额  =  产品单价productPrice * 数量
    private BigDecimal totalPriceUntaxed;

    //单位名称,根据产品的单位id查出
    private String unitName;

    //格式化后的deliveryTime
    private String deliveryTimeFormat;
}
