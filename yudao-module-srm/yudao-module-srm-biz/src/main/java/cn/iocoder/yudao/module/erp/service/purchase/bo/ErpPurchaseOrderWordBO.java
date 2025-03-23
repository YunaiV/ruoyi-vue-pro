package cn.iocoder.yudao.module.erp.service.purchase.bo;

import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import com.deepoove.poi.data.TableRenderData;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 返回给word模板的采购订单
 */
@Data
public class ErpPurchaseOrderWordBO extends ErpPurchaseOrderDO {
    // 采购订单产品表
    private TableRenderData order;

    List<ErpPurchaseOrderItemBO> products;

    //不含税总额
    private BigDecimal totalPriceUntaxed;
}
