package cn.iocoder.yudao.module.oms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: $
 * @Author: c-tao
 * @Date: 2025/4/1$
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OmsShopSaveReqDTO {
    /**
     * 店铺id
     */
    private Long id;

    /**
     * 店铺名称
     */
    private String name;

    /**
     * 外部来源名称
     */
    private String externalName;

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
