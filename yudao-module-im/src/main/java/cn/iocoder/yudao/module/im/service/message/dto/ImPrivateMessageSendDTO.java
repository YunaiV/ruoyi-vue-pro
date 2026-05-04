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
    /**
     * 是否持久化 + 推送给接收方（单边语义开关）
     * <p>
     * null：默认按 {@link ImMessageTypeEnum#isPersistent()} 决定是否入库 + 双向 WS 推送（保持原行为）<br>
     * false：覆盖为单边——不入库、仅推 sender 多端，对方不感知（如「你已删除 XXX」这类仅自己可见的 TIP）<br>
     * true：覆盖为双向 + 入库
     */
    private Boolean persistent;

}
