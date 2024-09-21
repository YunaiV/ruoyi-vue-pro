package cn.iocoder.yudao.module.promotion.controller.admin.point.vo.product;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 积分商城商品 Response VO")
@Data
@ExcelIgnoreUnannotated
public class PointProductRespVO {

    @Schema(description = "积分商城商品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31718")
    @ExcelProperty("积分商城商品编号")
    private Long id;

    @Schema(description = "积分商城活动 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "29388")
    @ExcelProperty("积分商城活动 id")
    private Long activityId;

    @Schema(description = "商品 SPU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "8112")
    @ExcelProperty("商品 SPU 编号")
    private Long spuId;

    @Schema(description = "商品 SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2736")
    @ExcelProperty("商品 SKU 编号")
    private Long skuId;

    @Schema(description = "可兑换数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "3926")
    @ExcelProperty("可兑换数量")
    private Integer maxCount;

    @Schema(description = "兑换积分", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("兑换积分")
    private Integer point;

    @Schema(description = "兑换金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "15860")
    @ExcelProperty("兑换金额，单位：分")
    private Integer price;

    @Schema(description = "兑换类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("兑换类型")
    private Integer type;

    @Schema(description = "积分商城商品状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("积分商城商品状态")
    private Integer activityStatus;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}