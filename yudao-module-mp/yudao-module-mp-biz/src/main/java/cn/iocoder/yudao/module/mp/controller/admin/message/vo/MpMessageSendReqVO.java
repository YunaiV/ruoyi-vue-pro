package cn.iocoder.yudao.module.mp.controller.admin.message.vo;

import cn.iocoder.yudao.module.mp.dal.dataobject.message.MpMessageDO;
import cn.iocoder.yudao.module.mp.framework.mp.core.util.MpUtils.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@ApiModel("管理后台 - 公众号消息发送 Request VO")
@Data
public class MpMessageSendReqVO {

    @ApiModelProperty(value = "公众号粉丝的编号", required = true, example = "1024")
    @NotNull(message = "公众号粉丝的编号不能为空")
    private Long userId;

    // ========== 消息内容 ==========

    @ApiModelProperty(value = "消息类型", required = true, example = "text", notes = "TEXT/IMAGE/VOICE/VIDEO/NEWS")
    @NotEmpty(message = "消息类型不能为空")
    public String type;

    @ApiModelProperty(value = "消息内容", required = true, example = "你好呀")
    @NotEmpty(message = "消息内容不能为空", groups = TextMessageGroup.class)
    private String content;

    @ApiModelProperty(value = "媒体 ID", required = true, example = "qqc_2Fot30Jse-HDoZmo5RrUDijz2nGUkP")
    @NotEmpty(message = "消息内容不能为空", groups = {ImageMessageGroup.class, VoiceMessageGroup.class, VideoMessageGroup.class})
    private String mediaId;

    @ApiModelProperty(value = "标题", required = true, example = "没有标题")
    @NotEmpty(message = "消息内容不能为空", groups = VideoMessageGroup.class)
    private String title;

    @ApiModelProperty(value = "描述", required = true, example = "你猜")
    @NotEmpty(message = "消息描述不能为空", groups = VideoMessageGroup.class)
    private String description;

    @ApiModelProperty(value = "缩略图的媒体 id", required = true, example = "qqc_2Fot30Jse-HDoZmo5RrUDijz2nGUkP")
    @NotEmpty(message = "缩略图的媒体 id 不能为空", groups = MusicMessageGroup.class)
    private String thumbMediaId;

    @ApiModelProperty(value = "图文消息", required = true)
    @Valid
    @NotNull(message = "图文消息不能为空", groups = NewsMessageGroup.class)
    private List<MpMessageDO.Article> articles;

    @ApiModelProperty(value = "音乐链接", example = "https://www.iocoder.cn/music.mp3", notes = "消息类型为 MUSIC 时")
    private String musicUrl;

    @ApiModelProperty(value = "高质量音乐链接", example = "https://www.iocoder.cn/music.mp3", notes = "消息类型为 MUSIC 时")
    private String hqMusicUrl;

}
