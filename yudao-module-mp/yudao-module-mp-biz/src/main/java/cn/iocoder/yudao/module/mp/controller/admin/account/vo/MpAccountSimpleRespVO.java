package cn.iocoder.yudao.module.mp.controller.admin.account.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("管理后台 - 公众号账号精简信息 Response VO")
@Data
public class MpAccountSimpleRespVO {

    @ApiModelProperty(value = "编号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "公众号名称", required = true, example = "芋道源码")
    private String name;

}
