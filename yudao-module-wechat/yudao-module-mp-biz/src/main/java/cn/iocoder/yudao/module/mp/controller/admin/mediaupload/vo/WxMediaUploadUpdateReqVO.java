package cn.iocoder.yudao.module.mp.controller.admin.mediaupload.vo;

import lombok.*;
import io.swagger.annotations.*;

import javax.validation.constraints.*;

@ApiModel("管理后台 - 微信素材上传表 更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WxMediaUploadUpdateReqVO extends WxMediaUploadBaseVO {

    @ApiModelProperty(value = "主键", required = true)
    @NotNull(message = "主键不能为空")
    private Integer id;

}
