package cn.iocoder.yudao.module.im.service.websocket.dto.message;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 频道素材消息 payload
 * <p>
 * 对应 {@link cn.iocoder.yudao.module.im.enums.message.ImMessageTypeEnum#MATERIAL}（type=125）。
 * 客户端按本字段集渲染图文卡片；
 * 富文本正文不在本 payload 中传递，点击详情时另调 /im/channel/material/get-content?id= 按需拉取。
 */
@Data
@Accessors(chain = true)
public class MaterialMessage {

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
     * 跳转链接；为空时点击在客户端内置详情页按 materialId 拉 content 渲染；非空则跳 url
     */
    private String url;

}
