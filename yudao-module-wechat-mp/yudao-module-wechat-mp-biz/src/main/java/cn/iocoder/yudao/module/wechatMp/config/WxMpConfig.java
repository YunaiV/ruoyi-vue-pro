package cn.iocoder.yudao.module.wechatMp.config;


import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.wechatMp.controller.admin.account.vo.WxAccountExportReqVO;
import cn.iocoder.yudao.module.wechatMp.dal.dataobject.account.WxAccountDO;
import cn.iocoder.yudao.module.wechatMp.handler.*;
import cn.iocoder.yudao.module.wechatMp.service.account.WxAccountService;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.constant.WxMpEventConstants;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// TODO @芋艿：思考有没更好的处理方式
@Component
@Slf4j
public class WxMpConfig implements InitializingBean {

    private static Map<String, WxMpMessageRouter> routers = Maps.newHashMap();
    private static Map<String, WxMpService> mpServices = Maps.newHashMap();

    @Autowired
    private WxAccountService wxAccountService;

    /**
     * 初始化公众号配置
     */
    public synchronized void initWxConfig() {
        WxAccountExportReqVO req = new WxAccountExportReqVO();
        List<WxAccountDO> wxAccountList = wxAccountService.getWxAccountList(req);
        if (CollectionUtils.isEmpty(wxAccountList)) {
            return;
        }
        WxMpConfig.init(wxAccountList);
        log.info("加载公众号配置成功");
    }

    public static void init(List<WxAccountDO> wxAccountDOS) {
        mpServices = wxAccountDOS.stream().map(wxAccountDO -> {
            // TODO 亚洲：使用 WxMpInMemoryConfigStorage 的话，多节点会不会存在 accessToken 冲突
            WxMpInMemoryConfigStorage configStorage = new WxMpInMemoryConfigStorage();
            configStorage.setAppId(wxAccountDO.getAppid());
            configStorage.setSecret(wxAccountDO.getAppsecret());
            configStorage.setToken(wxAccountDO.getToken());
            configStorage.setAesKey(wxAccountDO.getAeskey());

            WxMpService service = new WxMpServiceImpl();
            service.setWxMpConfigStorage(configStorage);
            routers.put(wxAccountDO.getAppid(), newRouter(service));
            return service;
        }).collect(Collectors.toMap(s -> s.getWxMpConfigStorage().getAppId(), a -> a, (o, n) -> o));
    }

    public static Map<String, WxMpMessageRouter> getRouters() {
        return routers;
    }

    public static Map<String, WxMpService> getMpServices() {
        return mpServices;
    }

    private static WxMpMessageRouter newRouter(WxMpService wxMpService) {
        final WxMpMessageRouter newRouter = new WxMpMessageRouter(wxMpService);
        // 记录所有事件的日志 （异步执行）
        newRouter.rule().handler(SpringUtil.getBean(LogHandler.class)).next();

        // 接收客服会话管理事件
        newRouter.rule().async(false).msgType(WxConsts.XmlMsgType.EVENT)
                .event(WxMpEventConstants.CustomerService.KF_CREATE_SESSION)
                .handler(SpringUtil.getBean(KfSessionHandler.class)).end();
        newRouter.rule().async(false).msgType(WxConsts.XmlMsgType.EVENT)
                .event(WxMpEventConstants.CustomerService.KF_CLOSE_SESSION)
                .handler(SpringUtil.getBean(KfSessionHandler.class))
                .end();
        newRouter.rule().async(false).msgType(WxConsts.XmlMsgType.EVENT)
                .event(WxMpEventConstants.CustomerService.KF_SWITCH_SESSION)
                .handler(SpringUtil.getBean(KfSessionHandler.class)).end();

        // 门店审核事件
        newRouter.rule().async(false).msgType(WxConsts.XmlMsgType.EVENT)
                .event(WxMpEventConstants.POI_CHECK_NOTIFY)
                .handler(SpringUtil.getBean(StoreCheckNotifyHandler.class)).end();

        // 自定义菜单事件
        newRouter.rule().async(false).msgType(WxConsts.XmlMsgType.EVENT)
                .event(WxConsts.MenuButtonType.CLICK).handler(SpringUtil.getBean(MenuHandler.class)).end();

        // 点击菜单连接事件
        newRouter.rule().async(false).msgType(WxConsts.XmlMsgType.EVENT)
                .event(WxConsts.MenuButtonType.VIEW).handler(SpringUtil.getBean(NullHandler.class)).end();

        // 关注事件
        newRouter.rule().async(false).msgType(WxConsts.XmlMsgType.EVENT)
                .event(WxConsts.EventType.SUBSCRIBE).handler(SpringUtil.getBean(SubscribeHandler.class))
                .end();

        // 取消关注事件
        newRouter.rule().async(false).msgType(WxConsts.XmlMsgType.EVENT)
                .event(WxConsts.EventType.UNSUBSCRIBE)
                .handler(SpringUtil.getBean(UnsubscribeHandler.class)).end();

        // 上报地理位置事件
        newRouter.rule().async(false).msgType(WxConsts.XmlMsgType.EVENT)
                .event(WxConsts.EventType.LOCATION).handler(SpringUtil.getBean(LocationHandler.class))
                .end();

        // 接收地理位置消息
        newRouter.rule().async(false).msgType(WxConsts.XmlMsgType.LOCATION)
                .handler(SpringUtil.getBean(LocationHandler.class)).end();

        // 扫码事件
        newRouter.rule().async(false).msgType(WxConsts.XmlMsgType.EVENT)
                .event(WxConsts.EventType.SCAN).handler(SpringUtil.getBean(ScanHandler.class)).end();

        // 默认
        newRouter.rule().async(false).handler(SpringUtil.getBean(MsgHandler.class)).end();

        return newRouter;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initWxConfig();
    }
}
