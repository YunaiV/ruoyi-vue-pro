package cn.iocoder.yudao.module.market.controller.admin.activity.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;

@ApiModel("管理后台 - 促销活动更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ActivityUpdateReqVO extends ActivityBaseVO {

    @ApiModelProperty(value = "活动编号", required = true)
    @NotNull(message = "活动编号不能为空")
    private Long id;

}
