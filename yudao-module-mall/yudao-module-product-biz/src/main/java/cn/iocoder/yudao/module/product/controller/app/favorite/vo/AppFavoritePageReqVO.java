package cn.iocoder.yudao.module.product.controller.app.favorite.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.product.enums.favorite.ProductFavoriteTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * @author jason
 */
@Schema(description = "用户APP - 喜爱商品分页查询 Request VO")
@Data
public class AppFavoritePageReqVO extends PageParam {

    @Schema(description = "类型 1:收藏 2：点赞", requiredMode = REQUIRED, example = "1")
    @NotNull(message = "类型不能为空")
    @InEnum(ProductFavoriteTypeEnum.class)
    private Integer type;
}
