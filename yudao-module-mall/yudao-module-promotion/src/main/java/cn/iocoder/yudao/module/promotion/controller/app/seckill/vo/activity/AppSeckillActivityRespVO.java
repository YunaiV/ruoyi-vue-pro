package cn.iocoder.yudao.module.promotion.controller.app.seckill.vo.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 秒杀活动 Response VO")
@Data
public class AppSeckillActivityRespVO {

    @Schema(description = "秒杀活动编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "秒杀活动名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "晚九点限时秒杀")
    private String name;

    @Schema(description = "商品 SPU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long spuId;

    @Schema(description = "商品 SPU 名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "一个白菜")
    private String spuName; // 从 SPU 的 name 读取

    @Schema(description = "商品图片", requiredMode = Schema.RequiredMode.REQUIRED, // 从 SPU 的 picUrl 读取
            example = "https://www.iocoder.cn/xx.png")
    private String picUrl;

    @Schema(description = "商品市场价，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, // 从 SPU 的 marketPrice 读取
            example = "50")
    private Integer marketPrice;

    @Schema(description = "秒杀活动状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "秒杀库存（剩余）", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer stock;
    @Schema(description = "秒杀库存（总共）", requiredMode = Schema.RequiredMode.REQUIRED, example = "200")
    private Integer totalStock;

    @Schema(description = "秒杀金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer seckillPrice;

}
