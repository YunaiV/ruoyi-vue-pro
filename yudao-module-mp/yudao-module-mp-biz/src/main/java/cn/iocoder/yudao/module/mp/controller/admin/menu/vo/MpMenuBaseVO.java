package cn.iocoder.yudao.module.mp.controller.admin.menu.vo;

import cn.iocoder.yudao.module.mp.dal.dataobject.message.MpMessageDO;
import lombok.Data;
import me.chanjar.weixin.common.api.WxConsts;

import java.util.List;

// TODO 芋艿：完善 swagger 注解
/**
 * 微信菜单 Base VO，提供给添加、修改、详细的子 VO 使用
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

    /**
     * 网页链接
     *
     * 用户点击菜单可打开链接，不超过 1024 字节
     *
     * 类型为 {@link WxConsts.XmlMsgType} 的 VIEW、MINIPROGRAM
     */
    private String url;

    /**
     * 小程序的 appId
     *
     * 类型为 {@link WxConsts.MenuButtonType} 的 MINIPROGRAM
     */
    private String miniProgramAppId;
    /**
     * 小程序的页面路径
     *
     * 类型为 {@link WxConsts.MenuButtonType} 的 MINIPROGRAM
     */
    private String miniProgramPagePath;

    // ========== 消息内容 ==========

    /**
     * 消息类型
     *
     * 当 {@link #type} 为 CLICK、SCANCODE_WAITMSG
     *
     * 枚举 {@link WxConsts.XmlMsgType} 中的 TEXT、IMAGE、VOICE、VIDEO、NEWS
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
     * 回复的图文消息数组
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 NEWS
     */
    private List<MpMessageDO.Article> replyArticles;

}
