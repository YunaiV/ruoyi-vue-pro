package cn.iocoder.yudao.module.trade.controller.app.order.vo.item;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Schema(description = "用户 App - 商品评价创建 Request VO")
@Data
public class AppTradeOrderItemCommentCreateReqVO {

    @Schema(description = "是否匿名", required = true, example = "true")
    @NotNull(message = "是否匿名不能为空")
    private Boolean anonymous;

    @Schema(description = "交易订单项编号", required = true, example = "2312312")
    @NotNull(message = "交易订单项编号不能为空")
    private Long orderItemId;

    // TODO @puhui：spuId、spuName、skuId 直接查询出来；
    @Schema(description = "商品SPU编号", required = true, example = "29502")
    @NotNull(message = "商品SPU编号不能为空")
    private Long spuId;

    @Schema(description = "商品SPU名称", required = true, example = "丝滑飘逸小短裙")
    @NotNull(message = "商品SPU名称不能为空")
    private String spuName;

    @Schema(description = "商品SKU编号", required = true, example = "3082")
    @NotNull(message = "商品SKU编号不能为空")
    private Long skuId;

    @Schema(description = "评分星级 1-5 分", required = true, example = "5")
    @NotNull(message = "评分星级 1-5 分不能为空")
    private Integer scores;

    @Schema(description = "描述星级 1-5 分", required = true, example = "5")
    @NotNull(message = "描述星级 1-5 分不能为空")
    private Integer descriptionScores;

    @Schema(description = "服务星级 1-5 分", required = true, example = "5")
    @NotNull(message = "服务星级 1-5 分不能为空")
    private Integer benefitScores;

    @Schema(description = "评论内容", required = true, example = "穿身上很漂亮诶(*^▽^*)")
    @NotNull(message = "评论内容不能为空")
    private String content;

    @Schema(description = "评论图片地址数组，以逗号分隔最多上传 9 张", required = true, example = "[https://www.iocoder.cn/xx.png, https://www.iocoder.cn/xx.png]")
    @Size(max = 9, message = "评论图片地址数组长度不能超过 9 张")
    private List<String> picUrls;

    @Schema(description = "评价人名称", required = true, example = "小姑凉")
    @NotNull(message = "评价人名称不能为空")
    private String userNickname;

    // TODO @puhui：userAvatar、userAvatar、userId 直接查询出来；

    @Schema(description = "评价人头像", required = true, example = "https://www.iocoder.cn/xx.png")
    @NotNull(message = "评价人头像不能为空")
    private String userAvatar;

    @Schema(description = "评价人", required = true, example = "16868")
    @NotNull(message = "评价人不能为空")
    private Long userId;

}
