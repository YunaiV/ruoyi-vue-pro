package cn.iocoder.yudao.module.product.controller.admin.comment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 商品评价的商家回复 Request VO")
@Data
@ToString(callSuper = true)
public class ProductCommentReplyReqVO {

    @Schema(description = "评价编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15721")
    @NotNull(message = "评价编号不能为空")
    private Long id;

    @Schema(description = "商家回复内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "谢谢亲")
    @NotEmpty(message = "商家回复内容不能为空")
    private String replyContent;

}
