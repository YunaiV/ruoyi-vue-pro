package cn.iocoder.yudao.module.im.service.websocket.dto.friend;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 好友更新消息内容（目前仅免打扰）
 */
@Data
@Accessors(chain = true)
public class FriendUpdateMessage {

    /**
     * 好友的用户编号
     */
    private Long friendUserId;
    /**
     * 是否免打扰
     */
    private Boolean muted;

}
