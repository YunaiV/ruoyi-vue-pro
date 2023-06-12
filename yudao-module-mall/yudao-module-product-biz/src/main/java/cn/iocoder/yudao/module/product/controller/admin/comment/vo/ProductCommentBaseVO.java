package cn.iocoder.yudao.module.product.controller.admin.comment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class ProductCommentBaseVO {

    @Schema(description = "评价人名称", required = true, example = "小姑凉")
    @NotNull(message = "评价人名称不能为空")
    private String userNickname;

    @Schema(description = "评价人头像", required = true, example = "https://www.iocoder.cn/xx.png")
    @NotNull(message = "评价人头像不能为空")
    private String userAvatar;

    @Schema(description = "商品 SPU 编号", required = true, example = "清凉丝滑透气小短袖")
    @NotNull(message = "商品 SPU 编号不能为空")
    private Long spuId;

    @Schema(description = "商品 SPU 名称", required = true, example = "赵六")
    @NotNull(message = "商品 SPU 名称不能为空")
    private String spuName;

    @Schema(description = "商品 SKU 编号", required = true, example = "1")
    @NotNull(message = "商品 SKU 编号不能为空")
    private Long skuId;

    @Schema(description = "评分星级 1-5分", required = true, example = "5")
    @NotNull(message = "评分星级不能为空")
    private Integer scores;

    @Schema(description = "描述星级 1-5分", required = true, example = "5")
    @NotNull(message = "描述星级不能为空")
    private Integer descriptionScores;

    @Schema(description = "服务星级 1-5分", required = true, example = "5")
    @NotNull(message = "服务星级分不能为空")
    private Integer benefitScores;

    @Schema(description = "评论内容", required = true, example = "穿起来非常丝滑凉快")
    @NotNull(message = "评论内容不能为空")
    private String content;

    @Schema(description = "评论图片地址数组，以逗号分隔最多上传9张", required = true, example = "[https://www.iocoder.cn/xx.png, https://www.iocoder.cn/xxx.png]")
    @Size(max = 9, message = "评论图片地址数组长度不能超过9张")
    private List<String> picUrls;

}
