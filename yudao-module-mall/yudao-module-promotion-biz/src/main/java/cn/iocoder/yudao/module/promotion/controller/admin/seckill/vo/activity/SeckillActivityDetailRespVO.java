package cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Schema(description = "管理后台 - 秒杀活动的详细 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SeckillActivityDetailRespVO extends SeckillActivityRespVO {

    /**
     * 商品列表
     */
    private List<Product> products;

}
