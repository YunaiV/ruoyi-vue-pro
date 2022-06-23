package cn.iocoder.yudao.module.mp.controller.admin.account.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

/**
 * @author fengdan
 */
@ApiModel("管理后台 - 公众号账户 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WxAccountRespVO extends WxAccountBaseVO {

    @ApiModelProperty(value = "编号", required = true)
    private Long id;

    @ApiModelProperty(value = "公众号url")
    private String url;

    @ApiModelProperty(value = "二维码图片URL")
    private String qrCodeUrl;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

    @ApiModelProperty(value = "公众号密钥", required = true)
    private String appSecret;
}
