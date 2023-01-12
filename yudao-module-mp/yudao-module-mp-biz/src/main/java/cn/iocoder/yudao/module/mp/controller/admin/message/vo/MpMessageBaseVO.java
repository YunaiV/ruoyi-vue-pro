package cn.iocoder.yudao.module.mp.controller.admin.message.vo;

import cn.iocoder.yudao.module.mp.dal.dataobject.account.MpAccountDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.message.MpMessageDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.user.MpUserDO;
import cn.iocoder.yudao.module.mp.enums.message.MpMessageSendFromEnum;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import me.chanjar.weixin.common.api.WxConsts;

import java.util.List;

// TODO 芋艿：VO 的注解
/**
 * 粉丝消息  Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class MpMessageBaseVO {

    /**
     * 微信公众号消息 id
     */
    private Long msgId;
    /**
     * 微信公众号 ID
     *
     * 关联 {@link MpAccountDO#getId()}
     */
    private Long accountId;
    /**
     * 微信公众号 appid
     *
     * 冗余 {@link MpAccountDO#getAppId()}
     */
    private String appId;
    /**
     * 微信用户编号
     *
     * 关联 {@link MpUserDO#getId()}
     */
    private Long userId;
    /**
     * 用户标识
     *
     * 冗余 {@link MpUserDO#getOpenid()}
     */
    private String openid;

    /**
     * 消息类型
     *
     * 枚举 {@link WxConsts.XmlMsgType}
     */
    private String type;
    /**
     * 消息来源
     *
     * 枚举 {@link MpMessageSendFromEnum}
     */
    private Integer sendFrom;

    // ========= 普通消息内容 https://developers.weixin.qq.com/doc/offiaccount/Message_Management/Receiving_standard_messages.html

    /**
     * 消息内容
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 TEXT
     */
    private String content;

    /**
     * 通过素材管理中的接口上传多媒体文件，得到的 id
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 IMAGE、VOICE、VIDEO
     */
    private String mediaId;
    /**
     * 媒体文件的 URL
     */
    private String mediaUrl;
    /**
     * 语音识别后文本
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 VOICE
     */
    private String recognition;
    /**
     * 语音格式，如 amr，speex 等
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 VOICE
     */
    private String format;
    /**
     * 标题
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 VIDEO、MUSIC、LINK
     */
    private String title;
    /**
     * 描述
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 VIDEO、MUSIC
     */
    private String description;

    /**
     * 缩略图的媒体 id，通过素材管理中的接口上传多媒体文件，得到的 id
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 VIDEO
     */
    private String thumbMediaId;
    /**
     * 缩略图的媒体 URL
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 VIDEO
     */
    private String thumbMediaUrl;

    /**
     * 点击图文消息跳转链接
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 LINK
     */
    private String url;

    /**
     * 地理位置维度
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 LOCATION
     */
    private Double locationX;
    /**
     * 地理位置经度
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 LOCATION
     */
    private Double locationY;
    /**
     * 地图缩放大小
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 LOCATION
     */
    private Double scale;
    /**
     * 详细地址
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 LOCATION
     *
     * 例如说杨浦区黄兴路 221-4 号临
     */
    private String label;

    /**
     * 图文消息数组
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 NEWS
     */
    @TableField(typeHandler = MpMessageDO.ArticleTypeHandler.class)
    private List<MpMessageDO.Article> articles;

    /**
     * 音乐链接
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 MUSIC
     */
    private String musicUrl;
    /**
     * 高质量音乐链接
     *
     * WIFI 环境优先使用该链接播放音乐
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 MUSIC
     */
    private String hqMusicUrl;

    // ========= 事件推送 https://developers.weixin.qq.com/doc/offiaccount/Message_Management/Receiving_event_pushes.html

    /**
     * 事件类型
     *
     * 枚举 {@link WxConsts.EventType}
     */
    private String event;
    /**
     * 事件 Key
     *
     * 1. {@link WxConsts.EventType} 的 SCAN：qrscene_ 为前缀，后面为二维码的参数值
     * 2. {@link WxConsts.EventType} 的 CLICK：与自定义菜单接口中 KEY 值对应
     */
    private String eventKey;

}
