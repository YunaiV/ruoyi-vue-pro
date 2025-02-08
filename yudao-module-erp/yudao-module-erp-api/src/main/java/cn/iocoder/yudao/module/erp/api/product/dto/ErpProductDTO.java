package cn.iocoder.yudao.module.erp.api.product.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @date: 2025/1/3 8:31
 * @Version: 1.0
 * @description:
 */
@Data
@Builder
public class ErpProductDTO {
    /**
     * 产品id
     */
    private Long id;
    /**
     * 产品名称
     */
    private String name;
    /**
     * 部门id
     */
    private Long deptId;
    /**
     * SKU（编码）
     */
    private String barCode;
    /**
     * 材料（中文）
     */
    private String material;
    /**
     * 基础重量（kg）
     */
    private BigDecimal weight;
    /**
     * 基础宽度（mm）
     */
    private Integer width;
    /**
     * 基础长度（mm）
     */
    private Integer length;
    /**
     * 基础高度（mm）
     */
    private Integer height;
    /**
     * 包装高度（mm）
     */
    private Integer packageHeight;
    /**
     * 包装长度（mm）
     */
    private Integer packageLength;
    /**
     * 包装重量(kg)
     */
    private BigDecimal packageWeight;
    /**
     * 包装宽度（mm）
     */
    private Integer packageWidth;
    /**
     * 主图url
     */
    private String primaryImageUrl;

    /**
     * 主创建人
     */
    private String creator;

}
