package cn.iocoder.yudao.module.promotion.controller.app.seckill.vo.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 秒杀活动 Response VO")
@Data
public class AppSeckillActivityRespVO {

    @Schema(description = "秒杀活动编号", required = true, example = "1024")
    private Long id;

    @Schema(description = "秒杀活动名称", required = true, example = "晚九点限时秒杀")
    private String name;

    @Schema(description = "商品 SPU 编号", required = true, example = "2048")
    private Long spuId;

    @Schema(description = "商品图片", required = true, example = "4096") // 从 SPU 的 picUrl 读取
    private String picUrl;

    @Schema(description = "商品市场价，单位：分", required = true, example = "50") // 从 SPU 的 marketPrice 读取
    private Integer marketPrice;

    @Schema(description = "秒杀金额，单位：分", required = true, example = "100") // 从秒杀商品里取最低价
    private Integer seckillPrice;

}
