package cn.iocoder.yudao.module.im.service.message.dto;

import cn.iocoder.yudao.module.im.enums.message.ImMessageTypeEnum;
import lombok.Data;

/**
 * IM 私聊消息发送 DTO
 *
 * @author 芋道源码
 */
@Data
public class ImPrivateMessageSendDTO {

    /**
     * 接收人编号
     */
    private Long receiverId;
    /**
     * 消息类型
     * <p>
     * 枚举 {@link ImMessageTypeEnum}
     */
    private Integer type;
    /**
     * 消息内容
     * <p>
     * 支持 String / POJO；非 String 时由 service 序列化为 JSON
     */
    private Object content;

}
