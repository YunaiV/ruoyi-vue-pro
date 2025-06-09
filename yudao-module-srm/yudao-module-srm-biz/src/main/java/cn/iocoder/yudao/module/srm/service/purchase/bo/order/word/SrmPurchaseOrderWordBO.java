package cn.iocoder.yudao.module.srm.service.purchase.bo.order.word;

import cn.iocoder.yudao.module.fms.api.finance.dto.FmsCompanyDTO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderDO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 返回给word模板的采购订单
 */
@Data
public class SrmPurchaseOrderWordBO extends SrmPurchaseOrderDO {
    // 采购订单产品表
//    private TableRenderData order;

    List<SrmPurchaseOrderItemWordBO> products;

    //不含税总额
    private BigDecimal totalPriceUntaxed;

    //付款方
    FmsCompanyDTO a;
    //供货方
    FmsCompanyDTO b;

    //付款条款
    private String paymentTerms;

    //币别名称(计价单位)
    private String currencyName;

    //签订地点
    private String signingPlace;
    //签订日期
    private LocalDateTime signingDate;
    //签订日期格式化str
    private String signingDateFormat;

    //word起始港名称
    private String portOfLoading;
    //word目的港口名称
    private String portOfDischarge;
}
