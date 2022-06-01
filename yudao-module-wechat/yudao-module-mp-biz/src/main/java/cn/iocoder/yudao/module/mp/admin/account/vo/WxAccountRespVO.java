package cn.iocoder.yudao.module.mp.admin.account.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;

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
    private String qrUrl;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
