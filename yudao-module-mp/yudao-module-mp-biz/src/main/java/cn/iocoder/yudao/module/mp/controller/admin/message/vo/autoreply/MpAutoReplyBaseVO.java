package cn.iocoder.yudao.module.mp.controller.admin.message.vo.autoreply;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.mp.dal.dataobject.message.MpMessageDO;
import cn.iocoder.yudao.module.mp.enums.message.MpAutoReplyTypeEnum;
import cn.iocoder.yudao.module.mp.framework.mp.core.util.MpUtils.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import me.chanjar.weixin.common.api.WxConsts;
import org.hibernate.validator.constraints.URL;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 公众号自动回复  Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class MpAutoReplyBaseVO {

    @ApiModelProperty(value = "回复类型", example = "1", notes = "参见 MpAutoReplyTypeEnum 枚举")
    @NotNull(message = "回复类型不能为空")
    private Integer type;

    // ==================== 请求消息 ====================

    @ApiModelProperty(value = "请求的关键字", example = "关键字", notes = "当 type 为 MpAutoReplyTypeEnum#KEYWORD 时，必填")
    private String requestKeyword;
    @ApiModelProperty(value = "请求的关键字", example = "关键字", notes = "当 type 为 MpAutoReplyTypeEnum#KEYWORD 时，必填")
    private Integer requestMatch;

    @ApiModelProperty(value = "请求的消息类型", example = "text", notes = "当 type 为 MpAutoReplyTypeEnum#MESSAGE 时，必填")
    private String requestMessageType;

    // ==================== 响应消息 ====================

    @ApiModelProperty(value = "回复的消息类型", example = "text",
            notes = "枚举 TEXT、IMAGE、VOICE、VIDEO、NEWS、MUSIC")
    @NotEmpty(message = "回复的消息类型不能为空")
    private String responseMessageType;

    @ApiModelProperty(value = "回复的消息内容", example = "欢迎关注")
    @NotEmpty(message = "回复的消息内容不能为空", groups = TextMessageGroup.class)
    private String responseContent;

    @ApiModelProperty(value = "回复的媒体 id", example = "123456")
    @NotEmpty(message = "回复的消息 mediaId 不能为空",
            groups = {ImageMessageGroup.class, VoiceMessageGroup.class, VideoMessageGroup.class})
    private String responseMediaId;
    @ApiModelProperty(value = "回复的媒体 URL", example = "https://www.iocoder.cn/xxx.jpg")
    @NotEmpty(message = "回复的消息 mediaId 不能为空",
            groups = {ImageMessageGroup.class, VoiceMessageGroup.class, VideoMessageGroup.class})
    private String responseMediaUrl;

    @ApiModelProperty(value = "缩略图的媒体 id", example = "123456")
    @NotEmpty(message = "回复的消息 thumbMediaId 不能为空", groups = {MusicMessageGroup.class})
    private String responseThumbMediaId;
    @ApiModelProperty(value = "缩略图的媒体 URL",example = "https://www.iocoder.cn/xxx.jpg")
    @NotEmpty(message = "回复的消息 thumbMedia 地址不能为空", groups = {MusicMessageGroup.class})
    private String responseThumbMediaUrl;

    @ApiModelProperty(value = "回复的标题", example = "视频标题")
    @NotEmpty(message = "回复的消息标题不能为空", groups = VideoMessageGroup.class)
    private String responseTitle;
    @ApiModelProperty(value = "回复的描述", example = "视频描述")
    @NotEmpty(message = "消息描述不能为空", groups = VideoMessageGroup.class)
    private String responseDescription;

    /**
     * 回复的图文消息
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 NEWS
     */
    @NotNull(message = "回复的图文消息不能为空", groups = {NewsMessageGroup.class, ViewLimitedButtonGroup.class})
    @Valid
    private List<MpMessageDO.Article> responseArticles;

    @ApiModelProperty(value = "回复的音乐链接", example = "https://www.iocoder.cn/xxx.mp3")
    @NotEmpty(message = "回复的音乐链接不能为空", groups = MusicMessageGroup.class)
    @URL(message = "回复的高质量音乐链接格式不正确", groups = MusicMessageGroup.class)
    private String responseMusicUrl;
    @ApiModelProperty(value = "高质量音乐链接", example = "https://www.iocoder.cn/xxx.mp3")
    @NotEmpty(message = "回复的高质量音乐链接不能为空", groups = MusicMessageGroup.class)
    @URL(message = "回复的高质量音乐链接格式不正确", groups = MusicMessageGroup.class)
    private String responseHqMusicUrl;

    @AssertTrue(message = "请求的关键字不能为空")
    public boolean isRequestKeywordValid() {
        return ObjectUtil.notEqual(type, MpAutoReplyTypeEnum.KEYWORD)
                || requestKeyword != null;
    }

    @AssertTrue(message = "请求的关键字的匹配不能为空")
    public boolean isRequestMatchValid() {
        return ObjectUtil.notEqual(type, MpAutoReplyTypeEnum.KEYWORD)
                || requestMatch != null;
    }

    @AssertTrue(message = "请求的消息类型不能为空")
    public boolean isRequestMessageTypeValid() {
        return ObjectUtil.notEqual(type, MpAutoReplyTypeEnum.MESSAGE)
                || requestMessageType != null;
    }


}
