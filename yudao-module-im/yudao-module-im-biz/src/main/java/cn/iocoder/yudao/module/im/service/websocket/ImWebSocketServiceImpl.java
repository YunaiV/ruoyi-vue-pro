package cn.iocoder.yudao.module.im.service.websocket;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.websocket.core.sender.WebSocketMessageSender;
import cn.iocoder.yudao.module.im.service.websocket.dto.ImGroupMessageDTO;
import cn.iocoder.yudao.module.im.service.websocket.dto.ImPrivateMessageDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * IM WebSocket 推送 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class ImWebSocketServiceImpl implements ImWebSocketService {

    @Resource
    private WebSocketMessageSender webSocketMessageSender;

    @Async
    @Override
    public void sendPrivateMessageAsync(Long userId, ImPrivateMessageDTO dto) {
        try {
            webSocketMessageSender.sendObject(UserTypeEnum.ADMIN.getValue(), userId,
                    ImPrivateMessageDTO.TYPE, dto);
        } catch (Exception e) {
            log.error("[sendPrivateMessageAsync][userId({}) dto({}) 发送失败]", userId, dto, e);
        }
    }

    @Async
    @Override
    public void sendGroupMessageAsync(Collection<Long> userIds, ImGroupMessageDTO dto) {
        for (Long userId : userIds) {
            try {
                webSocketMessageSender.sendObject(UserTypeEnum.ADMIN.getValue(), userId,
                        ImGroupMessageDTO.TYPE, dto);
            } catch (Exception e) {
                log.error("[sendGroupMessageAsync][userId({}) dto({}) 发送失败]", userId, dto, e);
            }
        }
    }

}
