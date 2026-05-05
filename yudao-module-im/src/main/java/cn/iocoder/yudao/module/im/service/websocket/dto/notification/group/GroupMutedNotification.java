package cn.iocoder.yudao.module.im.service.websocket.dto.notification.group;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 全群禁言通知
 */
@Data
@EqualsAndHashCode(callSuper = true) // TODO @AI：不需要添加这个，全局已经默认了；
public class GroupMutedNotification extends BaseGroupNotification {

}
