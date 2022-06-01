package cn.iocoder.yudao.module.mp.admin.account.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;

/**
* 公众号账户 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class WxAccountBaseVO {

    @ApiModelProperty(value = "公众号名称")
    private String name;

    @ApiModelProperty(value = "公众号账户")
    private String account;

    @ApiModelProperty(value = "公众号appid")
    private String appid;

    @ApiModelProperty(value = "公众号密钥")
    private String appsecret;

    @ApiModelProperty(value = "公众号token")
    private String token;

    @ApiModelProperty(value = "加密密钥")
    private String aeskey;

    @ApiModelProperty(value = "备注")
    private String remark;

}
