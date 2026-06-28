package cn.iocoder.yudao.module.im.service.websocket.notification.message;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImChannelMessageDO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * IM 频道消息 WebSocket 推送通知
 * <p>
 * 单向：服务端运营推送 → C 端用户；C 端不能向频道发消息。
 * 字段分层：顶层是消息元数据 + 检索维度，content 是 MaterialMessage payload 的 JSON 串。
 *
 * @author 芋道源码
 */
@Data
@Accessors(chain = true)
public class ImChannelMessageNotification {

    /**
     * 消息编号
     */
    private Long id;
    /**
     * 频道编号
     */
    private Long channelId;
    /**
     * 关联素材编号
     */
    private Long materialId;
    /**
     * 消息类型
     */
    private Integer type;
    /**
     * 消息内容；payload JSON 串
     */
    private String content;
    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 由频道消息 DO 构建推送通知
     */
    public static ImChannelMessageNotification ofSend(ImChannelMessageDO message) {
        return BeanUtils.toBean(message, ImChannelMessageNotification.class);
    }

}
