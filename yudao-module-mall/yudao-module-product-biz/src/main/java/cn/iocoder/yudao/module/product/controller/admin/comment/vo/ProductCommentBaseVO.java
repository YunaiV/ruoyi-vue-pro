package cn.iocoder.yudao.module.product.controller.admin.comment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class ProductCommentBaseVO {

    @Schema(description = "评价人", requiredMode = Schema.RequiredMode.REQUIRED, example = "16868")
    private Long userId;

    @Schema(description = "评价订单项", requiredMode = Schema.RequiredMode.REQUIRED, example = "19292")
    private Long orderItemId;

    @Schema(description = "评价人名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "小姑凉")
    @NotNull(message = "评价人名称不能为空")
    private String userNickname;

    @Schema(description = "评价人头像", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/xx.png")
    @NotNull(message = "评价人头像不能为空")
    private String userAvatar;

    @Schema(description = "商品 SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "商品 SKU 编号不能为空")
    private Long skuId;

    @Schema(description = "描述星级 1-5 分", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @NotNull(message = "描述星级不能为空")
    private Integer descriptionScores;

    @Schema(description = "服务星级 1-5 分", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @NotNull(message = "服务星级分不能为空")
    private Integer benefitScores;

    @Schema(description = "评论内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "穿起来非常丝滑凉快")
    @NotNull(message = "评论内容不能为空")
    private String content;

    @Schema(description = "评论图片地址数组，以逗号分隔最多上传 9 张", requiredMode = Schema.RequiredMode.REQUIRED, example = "[https://www.iocoder.cn/xx.png]")
    @Size(max = 9, message = "评论图片地址数组长度不能超过 9 张")
    private List<String> picUrls;

}
