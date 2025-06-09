package cn.iocoder.yudao.module.erp.service.product.bo;

import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.json.GuidePriceJson;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author: Wqh
 * @date: 2024/12/3 16:52
 */
@Data
public class ErpProductBO {
    /**
     * 产品编号
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
    private String code;
    /**
     * 产品分类编号
     */
    private Long categoryId;
    /**
     * 单位编号
     */
    private Long unitId;
    /**
     * 材料（中文）
     */
    private String material;
    /**
     * 产品材质-关联海关分类
     */
    private Long customCategoryId;
    /**
     * 产品状态（1启用，0禁用）
     */
    private Boolean status;
    /**
     * 品牌
     */
    private String brand;
    /**
     * 备注
     */
    private String remark;
    /**
     * 基础重量（kg）
     */
    private BigDecimal weight;
    /**
     * 系列
     */
    private String series;
    /**
     * 颜色
     */
    private String color;
    /**
     * 型号
     */
    private String model;
    /**
     * 流水号
     */
    private Integer serial;
    /**
     * 生产编号
     */
    private String productionNo;
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
     * 主图url
     */
    private String primaryImageUrl;
    /**
     * 次图urls
     */
    private String secondaryImageUrls;
    /**
     * 副图urlList
     */
    private List<String> secondaryImageUrlList;
    /**
     * 指导价list
     */
    private List<GuidePriceJson> guidePriceList;
    /**
     * 指导价，json格式
     */
    private String guidePrices;
    /**
     * 专利国别
     */
    private String patentCountryCodes;
    /**
     * 专利国别list
     */
    private List<Integer> patentCountryCodeList;
    /**
     * 专利类型
     */
    private Integer patentType;
    /**
     * PO产品经理id
     */
    private Long productOwnerId;
    /**
     * ID工业设计id
     */
    private Long industrialDesignerId;
    /**
     * RD研发工程师id
     */
    private Long researchDeveloperId;
    /**
     * 维护工程师id
     */
    private Long maintenanceEngineerId;
    /**
     * 设计说明
     */
    private String description;
    /**
     * 包装高度（mm）
     */
    private Integer packageHeight;
    /**
     * 包装长度（mm）
     */
    private Integer packageLength;
    /**
     * 包装重量
     */
    private BigDecimal packageWeight;
    /**
     * 包装宽度（mm）
     */
    private Integer packageWidth;
}
