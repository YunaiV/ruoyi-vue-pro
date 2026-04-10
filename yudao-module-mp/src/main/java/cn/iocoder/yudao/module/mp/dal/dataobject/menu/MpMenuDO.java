package cn.iocoder.yudao.module.mp.dal.dataobject.menu;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.MpAccountDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.message.MpMessageDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.api.WxConsts.MenuButtonType;

import java.util.List;

/**
 * 公众号菜单 DO
 *
 * @author 芋道源码
 */
@TableName(value = "mp_menu", autoResultMap = true)
@KeySequence("mp_menu_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MpMenuDO extends BaseDO {

    /**
     * 编号 - 顶级菜单
     */
    public static final Long ID_ROOT = 0L;

    /**
     * 编号
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
     * 枚举 {@link MenuButtonType}
     */
    private String type;

    /**
     * 网页链接
     *
     * 粉丝点击菜单可打开链接，不超过 1024 字节
     *
     * 类型为 {@link WxConsts.XmlMsgType} 的 VIEW、MINIPROGRAM
     */
    private String url;

    /**
     * 小程序的 appId
     *
     * 类型为 {@link MenuButtonType} 的 MINIPROGRAM
     */
    private String miniProgramAppId;
    /**
     * 小程序的页面路径
     *
     * 类型为 {@link MenuButtonType} 的 MINIPROGRAM
     */
    private String miniProgramPagePath;

    /**
     * 跳转图文的媒体编号
     */
    private String articleId;

    // ========== 消息内容 ==========

    /**
     * 消息类型
     *
     * 当 {@link #type} 为 CLICK、SCANCODE_WAITMSG
     *
     * 枚举 {@link WxConsts.XmlMsgType} 中的 TEXT、IMAGE、VOICE、VIDEO、NEWS、MUSIC
     */
    private String replyMessageType;

    /**
     * 回复的消息内容
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 TEXT
     */
    private String replyContent;

    /**
     * 回复的媒体 id
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 IMAGE、VOICE、VIDEO
     */
    private String replyMediaId;
    /**
     * 回复的媒体 URL
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 IMAGE、VOICE、VIDEO
     */
    private String replyMediaUrl;

    /**
     * 回复的标题
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 VIDEO
     */
    private String replyTitle;
    /**
     * 回复的描述
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 VIDEO
     */
    private String replyDescription;

    /**
     * 回复的缩略图的媒体 id，通过素材管理中的接口上传多媒体文件，得到的 id
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 MUSIC、VIDEO
     */
    private String replyThumbMediaId;
    /**
     * 回复的缩略图的媒体 URL
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 MUSIC、VIDEO
     */
    private String replyThumbMediaUrl;

    /**
     * 回复的图文消息数组
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 NEWS
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<MpMessageDO.Article> replyArticles;

    /**
     * 回复的音乐链接
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 MUSIC
     */
    private String replyMusicUrl;
    /**
     * 回复的高质量音乐链接
     *
     * WIFI 环境优先使用该链接播放音乐
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 MUSIC
     */
    private String replyHqMusicUrl;

}
