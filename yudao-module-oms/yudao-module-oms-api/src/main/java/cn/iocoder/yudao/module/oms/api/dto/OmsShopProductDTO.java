package cn.iocoder.yudao.module.oms.api.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class OmsShopProductDTO {

    /**
     * 店铺产品id
     */
    private Long id;

    /**
     * 店铺id
     **/
    private Long shopId;

    /**
     * 平台sku
     **/
    private String code;

    /**
     * 外部资源来源id，唯一标识
     */
    private String externalId;

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
    private String currencyCode;

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

    /**
     * 店铺产品关联项
     */
    private List<OmsShopProductItemDTO> items;

    /**
     * 店铺所属店铺
     */
    private OmsShopDTO shop;

    /***
     *
     * 激活状态【0是未激活，1是已激活】
     */
    private Boolean status;

    /**
     * 外部来源激活状态【0是未激活，1是已激活】
     */
    private Boolean externalStatus;
}