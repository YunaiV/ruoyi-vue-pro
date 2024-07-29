package cn.iocoder.yudao.module.pay.message.subscribe;

import cn.iocoder.yudao.module.system.api.social.SocialClientApi;
import cn.iocoder.yudao.module.system.enums.social.SocialTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;

import static cn.iocoder.yudao.module.pay.enums.MessageTemplateConstants.PAY_WALLET_CHANGE;

/**
 * 订阅消息
 *
 * @author HUIHUI
 */
@Component
@Slf4j
public class SubscribeMessageClient {

    public static final String WALLET_MONEY_PATH = "pages/user/wallet/money"; // 钱包详情页

    @Resource
    public SocialClientApi socialClientApi;

    /**
     * 发送钱包充值通知
     *
     * @param messages 消息
     * @param userType 用户类型
     * @param userId   用户编号
     */
    @Async
    public void sendPayWalletChangeMessage(Map<String, String> messages, Integer userType, Long userId) {
        sendWxMessage(PAY_WALLET_CHANGE, messages, userType, userId, WALLET_MONEY_PATH);
    }


    /**
     * 发送微信订阅消息
     *
     * @param templateTitle 模版标题
     * @param messages      消息
     * @param userType      用户类型
     * @param userId        用户编号
     * @param path          点击模板卡片后的跳转页面，仅限本小程序内的页面
     */
    private void sendWxMessage(String templateTitle, Map<String, String> messages, Integer userType, Long userId,
                               String path) {
        socialClientApi.sendSubscribeMessage(templateTitle, messages, userType, userId, SocialTypeEnum.WECHAT_MINI_APP.getType(), path);
    }

}
