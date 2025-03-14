package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Collection;

@Schema(description = "管理后台 - ERP 采购订单 合并 VO")
@Data
public class ErpPurchaseOrderMergeReqVO {


    /**
     * 订单项ids
     */
    Collection<Long> itemIds;
}
