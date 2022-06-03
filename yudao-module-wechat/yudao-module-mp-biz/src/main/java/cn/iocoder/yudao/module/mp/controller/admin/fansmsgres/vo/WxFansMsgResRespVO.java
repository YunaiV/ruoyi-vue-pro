package cn.iocoder.yudao.module.mp.controller.admin.fansmsgres.vo;

import lombok.*;

import java.util.*;

import io.swagger.annotations.*;

@ApiModel("管理后台 - 回复粉丝消息历史表  Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WxFansMsgResRespVO extends WxFansMsgResBaseVO {

    @ApiModelProperty(value = "主键", required = true)
    private Integer id;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
