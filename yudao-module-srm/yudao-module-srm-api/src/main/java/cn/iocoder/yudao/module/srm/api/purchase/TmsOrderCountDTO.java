package cn.iocoder.yudao.module.srm.api.purchase;

import lombok.Builder;
import lombok.Data;

/**
 * 采购订单统计 DTO
 * 增加减少库存数量、xx数量
 */
@Data
@Builder
public class TmsOrderCountDTO {
    //申请单订单项ID
    private Long purchaseOrderItemId;
    //订购数量
    private Integer quantity;

}
