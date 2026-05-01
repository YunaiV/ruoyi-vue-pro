package cn.iocoder.yudao.module.im.service.websocket.dto.message;

import lombok.Data;

/**
 * 图片消息内容
 */
@Data
public class ImageMessage {

    /**
     * 图片 URL
     */
    private String url;
    /**
     * 缩略图 URL
     */
    private String thumbnailUrl;
    /**
     * 图片宽度
     */
    private Integer width;
    /**
     * 图片高度
     */
    private Integer height;
    /**
     * 图片大小（字节）
     */
    private Long size;

    /**
     * 引用消息（可选）
     */
    private QuoteMessage quote;

}
