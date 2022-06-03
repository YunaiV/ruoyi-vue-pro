package cn.iocoder.yudao.module.mp.service.newsarticleitem;

import java.util.*;
import javax.validation.*;

import cn.iocoder.yudao.module.mp.controller.admin.newsarticleitem.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.newsarticleitem.WxNewsArticleItemDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 图文消息文章列表表  Service 接口
 *
 * @author 芋道源码
 */
public interface WxNewsArticleItemService {

    /**
     * 创建图文消息文章列表表
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createWxNewsArticleItem(@Valid WxNewsArticleItemCreateReqVO createReqVO);

    /**
     * 更新图文消息文章列表表
     *
     * @param updateReqVO 更新信息
     */
    void updateWxNewsArticleItem(@Valid WxNewsArticleItemUpdateReqVO updateReqVO);

    /**
     * 删除图文消息文章列表表
     *
     * @param id 编号
     */
    void deleteWxNewsArticleItem(Integer id);

    /**
     * 获得图文消息文章列表表
     *
     * @param id 编号
     * @return 图文消息文章列表表
     */
    WxNewsArticleItemDO getWxNewsArticleItem(Integer id);

    /**
     * 获得图文消息文章列表表 列表
     *
     * @param ids 编号
     * @return 图文消息文章列表表 列表
     */
    List<WxNewsArticleItemDO> getWxNewsArticleItemList(Collection<Integer> ids);

    /**
     * 获得图文消息文章列表表 分页
     *
     * @param pageReqVO 分页查询
     * @return 图文消息文章列表表 分页
     */
    PageResult<WxNewsArticleItemDO> getWxNewsArticleItemPage(WxNewsArticleItemPageReqVO pageReqVO);

    /**
     * 获得图文消息文章列表表 列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 图文消息文章列表表 列表
     */
    List<WxNewsArticleItemDO> getWxNewsArticleItemList(WxNewsArticleItemExportReqVO exportReqVO);

}
