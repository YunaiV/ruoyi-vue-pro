package cn.iocoder.yudao.module.promotion.controller.app.combination.vo.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 拼团活动 Response VO")
@Data
public class AppCombinationActivityRespVO {

    @Schema(description = "拼团活动编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "拼团活动名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "618 大拼团")
    private String name;

    @Schema(description = "拼团人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer userSize;

    @Schema(description = "商品 SPU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long spuId;

    @Schema(description = "商品图片", requiredMode = Schema.RequiredMode.REQUIRED, example = "4096")
    // 从 SPU 的 picUrl 读取
    private String picUrl;

    @Schema(description = "商品市场价，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    // 从 SPU 的 marketPrice 读取
    private Integer marketPrice;

    @Schema(description = "拼团金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer combinationPrice;

}
