package cn.iocoder.yudao.module.oms.api.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @Description: $
 * @Author: c-tao
 * @Date: 2025/4/1$
 */
@Data
@Builder
public class OmsShopDTO {
    /**
     * 店铺id
     */
    private Long id;

    /**
     * 店铺名称
     */
    private String name;

    /**
     * 平台店铺名称
     */
    private String platformShopName;

    /**
     * 店铺编码
     */
    private String code;

    /**
     * 平台店铺编码
     */
    private String platformShopCode;


    /**
     * 平台名称
     */
    private String platformCode;

    /**
     * 店铺类型
     */
    private Integer type;
}
