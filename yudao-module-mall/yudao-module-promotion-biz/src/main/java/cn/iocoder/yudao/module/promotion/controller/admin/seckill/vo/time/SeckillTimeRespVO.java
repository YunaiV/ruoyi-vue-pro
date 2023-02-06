package cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.time;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 秒杀时段 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SeckillTimeRespVO extends SeckillTimeBaseVO {

    @Schema(description = "编号", required = true, example = "1")
    private Long id;

    @Schema(description = "秒杀活动数量", required = true, example = "1")
    private Integer seckillActivityCount;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

}
