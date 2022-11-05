package cn.iocoder.yudao.module.promotion.controller.admin.reward.vo;

import lombok.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;

@ApiModel("管理后台 - 满减送活动更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RewardActivityUpdateReqVO extends RewardActivityBaseVO {

    @ApiModelProperty(value = "活动编号", required = true, example = "1024")
    @NotNull(message = "活动编号不能为空")
    private Long id;

}
