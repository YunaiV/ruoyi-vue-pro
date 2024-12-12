package cn.iocoder.yudao.module.erp.controller.admin.product.vo.product;

import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.json.GuidePriceJson;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Administrator
 */
@Schema(description = "管理后台 - ERP 产品新增/修改 Request VO")
@Data
public class ErpProductSaveReqVO {

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "731")
    private Long id;

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotEmpty(message = "产品名称不能为空")
    private String name;

    @Schema(description = "产品分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "30796")
    @NotNull(message = "产品分类编号不能为空")
    private Long categoryId;

    @Schema(description = "部门id", requiredMode = Schema.RequiredMode.REQUIRED, example = "8369")
    @NotNull(message = "部门id不能为空")
    private Long deptId;

    @Schema(description = "SKU（编码）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "SKU（编码）不能为空")
    private String barCode;

    @Schema(description = "单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "30975")
    @NotNull(message = "单位编号不能为空")
    private Long unitId;

    @Schema(description = "材料（中文）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "材料（中文）不能为空")
    private String material;

    @Schema(description = "产品状态（1启用，0禁用）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "产品状态（1启用，0禁用）不能为空")
    private Boolean status;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

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
    private BigDecimal width;

    @Schema(description = "基础长度（mm）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "基础长度（mm）不能为空")
    private BigDecimal length;

    @Schema(description = "基础高度（mm）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "基础高度（mm）不能为空")
    private BigDecimal height;

    @Schema(description = "主图", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn")
    @NotBlank(message = "主图不能为空")
    private String primaryImageUrl;

    @Schema(description = "副图", requiredMode = Schema.RequiredMode.REQUIRED, example = "[https://www.iocoder.cn]")
    private List<String> secondaryImageUrlList;

    @Schema(description = "指导价，json格式", requiredMode = Schema.RequiredMode.REQUIRED, example = "16486")
    private List<@Valid GuidePriceJson> guidePriceList;

    @Schema(description = "专利")
    private String patent;

    @Schema(description = "PO产品经理id", example = "28770")
    private Long productOwnerId;

    @Schema(description = "ID工业设计id", example = "27774")
    private Long industrialDesignerId;

    @Schema(description = "RD研发工程师id", example = "10321")
    private Long researchDeveloperId;

    @Schema(description = "维护工程师id", example = "22163")
    private Long maintenanceEngineerId;

    @Schema(description = "层板承重")
    private BigDecimal shelfLoadCapacity;

    @Schema(description = "层板数量", example = "352")
    private Integer shelvesCount;

    @Schema(description = "电视调节方式")
    private String tvAdjustmentMethod;

    @Schema(description = "层板调节方式")
    private String shelfAdjustmentMethod;

    @Schema(description = "设计说明", example = "你说的对")
    private String description;

    @Schema(description = "宽度最大值")
    private BigDecimal widthMax;

    @Schema(description = "宽度最小值")
    private BigDecimal widthMin;

    @Schema(description = "长度最大值")
    private BigDecimal lengthMax;

    @Schema(description = "长度最小值")
    private BigDecimal lengthMin;

}