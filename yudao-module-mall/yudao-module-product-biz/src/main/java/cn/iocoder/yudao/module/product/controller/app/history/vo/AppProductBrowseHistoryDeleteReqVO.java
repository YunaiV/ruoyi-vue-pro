package cn.iocoder.yudao.module.product.controller.app.history.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "用户 APP - 删除商品浏览记录的 Request VO")
@Data
public class AppProductBrowseHistoryDeleteReqVO {

    @Schema(description = "商品 SPU 编号数组", requiredMode = REQUIRED, example = "29502")
    @NotEmpty(message = "商品 SPU 编号数组不能为空")
    private List<Long> spuIds;

}
