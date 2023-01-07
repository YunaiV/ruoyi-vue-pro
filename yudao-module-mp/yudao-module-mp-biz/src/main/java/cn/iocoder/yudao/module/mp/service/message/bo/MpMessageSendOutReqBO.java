package cn.iocoder.yudao.module.mp.service.message.bo;

import cn.iocoder.yudao.module.mp.dal.dataobject.message.MpMessageDO;
import cn.iocoder.yudao.module.mp.framework.mp.core.util.MpUtils;
import cn.iocoder.yudao.module.mp.framework.mp.core.util.MpUtils.*;
import lombok.Data;
import me.chanjar.weixin.common.api.WxConsts;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 公众号消息发送 Request BO
 *
 * 为什么要有该 BO 呢？在自动回复、客服消息、菜单回复消息等场景，都涉及到 MP 给用户发送消息，所以使用该 BO 统一承接
 *
 * @author 芋道源码
 */
@Data
public class MpMessageSendOutReqBO {

    /**
     * 公众号 appId
     */
    @NotEmpty(message = "公众号 appId 不能为空")
    private String appId;
    /**
     * 公众号用户 openid
     */
    @NotEmpty(message = "公众号用户 openid 不能为空")
    private String openid;

    // ========== 消息内容 ==========
    /**
     * 消息类型
     *
     * 枚举 {@link WxConsts.XmlMsgType} 中的 TEXT、IMAGE、VOICE、VIDEO、NEWS
     */
    @NotEmpty(message = "消息类型不能为空")
    public String type;

    /**
     * 消息内容
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 TEXT
     */
    @NotEmpty(message = "消息内容不能为空", groups = TextGroup.class)
    private String content;

    /**
     * 媒体 id
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 IMAGE、VOICE、VIDEO
     */
    @NotEmpty(message = "消息内容不能为空", groups = {ImageGroup.class, VoiceGroup.class, VideoGroup.class})
    private String mediaId;
    /**
     * 媒体 URL
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 IMAGE、VOICE、VIDEO
     */
    @NotEmpty(message = "消息内容不能为空", groups = {ImageGroup.class, VoiceGroup.class, VideoGroup.class})
    private String mediaUrl;

    /**
     * 标题
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 VIDEO
     */
    @NotEmpty(message = "消息内容不能为空", groups = VideoGroup.class)
    private String title;
    /**
     * 描述
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 VIDEO
     */
    @NotEmpty(message = "消息内容不能为空", groups = VideoGroup.class)
    private String description;

    /**
     * 图文消息
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 NEWS
     */
    @Valid
    @NotNull(message = "图文消息不能为空", groups = NewsGroup.class)
    private List<MpMessageDO.Article> articles;

}
