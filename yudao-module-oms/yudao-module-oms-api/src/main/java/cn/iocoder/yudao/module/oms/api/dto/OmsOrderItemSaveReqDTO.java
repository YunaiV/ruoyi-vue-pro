package cn.iocoder.yudao.module.oms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OmsOrderItemSaveReqDTO {
    /**
     * 主键
     */
    private Long id;
    /**
     * 销售订单id
     */
    private Long orderId;
    /**
     * 店铺产品编号
     */
    private Long shopProductId;


    /**
     * 店铺产品外部来源id
     */
    private String shopProductExternalId;

    /**
     * 店铺产品外部编码
     */
    private String shopProductExternalCode;

    /**
     * 外部来源唯一标识
     */
    private String externalId;
    /**
     * 店铺产品数量
     */
    private Integer qty;
    /**
     * 单价
     */
    private BigDecimal price;
}
