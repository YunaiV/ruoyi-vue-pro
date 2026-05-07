package cn.iocoder.yudao.module.im.service.websocket.dto.message;

import cn.iocoder.yudao.module.im.enums.ImConversationTypeEnum;
import lombok.Data;

/**
 * 名片消息内容
 * <p>
 * 支持两类名片，按 targetType 区分：
 * 1. 用户名片（targetType = PRIVATE）：targetId = 用户编号，name = 用户昵称
 * 2. 群名片（targetType = GROUP）：targetId = 群编号，name = 群名，可带 memberCount
 */
@Data
public class CardMessage {

    /**
     * 名片对象类型
     * <p>
     * 枚举 {@link ImConversationTypeEnum}
     */
    private Integer targetType;
    /**
     * 目标对象编号：PRIVATE 时 = 用户编号；GROUP 时 = 群编号
     */
    private Long targetId;
    /**
     * 显示名快照：PRIVATE 时 = 用户昵称；GROUP 时 = 群名
     */
    private String name;
    /**
     * 头像（快照）
     */
    private String avatar;
    /**
     * 群成员数（仅 targetType = GROUP；接收端展示「N 人群聊」）
     */
    private Integer memberCount;

    /**
     * 引用消息（可选）
     */
    private QuoteMessage quote;

}
