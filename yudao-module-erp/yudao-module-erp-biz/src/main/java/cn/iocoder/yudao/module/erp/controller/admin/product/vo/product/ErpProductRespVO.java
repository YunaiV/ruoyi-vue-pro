package cn.iocoder.yudao.module.erp.controller.admin.product.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - ERP 产品 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpProductRespVO {

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

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

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

    @Schema(description = "材料（中文）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("材料（中文）")
    private String material;

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
    private BigDecimal width;

    @Schema(description = "基础长度（mm）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("基础长度（mm）")
    private BigDecimal length;

    @Schema(description = "基础高度（mm）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("基础高度（mm）")
    private BigDecimal height;

    @Schema(description = "图片url", example = "https://www.iocoder.cn")
    @ExcelProperty("图片url")
    private String imageUrl;

    @Schema(description = "指导价，json格式", example = "19540")
    @ExcelProperty("指导价，json格式")
    private String guidePrice;

    @Schema(description = "专利")
    @ExcelProperty("专利")
    private String patent;

    @Schema(description = "PO产品经理id", example = "28770")
    @ExcelProperty("PO产品经理id")
    private Long productOwnerId;

    @Schema(description = "PO产品经理名称", example = "王五")
    @ExcelProperty("PO产品经理名称")
    private String productManagerName;

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

    @Schema(description = "层板承重")
    @ExcelProperty("层板承重")
    private BigDecimal shelfLoadCapacity;

    @Schema(description = "层板数量", example = "352")
    @ExcelProperty("层板数量")
    private Integer shelvesCount;

    @Schema(description = "电视调节方式")
    @ExcelProperty("电视调节方式")
    private String tvAdjustmentMethod;

    @Schema(description = "层板调节方式")
    @ExcelProperty("层板调节方式")
    private String shelfAdjustmentMethod;

    @Schema(description = "设计说明", example = "你说的对")
    @ExcelProperty("设计说明")
    private String description;

    @Schema(description = "宽度最大值")
    @ExcelProperty("宽度最大值")
    private BigDecimal widthMax;

    @Schema(description = "宽度最小值")
    @ExcelProperty("宽度最小值")
    private BigDecimal widthMin;

    @Schema(description = "长度最大值")
    @ExcelProperty("长度最大值")
    private BigDecimal lengthMax;

    @Schema(description = "长度最小值")
    @ExcelProperty("长度最小值")
    private BigDecimal lengthMin;

}