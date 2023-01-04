package cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.time;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@ApiModel("管理后台 - 秒杀时段 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SeckillTimeRespVO extends SeckillTimeBaseVO {

    @ApiModelProperty(value = "编号", required = true, example = "1")
    private Long id;

    @ApiModelProperty(value = "秒杀活动数量", required = true, example = "1")
    private Integer seckillActivityCount;

    @ApiModelProperty(value = "创建时间", required = true)
    private LocalDateTime createTime;

}
