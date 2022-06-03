package cn.iocoder.yudao.module.mp.controller.admin.fansmsgres.vo;

import lombok.*;
import io.swagger.annotations.*;

import javax.validation.constraints.*;

@ApiModel("管理后台 - 回复粉丝消息历史表 更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WxFansMsgResUpdateReqVO extends WxFansMsgResBaseVO {

    @ApiModelProperty(value = "主键", required = true)
    @NotNull(message = "主键不能为空")
    private Integer id;

}
