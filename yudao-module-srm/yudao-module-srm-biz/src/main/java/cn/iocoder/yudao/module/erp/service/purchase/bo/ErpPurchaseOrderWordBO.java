package cn.iocoder.yudao.module.erp.service.purchase.bo;

import cn.iocoder.yudao.module.erp.api.finance.dto.ErpFinanceSubjectDTO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 返回给word模板的采购订单
 */
@Data
public class ErpPurchaseOrderWordBO extends ErpPurchaseOrderDO {
    // 采购订单产品表
//    private TableRenderData order;

    List<ErpPurchaseOrderItemBO> products;

    //不含税总额
    private BigDecimal totalPriceUntaxed;

    //付款方
    ErpFinanceSubjectDTO a;
    //供货方
    ErpFinanceSubjectDTO b;

    //付款条款
    private String paymentTerms;

    //币别名称(计价单位)
    private String currencyName;

    //签订地点
    private String signingPlace;
    //签订日期
    private LocalDateTime signingDate;
}
