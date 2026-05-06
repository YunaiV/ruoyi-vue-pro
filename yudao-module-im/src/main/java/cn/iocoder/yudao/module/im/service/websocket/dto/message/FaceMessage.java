package cn.iocoder.yudao.module.im.service.websocket.dto.message;

import lombok.Data;

/**
 * 表情消息内容
 * <p>
 * 承载「贴图 / 自定义表情包」类消息；Unicode emoji（😀😂）仍走 TEXT
 */
@Data
public class FaceMessage {

    /**
     * 表情图 URL
     */
    private String url;
    // TODO @AI：openim 有这个字段么？不是一般 url 就足够了呀。
    /**
     * 渲染宽度（像素）
     * <p>
     * 客户端渲染时按此尺寸占位，避免图片加载完成后的布局抖动
     */
    private Integer width;
    /**
     * 渲染高度（像素）
     */
    private Integer height;
    /**
     * 表情名（可选）
     * <p>
     * 系统表情包通常带名字（如「狗头」「捂脸」），用户私有表情包通常为空
     */
    private String name;

    /**
     * 引用消息（可选）
     */
    private QuoteMessage quote;

}
