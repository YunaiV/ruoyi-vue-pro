package cn.iocoder.yudao.module.product.controller.app.comment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Schema(description = "用户APP - 商品评价创建 Request VO")
@Data
@ToString(callSuper = true)
public class AppProductCommentCreateReqVO {

    @Schema(description = "是否匿名", required = true, example = "true")
    @NotNull(message = "是否匿名不能为空")
    private Boolean anonymous;

    @Schema(description = "交易订单项编号", required = true, example = "2312312")
    @NotNull(message = "交易订单项编号不能为空")
    private Long orderItemId;

    @Schema(description = "商品 SPU 编号", required = true, example = "91192")
    @NotNull(message = "商品SPU编号不能为空")
    private Long spuId;

    @Schema(description = "商品 SPU 名称", required = true, example = "清凉丝滑小短袖")
    @NotNull(message = "商品SPU名称不能为空")
    private String spuName;

    @Schema(description = "商品 SKU 编号", required = true, example = "81192")
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

    @Schema(description = "评论内容", required = true, example = "哇，真的很丝滑凉快诶，好评")
    @NotNull(message = "评论内容不能为空")
    private String content;

    @Schema(description = "评论图片地址数组，以逗号分隔最多上传 9 张", required = true, example = "[https://www.iocoder.cn/xx.png, https://www.iocoder.cn/xxx.png]")
    @Size(max = 9, message = "评论图片地址数组长度不能超过 9 张")
    private List<String> picUrls;

}
