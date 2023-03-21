package cn.iocoder.yudao.module.product.controller.app.comment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "用户APP - 商品评价创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppCommentCreateReqVO extends AppCommentBaseVO {

    @Schema(description = "是否匿名 true:是 false:否", required = true, example = "true")
    @NotNull(message = "是否匿名不能为空")
    private Boolean anonymous;

    @Schema(description = "交易订单编号", required = true, example = "12312")
    @NotNull(message = "交易订单编号不能为空")
    private Long orderId;

    @Schema(description = "交易订单项编号", required = true, example = "2312312")
    @NotNull(message = "交易订单项编号不能为空")
    private Long orderItemId;

}
