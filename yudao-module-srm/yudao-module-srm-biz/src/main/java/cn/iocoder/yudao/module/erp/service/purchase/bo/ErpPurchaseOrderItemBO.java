package cn.iocoder.yudao.module.erp.service.purchase.bo;

import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ErpPurchaseOrderItemBO extends ErpPurchaseOrderItemDO {

    //产品信息
    ErpProductDTO product;

    private BigDecimal count;

    //不含税总额 = 总产品金额 - 总税额
//    private BigDecimal totalPriceUntaxed = this.getTotalProductPrice().subtract(this.getTotalTaxPrice());
    private BigDecimal totalPriceUntaxed;

}
