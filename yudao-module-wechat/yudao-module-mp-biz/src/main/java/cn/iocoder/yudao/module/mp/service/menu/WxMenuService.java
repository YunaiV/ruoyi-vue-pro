package cn.iocoder.yudao.module.mp.service.menu;

import java.util.*;
import javax.validation.*;

import cn.iocoder.yudao.module.mp.controller.admin.menu.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.menu.WxMenuDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 微信菜单 Service 接口
 *
 * @author 芋道源码
 */
public interface WxMenuService {

    /**
     * 创建微信菜单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createWxMenu(@Valid WxMenuCreateReqVO createReqVO);

    /**
     * 更新微信菜单
     *
     * @param updateReqVO 更新信息
     */
    void updateWxMenu(@Valid WxMenuUpdateReqVO updateReqVO);

    /**
     * 删除微信菜单
     *
     * @param id 编号
     */
    void deleteWxMenu(Integer id);

    /**
     * 获得微信菜单
     *
     * @param id 编号
     * @return 微信菜单
     */
    WxMenuDO getWxMenu(Integer id);

    /**
     * 获得微信菜单列表
     *
     * @param ids 编号
     * @return 微信菜单列表
     */
    List<WxMenuDO> getWxMenuList(Collection<Integer> ids);

    /**
     * 获得微信菜单分页
     *
     * @param pageReqVO 分页查询
     * @return 微信菜单分页
     */
    PageResult<WxMenuDO> getWxMenuPage(WxMenuPageReqVO pageReqVO);

    /**
     * 获得微信菜单列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 微信菜单列表
     */
    List<WxMenuDO> getWxMenuList(WxMenuExportReqVO exportReqVO);

}
