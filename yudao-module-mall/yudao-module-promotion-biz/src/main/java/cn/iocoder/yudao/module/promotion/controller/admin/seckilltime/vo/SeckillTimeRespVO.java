package cn.iocoder.yudao.module.promotion.controller.admin.seckilltime.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;

@ApiModel("管理后台 - 秒杀时段 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SeckillTimeRespVO extends SeckillTimeBaseVO {

    @ApiModelProperty(value = "编号", required = true)
    private Long id;

    @ApiModelProperty(value = "秒杀活动数量", required = true)
    private Integer seckillActivityCount;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
