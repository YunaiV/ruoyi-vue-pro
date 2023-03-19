package cn.iocoder.yudao.module.product.controller.admin.comment.vo;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理后台 - 商品评价创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductCommentCreateReqVO extends ProductCommentBaseVO {

}
