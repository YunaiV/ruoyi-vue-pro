package cn.iocoder.yudao.module.mp.service.menu;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.mp.convert.menu.MpMenuConvert;
import cn.iocoder.yudao.module.mp.dal.dataobject.menu.MpMenuDO;
import cn.iocoder.yudao.module.mp.framework.mp.core.MpServiceFactory;
import cn.iocoder.yudao.module.mp.service.message.MpMessageService;
import cn.iocoder.yudao.module.mp.service.message.bo.MpMessageSendOutReqBO;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.menu.WxMenu;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;

import cn.iocoder.yudao.module.mp.controller.admin.menu.vo.*;

import cn.iocoder.yudao.module.mp.dal.mysql.menu.MpMenuMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mp.enums.ErrorCodeConstants.*;

/**
 * 微信菜单 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class MpMenuServiceImpl implements MpMenuService {

    @Resource
    private MpMessageService mpMessageService;

    @Resource
    @Lazy // 延迟加载，避免循环引用报错
    private MpServiceFactory mpServiceFactory;

    @Resource
    private MpMenuMapper mpMenuMapper;

    @Override
    public Long saveMenu(MpMenuSaveReqVO createReqVO) {
        String appId = "wx5b23ba7a5589ecbb";
        // 插入
        MpMenuDO menu = MpMenuConvert.INSTANCE.convert(createReqVO);
//        mpMenuMapper.insert(menu);

        // TODO 同步菜单
        WxMpService mpService = mpServiceFactory.getRequiredMpService(appId);
        WxMenu wxMenu = new WxMenu();
        wxMenu.setButtons(createReqVO.getButtons());
        try {
            mpService.getMenuService().menuCreate(wxMenu);
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }

        // 返回
        return menu.getId();
    }

    @Override
    public void deleteMenu(Long id) {
        // 校验存在
        validateMenuExists(id);
        // 删除
        mpMenuMapper.deleteById(id);
    }

    private void validateMenuExists(Long id) {
        if (mpMenuMapper.selectById(id) == null) {
            // TODO 芋艿：错误码不太对
            throw exception(MENU_NOT_EXISTS);
        }
    }

    @Override
    public MpMenuDO getMenu(Long id) {
        return mpMenuMapper.selectById(id);
    }

    @Override
    public WxMpXmlOutMessage reply(String appId, String key, String openid) {
        // 第一步，获得菜单
        MpMenuDO menu = mpMenuMapper.selectByAppIdAndMenuKey(appId, key);
        if (menu == null) {
            log.error("[reply][appId({}) key({}) 找不到对应的菜单]", appId, key);
            return null;
        }
        // 按钮必须要有消息类型，不然后续无法回复消息
        if (StrUtil.isEmpty(menu.getReplyMessageType())) {
            log.error("[reply][menu({}) 不存在对应的消息类型]", menu);
            return null;
        }

        // 第二步，回复消息
        MpMessageSendOutReqBO sendReqBO = MpMenuConvert.INSTANCE.convert(openid, menu);
        return mpMessageService.sendOutMessage(sendReqBO);
    }

}
