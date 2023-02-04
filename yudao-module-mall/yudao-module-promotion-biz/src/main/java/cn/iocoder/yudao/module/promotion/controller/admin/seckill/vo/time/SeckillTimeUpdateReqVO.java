package cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.time;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 秒杀时段更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SeckillTimeUpdateReqVO extends SeckillTimeBaseVO {

    @Schema(description = "编号", required = true, example = "1")
    @NotNull(message = "编号不能为空")
    private Long id;

}
