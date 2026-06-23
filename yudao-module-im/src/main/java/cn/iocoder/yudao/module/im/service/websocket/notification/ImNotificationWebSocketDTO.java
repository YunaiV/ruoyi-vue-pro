package cn.iocoder.yudao.module.im.service.websocket.notification;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * IM WebSocket 在线通知外壳
 * <p>
 * conversationType 定位会话维度；contentType 定位业务内容；payload 承载对应通知对象。
 * 会进入聊天流的私聊、群聊、频道事件走 message 子包；不进入聊天流的好友、加群申请、通话信令走 NONE 会话。
 *
 * @author 芋道源码
 */
@Data
@Accessors(chain = true)
public class ImNotificationWebSocketDTO {

    public static final String TYPE = "im-notification";

    /**
     * 会话类型，参见 ImConversationTypeEnum 枚举类
     */
    private Integer conversationType;
    /**
     * 内容类型，参见 ImContentTypeEnum 枚举类
     */
    private Integer contentType;
    /**
     * 负载数据
     */
    private Object payload;

}
