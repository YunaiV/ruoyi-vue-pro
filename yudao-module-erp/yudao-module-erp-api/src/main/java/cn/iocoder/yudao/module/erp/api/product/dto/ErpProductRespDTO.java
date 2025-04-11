package cn.iocoder.yudao.module.erp.api.product.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpProductRespDTO{

    /**
     * 产品编号
     */
    private Long id;

    /**
     * 产品名称
     */
    private String name;

    /**
     * 产品分类编号
     */
    private Long categoryId;

    /**
     * 产品分类名称
     */
    private String categoryName;

    /**
     * 部门id
     */
    private Long deptId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * sku（编码）
     */
    private String barCode;

    /**
     * 单位编号
     */
    private Long unitId;

    /**
     * 单位名称
     */
    private String unitName;

    /**
     * 品牌
     */
    private String brand;

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
     * 主图
     */
    private String primaryImageUrl;

    /**
     * 副图
     */
    private List<String> secondaryImageUrlList;

    /**
     * 指导价，json格式
     */
    private List<GuidePriceJsonDTO> guidePriceList;

    /**
     * 专利类型
     */
    private Integer patentType;

    /**
     * 专利国别代码
     */
    private List<Integer> patentCountryCodeList;

    /**
     * PO产品经理id
     */
    private Long productOwnerId;

    /**
     * PO产品经理名称
     */
    private String productOwnerName;


    /**
     * ID工业设计id
     */
    private Long industrialDesignerId;

    /**
     * ID工业设计名称
     */
    private String industrialDesignerName;

    /**
     * RD研发工程师id
     */
    private Long researchDeveloperId;

    /**
     * RD研发工程师名称
     */
    private String researchDeveloperName;

    /**
     * 维护工程师id
     */
    private Long maintenanceEngineerId;

    /**
     * 维护工程师名称
     */
    private String maintenanceEngineerName;

    /**
     * 设计说明
     */
    private String description;

    /**
     * VESA孔距最小宽度
     */
    private Integer vesaWidthMin;

    /**
     * VESA孔距最大宽度
     */
    private Integer vesaWidthMax;

    /**
     * VESA孔距最大长度
     */
    private Integer vesaLengthMax;

    /**
     * VESA孔距最小长度
     */
    private Integer vesaLengthMin;

    /**
     * 电视尺寸最小值
     */
    private Integer tvSizeMin;

    /**
     * 电视尺寸最大值
     */
    private Integer tvSizeMax;

    /**
     * 承重
     */
    private Integer loadCapacity;

    /**
     * 中心高度最小值（最低高度）
     */
    private Integer centerHeightMin;

    /**
     * 中心高度最大值（最高高度）
     */
    private Integer centerHeightMax;

    /**
     * 电视旋转
     */
    private String tvRotation;

    /**
     * 电视俯仰
     */
    private String tvTilt;


    /**
     * 高度调节
     */
    private String heightAdjustment;

    /**
     * 横竖屏旋转
     */
    private String horizontalScreenRotation;

    /**
     * 电缆管理
     */
    private String cableManagement;

    /**
     * 收纳管理
     */
    private String storageManagement;

    /**
     * 调节脚垫
     */
    private String adjustableFootPad;

    /**
     * 移动功能
     */
    private String mobileFunction;

    /**
     * 其他
     */
    private String otherFeatures;

    /**
     * 适配尺寸
     */
    private String adaptiveSize;

    /**
     * 兼容方式
     */
    private String compatibilityMode;

    /**
     * 脚轮
     */
    private Boolean casters;

    /**
     * 电子集成模块
     */
    private Boolean electronicIntegrationModules;

    /**
     * 功能配件
     */
    private String functionalAccessories;

    /**
     * 包装长度
     */
    private Integer packageLength;

    /**
     * 包装宽度
     */
    private Integer packageWidth;

    /**
     * 包装高度
     */
    private Integer packageHeight;

    /**
     * 包装重量
     */
    private BigDecimal packageWeight;

}