package cn.iocoder.yudao.module.promotion.enums;

/**
 * Promotion 的 WebSocket 消息类型枚举类
 *
 * @author HUIHUI
 */
public interface WebSocketMessageTypeConstants {

    // ======================= mall 客服 =======================

    String KEFU_MESSAGE_TYPE = "kefu_message_type"; // 客服消息类型
    String KEFU_MESSAGE_ADMIN_READ = "kefu_message_read_status_change"; // 客服消息管理员已读

}
