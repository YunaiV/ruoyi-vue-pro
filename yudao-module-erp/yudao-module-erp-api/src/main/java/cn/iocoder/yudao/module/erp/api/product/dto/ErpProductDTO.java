package cn.iocoder.yudao.module.erp.api.product.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @date: 2025/1/3 8:31
 * @Version: 1.0
 * @description:
 */
@Data
@Builder
@SuppressWarnings("ALL")
public class ErpProductDTO {
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
    private String barCode;
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
     * {@link cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.ErpCustomCategoryDO::getId()}
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
     * 指导价，json格式
     */
    private String guidePrices;
    /**
     * 专利国别
     */
    private String patentCountryCodes;
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

    /**
     * 多租户编号
     */
    private Long tenantId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 最后更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 创建者，目前使用 SysUser 的 id 编号
     * <p>
     * 使用 String 类型的原因是，未来可能会存在非数值的情况，留好拓展性。
     */
    private String creator;
    /**
     * 更新者，目前使用 SysUser 的 id 编号
     * <p>
     * 使用 String 类型的原因是，未来可能会存在非数值的情况，留好拓展性。
     */
    private String updater;
    /**
     * 是否删除
     */
    private Boolean deleted;

}
