package cn.iocoder.yudao.module.erp.service.purchase.bo;

import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ErpPurchaseOrderItemBO extends ErpPurchaseOrderItemDO {
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
