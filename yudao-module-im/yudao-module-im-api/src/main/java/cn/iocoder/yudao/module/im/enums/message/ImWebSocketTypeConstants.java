package cn.iocoder.yudao.module.im.enums.message;

// TODO @芋艿：稍后在 review
/**
 * IM WebSocket 事件类型常量
 *
 * @author 芋道源码
 */
public interface ImWebSocketTypeConstants {

    /**
     * 私聊消息事件
     */
    String PRIVATE_MESSAGE = "im-private-message";
    /**
     * 群聊消息事件
     */
    String GROUP_MESSAGE = "im-group-message";
    /**
     * 已读事件
     */
    String READ = "im-read";
    /**
     * 回执事件
     */
    String RECEIPT = "im-receipt";
    /**
     * 撤回事件
     */
    String RECALL = "im-recall";

}