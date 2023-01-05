package cn.iocoder.yudao.module.mp.service.menu;

import cn.iocoder.yudao.module.mp.convert.menu.MpMenuConvert;
import cn.iocoder.yudao.module.mp.dal.dataobject.menu.MpMenuDO;
import cn.iocoder.yudao.module.mp.framework.mp.core.MpServiceFactory;
import me.chanjar.weixin.common.bean.menu.WxMenu;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
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
public class MpMenuServiceImpl implements MpMenuService {

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
            throw exception(COMMON_NOT_EXISTS);
        }
    }

    @Override
    public MpMenuDO getMenu(Long id) {
        return mpMenuMapper.selectById(id);
    }

}
