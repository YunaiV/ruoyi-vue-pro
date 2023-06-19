package cn.iocoder.yudao.module.product.controller.admin.comment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 商品评价 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductCommentRespVO extends ProductCommentBaseVO {

    @Schema(description = "订单项编号", required = true, example = "24965")
    private Long id;

    @Schema(description = "是否匿名", required = true, example = "false")
    private Boolean anonymous;

    @Schema(description = "交易订单编号", required = true, example = "24428")
    private Long orderId;

    @Schema(description = "评价人 用户编号", required = true, example = "15721")
    private Long userId;

    @Schema(description = "交易订单项编号", required = true, example = "8233")
    private Long orderItemId;

    @Schema(description = "是否可见：[true:显示 false:隐藏]", required = true)
    private Boolean visible;

    @Schema(description = "商家是否回复：[1:回复 0:未回复]", required = true)
    private Boolean replyStatus;

    @Schema(description = "回复管理员编号", example = "22212")
    private Long replyUserId;

    @Schema(description = "商家回复内容")
    private String replyContent;

    @Schema(description = "商家回复时间")
    private LocalDateTime replyTime;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

}
