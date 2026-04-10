package cn.iocoder.yudao.module.mp.service.menu;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.mp.controller.admin.menu.vo.MpMenuSaveReqVO;
import cn.iocoder.yudao.module.mp.convert.menu.MpMenuConvert;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.MpAccountDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.menu.MpMenuDO;
import cn.iocoder.yudao.module.mp.dal.mysql.menu.MpMenuMapper;
import cn.iocoder.yudao.module.mp.framework.mp.core.MpServiceFactory;
import cn.iocoder.yudao.module.mp.framework.mp.core.util.MpUtils;
import cn.iocoder.yudao.module.mp.service.account.MpAccountService;
import cn.iocoder.yudao.module.mp.service.message.MpMessageService;
import cn.iocoder.yudao.module.mp.service.message.bo.MpMessageSendOutReqBO;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.menu.WxMenu;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import jakarta.validation.Validator;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mp.enums.ErrorCodeConstants.MENU_DELETE_FAIL;
import static cn.iocoder.yudao.module.mp.enums.ErrorCodeConstants.MENU_SAVE_FAIL;

/**
 * 公众号菜单 Service 实现类
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
    private MpAccountService mpAccountService;

    @Resource
    @Lazy // 延迟加载，避免循环引用报错
    private MpServiceFactory mpServiceFactory;

    @Resource
    private Validator validator;

    @Resource
    private MpMenuMapper mpMenuMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMenu(MpMenuSaveReqVO createReqVO) {
        MpAccountDO account = mpAccountService.getRequiredAccount(createReqVO.getAccountId());
        WxMpService mpService = mpServiceFactory.getRequiredMpService(createReqVO.getAccountId());

        // 参数校验
        createReqVO.getMenus().forEach(this::validateMenu);

        // 第一步，同步公众号
        WxMenu wxMenu = new WxMenu();
        wxMenu.setButtons(MpMenuConvert.INSTANCE.convert(createReqVO.getMenus()));
        try {
            mpService.getMenuService().menuCreate(wxMenu);
        } catch (WxErrorException e) {
            throw exception(MENU_SAVE_FAIL, e.getError().getErrorMsg());
        }

        // 第二步，存储到数据库
        mpMenuMapper.deleteByAccountId(createReqVO.getAccountId());
        createReqVO.getMenus().forEach(menu -> {
            // 先保存顶级菜单
            MpMenuDO menuDO = createMenu(menu, null, account);
            // 再保存子菜单
            if (CollUtil.isEmpty(menu.getChildren())) {
                return;
            }
            menu.getChildren().forEach(childMenu -> createMenu(childMenu, menuDO, account));
        });
    }

    /**
     * 校验菜单的格式是否正确
     *
     * @param menu 菜单
     */
    private void validateMenu(MpMenuSaveReqVO.Menu menu) {
        MpUtils.validateButton(validator, menu.getType(), menu.getReplyMessageType(), menu);
        // 子菜单
        if (CollUtil.isEmpty(menu.getChildren())) {
            return;
        }
        menu.getChildren().forEach(this::validateMenu);
    }

    /**
     * 创建菜单，并存储到数据库
     *
     * @param wxMenu 菜单信息
     * @param parentMenu 父菜单
     * @param account 公众号账号
     * @return 创建后的菜单
     */
    private MpMenuDO createMenu(MpMenuSaveReqVO.Menu wxMenu, MpMenuDO parentMenu, MpAccountDO account) {
        // 创建菜单
        MpMenuDO menu = CollUtil.isNotEmpty(wxMenu.getChildren())
                ? new MpMenuDO().setName(wxMenu.getName())
                : MpMenuConvert.INSTANCE.convert02(wxMenu);
        // 设置菜单的公众号账号信息
        if (account != null) {
            menu.setAccountId(account.getId()).setAppId(account.getAppId());
        }
        // 设置父编号
        if (parentMenu != null) {
            menu.setParentId(parentMenu.getId());
        } else {
            menu.setParentId(MpMenuDO.ID_ROOT);
        }

        // 插入到数据库
        mpMenuMapper.insert(menu);
        return menu;
    }

    @Override
    public void deleteMenuByAccountId(Long accountId) {
        WxMpService mpService = mpServiceFactory.getRequiredMpService(accountId);
        // 第一步，同步公众号
        try {
            mpService.getMenuService().menuDelete();
        } catch (WxErrorException e) {
            throw exception(MENU_DELETE_FAIL, e.getError().getErrorMsg());
        }

        // 第二步，存储到数据库
        mpMenuMapper.deleteByAccountId(accountId);
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

    @Override
    public List<MpMenuDO> getMenuListByAccountId(Long accountId) {
        return mpMenuMapper.selectListByAccountId(accountId);
    }

}
