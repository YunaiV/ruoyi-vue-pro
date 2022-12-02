package cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.time;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@ApiModel("管理后台 - 秒杀时段更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SeckillTimeUpdateReqVO extends SeckillTimeBaseVO {

    @ApiModelProperty(value = "编号", required = true)
    @NotNull(message = "编号不能为空")
    private Long id;

}
