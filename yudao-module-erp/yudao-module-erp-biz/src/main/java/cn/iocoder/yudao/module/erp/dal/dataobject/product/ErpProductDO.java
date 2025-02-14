package cn.iocoder.yudao.module.erp.dal.dataobject.product;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * ERP 产品 DO
 *
 * @author 王奇辉
 */
@TableName("erp_product")
@KeySequence("erp_product_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpProductDO extends TenantBaseDO {

    /**
     * 产品编号
     */
    @TableId
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
}