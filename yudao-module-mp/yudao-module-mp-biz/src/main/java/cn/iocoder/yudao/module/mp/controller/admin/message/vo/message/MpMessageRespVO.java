package cn.iocoder.yudao.module.mp.controller.admin.message.vo.message;

import cn.iocoder.yudao.module.mp.dal.dataobject.message.MpMessageDO;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import me.chanjar.weixin.common.api.WxConsts;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Schema(description = "管理后台 - 公众号消息 Response VO")
@Data
public class MpMessageRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer id;

    @Schema(description = "微信公众号消息 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "23953173569869169")
    private Long msgId;

    @Schema(description = "公众号账号的编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long accountId;
    @Schema(description = "公众号账号的 appid", requiredMode = Schema.RequiredMode.REQUIRED, example = "wx1234567890")
    private String appId;

    @Schema(description = "公众号粉丝编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long userId;
    @Schema(description = "公众号粉丝标志", requiredMode = Schema.RequiredMode.REQUIRED, example = "o6_bmjrPTlm6_2sgVt7hMZOPfL2M")
    private String openid;

    @Schema(description = "消息类型 参见 WxConsts.XmlMsgType 枚举", requiredMode = Schema.RequiredMode.REQUIRED, example = "text")
    private String type;
    @Schema(description = "消息来源 参见 MpMessageSendFromEnum 枚举", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer sendFrom;

    // ========= 普通消息内容 https://developers.weixin.qq.com/doc/offiaccount/Message_Management/Receiving_standard_messages.html

    @Schema(description = "消息内容 消息类型为 text 时，才有值", example = "你好呀")
    private String content;

    @Schema(description = "媒体素材的编号 消息类型为 image、voice、video 时，才有值", example = "1234567890")
    private String mediaId;
    @Schema(description = "媒体文件的 URL 消息类型为 image、voice、video 时，才有值", example = "https://www.iocoder.cn/xxx.png")
    private String mediaUrl;

    @Schema(description = "语音识别后文本 消息类型为 voice 时，才有值", example = "语音识别后文本")
    private String recognition;
    @Schema(description = "语音格式 消息类型为 voice 时，才有值", example = "amr")
    private String format;

    @Schema(description = "标题 消息类型为 video、music、link 时，才有值", example = "我是标题")
    private String title;

    @Schema(description = "描述 消息类型为 video、music 时，才有值", example = "我是描述")
    private String description;

    @Schema(description = "缩略图的媒体 id 消息类型为 video、music 时，才有值", example = "1234567890")
    private String thumbMediaId;
    @Schema(description = "缩略图的媒体 URL 消息类型为 video、music 时，才有值", example = "https://www.iocoder.cn/xxx.png")
    private String thumbMediaUrl;

    @Schema(description = "点击图文消息跳转链接 消息类型为 link 时，才有值", example = "https://www.iocoder.cn")
    private String url;

    @Schema(description = "地理位置维度 消息类型为 location 时，才有值", example = "23.137466")
    private Double locationX;

    @Schema(description = "地理位置经度 消息类型为 location 时，才有值", example = "113.352425")
    private Double locationY;

    @Schema(description = "地图缩放大小 消息类型为 location 时，才有值", example = "13")
    private Double scale;

    @Schema(description = "详细地址 消息类型为 location 时，才有值", example = "杨浦区黄兴路 221-4 号临")
    private String label;

    /**
     * 图文消息数组
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 NEWS
     */
    @TableField(typeHandler = MpMessageDO.ArticleTypeHandler.class)
    private List<MpMessageDO.Article> articles;

    @Schema(description = "音乐链接 消息类型为 music 时，才有值", example = "https://www.iocoder.cn/xxx.mp3")
    private String musicUrl;
    @Schema(description = "高质量音乐链接 消息类型为 music 时，才有值", example = "https://www.iocoder.cn/xxx.mp3")
    private String hqMusicUrl;

    // ========= 事件推送 https://developers.weixin.qq.com/doc/offiaccount/Message_Management/Receiving_event_pushes.html

    @Schema(description = "事件类型 参见 WxConsts.EventType 枚举", example = "subscribe")
    private String event;
    @Schema(description = "事件 Key 参见 WxConsts.EventType 枚举", example = "qrscene_123456")
    private String eventKey;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
