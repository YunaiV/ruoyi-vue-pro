package cn.iocoder.yudao.module.promotion.controller.admin.reward.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;

@ApiModel("管理后台 - 满减送活动 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RewardActivityRespVO extends RewardActivityBaseVO {

    @ApiModelProperty(value = "活动编号", required = true, example = "1024")
    private Integer id;

    @ApiModelProperty(value = "活动状态", required = true, example = "1")
    private Integer status;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
