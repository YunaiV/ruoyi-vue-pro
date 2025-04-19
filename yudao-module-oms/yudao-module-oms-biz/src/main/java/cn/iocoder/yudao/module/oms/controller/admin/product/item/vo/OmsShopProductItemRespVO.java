package cn.iocoder.yudao.module.oms.controller.admin.product.item.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - OMS 店铺产品项 Response VO")
@Data
@ExcelIgnoreUnannotated
public class OmsShopProductItemRespVO {

    @Schema(description = "店铺产品项编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15443")
    @ExcelProperty("店铺产品项编号")
    private Long id;

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "27593")
    @ExcelProperty("产品编号")
    private Long productId;

    @Schema(description = "产品数量", example = "27593")
    @ExcelProperty("产品数量")
    private Long qty;

    @Schema(description = "关联产品", requiredMode = Schema.RequiredMode.REQUIRED, example = "27593")
    @ExcelProperty("关联产品")
    private OmsProductRespSimpleVO product;

    @Schema(description = "店铺产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "27593")
    @ExcelProperty("店铺产品编号")
    private Long shopProductId;

    @Schema(description = "备注", example = "你说的对")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}