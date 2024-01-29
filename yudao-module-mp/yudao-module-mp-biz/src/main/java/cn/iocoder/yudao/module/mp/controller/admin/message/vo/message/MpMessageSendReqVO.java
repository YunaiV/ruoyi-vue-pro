package cn.iocoder.yudao.module.mp.controller.admin.message.vo.message;

import cn.iocoder.yudao.module.mp.dal.dataobject.message.MpMessageDO;
import cn.iocoder.yudao.module.mp.framework.mp.core.util.MpUtils.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 公众号消息发送 Request VO")
@Data
public class MpMessageSendReqVO {

    @Schema(description = "公众号粉丝的编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "公众号粉丝的编号不能为空")
    private Long userId;

    // ========== 消息内容 ==========

    @Schema(description = "消息类型 TEXT/IMAGE/VOICE/VIDEO/NEWS", requiredMode = Schema.RequiredMode.REQUIRED, example = "text")
    @NotEmpty(message = "消息类型不能为空")
    public String type;

    @Schema(description = "消息内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "你好呀")
    @NotEmpty(message = "消息内容不能为空", groups = TextMessageGroup.class)
    private String content;

    @Schema(description = "媒体 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "qqc_2Fot30Jse-HDoZmo5RrUDijz2nGUkP")
    @NotEmpty(message = "消息内容不能为空", groups = {ImageMessageGroup.class, VoiceMessageGroup.class, VideoMessageGroup.class})
    private String mediaId;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "没有标题")
    @NotEmpty(message = "消息内容不能为空", groups = VideoMessageGroup.class)
    private String title;

    @Schema(description = "描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "你猜")
    @NotEmpty(message = "消息描述不能为空", groups = VideoMessageGroup.class)
    private String description;

    @Schema(description = "缩略图的媒体 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "qqc_2Fot30Jse-HDoZmo5RrUDijz2nGUkP")
    @NotEmpty(message = "缩略图的媒体 id 不能为空", groups = MusicMessageGroup.class)
    private String thumbMediaId;

    @Schema(description = "图文消息", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotNull(message = "图文消息不能为空", groups = NewsMessageGroup.class)
    private List<MpMessageDO.Article> articles;

    @Schema(description = "音乐链接 消息类型为 MUSIC 时", example = "https://www.iocoder.cn/music.mp3")
    private String musicUrl;

    @Schema(description = "高质量音乐链接 消息类型为 MUSIC 时", example = "https://www.iocoder.cn/music.mp3")
    private String hqMusicUrl;

}
