package cn.iocoder.yudao.module.mp.dal.dataobject.message;

import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.MpAccountDO;
import cn.iocoder.yudao.module.mp.enums.message.MpAutoReplyMatchEnum;
import cn.iocoder.yudao.module.mp.enums.message.MpAutoReplyTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.api.WxConsts.XmlMsgType;

import java.util.List;
import java.util.Set;

/**
 * 公众号消息自动回复 DO
 *
 * @author 芋道源码
 */
@TableName(value = "mp_auto_reply", autoResultMap = true)
@KeySequence("mp_auto_reply_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MpAutoReplyDO extends BaseDO {

    public static Set<String> REQUEST_MESSAGE_TYPE = SetUtils.asSet(WxConsts.XmlMsgType.TEXT, WxConsts.XmlMsgType.IMAGE,
            WxConsts.XmlMsgType.VOICE, WxConsts.XmlMsgType.VIDEO, WxConsts.XmlMsgType.SHORTVIDEO,
            WxConsts.XmlMsgType.LOCATION, WxConsts.XmlMsgType.LINK);

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 公众号账号的编号
     *
     * 关联 {@link MpAccountDO#getId()}
     */
    private Long accountId;
    /**
     * 公众号 appId
     *
     * 冗余 {@link MpAccountDO#getAppId()}
     */
    private String appId;

    /**
     * 回复类型
     *
     * 枚举 {@link MpAutoReplyTypeEnum}
     */
    private Integer type;

    // ==================== 请求消息 ====================

    /**
     * 请求的关键字
     *
     * 当 {@link #type} 为 {@link MpAutoReplyTypeEnum#KEYWORD}
     */
    private String requestKeyword;
    /**
     * 请求的关键字的匹配
     *
     * 当 {@link #type} 为 {@link MpAutoReplyTypeEnum#KEYWORD}
     *
     * 枚举 {@link MpAutoReplyMatchEnum}
     */
    private Integer requestMatch;

    /**
     * 请求的消息类型
     *
     * 当 {@link #type} 为 {@link MpAutoReplyTypeEnum#MESSAGE}
     *
     * 枚举 {@link XmlMsgType} 中的 {@link #REQUEST_MESSAGE_TYPE}
     */
    private String requestMessageType;

    // ==================== 响应消息 ====================

    /**
     * 回复的消息类型
     *
     * 枚举 {@link XmlMsgType} 中的 TEXT、IMAGE、VOICE、VIDEO、NEWS
     */
    private String responseMessageType;

    /**
     * 回复的消息内容
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 TEXT
     */
    private String responseContent;

    /**
     * 回复的媒体 id
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 IMAGE、VOICE、VIDEO
     */
    private String responseMediaId;
    /**
     * 回复的媒体 URL
     */
    private String responseMediaUrl;

    /**
     * 回复的标题
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 VIDEO
     */
    private String responseTitle;
    /**
     * 回复的描述
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 VIDEO
     */
    private String responseDescription;

    /**
     * 回复的缩略图的媒体 id，通过素材管理中的接口上传多媒体文件，得到的 id
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 MUSIC、VIDEO
     */
    private String responseThumbMediaId;
    /**
     * 回复的缩略图的媒体 URL
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 MUSIC、VIDEO
     */
    private String responseThumbMediaUrl;

    /**
     * 回复的图文消息
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 NEWS
     */
    @TableField(typeHandler = MpMessageDO.ArticleTypeHandler.class)
    private List<MpMessageDO.Article> responseArticles;

    /**
     * 回复的音乐链接
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 MUSIC
     */
    private String responseMusicUrl;
    /**
     * 回复的高质量音乐链接
     *
     * WIFI 环境优先使用该链接播放音乐
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 MUSIC
     */
    private String responseHqMusicUrl;

}
