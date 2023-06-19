package cn.iocoder.yudao.module.product.controller.admin.comment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 商品评价创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductCommentCreateReqVO extends ProductCommentBaseVO {

    // TODO @puhui999：是不是也放到父类里？
    @Schema(description = "评价人", required = true, example = "16868")
    @NotNull(message = "评价人不能为空")
    private Long userId;

    @Schema(description = "评价订单项", required = true, example = "19292")
    @NotNull(message = "评价订单项不能为空")
    private Long orderItemId;

}
