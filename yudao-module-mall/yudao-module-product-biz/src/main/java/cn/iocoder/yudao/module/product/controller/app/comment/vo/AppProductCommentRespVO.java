package cn.iocoder.yudao.module.product.controller.app.comment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "用户APP - 商品评价 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppProductCommentRespVO extends AppProductCommentBaseVO {

    @Schema(description = "评价人的用户编号", required = true, example = "15721")
    private Long userId;

    @Schema(description = "评价人名称", required = true, example = "张三")
    private String userNickname;

    @Schema(description = "评价人头像", required = true, example = "https://www.iocoder.cn/xx.png")
    private String userAvatar;

    @Schema(description = "订单项编号", required = true, example = "24965")
    private Long id;

    @Schema(description = "是否匿名", required = true, example = "false")
    private Boolean anonymous;

    @Schema(description = "交易订单编号", required = true, example = "24428")
    private Long orderId;

    @Schema(description = "交易订单项编号", required = true, example = "8233")
    private Long orderItemId;

    @Schema(description = "商家是否回复", required = true, example = "true")
    private Boolean replyStatus;

    @Schema(description = "回复管理员编号", example = "22212")
    private Long replyUserId;

    @Schema(description = "商家回复内容", example = "亲，你的好评就是我的动力(*^▽^*)")
    private String replyContent;

    @Schema(description = "商家回复时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime replyTime;

    @Schema(description = "追加评价内容", example = "穿了很久都很丝滑诶")
    private String additionalContent;

    @Schema(description = "追评评价图片地址数组，以逗号分隔最多上传9张", example = "[https://www.iocoder.cn/xx.png, https://www.iocoder.cn/xxx.png]")
    private List<String> additionalPicUrls;

    @Schema(description = "追加评价时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime additionalTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime createTime;

}
