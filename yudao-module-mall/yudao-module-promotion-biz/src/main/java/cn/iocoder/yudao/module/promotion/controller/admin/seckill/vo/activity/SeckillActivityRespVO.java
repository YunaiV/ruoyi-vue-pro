package cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity;

import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.product.SeckillProductRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理后台 - 秒杀活动 Response VO
 *
 * @author HUIHUI
 */
@Schema(description = "管理后台 - 秒杀活动 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SeckillActivityRespVO extends SeckillActivityBaseVO {

    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "618大促")
    private String spuName;

    @Schema(description = "商品主图", requiredMode = Schema.RequiredMode.REQUIRED, example = "618大促")
    private String picUrl;

    @Schema(description = "秒杀活动id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "秒杀商品", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<SeckillProductRespVO> products;

    @Schema(description = "活动状态 开启：0 禁用：1", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "订单实付金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "22354")
    private Integer totalPrice;

    @Schema(description = "秒杀库存", example = "10")
    private Integer stock;

    @Schema(description = "秒杀总库存", example = "20")
    private Integer totalStock;

    @Schema(description = "新增订单数", example = "20")
    private Integer orderCount;

    @Schema(description = "付款人数", example = "20")
    private Integer userCount;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
