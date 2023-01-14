package cn.iocoder.yudao.module.mp.controller.admin.material.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@ApiModel("管理后台 - 公众号素材的分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MpMaterialPageReqVO extends PageParam {

    @ApiModelProperty(value = "公众号账号的编号", required = true, example = "2048")
    @NotNull(message = "公众号账号的编号不能为空")
    private Long accountId;

    @ApiModelProperty(value = "是否永久", example = "true")
    private Boolean permanent;

    @ApiModelProperty(value = "文件类型", example = "image", notes = "参见 WxConsts.MediaFileType 枚举")
    private String type;

}
