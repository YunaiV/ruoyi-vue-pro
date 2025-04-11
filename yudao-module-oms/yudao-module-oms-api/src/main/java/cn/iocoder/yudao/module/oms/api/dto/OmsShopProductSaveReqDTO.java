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
public class OmsShopProductSaveReqDTO {
    /**
     * 店铺产品id
     */
    private Long id;

    /**
     * 店铺id
     **/
    private Long shopId;

    /**
     * 店铺产品编码
     **/
    private String code;

    /**
     * 外部资源来源id，唯一标识
     */
    private String sourceId;

    /**
     * 店铺产品名称
     */
    private String name;

    /**
     * 价格
     **/
    private BigDecimal price;

    /**
     * 币种
     **/
    private String currency;

    /**
     * 链接
     */
    private String url;

    /**
     * 可售数量
     */
    private Integer sellableQty;

    /**
     * 部门ID
     */
    private Long deptId;
}
