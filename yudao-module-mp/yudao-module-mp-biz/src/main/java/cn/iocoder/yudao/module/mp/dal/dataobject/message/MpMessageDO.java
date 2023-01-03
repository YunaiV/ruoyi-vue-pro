package cn.iocoder.yudao.module.mp.dal.dataobject.message;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.MpAccountDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.user.MpUserDO;
import cn.iocoder.yudao.module.mp.enums.message.MpMessageSendFromEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import me.chanjar.weixin.common.api.WxConsts;

/**
 * 微信消息 DO
 *
 * @author 芋道源码
 */
@TableName(value = "mp_message", autoResultMap = true)
@KeySequence("mp_message_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MpMessageDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
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
