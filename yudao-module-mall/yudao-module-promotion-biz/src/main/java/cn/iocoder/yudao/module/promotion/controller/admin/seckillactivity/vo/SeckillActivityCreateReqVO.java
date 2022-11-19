package cn.iocoder.yudao.module.promotion.controller.admin.seckillactivity.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;

@ApiModel("管理后台 - 秒杀活动创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SeckillActivityCreateReqVO extends SeckillActivityBaseVO {

    @ApiModelProperty(value = "备注", example = "限时秒杀活动")
    private String remark;

    @ApiModelProperty(value = "排序", required = true)
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @ApiModelProperty(value = "秒杀时段id", required = true)
    @NotNull(message = "秒杀时段id不能为空")
    private Long timeId;

}
