package cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity;

import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.product.SeckillProductRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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

    @Schema(description = "秒杀活动id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "秒杀商品", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<SeckillProductRespVO> products; // TODO puhui: 考虑是否去除

    @Schema(description = "活动状态 开启：0 禁用：1", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

}
