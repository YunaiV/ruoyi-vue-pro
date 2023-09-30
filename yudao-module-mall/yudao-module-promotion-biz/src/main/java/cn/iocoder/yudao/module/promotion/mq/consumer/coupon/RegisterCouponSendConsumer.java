package cn.iocoder.yudao.module.promotion.mq.consumer.coupon;

import cn.iocoder.yudao.framework.mq.core.stream.AbstractStreamMessageListener;
import cn.iocoder.yudao.module.member.mq.message.user.RegisterCouponSendMessage;
import cn.iocoder.yudao.module.promotion.service.coupon.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 针对 {@link RegisterCouponSendMessage} 的消费者
 *
 * @author owen
 */
@Component
@Slf4j
public class RegisterCouponSendConsumer extends AbstractStreamMessageListener<RegisterCouponSendMessage> {

    @Resource
    private CouponService couponService;

    @Override
    public void onMessage(RegisterCouponSendMessage message) {
        log.info("[onMessage][消息内容({})]", message);
        couponService.takeCouponByRegister(message.getUserId());
    }

}
