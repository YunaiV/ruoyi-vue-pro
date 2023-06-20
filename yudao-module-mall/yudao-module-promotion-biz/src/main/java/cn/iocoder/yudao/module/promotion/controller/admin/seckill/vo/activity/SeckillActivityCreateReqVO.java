package cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity;


import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.product.SeckillProductCreateReqVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 管理后台 - 秒杀活动创建 Request VO
 *
 * @author HUIHUI
 */
@Schema(description = "管理后台 - 秒杀活动创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SeckillActivityCreateReqVO extends SeckillActivityBaseVO {

    @Schema(description = "秒杀商品", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<SeckillProductCreateReqVO> products;

}
