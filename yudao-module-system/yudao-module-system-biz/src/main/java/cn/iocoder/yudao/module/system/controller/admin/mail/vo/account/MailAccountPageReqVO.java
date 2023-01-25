package cn.iocoder.yudao.module.system.controller.admin.mail.vo.account;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ApiModel("管理后台 - 邮箱账号分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MailAccountPageReqVO extends PageParam {

    @ApiModelProperty(value = "邮箱", required = true, example = "yudaoyuanma@123.com")
    private String mail;

    @ApiModelProperty(value = "用户名" , required = true , example = "yudao")
    private String username;

}
