package cn.iocoder.yudao.module.im.dal.dataobject.message.content;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 频道素材消息 payload
 * <p>
 * 对应 {@link cn.iocoder.yudao.module.im.enums.ImContentTypeEnum#MATERIAL}（type=125）。
 * 客户端按本字段集渲染图文卡片；
 * 富文本正文不在本 payload 中传递，点击详情时另调 /im/channel/material/get-content?id= 按需拉取。
 */
@Data
@Accessors(chain = true)
public class MaterialMessage {

    /**
     * 素材编号
     */
    private Long materialId;
    /**
     * 频道编号
     * <p>
     * 冗余到 payload 内；转发到私聊 / 群聊后客户端用本字段定位频道，渲染卡片底部的频道头像 + 名称
     */
    private Long channelId;
    /**
     * 素材内容类型
     * <p>
     * 枚举 {@link cn.iocoder.yudao.module.im.enums.channel.ImChannelMaterialTypeEnum}
     * 客户端按本字段判定点击行为：CONTENT 走站内详情页拉富文本；LINK 跳 url
     */
    private Integer type;
    /**
     * 标题
     */
    private String title;
    /**
     * 封面图
     */
    private String coverUrl;
    /**
     * 摘要
     */
    private String summary;
    /**
     * 跳转链接
     */
    private String url;

}
