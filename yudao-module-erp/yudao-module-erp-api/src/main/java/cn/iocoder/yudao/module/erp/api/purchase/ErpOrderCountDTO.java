package cn.iocoder.yudao.module.erp.api.purchase;

import lombok.Data;

/**
 * 采购订单统计 DTO
 * 增加减少库存数量、xx数量
 */
@Data
public class ErpOrderCountDTO {
    //申请单订单项ID
    private Long purchaseOrderItemId;
    //订购数量
    private Integer orderCount;
    //布尔 增加、减少
//    private Boolean orderCountIsAdd;
}
