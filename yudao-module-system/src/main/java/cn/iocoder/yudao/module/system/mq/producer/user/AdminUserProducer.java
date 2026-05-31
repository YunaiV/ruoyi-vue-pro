package cn.iocoder.yudao.module.system.mq.producer.user;

import cn.iocoder.yudao.module.system.api.message.user.AdminUserProfileUpdateMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 管理员用户 Producer
 *
 * @author 芋道源码
 */
@Slf4j
@Component
public class AdminUserProducer {

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 发送 {@link AdminUserProfileUpdateMessage} 消息
     *
     * @param userId   用户编号
     * @param nickname 变更后的昵称（无变更传 null）
     * @param avatar   变更后的头像（无变更传 null）
     */
    public void sendUserProfileUpdateMessage(Long userId, String nickname, String avatar) {
        applicationContext.publishEvent(new AdminUserProfileUpdateMessage()
                .setUserId(userId).setNickname(nickname).setAvatar(avatar));
    }

}
