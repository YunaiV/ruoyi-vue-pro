package cn.iocoder.yudao.module.mp.controller.admin.account.vo;

import lombok.*;
import io.swagger.annotations.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

@ApiModel("管理后台 - 公众号账号分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MpAccountPageReqVO extends PageParam {

    @ApiModelProperty(value = "公众号名称", notes = "模糊匹配")
    private String name;

    @ApiModelProperty(value = "公众号账号", notes = "模糊匹配")
    private String account;

    @ApiModelProperty(value = "公众号 appid", notes = "模糊匹配")
    private String appId;

}
