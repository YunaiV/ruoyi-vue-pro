package cn.iocoder.yudao.module.oms.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OmsShopProductItemDTO {

    /**
     * 店铺产品项编号
     */
    private Long id;

    /**
     * 产品编号
     */
    private Long productId;

    /**
     * 产品数量
     */
    private Long qty;

    /**
     * 关联产品
     */
    private OmsProductRespSimpleDTO product;

    /***
     * 店铺产品编号
     */
    private Long shopProductId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
