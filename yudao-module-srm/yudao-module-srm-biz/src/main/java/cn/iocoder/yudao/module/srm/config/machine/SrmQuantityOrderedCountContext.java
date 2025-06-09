package cn.iocoder.yudao.module.srm.config.machine;

import lombok.Builder;
import lombok.Data;

/**
 * 采购订单-订购数量 DTO
 * 增加减少库存数量、xx数量 , quantity ordered
 */
@Data
@Builder
public class SrmQuantityOrderedCountContext {
    //申请单订单项ID
    private Long purchaseRequestItemId;
    //订购数量
    private Integer quantity;

}
