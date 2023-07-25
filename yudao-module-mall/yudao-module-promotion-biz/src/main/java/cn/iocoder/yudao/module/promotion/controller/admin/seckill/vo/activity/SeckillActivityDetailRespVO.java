package cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity;

import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.product.SeckillProductRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Schema(description = "管理后台 - 秒杀活动的详细 Response VO")
@Data
@ToString(callSuper = true)
public class SeckillActivityDetailRespVO extends SeckillActivityBaseVO{

    @Schema(description = "秒杀活动id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "秒杀商品", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<SeckillProductRespVO> products;

}
