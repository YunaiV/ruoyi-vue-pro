package cn.iocoder.yudao.module.erp.controller.admin.product.vo.product;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.json.GuidePriceJson;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - ERP 产品 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpProductRespVO extends BaseDO {

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "731")
    @ExcelProperty("产品编号")
    private Long id;

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty("产品名称")
    private String name;

    @Schema(description = "产品分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "30796")
    @ExcelProperty("产品分类编号")
    private Long categoryId;

    @Schema(description = "产品分类名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("产品分类名称")
    private String categoryName;

    @Schema(description = "部门id", requiredMode = Schema.RequiredMode.REQUIRED, example = "8369")
    @ExcelProperty("部门id")
    private Long deptId;

    @Schema(description = "部门名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "研发部")
    @ExcelProperty("部门名称")
    private String deptName;

    @Schema(description = "SKU（编码）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("SKU（编码）")
    private String barCode;

    @Schema(description = "单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "30975")
    @ExcelProperty("单位编号")
    private Long unitId;

    @Schema(description = "单位名称")
    @ExcelProperty("单位名称")
    private String unitName;

    @Schema(description = "品牌", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("品牌")
    private String brand;

    @Schema(description = "材料（中文）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("材料（中文）")
    private String material;

    @Schema(description = "产品材质-关联海关分类")
    private Long customCategoryId;

    @Schema(description = "产品状态（1启用，0禁用）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("产品状态（1启用，0禁用）")
    private Boolean status;

    @Schema(description = "备注", example = "你说的对")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "基础重量（kg）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("基础重量（kg）")
    private BigDecimal weight;

    @Schema(description = "系列")
    @ExcelProperty("系列")
    private String series;

    @Schema(description = "颜色", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("颜色")
    private String color;

    @Schema(description = "型号")
    @ExcelProperty("型号")
    private String model;

    @Schema(description = "流水号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("流水号")
    private Integer serial;

    @Schema(description = "生产编号")
    @ExcelProperty("生产编号")
    private String productionNo;

    @Schema(description = "基础宽度（mm）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("基础宽度（mm）")
    private Integer width;

    @Schema(description = "基础长度（mm）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("基础长度（mm）")
    private Integer length;

    @Schema(description = "基础高度（mm）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("基础高度（mm）")
    private Integer height;

    @Schema(description = "主图", example = "https://www.iocoder.cn")
    @ExcelProperty("主图")
    private String primaryImageUrl;

    @Schema(description = "副图", requiredMode = Schema.RequiredMode.REQUIRED, example = "[https://www.iocoder.cn]")
    private List<String> secondaryImageUrlList;

    @Schema(description = "指导价，json格式", example = "19540")
    @ExcelProperty("指导价，json格式")
    private List<GuidePriceJson> guidePriceList;

    @Schema(description = "专利类型", example = "19540")
    @ExcelProperty("专利类型")
    private Integer patentType;

    @Schema(description = "专利国别代码", example = "19540")
    @ExcelProperty("专利国别代码")
    private List<Integer> patentCountryCodeList;

    @Schema(description = "PO产品经理id", example = "28770")
    @ExcelProperty("PO产品经理id")
    private Long productOwnerId;

    @Schema(description = "PO产品经理名称", example = "王五")
    @ExcelProperty("PO产品经理名称")
    private String productOwnerName;

    @Schema(description = "ID工业设计id", example = "27774")
    @ExcelProperty("ID工业设计id")
    private Long industrialDesignerId;

    @Schema(description = "ID工业设计名称", example = "王五")
    @ExcelProperty("ID工业设计名称")
    private String industrialDesignerName;

    @Schema(description = "RD研发工程师id", example = "10321")
    @ExcelProperty("RD研发工程师id")
    private Long researchDeveloperId;

    @Schema(description = "RD研发工程师名称", example = "王五")
    @ExcelProperty("RD研发工程师名称")
    private String researchDeveloperName;

    @Schema(description = "维护工程师id", example = "22163")
    @ExcelProperty("维护工程师id")
    private Long maintenanceEngineerId;

    @Schema(description = "维护工程师名称", example = "王五")
    @ExcelProperty("维护工程师名称")
    private String maintenanceEngineerName;

    @Schema(description = "设计说明", example = "你猜")
    @ExcelProperty("设计说明")
    private String description;

    @Schema(description = "VESA孔距最小宽度")
    @ExcelProperty("VESA孔距最小宽度")
    private Integer vesaWidthMin;

    @Schema(description = "VESA孔距最大宽度")
    @ExcelProperty("VESA孔距最大宽度")
    private Integer vesaWidthMax;

    @Schema(description = "VESA孔距最大长度")
    @ExcelProperty("VESA孔距最大长度")
    private Integer vesaLengthMax;

    @Schema(description = "VESA孔距最小长度")
    @ExcelProperty("VESA孔距最小长度")
    private Integer vesaLengthMin;

    @Schema(description = "电视尺寸最小值", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("电视尺寸最小值")
    private Integer tvSizeMin;

    @Schema(description = "电视尺寸最大值", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("电视尺寸最大值")
    private Integer tvSizeMax;

    @Schema(description = "承重")
    @ExcelProperty("承重")
    private Integer loadCapacity;

    @Schema(description = "中心高度最小值（最低高度）")
    @ExcelProperty("中心高度最小值（最低高度）")
    private Integer centerHeightMin;

    @Schema(description = "中心高度最大值（最高高度）")
    @ExcelProperty("中心高度最大值（最高高度）")
    private Integer centerHeightMax;

    @Schema(description = "电视旋转")
    @ExcelProperty("电视旋转")
    private String tvRotation;

    @Schema(description = "电视俯仰")
    @ExcelProperty("电视俯仰")
    private String tvTilt;

    @Schema(description = "高度调节")
    @ExcelProperty("高度调节")
    private String heightAdjustment;

    @Schema(description = "横竖屏旋转")
    @ExcelProperty("横竖屏旋转")
    private String horizontalScreenRotation;

    @Schema(description = "电缆管理")
    @ExcelProperty("电缆管理")
    private String cableManagement;

    @Schema(description = "收纳管理")
    @ExcelProperty("收纳管理")
    private String storageManagement;

    @Schema(description = "调节脚垫")
    @ExcelProperty("调节脚垫")
    private String adjustableFootPad;

    @Schema(description = "移动功能")
    @ExcelProperty("移动功能")
    private String mobileFunction;

    @Schema(description = "其他")
    @ExcelProperty("其他")
    private String otherFeatures;

    @Schema(description = "适配尺寸")
    @ExcelProperty("适配尺寸")
    private String adaptiveSize;

    @Schema(description = "兼容方式")
    @ExcelProperty("兼容方式")
    private String compatibilityMode;

    @Schema(description = "脚轮", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("脚轮")
    private Boolean casters;

    @Schema(description = "电子集成模块", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("电子集成模块")
    private Boolean electronicIntegrationModules;

    @Schema(description = "功能配件")
    @ExcelProperty("功能配件")
    private String functionalAccessories;

    @Schema(description = "包装长度（整数，没有小数点，单位mm）", example = "500")
    @ExcelProperty("包装长度")
    private Integer packageLength;

    @Schema(description = "包装宽度（整数，没有小数点，单位mm）", example = "300")
    @ExcelProperty("包装宽度")
    private Integer packageWidth;

    @Schema(description = "包装高度（整数，没有小数点，单位mm）", example = "200")
    @ExcelProperty("包装高度")
    private Integer packageHeight;

    @Schema(description = "包装重量（保留至小数点后两位，单位kg）", example = "12.50")
    @ExcelProperty("包装重量")
    private BigDecimal packageWeight;

}