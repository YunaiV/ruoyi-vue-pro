package cn.iocoder.yudao.module.im.service.message.dto;

import cn.iocoder.yudao.module.im.enums.message.ImMessageTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * IM 群聊消息发送 DTO
 *
 * @author 芋道源码
 */
@Data
public class ImGroupMessageSendDTO {

    /**
     * 群编号
     */
    private Long groupId;
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
     * @ 目标用户编号列表
     */
    private List<Long> atUserIds;
    /**
     * 定向接收用户编号列表
     * <p>
     * 为空表示全员可见
     */
    private List<Long> receiverUserIds;
    /**
     * 是否需要回执
     * <p>
     * 缺省视为无需回执（false）
     */
    private Boolean receipt;

}
