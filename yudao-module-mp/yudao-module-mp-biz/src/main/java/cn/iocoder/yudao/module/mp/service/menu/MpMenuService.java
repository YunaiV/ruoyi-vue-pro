package cn.iocoder.yudao.module.mp.service.menu;

import javax.validation.*;

import cn.iocoder.yudao.module.mp.controller.admin.menu.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.menu.MpMenuDO;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;

/**
 * 微信菜单 Service 接口
 *
 * @author 芋道源码
 */
public interface MpMenuService {

    /**
     * 保存微信菜单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long saveMenu(@Valid MpMenuSaveReqVO createReqVO);

    /**
     * 删除微信菜单
     *
     * @param id 编号
     */
    void deleteMenu(Long id);

    /**
     * 获得微信菜单
     *
     * @param id 编号
     * @return 微信菜单
     */
    MpMenuDO getMenu(Long id);

    /**
     * 用户点击菜单按钮时，回复对应的消息
     *
     * @param appId 公众号 AppId
     * @param key 菜单按钮的标识
     * @param openid 用户的 openid
     * @return 消息
     */
    WxMpXmlOutMessage reply(String appId, String key, String openid);

}
