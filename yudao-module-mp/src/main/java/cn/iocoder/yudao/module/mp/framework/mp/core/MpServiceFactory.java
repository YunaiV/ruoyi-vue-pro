package cn.iocoder.yudao.module.mp.framework.mp.core;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.MpAccountDO;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;

import java.util.List;

/**
 * {@link WxMpService} 工厂接口
 *
 * @author 芋道源码
 */
public interface MpServiceFactory {

    /**
     * 基于微信公众号的账号，初始化对应的 WxMpService 与 WxMpMessageRouter 实例
     *
     * @param list 公众号的账号列表
     */
    void init(List<MpAccountDO> list);

    /**
     * 获得 id 对应的 WxMpService 实例
     *
     * @param id 微信公众号的编号
     * @return WxMpService 实例
     */
    WxMpService getMpService(Long id);

    default WxMpService getRequiredMpService(Long id) {
        WxMpService wxMpService = getMpService(id);
        Assert.notNull(wxMpService, "找到对应 id({}) 的 WxMpService，请核实！", id);
        return wxMpService;
    }

    /**
     * 获得 appId 对应的 WxMpService 实例
     *
     * @param appId 微信公众号 appId
     * @return WxMpService 实例
     */
    WxMpService getMpService(String appId);

    default WxMpService getRequiredMpService(String appId) {
        WxMpService wxMpService = getMpService(appId);
        Assert.notNull(wxMpService, "找到对应 appId({}) 的 WxMpService，请核实！", appId);
        return wxMpService;
    }

    /**
     * 获得 appId 对应的 WxMpMessageRouter 实例
     *
     * @param appId 微信公众号 appId
     * @return WxMpMessageRouter 实例
     */
    WxMpMessageRouter getMpMessageRouter(String appId);

    default WxMpMessageRouter getRequiredMpMessageRouter(String appId) {
        WxMpMessageRouter wxMpMessageRouter = getMpMessageRouter(appId);
        Assert.notNull(wxMpMessageRouter, "找到对应 appId({}) 的 WxMpMessageRouter，请核实！", appId);
        return wxMpMessageRouter;
    }

}
