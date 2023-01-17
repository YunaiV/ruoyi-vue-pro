package cn.iocoder.yudao.module.mp.framework.mp.core;

import cn.iocoder.yudao.module.mp.dal.dataobject.account.MpAccountDO;
import cn.iocoder.yudao.module.mp.service.handler.menu.MenuHandler;
import cn.iocoder.yudao.module.mp.service.handler.message.MessageReceiveHandler;
import cn.iocoder.yudao.module.mp.service.handler.message.MessageAutoReplyHandler;
import cn.iocoder.yudao.module.mp.service.handler.other.KfSessionHandler;
import cn.iocoder.yudao.module.mp.service.handler.other.NullHandler;
import cn.iocoder.yudao.module.mp.service.handler.other.ScanHandler;
import cn.iocoder.yudao.module.mp.service.handler.other.StoreCheckNotifyHandler;
import cn.iocoder.yudao.module.mp.service.handler.user.LocationHandler;
import cn.iocoder.yudao.module.mp.service.handler.user.SubscribeHandler;
import cn.iocoder.yudao.module.mp.service.handler.user.UnsubscribeHandler;
import com.binarywang.spring.starter.wxjava.mp.properties.WxMpProperties;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.redis.RedisTemplateWxRedisOps;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpRedisConfigImpl;
import me.chanjar.weixin.mp.constant.WxMpEventConstants;

import java.util.List;
import java.util.Map;

/**
 * 默认的 {@link MpServiceFactory} 实现类
 *
 * @author 芋道源码
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultMpServiceFactory implements MpServiceFactory {

    /**
     * 微信 appId 与 WxMpService 的映射
     */
    private volatile Map<String, WxMpService> appId2MpServices;
    /**
     * 公众号账号 id 与 WxMpService 的映射
     */
    private volatile Map<Long, WxMpService> id2MpServices;
    /**
     * 微信 appId 与 WxMpMessageRouter 的映射
     */
    private volatile Map<String, WxMpMessageRouter> mpMessageRouters;

    private final RedisTemplateWxRedisOps redisTemplateWxRedisOps;
    private final WxMpProperties mpProperties;

    // ========== 各种 Handler ==========

    private final MessageReceiveHandler messageReceiveHandler;
    private final KfSessionHandler kfSessionHandler;
    private final StoreCheckNotifyHandler storeCheckNotifyHandler;
    private final MenuHandler menuHandler;
    private final NullHandler nullHandler;
    private final SubscribeHandler subscribeHandler;
    private final UnsubscribeHandler unsubscribeHandler;
    private final LocationHandler locationHandler;
    private final ScanHandler scanHandler;
    private final MessageAutoReplyHandler messageAutoReplyHandler;

    @Override
    public void init(List<MpAccountDO> list) {
        Map<String, WxMpService> appId2MpServices = Maps.newHashMap();
        Map<Long, WxMpService> id2MpServices = Maps.newHashMap();
        Map<String, WxMpMessageRouter> mpMessageRouters = Maps.newHashMap();
        // 处理 list
        list.forEach(account -> {
            // 构建 WxMpService 对象
            WxMpService mpService = buildMpService(account);
            appId2MpServices.put(account.getAppId(), mpService);
            id2MpServices.put(account.getId(), mpService);
            // 构建 WxMpMessageRouter 对象
            WxMpMessageRouter mpMessageRouter = buildMpMessageRouter(mpService);
            mpMessageRouters.put(account.getAppId(), mpMessageRouter);
        });

        // 设置到缓存
        this.appId2MpServices = appId2MpServices;
        this.id2MpServices = id2MpServices;
        this.mpMessageRouters = mpMessageRouters;
    }

    @Override
    public WxMpService getMpService(Long id) {
        return id2MpServices.get(id);
    }

    @Override
    public WxMpService getMpService(String appId) {
        return appId2MpServices.get(appId);
    }

    @Override
    public WxMpMessageRouter getMpMessageRouter(String appId) {
        return mpMessageRouters.get(appId);
    }

    private WxMpService buildMpService(MpAccountDO account) {
        // 第一步，创建 WxMpRedisConfigImpl 对象
        WxMpRedisConfigImpl configStorage = new WxMpRedisConfigImpl(
                redisTemplateWxRedisOps, mpProperties.getConfigStorage().getKeyPrefix());
        configStorage.setAppId(account.getAppId());
        configStorage.setSecret(account.getAppSecret());
        configStorage.setToken(account.getToken());
        configStorage.setAesKey(account.getAesKey());

        // 第二步，创建 WxMpService 对象
        WxMpService service = new WxMpServiceImpl();
        service.setWxMpConfigStorage(configStorage);
        return service;
    }

    private WxMpMessageRouter buildMpMessageRouter(WxMpService mpService) {
        WxMpMessageRouter router = new WxMpMessageRouter(mpService);
        // 记录所有事件的日志（异步执行）
        router.rule().handler(messageReceiveHandler).next();

        // 接收客服会话管理事件
        router.rule().async(false).msgType(WxConsts.XmlMsgType.EVENT)
                .event(WxMpEventConstants.CustomerService.KF_CREATE_SESSION)
                .handler(kfSessionHandler).end();
        router.rule().async(false).msgType(WxConsts.XmlMsgType.EVENT)
                .event(WxMpEventConstants.CustomerService.KF_CLOSE_SESSION)
                .handler(kfSessionHandler)
                .end();
        router.rule().async(false).msgType(WxConsts.XmlMsgType.EVENT)
                .event(WxMpEventConstants.CustomerService.KF_SWITCH_SESSION)
                .handler(kfSessionHandler).end();

        // 门店审核事件
        router.rule().async(false).msgType(WxConsts.XmlMsgType.EVENT)
                .event(WxMpEventConstants.POI_CHECK_NOTIFY)
                .handler(storeCheckNotifyHandler).end();

        // 自定义菜单事件
        router.rule().async(false).msgType(WxConsts.XmlMsgType.EVENT)
                .event(WxConsts.MenuButtonType.CLICK).handler(menuHandler).end();

        // 点击菜单连接事件
        router.rule().async(false).msgType(WxConsts.XmlMsgType.EVENT)
                .event(WxConsts.MenuButtonType.VIEW).handler(nullHandler).end();

        // 关注事件
        router.rule().async(false).msgType(WxConsts.XmlMsgType.EVENT)
                .event(WxConsts.EventType.SUBSCRIBE).handler(subscribeHandler)
                .end();

        // 取消关注事件
        router.rule().async(false).msgType(WxConsts.XmlMsgType.EVENT)
                .event(WxConsts.EventType.UNSUBSCRIBE)
                .handler(unsubscribeHandler).end();

        // 上报地理位置事件
        router.rule().async(false).msgType(WxConsts.XmlMsgType.EVENT)
                .event(WxConsts.EventType.LOCATION).handler(locationHandler)
                .end();

        // 接收地理位置消息
        router.rule().async(false).msgType(WxConsts.XmlMsgType.LOCATION)
                .handler(locationHandler).end();

        // 扫码事件
        router.rule().async(false).msgType(WxConsts.XmlMsgType.EVENT)
                .event(WxConsts.EventType.SCAN).handler(scanHandler).end();

        // 默认
        router.rule().async(false).handler(messageAutoReplyHandler).end();
        return router;
    }

}
