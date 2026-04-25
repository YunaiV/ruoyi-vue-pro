package cn.iocoder.yudao.module.product.controller.app.review.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;
import java.util.List;

@Schema(description = "用户 App - 提交可信评价 Request VO")
@Data
public class AppTrustworthyReviewSubmitReqVO {

    @Schema(description = "商品 SPU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotNull(message = "商品 SPU 编号不能为空")
    private Long spuId;

    @Schema(description = "交易订单编号（填写后提升可信度）", example = "20240101001")
    private Long orderId;

    @Schema(description = "订单项编号", example = "1")
    private Long orderItemId;

    @Schema(description = "评分（1-5 星）", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低 1 星")
    @Max(value = 5, message = "评分最高 5 星")
    private Integer scores;

    @Schema(description = "评价内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "面料很好，版型合适")
    @NotBlank(message = "评价内容不能为空")
    private String content;

    @Schema(description = "评价图片地址列表")
    private List<String> picUrls;

    @Schema(description = "是否匿名", example = "false")
    private Boolean anonymous;

}
