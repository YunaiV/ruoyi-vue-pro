package cn.iocoder.yudao.module.mp.service.menu;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;

import java.util.*;

import cn.iocoder.yudao.module.mp.controller.admin.menu.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.menu.WxMenuDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.mp.convert.menu.WxMenuConvert;
import cn.iocoder.yudao.module.mp.dal.mysql.menu.WxMenuMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mp.enums.ErrorCodeConstants.*;

/**
 * 微信菜单 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class WxMenuServiceImpl implements WxMenuService {

    @Resource
    private WxMenuMapper wxMenuMapper;

    @Override
    public Integer createWxMenu(WxMenuCreateReqVO createReqVO) {
        // 插入
        WxMenuDO wxMenu = WxMenuConvert.INSTANCE.convert(createReqVO);
        wxMenuMapper.insert(wxMenu);
        // 返回
        return wxMenu.getId();
    }

    @Override
    public void updateWxMenu(WxMenuUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateWxMenuExists(updateReqVO.getId());
        // 更新
        WxMenuDO updateObj = WxMenuConvert.INSTANCE.convert(updateReqVO);
        wxMenuMapper.updateById(updateObj);
    }

    @Override
    public void deleteWxMenu(Integer id) {
        // 校验存在
        this.validateWxMenuExists(id);
        // 删除
        wxMenuMapper.deleteById(id);
    }

    private void validateWxMenuExists(Integer id) {
        if (wxMenuMapper.selectById(id) == null) {
            throw exception(COMMON_NOT_EXISTS);
        }
    }

    @Override
    public WxMenuDO getWxMenu(Integer id) {
        return wxMenuMapper.selectById(id);
    }

    @Override
    public List<WxMenuDO> getWxMenuList(Collection<Integer> ids) {
        return wxMenuMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<WxMenuDO> getWxMenuPage(WxMenuPageReqVO pageReqVO) {
        return wxMenuMapper.selectPage(pageReqVO);
    }

    @Override
    public List<WxMenuDO> getWxMenuList(WxMenuExportReqVO exportReqVO) {
        return wxMenuMapper.selectList(exportReqVO);
    }

}
