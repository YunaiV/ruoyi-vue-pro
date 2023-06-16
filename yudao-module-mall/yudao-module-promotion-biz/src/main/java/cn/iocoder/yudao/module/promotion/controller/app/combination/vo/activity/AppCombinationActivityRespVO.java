package cn.iocoder.yudao.module.promotion.controller.app.combination.vo.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 拼团活动 Response VO")
@Data
public class AppCombinationActivityRespVO {

    @Schema(description = "拼团活动编号", required = true, example = "1024")
    private Long id;

    @Schema(description = "拼团活动名称", required = true, example = "618 大拼团")
    private String name;

    @Schema(description = "拼团人数", required = true, example = "3")
    private Integer userSize;

    @Schema(description = "商品 SPU 编号", required = true, example = "2048")
    private Long spuId;

    @Schema(description = "商品图片", required = true, example = "4096") // 从 SPU 的 picUrl 读取
    private String picUrl;

    @Schema(description = "商品市场价，单位：分", required = true, example = "50") // 从 SPU 的 marketPrice 读取
    private Integer marketPrice;

    @Schema(description = "拼团金额，单位：分", required = true, example = "100") // 从拼团商品里取最低价
    private Integer combinationPrice;

}
