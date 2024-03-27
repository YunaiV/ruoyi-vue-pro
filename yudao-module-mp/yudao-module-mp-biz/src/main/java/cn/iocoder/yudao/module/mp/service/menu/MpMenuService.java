package cn.iocoder.yudao.module.mp.service.menu;

import cn.iocoder.yudao.module.mp.controller.admin.menu.vo.MpMenuSaveReqVO;
import cn.iocoder.yudao.module.mp.dal.dataobject.menu.MpMenuDO;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;

import javax.validation.Valid;
import java.util.List;

/**
 * 公众号菜单 Service 接口
 *
 * @author 芋道源码
 */
public interface MpMenuService {

    /**
     * 保存公众号菜单
     *
     * @param createReqVO 创建信息
     */
    void saveMenu(@Valid MpMenuSaveReqVO createReqVO);

    /**
     * 删除公众号菜单
     *
     * @param accountId 公众号账号的编号
     */
    void deleteMenuByAccountId(Long accountId);

    /**
     * 粉丝点击菜单按钮时，回复对应的消息
     *
     * @param appId 公众号 AppId
     * @param key 菜单按钮的标识
     * @param openid 粉丝的 openid
     * @return 消息
     */
    WxMpXmlOutMessage reply(String appId, String key, String openid);

    /**
     * 获得公众号菜单列表
     *
     * @param accountId 公众号账号的编号
     * @return 公众号菜单列表
     */
    List<MpMenuDO> getMenuListByAccountId(Long accountId);

}
