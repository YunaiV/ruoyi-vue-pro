package cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import javax.validation.constraints.*;

/**
 * 管理后台 - 秒杀参与商品更新 Request VO
 *
 * @author HUIHUI
 */
@Schema(description = "管理后台 - 秒杀参与商品更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SeckillProductUpdateReqVO extends SeckillProductBaseVO {

    @Schema(description = "秒杀参与商品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "256")
    @NotNull(message = "秒杀参与商品编号不能为空")
    private Long id;

}
