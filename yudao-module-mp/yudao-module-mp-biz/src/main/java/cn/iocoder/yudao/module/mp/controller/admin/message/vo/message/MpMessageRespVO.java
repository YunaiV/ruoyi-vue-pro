package cn.iocoder.yudao.module.mp.controller.admin.message.vo.message;

import cn.iocoder.yudao.module.mp.dal.dataobject.message.MpMessageDO;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import me.chanjar.weixin.common.api.WxConsts;

import java.util.Date;
import java.util.List;

@ApiModel("管理后台 - 公众号消息 Response VO")
@Data
public class MpMessageRespVO {

    @ApiModelProperty(value = "主键", required = true, example = "1024")
    private Integer id;

    @ApiModelProperty(value = "微信公众号消息 id", required = true, example = "23953173569869169")
    private Long msgId;

    @ApiModelProperty(value = "公众号账号的编号", required = true, example = "1")
    private Long accountId;
    @ApiModelProperty(value = "公众号账号的 appid", required = true, example = "wx1234567890")
    private String appId;

    @ApiModelProperty(value = "公众号粉丝编号", required = true, example = "2048")
    private Long userId;
    @ApiModelProperty(value = "公众号粉丝标志", required = true, example = "o6_bmjrPTlm6_2sgVt7hMZOPfL2M")
    private String openid;

    @ApiModelProperty(value = "消息类型", required = true, example = "text", notes = "参见 WxConsts.XmlMsgType 枚举")
    private String type;
    @ApiModelProperty(value = "消息来源", required = true, example = "1", notes = "参见 MpMessageSendFromEnum 枚举")
    private Integer sendFrom;

    // ========= 普通消息内容 https://developers.weixin.qq.com/doc/offiaccount/Message_Management/Receiving_standard_messages.html

    @ApiModelProperty(value = "消息内容", example = "你好呀", notes = "消息类型为 text 时，才有值")
    private String content;

    @ApiModelProperty(value = "媒体素材的编号", example = "1234567890", notes = "消息类型为 image、voice、video 时，才有值")
    private String mediaId;
    @ApiModelProperty(value = "媒体文件的 URL", example = "https://www.iocoder.cn/xxx.png",
            notes = "消息类型为 image、voice、video 时，才有值")
    private String mediaUrl;

    @ApiModelProperty(value = "语音识别后文本", example = "语音识别后文本", notes = "消息类型为 voice 时，才有值")
    private String recognition;
    @ApiModelProperty(value = "语音格式", example = "amr", notes = "消息类型为 voice 时，才有值")
    private String format;

    @ApiModelProperty(value = "标题", example = "我是标题", notes = "消息类型为 video、music、link 时，才有值")
    private String title;

    @ApiModelProperty(value = "描述", example = "我是描述", notes = "消息类型为 video、music 时，才有值")
    private String description;

    @ApiModelProperty(value = "缩略图的媒体 id", example = "1234567890", notes = "消息类型为 video、music 时，才有值")
    private String thumbMediaId;
    @ApiModelProperty(value = "缩略图的媒体 URL", example = "https://www.iocoder.cn/xxx.png",
            notes = "消息类型为 video、music 时，才有值")
    private String thumbMediaUrl;

    @ApiModelProperty(value = "点击图文消息跳转链接", example = "https://www.iocoder.cn", notes = "消息类型为 link 时，才有值")
    private String url;

    @ApiModelProperty(value = "地理位置维度", example = "23.137466", notes = "消息类型为 location 时，才有值")
    private Double locationX;

    @ApiModelProperty(value = "地理位置经度", example = "113.352425", notes = "消息类型为 location 时，才有值")
    private Double locationY;

    @ApiModelProperty(value = "地图缩放大小", example = "13", notes = "消息类型为 location 时，才有值")
    private Double scale;

    @ApiModelProperty(value = "详细地址", example = "杨浦区黄兴路 221-4 号临", notes = "消息类型为 location 时，才有值")
    private String label;

    /**
     * 图文消息数组
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 NEWS
     */
    @TableField(typeHandler = MpMessageDO.ArticleTypeHandler.class)
    private List<MpMessageDO.Article> articles;

    @ApiModelProperty(value = "音乐链接", example = "https://www.iocoder.cn/xxx.mp3", notes = "消息类型为 music 时，才有值")
    private String musicUrl;
    @ApiModelProperty(value = "高质量音乐链接", example = "https://www.iocoder.cn/xxx.mp3", notes = "消息类型为 music 时，才有值")
    private String hqMusicUrl;

    // ========= 事件推送 https://developers.weixin.qq.com/doc/offiaccount/Message_Management/Receiving_event_pushes.html

    @ApiModelProperty(value = "事件类型", example = "subscribe", notes = "参见 WxConsts.EventType 枚举")
    private String event;
    @ApiModelProperty(value = "事件 Key", example = "qrscene_123456", notes = "参见 WxConsts.EventType 枚举")
    private String eventKey;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
