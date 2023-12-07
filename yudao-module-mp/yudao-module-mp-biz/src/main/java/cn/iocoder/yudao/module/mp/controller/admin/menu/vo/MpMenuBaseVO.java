package cn.iocoder.yudao.module.mp.controller.admin.menu.vo;

import cn.iocoder.yudao.module.mp.dal.dataobject.message.MpMessageDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import me.chanjar.weixin.common.api.WxConsts;
import org.hibernate.validator.constraints.URL;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

import static cn.iocoder.yudao.module.mp.framework.mp.core.util.MpUtils.*;

/**
 * 公众号菜单 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class MpMenuBaseVO {

    /**
     * 菜单名称
     */
    private String name;
    /**
     * 菜单标识
     *
     * 支持多 DB 类型时，无法直接使用 key + @TableField("menuKey") 来实现转换，原因是 "menuKey" AS key 而存在报错
     */
    private String menuKey;
    /**
     * 父菜单编号
     */
    private Long parentId;

    // ========== 按钮操作 ==========

    /**
     * 按钮类型
     *
     * 枚举 {@link WxConsts.MenuButtonType}
     */
    private String type;

    @Schema(description = "网页链接", example = "https://www.iocoder.cn/")
    @NotEmpty(message = "网页链接不能为空", groups = {ViewButtonGroup.class, MiniProgramButtonGroup.class})
    @URL(message = "网页链接必须是 URL 格式")
    private String url;

    @Schema(description = "小程序的 appId", example = "wx1234567890")
    @NotEmpty(message = "小程序的 appId 不能为空", groups = MiniProgramButtonGroup.class)
    private String miniProgramAppId;

    @Schema(description = "小程序的页面路径", example = "pages/index/index")
    @NotEmpty(message = "小程序的页面路径不能为空", groups = MiniProgramButtonGroup.class)
    private String miniProgramPagePath;

    @Schema(description ="跳转图文的媒体编号", example = "jCQk93AIIgp8ixClWcW_NXXqBKInNWNmq2XnPeDZl7IMVqWiNeL4FfELtggRXd83")
    @NotEmpty(message = "跳转图文的媒体编号不能为空", groups = ViewLimitedButtonGroup.class)
    private String articleId;

    // ========== 消息内容 ==========

    @Schema(description = "回复的消息类型 枚举 TEXT、IMAGE、VOICE、VIDEO、NEWS、MUSIC", example = "text")
    @NotEmpty(message = "回复的消息类型不能为空", groups = {ClickButtonGroup.class, ScanCodeWaitMsgButtonGroup.class})
    private String replyMessageType;

    @Schema(description = "回复的消息内容", example = "欢迎关注")
    @NotEmpty(message = "回复的消息内容不能为空", groups = TextMessageGroup.class)
    private String replyContent;

    @Schema(description = "回复的媒体 id", example = "123456")
    @NotEmpty(message = "回复的消息 mediaId 不能为空",
            groups = {ImageMessageGroup.class, VoiceMessageGroup.class, VideoMessageGroup.class})
    private String replyMediaId;
    @Schema(description = "回复的媒体 URL", example = "https://www.iocoder.cn/xxx.jpg")
    @NotEmpty(message = "回复的消息 mediaId 不能为空",
            groups = {ImageMessageGroup.class, VoiceMessageGroup.class, VideoMessageGroup.class})
    private String replyMediaUrl;

    @Schema(description = "缩略图的媒体 id", example = "123456")
    @NotEmpty(message = "回复的消息 thumbMediaId 不能为空", groups = {MusicMessageGroup.class})
    private String replyThumbMediaId;
    @Schema(description = "缩略图的媒体 URL",example = "https://www.iocoder.cn/xxx.jpg")
    @NotEmpty(message = "回复的消息 thumbMedia 地址不能为空", groups = {MusicMessageGroup.class})
    private String replyThumbMediaUrl;

    @Schema(description = "回复的标题", example = "视频标题")
    @NotEmpty(message = "回复的消息标题不能为空", groups = VideoMessageGroup.class)
    private String replyTitle;
    @Schema(description = "回复的描述", example = "视频描述")
    @NotEmpty(message = "消息描述不能为空", groups = VideoMessageGroup.class)
    private String replyDescription;

    /**
     * 回复的图文消息数组
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 NEWS
     */
    @NotNull(message = "回复的图文消息不能为空", groups = {NewsMessageGroup.class, ViewLimitedButtonGroup.class})
    @Valid
    private List<MpMessageDO.Article> replyArticles;

    @Schema(description = "回复的音乐链接", example = "https://www.iocoder.cn/xxx.mp3")
    @NotEmpty(message = "回复的音乐链接不能为空", groups = MusicMessageGroup.class)
    @URL(message = "回复的高质量音乐链接格式不正确", groups = MusicMessageGroup.class)
    private String replyMusicUrl;
    @Schema(description = "高质量音乐链接", example = "https://www.iocoder.cn/xxx.mp3")
    @NotEmpty(message = "回复的高质量音乐链接不能为空", groups = MusicMessageGroup.class)
    @URL(message = "回复的高质量音乐链接格式不正确", groups = MusicMessageGroup.class)
    private String replyHqMusicUrl;

}
