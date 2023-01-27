package cn.iocoder.yudao.module.mp.controller.admin.material.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("管理后台 - 公众号素材上传结果 Response VO")
@Data
public class MpMaterialUploadRespVO {

    @ApiModelProperty(value = "素材的 media_id", required = true, example = "123")
    private String mediaId;

    @ApiModelProperty(value = "素材的 URL", required = true, example = "https://www.iocoder.cn/1.png")
    private String url;

}
