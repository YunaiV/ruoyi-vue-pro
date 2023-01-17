package cn.iocoder.yudao.module.mp.controller.admin.menu.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

@ApiModel("管理后台 - 公众号菜单 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MpMenuRespVO extends MpMenuBaseVO {

    @ApiModelProperty(value = "主键", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "公众号账号的编号", required = true, example = "2048")
    private Long accountId;

    @ApiModelProperty(value = "公众号 appId", required = true, example = "wx1234567890ox")
    private String appId;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
