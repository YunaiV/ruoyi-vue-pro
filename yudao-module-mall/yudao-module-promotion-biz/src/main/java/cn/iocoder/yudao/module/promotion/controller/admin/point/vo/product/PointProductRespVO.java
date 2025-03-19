package cn.iocoder.yudao.module.promotion.controller.admin.point.vo.product;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 积分商城商品 Response VO")
@Data
@ExcelIgnoreUnannotated
public class PointProductRespVO {

    @Schema(description = "积分商城商品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31718")
    private Long id;

    @Schema(description = "积分商城活动编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "29388")
    private Long activityId;

    @Schema(description = "商品 SPU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "8112")
    private Long spuId;

    @Schema(description = "商品 SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2736")
    private Long skuId;

    @Schema(description = "可兑换数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "3926")
    private Integer count;

    @Schema(description = "兑换积分", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer point;

    @Schema(description = "兑换金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "15860")
    private Integer price;

    @Schema(description = "积分商城商品库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer stock;

    @Schema(description = "积分商城商品状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer activityStatus;

}