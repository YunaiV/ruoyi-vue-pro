package cn.iocoder.yudao.module.product.controller.app.favorite.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "用户 APP - 商品收藏的批量 Request VO") // 用于收藏、取消收藏、获取收藏
@Data
public class AppFavoriteBatchReqVO {

    @Schema(description = "商品 SPU 编号数组", requiredMode = REQUIRED, example = "29502")
    @NotEmpty(message = "商品 SPU 编号数组不能为空")
    private List<Long> spuIds;

}
