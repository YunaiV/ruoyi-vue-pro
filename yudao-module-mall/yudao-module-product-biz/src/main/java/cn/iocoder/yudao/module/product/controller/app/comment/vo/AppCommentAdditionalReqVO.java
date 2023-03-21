package cn.iocoder.yudao.module.product.controller.app.comment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "用户APP - 商品追加评价创建 Request VO")
@Data
@ToString(callSuper = true)
public class AppCommentAdditionalReqVO {

    @Schema(description = "评论编号", required = true)
    @NotNull(message = "评论编号不能为空")
    private Long id;

    @Schema(description = "追加评价内容", required = true)
    @NotNull(message = "追加评价内容不能为空")
    private String additionalContent;

    @Schema(description = "追评评价图片地址数组，以逗号分隔最多上传9张", required = true)
    @NotNull(message = "追评评价图片地址数组，以逗号分隔最多上传9张不能为空")
    private List<String> additionalPicUrls;

}
