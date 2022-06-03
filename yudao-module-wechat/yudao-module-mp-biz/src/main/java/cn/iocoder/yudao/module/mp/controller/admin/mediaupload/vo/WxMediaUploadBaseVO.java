package cn.iocoder.yudao.module.mp.controller.admin.mediaupload.vo;

import lombok.*;
import io.swagger.annotations.*;

/**
 * 微信素材上传表  Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class WxMediaUploadBaseVO {

    @ApiModelProperty(value = "类型")
    private String type;

    @ApiModelProperty(value = "图片URL")
    private String url;

    @ApiModelProperty(value = "素材ID")
    private String mediaId;

    @ApiModelProperty(value = "缩略图素材ID")
    private String thumbMediaId;

    @ApiModelProperty(value = "微信账号ID")
    private String wxAccountId;

}
