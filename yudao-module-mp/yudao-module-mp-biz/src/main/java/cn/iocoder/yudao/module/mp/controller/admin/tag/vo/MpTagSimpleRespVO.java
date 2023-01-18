package cn.iocoder.yudao.module.mp.controller.admin.tag.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("管理后台 - 公众号标签精简信息 Response VO")
@Data
public class MpTagSimpleRespVO {

    @ApiModelProperty(value = "编号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "公众号的标签编号", required = true, example = "2048")
    private Long tagId;

    @ApiModelProperty(value = "标签名称", required = true, example = "快乐")
    private String name;

}
