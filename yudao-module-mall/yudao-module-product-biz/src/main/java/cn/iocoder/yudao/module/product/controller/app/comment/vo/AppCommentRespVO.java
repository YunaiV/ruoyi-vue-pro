package cn.iocoder.yudao.module.product.controller.app.comment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "用户APP - 商品评价 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppCommentRespVO extends AppCommentBaseVO {

    @Schema(description = "评价人 用户编号", required = true, example = "15721")
    private Long userId;

    @Schema(description = "评价人名称", required = true, example = "张三")
    private String userNickname;

    @Schema(description = "评价人头像", required = true)
    private String userAvatar;

    @Schema(description = "订单项编号", required = true, example = "24965")
    private Long id;

    @Schema(description = "是否匿名", required = true)
    private Boolean anonymous;

    @Schema(description = "交易订单编号", required = true, example = "24428")
    private Long orderId;

    @Schema(description = "交易订单项编号", required = true, example = "8233")
    private Long orderItemId;

    @Schema(description = "商家是否回复", required = true)
    private Boolean replied;

    @Schema(description = "回复管理员编号", example = "22212")
    private Long replyUserId;

    @Schema(description = "商家回复内容")
    private String replyContent;

    @Schema(description = "商家回复时间")
    private LocalDateTime replyTime;

    @Schema(description = "追加评价内容")
    private String additionalContent;

    @Schema(description = "追评评价图片地址数组，以逗号分隔最多上传9张")
    private List<String> additionalPicUrls;

    @Schema(description = "追加评价时间")
    private LocalDateTime additionalTime;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

    @Schema(description = "最终评分", required = true)
    private Integer finalScore;
}
