package cn.iocoder.yudao.module.product.controller.app.comment.vo;

import cn.iocoder.yudao.module.product.controller.app.property.vo.value.AppProductPropertyValueDetailRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "用户 App - 商品评价详情 Response VO")
@Data
@ToString(callSuper = true)
public class AppProductCommentRespVO {

    @Schema(description = "评价人的用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15721")
    private Long userId;

    @Schema(description = "评价人名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    private String userNickname;

    @Schema(description = "评价人头像", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/xx.png")
    private String userAvatar;

    @Schema(description = "订单项编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "24965")
    private Long id;

    @Schema(description = "是否匿名", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean anonymous;

    @Schema(description = "交易订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "24428")
    private Long orderId;

    @Schema(description = "交易订单项编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "8233")
    private Long orderItemId;

    @Schema(description = "商家是否回复", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean replyStatus;

    @Schema(description = "回复管理员编号", example = "22212")
    private Long replyUserId;

    @Schema(description = "商家回复内容", example = "亲，你的好评就是我的动力(*^▽^*)")
    private String replyContent;

    @Schema(description = "商家回复时间")
    private LocalDateTime replyTime;

    @Schema(description = "追加评价内容", example = "穿了很久都很丝滑诶")
    private String additionalContent;

    @Schema(description = "追评评价图片地址数组，以逗号分隔最多上传 9 张", example = "[https://www.iocoder.cn/xx.png, https://www.iocoder.cn/xxx.png]")
    private List<String> additionalPicUrls;

    @Schema(description = "追加评价时间")
    private LocalDateTime additionalTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "商品 SPU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "91192")
    @NotNull(message = "商品 SPU 编号不能为空")
    private Long spuId;

    @Schema(description = "商品 SPU 名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "清凉丝滑小短袖")
    @NotNull(message = "商品 SPU 名称不能为空")
    private String spuName;

    @Schema(description = "商品 SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "81192")
    @NotNull(message = "商品 SKU 编号不能为空")
    private Long skuId;

    @Schema(description = "商品 SKU 属性", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<AppProductPropertyValueDetailRespVO> skuProperties;

    @Schema(description = "评分星级 1-5 分", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @NotNull(message = "评分星级 1-5 分不能为空")
    private Integer scores;

    @Schema(description = "描述星级 1-5 分", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @NotNull(message = "描述星级 1-5 分不能为空")
    private Integer descriptionScores;

    @Schema(description = "服务星级 1-5 分", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @NotNull(message = "服务星级 1-5 分不能为空")
    private Integer benefitScores;

    @Schema(description = "评论内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "哇，真的很丝滑凉快诶，好评")
    @NotNull(message = "评论内容不能为空")
    private String content;

    @Schema(description = "评论图片地址数组，以逗号分隔最多上传 9 张", requiredMode = Schema.RequiredMode.REQUIRED, example = "[https://www.iocoder.cn/xx.png]")
    @Size(max = 9, message = "评论图片地址数组长度不能超过 9 张")
    private List<String> picUrls;

}
