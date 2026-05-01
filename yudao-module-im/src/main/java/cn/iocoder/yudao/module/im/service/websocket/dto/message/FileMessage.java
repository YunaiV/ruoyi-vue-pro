package cn.iocoder.yudao.module.im.service.websocket.dto.message;

import lombok.Data;

/**
 * 文件消息内容
 */
@Data
public class FileMessage {

    /**
     * 文件 URL
     */
    private String url;
    /**
     * 文件名
     */
    private String name;
    /**
     * 文件大小（字节）
     */
    private Long size;
    /**
     * 文件类型（扩展名）
     */
    private String type;

    /**
     * 引用消息（可选）
     */
    private QuoteMessage quote;

}
