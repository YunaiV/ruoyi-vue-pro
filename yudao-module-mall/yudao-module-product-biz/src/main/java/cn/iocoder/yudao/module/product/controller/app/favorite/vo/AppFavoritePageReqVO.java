package cn.iocoder.yudao.module.product.controller.app.favorite.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.product.enums.favorite.ProductFavoriteTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "用户APP - 商品收藏分页查询 Request VO")
@Data
public class AppFavoritePageReqVO extends PageParam {

    @Schema(description = "类型", requiredMode = REQUIRED, example = "1")
    @NotNull(message = "类型不能为空")
    @InEnum(ProductFavoriteTypeEnum.class)
    private Integer type;

}
