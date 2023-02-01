package cn.iocoder.yudao.module.mp.controller.admin.account.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

@ApiModel("管理后台 - 公众号账号 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MpAccountRespVO extends MpAccountBaseVO {

    @ApiModelProperty(value = "编号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "二维码图片URL", example = "https://www.iocoder.cn/1024.png")
    private String qrCodeUrl;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
