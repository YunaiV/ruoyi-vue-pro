package cn.iocoder.yudao.module.mp.controller.admin.account.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 公众号账户 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 *
 * @author fengdan
 */
@Data
public class MpAccountBaseVO {

    @ApiModelProperty(value = "公众号名称", required = true)
    @NotNull(message = "公众号名称不能为空")
    private String name;

    @ApiModelProperty(value = "公众号账户", required = true)
    @NotNull(message = "公众号账户不能为空")
    private String account;

    @ApiModelProperty(value = "公众号 appid", required = true)
    @NotNull(message = "公众号 appid 不能为空")
    private String appId;

    @ApiModelProperty(value = "公众号密钥", required = true)
    @NotNull(message = "公众号密钥不能为空")
    private String appSecret;

    @ApiModelProperty(value = "公众号 token", required = true)
    private String token;

    @ApiModelProperty(value = "加密密钥")
    private String aesKey;

    @ApiModelProperty(value = "备注")
    private String remark;

}
