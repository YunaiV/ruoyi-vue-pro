package cn.iocoder.yudao.module.promotion.controller.admin.reward.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

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
    private LocalDateTime createTime;

}
