package cn.iocoder.yudao.module.erp.controller.admin.product.vo.product;

import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.json.GuidePriceJson;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Administrator
 */
@Schema(description = "管理后台 - ERP 产品新增/修改 Request VO")
@Data
public class ErpProductSaveReqVO {

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotEmpty(message = "产品名称不能为空")
    private String name;

    @Schema(description = "产品分类编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "产品分类编号不能为空")
    private Long categoryId;

    @Schema(description = "部门id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "部门id不能为空")
    private Long deptId;

    @Schema(description = "SKU（编码）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "SKU（编码）不能为空")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "SKU（编码）只能包含大写字母、数字、中划线")
    private String barCode;

    @Schema(description = "单位编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "单位编号不能为空")
    private Long unitId;

    @Schema(description = "材料（中文）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "材料（中文）不能为空")
    private String material;

    @Schema(description = "产品材质-关联海关分类")
    private Long customCategoryId;

    @Schema(description = "产品状态（1启用，0禁用）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "产品状态（1启用，0禁用）不能为空")
    private Boolean status;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

    @Schema(description = "品牌",example = "you")
    private String brand;

    @Schema(description = "基础重量（kg）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "基础重量（kg）不能为空")
    private BigDecimal weight;

    @Schema(description = "系列")
    private String series;

    @Schema(description = "颜色", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "颜色不能为空")
    private String color;

    @Schema(description = "型号")
    private String model;

    @Schema(description = "流水号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer serial;

    @Schema(description = "生产编号")
    private String productionNo;

    @Schema(description = "基础宽度（mm）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "基础宽度（mm）不能为空")
    private Integer width;

    @Schema(description = "基础长度（mm）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "基础长度（mm）不能为空")
    private Integer length;

    @Schema(description = "基础高度（mm）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "基础高度（mm）不能为空")
    private Integer height;

    @Schema(description = "主图", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "主图不能为空")
    private String primaryImageUrl;

    @Schema(description = "副图", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> secondaryImageUrlList;

    @Schema(description = "指导价，json格式", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<@Valid GuidePriceJson> guidePriceList;

    @Schema(description = "专利类型")
    private Integer patentType;

    @Schema(description = "专利国别代码")
    private List<Integer> patentCountryCodeList;

    @Schema(description = "PO产品经理id")
    private Long productOwnerId;

    @Schema(description = "ID工业设计id")
    private Long industrialDesignerId;

    @Schema(description = "RD研发工程师id")
    private Long researchDeveloperId;

    @Schema(description = "维护工程师id")
    private Long maintenanceEngineerId;

    @Schema(description = "VESA孔距最小宽度")
    private Integer vesaWidthMin;

    @Schema(description = "VESA孔距最大宽度")
    private Integer vesaWidthMax;

    @Schema(description = "VESA孔距最大长度")
    private Integer vesaLengthMax;

    @Schema(description = "VESA孔距最小长度")
    private Integer vesaLengthMin;

    @Schema(description = "电视尺寸最小值", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer tvSizeMin;

    @Schema(description = "电视尺寸最大值", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer tvSizeMax;

    @Schema(description = "承重")
    private Integer loadCapacity;

    @Schema(description = "中心高度最小值（最低高度）")
    private Integer centerHeightMin;

    @Schema(description = "中心高度最大值（最高高度）")
    private Integer centerHeightMax;

    @Schema(description = "电视旋转")
    private String tvRotation;

    @Schema(description = "电视俯仰")
    private String tvTilt;

    @Schema(description = "高度调节")
    private String heightAdjustment;

    @Schema(description = "横竖屏旋转")
    private String horizontalScreenRotation;

    @Schema(description = "电缆管理")
    private String cableManagement;

    @Schema(description = "收纳管理")
    private String storageManagement;

    @Schema(description = "调节脚垫")
    private String adjustableFootPad;

    @Schema(description = "移动功能")
    private String mobileFunction;

    @Schema(description = "其他")
    private String otherFeatures;

    @Schema(description = "适配尺寸")
    private String adaptiveSize;

    @Schema(description = "兼容方式")
    private String compatibilityMode;

    @Schema(description = "脚轮", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean casters;

    @Schema(description = "电子集成模块", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean electronicIntegrationModules;

    @Schema(description = "功能配件")
    private String functionalAccessories;

    @Schema(description = "包装长度（整数，没有小数点，单位mm，必须为正数）", example = "500")
    @Min(value = 1, message = "包装长度必须为正整数")
    @Max(value = 1000000, message = "包装长度不能超过1000000mm")
    @NotNull(message = "包装长度不能为空")
    private Integer packageLength;

    @Schema(description = "包装宽度（整数，没有小数点，单位mm，必须为正数）", example = "300")
    @Min(value = 1, message = "包装宽度必须为正整数")
    @Max(value = 1000000, message = "包装宽度不能超过1000000mm")
    @NotNull(message = "包装宽度不能为空")
    private Integer packageWidth;

    @Schema(description = "包装高度（整数，没有小数点，单位mm，必须为正数）", example = "200")
    @Min(value = 1, message = "包装高度必须为正整数")
    @Max(value = 1000000, message = "包装高度不能超过1000000mm")
    @NotNull(message = "包装高度不能为空")
    private Integer packageHeight;

    @Schema(description = "包装重量（保留至小数点后两位，单位kg，必须为非负数）", example = "12.50")
    @DecimalMin(value = "0.00", message = "包装重量必须为非负数")
    @DecimalMax(value = "10000.00", message = "包装重量不能超过10000kg")
    @Digits(integer = 5, fraction = 2, message = "包装重量最多5位整数和2位小数")
    @NotNull(message = "包装重量不能为空")
    private BigDecimal packageWeight;
}