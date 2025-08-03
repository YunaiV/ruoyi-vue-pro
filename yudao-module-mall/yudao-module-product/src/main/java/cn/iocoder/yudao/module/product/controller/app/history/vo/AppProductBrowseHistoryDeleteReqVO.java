package cn.iocoder.yudao.module.product.controller.app.history.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "用户 APP - 删除商品浏览记录的 Request VO")
@Data
public class AppProductBrowseHistoryDeleteReqVO {

    @Schema(description = "商品 SPU 编号数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "29502")
    @NotEmpty(message = "商品 SPU 编号数组不能为空")
    private List<Long> spuIds;

}
