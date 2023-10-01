package cn.iocoder.yudao.module.member.mq.producer.user;

import cn.iocoder.yudao.framework.mq.core.RedisMQTemplate;
import cn.iocoder.yudao.module.member.mq.message.user.RegisterCouponSendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

// TODO @疯狂：发 UserCreateMessage；解耦，然后优惠劵监听到，去发卷；
/**
 * 新人券发放 Producer
 *
 * @author owen
 */
@Slf4j
@Component
public class RegisterCouponProducer {

    @Resource
    private RedisMQTemplate redisMQTemplate;

    /**
     * 发送 {@link RegisterCouponSendMessage} 消息
     *
     * @param userId 用户编号
     */
    public void sendMailSendMessage(Long userId) {
        redisMQTemplate.send(new RegisterCouponSendMessage().setUserId(userId));
    }

}
