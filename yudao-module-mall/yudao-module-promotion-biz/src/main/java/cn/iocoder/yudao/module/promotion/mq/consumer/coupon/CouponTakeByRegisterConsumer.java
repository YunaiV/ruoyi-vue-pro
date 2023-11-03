package cn.iocoder.yudao.module.promotion.mq.consumer.coupon;

import cn.iocoder.yudao.module.member.message.user.MemberUserCreateMessage;
import cn.iocoder.yudao.module.promotion.service.coupon.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 用户注册时，发送优惠劵的消费者，基 {@link MemberUserCreateMessage} 消息
 *
 * @author owen
 */
@Component
@Slf4j
public class CouponTakeByRegisterConsumer {

    @Resource
    private CouponService couponService;

    @EventListener
    @Async // Spring Event 默认在 Producer 发送的线程，通过 @Async 实现异步
    public void onMessage(MemberUserCreateMessage message) {
        log.info("[onMessage][消息内容({})]", message);
        couponService.takeCouponByRegister(message.getUserId());
    }

}
