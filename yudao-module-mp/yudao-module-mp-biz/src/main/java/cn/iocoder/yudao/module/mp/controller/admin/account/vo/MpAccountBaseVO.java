package cn.iocoder.yudao.module.mp.controller.admin.account.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * 公众号账号 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 *
 * @author fengdan
 */
@Data
public class MpAccountBaseVO {

    @Schema(description = "公众号名称", required = true, example = "芋道源码")
    @NotEmpty(message = "公众号名称不能为空")
    private String name;

    @Schema(description = "公众号微信号", required = true, example = "yudaoyuanma")
    @NotEmpty(message = "公众号微信号不能为空")
    private String account;

    @Schema(description = "公众号 appId", required = true, example = "wx5b23ba7a5589ecbb")
    @NotEmpty(message = "公众号 appId 不能为空")
    private String appId;

    @Schema(description = "公众号密钥", required = true, example = "3a7b3b20c537e52e74afd395eb85f61f")
    @NotEmpty(message = "公众号密钥不能为空")
    private String appSecret;

    @Schema(description = "公众号 token", required = true, example = "kangdayuzhen")
    @NotEmpty(message = "公众号 token 不能为空")
    private String token;

    @Schema(description = "加密密钥", example = "gjN+Ksei")
    private String aesKey;

    @Schema(description = "备注", example = "请关注芋道源码，学习技术")
    private String remark;

}
