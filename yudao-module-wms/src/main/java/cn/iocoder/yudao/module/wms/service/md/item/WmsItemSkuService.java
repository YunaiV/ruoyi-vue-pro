package cn.iocoder.yudao.module.wms.service.md.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.md.item.vo.sku.WmsItemSkuPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.md.item.vo.sku.WmsItemSkuSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemSkuDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;

/**
 * WMS 商品 SKU Service 接口
 *
 * @author 芋道源码
 */
public interface WmsItemSkuService {

    /**
     * 创建商品 SKU 列表
     *
     * @param itemId 商品编号
     * @param skus SKU 列表
     */
    void createItemSkuList(Long itemId, @Valid List<WmsItemSkuSaveReqVO> skus);

    /**
     * 更新商品 SKU 列表
     *
     * @param itemId 商品编号
     * @param skus SKU 列表
     */
    void updateItemSkuList(Long itemId, @Valid List<WmsItemSkuSaveReqVO> skus);

    /**
     * 按商品编号删除 SKU 列表
     *
     * @param itemId 商品编号
     */
    void deleteItemSkuListByItemId(Long itemId);

    /**
     * 校验 SKU 存在
     *
     * @param id 编号
     * @return SKU
     */
    WmsItemSkuDO validateItemSkuExists(Long id);

    /**
     * 按商品编号获得 SKU 列表
     *
     * @param itemId 商品编号
     * @return SKU 列表
     */
    List<WmsItemSkuDO> getItemSkuList(Long itemId);

    /**
     * 按商品编号集合获得 SKU 列表
     *
     * @param itemIds 商品编号集合
     * @return SKU 列表
     */
    List<WmsItemSkuDO> getItemSkuList(Collection<Long> itemIds);

    /**
     * 获得 SKU 列表
     *
     * @param itemIds 商品编号集合
     * @param code SKU 编号
     * @param name SKU 名称
     * @return SKU 列表
     */
    List<WmsItemSkuDO> getItemSkuList(Collection<Long> itemIds, String code, String name);

    /**
     * 按编号集合获得 SKU 列表
     *
     * @param ids 编号集合
     * @return SKU 列表
     */
    List<WmsItemSkuDO> getItemSkuListByIds(Collection<Long> ids);

    /**
     * 按 SKU 维度分页查询，支持商品 / 品牌 / 分类多表联查筛选。
     *
     * @param pageReqVO 分页与筛选条件
     * @return SKU 分页结果
     */
    PageResult<WmsItemSkuDO> getItemSkuPage(WmsItemSkuPageReqVO pageReqVO);

    /**
     * 按编号集合获得 SKU Map
     *
     * @param ids 编号集合
     * @return SKU Map
     */
    default Map<Long, WmsItemSkuDO> getItemSkuMap(Collection<Long> ids) {
        return convertMap(getItemSkuListByIds(ids), WmsItemSkuDO::getId);
    }

    /**
     * 按商品编号集合获得 SKU MultiMap
     *
     * @param itemIds 商品编号集合
     * @return 商品编号与 SKU 列表的映射
     */
    default Map<Long, List<WmsItemSkuDO>> getItemSkuMultiMap(Collection<Long> itemIds) {
        return convertMultiMap(getItemSkuList(itemIds), WmsItemSkuDO::getItemId);
    }

}
