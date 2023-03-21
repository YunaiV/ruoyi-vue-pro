package cn.iocoder.yudao.module.product.controller.app.comment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class AppCommentBaseVO {

    @Schema(description = "商品SPU编号", required = true, example = "29502")
    @NotNull(message = "商品SPU编号不能为空")
    private Long spuId;

    @Schema(description = "商品SPU名称", required = true, example = "赵六")
    @NotNull(message = "商品SPU名称不能为空")
    private String spuName;

    @Schema(description = "商品SKU编号", required = true, example = "3082")
    @NotNull(message = "商品SKU编号不能为空")
    private Long skuId;

    @Schema(description = "评分星级 1-5分", required = true)
    @NotNull(message = "评分星级 1-5分不能为空")
    private Integer scores;

    @Schema(description = "描述星级 1-5分", required = true)
    @NotNull(message = "描述星级 1-5分不能为空")
    private Integer descriptionScores;

    @Schema(description = "服务星级 1-5分", required = true)
    @NotNull(message = "服务星级 1-5分不能为空")
    private Integer benefitScores;

    @Schema(description = "配送星级 1-5分", required = true)
    @NotNull(message = "配送星级 1-5分不能为空")
    private Integer deliveryScores;

    @Schema(description = "评论内容", required = true)
    @NotNull(message = "评论内容不能为空")
    private String content;

    @Schema(description = "评论图片地址数组，以逗号分隔最多上传9张", required = true)
    @NotNull(message = "评论图片地址数组，以逗号分隔最多上传9张不能为空")
    private List<String> picUrls;

}
