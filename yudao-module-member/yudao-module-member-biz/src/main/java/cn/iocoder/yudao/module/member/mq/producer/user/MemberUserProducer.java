package cn.iocoder.yudao.module.member.mq.producer.user;

import cn.iocoder.yudao.framework.mq.core.RedisMQTemplate;
import cn.iocoder.yudao.module.member.mq.message.user.MemberUserCreateMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 会员用户 Producer
 *
 * @author owen
 */
@Slf4j
@Component
public class MemberUserProducer {

    @Resource
    private RedisMQTemplate redisMQTemplate;

    /**
     * 发送 {@link MemberUserCreateMessage} 消息
     *
     * @param userId 用户编号
     */
    public void sendUserCreateMessage(Long userId) {
        redisMQTemplate.send(new MemberUserCreateMessage().setUserId(userId));
    }

}
