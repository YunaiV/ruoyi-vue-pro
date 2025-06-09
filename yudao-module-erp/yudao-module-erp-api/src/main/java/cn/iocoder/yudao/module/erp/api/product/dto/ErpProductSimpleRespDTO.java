package cn.iocoder.yudao.module.erp.api.product.dto;


import lombok.Data;

@Data
public class ErpProductSimpleRespDTO {
    /**
     * 产品编号
     * */
    private Long id;

    /**
     * 产品名称
     * */
    private String name;

    /**
     * SKU (编码)
     * */
    private String code;

    /**
     * 产品分类编号
     * */
    private Long categoryId;

    /**
     * 产品分类名称
     * */
    private String categoryName;

    /**
     * 品牌
     * */
    private String brand;

    /**
     * 系列
     * */
    private String series;

    /**
     * 单位编号
     * */
    private Long unitId;

    /**
     * 单位名称
     * */
    private String unitName;
}
