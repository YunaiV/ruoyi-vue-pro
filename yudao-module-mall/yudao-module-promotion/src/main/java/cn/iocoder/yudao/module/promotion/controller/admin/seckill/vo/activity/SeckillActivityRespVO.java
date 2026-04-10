package cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity;

import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.product.SeckillProductRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 秒杀活动 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SeckillActivityRespVO extends SeckillActivityBaseVO {

    @Schema(description = "秒杀活动 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "秒杀商品", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<SeckillProductRespVO> products;

    @Schema(description = "活动状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "订单实付金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "22354")
    private Integer totalPrice;

    @Schema(description = "秒杀库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer stock;

    @Schema(description = "秒杀总库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Integer totalStock;

    @Schema(description = "新增订单数", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Integer orderCount;

    @Schema(description = "付款人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Integer userCount;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    // ========== 商品字段 ==========

    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED, // 从 SPU 的 name 读取
            example = "618大促")
    private String spuName;
    @Schema(description = "商品主图", requiredMode = Schema.RequiredMode.REQUIRED, // 从 SPU 的 picUrl 读取
            example = "https://www.iocoder.cn/xx.png")
    private String picUrl;
    @Schema(description = "商品市场价，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, // 从 SPU 的 marketPrice 读取
            example = "50")
    private Integer marketPrice;

    @Schema(description = "秒杀金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer seckillPrice; // 从 products 获取最小 price 读取

}
