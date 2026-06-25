package cn.iocoder.yudao.module.im.dal.dataobject.message.content;

import lombok.Data;

/**
 * 视频消息内容
 */
@Data
public class VideoMessage {

    /**
     * 视频文件 URL
     */
    private String url;
    /**
     * 视频封面 URL
     */
    private String coverUrl;
    /**
     * 视频时长（秒）
     */
    private Integer duration;
    /**
     * 视频宽度
     */
    private Integer width;
    /**
     * 视频高度
     */
    private Integer height;
    /**
     * 文件大小（字节）
     */
    private Long size;

    /**
     * 引用消息（可选）
     */
    private QuoteMessage quote;

}
