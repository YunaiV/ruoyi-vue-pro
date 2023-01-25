package cn.iocoder.yudao.module.mp.controller.admin.material.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@ApiModel("管理后台 - 公众号素材 Response VO")
@Data
public class MpMaterialRespVO {

    @ApiModelProperty(value = "主键", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "公众号账号的编号", required = true, example = "1")
    private Long accountId;
    @ApiModelProperty(value = "公众号账号的 appId", required = true, example = "wx1234567890")
    private String appId;

    @ApiModelProperty(value = "素材的 media_id", required = true, example = "123")
    private String mediaId;

    @ApiModelProperty(value = "文件类型", required = true, example = "image", notes = "参见 WxConsts.MediaFileType 枚举")
    private String type;

    @ApiModelProperty(value = "是否永久", required = true, example = "true", notes = "true - 永久；false - 临时")
    private Boolean permanent;

    @ApiModelProperty(value = "素材的 URL", required = true, example = "https://www.iocoder.cn/1.png")
    private String url;


    @ApiModelProperty(value = "名字", example = "yunai.png")
    private String name;

    @ApiModelProperty(value = "公众号文件 URL", example = "https://mmbiz.qpic.cn/xxx.mp3", notes = "只有【永久素材】使用")
    private String mpUrl;

    @ApiModelProperty(value = "视频素材的标题", example = "我是标题", notes = "只有【永久素材】使用")
    private String title;
    @ApiModelProperty(value = "视频素材的描述", example = "我是介绍", notes = "只有【永久素材】使用")
    private String introduction;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
