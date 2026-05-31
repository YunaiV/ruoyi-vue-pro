package cn.iocoder.yudao.module.im.service.websocket.dto.message;

import lombok.Data;

/**
 * 位置消息内容
 */
@Data
public class LocationMessage {

    /**
     * 位置描述
     */
    private String description;
    /**
     * 经度
     */
    private Double longitude;
    /**
     * 纬度
     */
    private Double latitude;

}
