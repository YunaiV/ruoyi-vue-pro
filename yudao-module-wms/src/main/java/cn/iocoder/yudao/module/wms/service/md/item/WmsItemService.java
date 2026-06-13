package cn.iocoder.yudao.module.wms.service.md.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.md.item.vo.item.WmsItemListReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.md.item.vo.item.WmsItemPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.md.item.vo.item.WmsItemSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * WMS 商品 Service 接口
 *
 * @author 芋道源码
 */
public interface WmsItemService {

    /**
     * 创建商品
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createItem(@Valid WmsItemSaveReqVO createReqVO);

    /**
     * 更新商品
     *
     * @param updateReqVO 更新信息
     */
    void updateItem(@Valid WmsItemSaveReqVO updateReqVO);

    /**
     * 删除商品
     *
     * @param id 编号
     */
    void deleteItem(Long id);

    /**
     * 校验商品存在
     *
     * @param id 编号
     * @return 商品
     */
    WmsItemDO validateItemExists(Long id);

    /**
     * 获得商品
     *
     * @param id 编号
     * @return 商品
     */
    WmsItemDO getItem(Long id);

    /**
     * 获得商品分页
     *
     * @param pageReqVO 分页查询
     * @return 商品分页
     */
    PageResult<WmsItemDO> getItemPage(WmsItemPageReqVO pageReqVO);

    /**
     * 获得商品列表
     *
     * @param listReqVO 查询条件
     * @return 商品列表
     */
    List<WmsItemDO> getItemList(WmsItemListReqVO listReqVO);

    /**
     * 按编号集合获得商品列表
     *
     * @param ids 编号集合
     * @return 商品列表
     */
    List<WmsItemDO> getItemList(Collection<Long> ids);

    /**
     * 按编号集合获得商品 Map
     *
     * @param ids 编号集合
     * @return 商品 Map
     */
    default Map<Long, WmsItemDO> getItemMap(Collection<Long> ids) {
        return convertMap(getItemList(ids), WmsItemDO::getId);
    }

    /**
     * 获得指定商品分类下的商品数量
     *
     * @param categoryId 商品分类编号
     * @return 商品数量
     */
    long getItemCountByCategoryId(Long categoryId);

    /**
     * 获得指定商品品牌下的商品数量
     *
     * @param brandId 商品品牌编号
     * @return 商品数量
     */
    long getItemCountByBrandId(Long brandId);

}
