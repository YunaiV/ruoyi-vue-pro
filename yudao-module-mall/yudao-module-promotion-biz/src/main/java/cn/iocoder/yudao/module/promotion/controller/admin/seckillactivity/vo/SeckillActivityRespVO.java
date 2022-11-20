package cn.iocoder.yudao.module.promotion.controller.admin.seckillactivity.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;

import javax.validation.constraints.NotNull;

@ApiModel("管理后台 - 秒杀活动 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SeckillActivityRespVO extends SeckillActivityBaseVO {

    @ApiModelProperty(value = "秒杀活动id",  required = true, example = "1")
    private Long id;

    @ApiModelProperty(value = "付款订单数", required = true)
    private Integer orderCount;

    @ApiModelProperty(value = "付款人数", required = true)
    private Integer userCount;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

    @ApiModelProperty(value = "秒杀时段id", required = true)
    private String timeId;

}
