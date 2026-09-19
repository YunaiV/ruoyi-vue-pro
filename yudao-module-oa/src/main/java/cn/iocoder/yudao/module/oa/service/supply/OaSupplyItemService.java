package cn.iocoder.yudao.module.oa.service.supply;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.oa.controller.admin.supply.vo.item.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.supply.*;

import java.util.*;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 办公用品 Service 接口
 *
 * @author 芋道源码
 */
public interface OaSupplyItemService {

    /**
     * 创建办公用品
     *
     * @param createReqVO 创建信息
     * @return 办公用品编号
     */
    Long createSupplyItem(OaSupplyItemSaveReqVO createReqVO);

    /**
     * 更新办公用品
     *
     * @param updateReqVO 更新信息
     */
    void updateSupplyItem(OaSupplyItemSaveReqVO updateReqVO);

    /**
     * 删除办公用品
     *
     * @param id 编号
     */
    void deleteSupplyItem(Long id);

    /**
     * 获得办公用品
     *
     * @param id 编号
     * @return 办公用品
     */
    OaSupplyItemDO getSupplyItem(Long id);

    /**
     * 获得办公用品分页
     *
     * @param pageReqVO 分页条件
     * @return 办公用品分页
     */
    PageResult<OaSupplyItemDO> getSupplyItemPage(OaSupplyItemPageReqVO pageReqVO);

    /**
     * 获得办公用品列表
     *
     * @param ids 编号集合
     * @return 办公用品列表
     */
    List<OaSupplyItemDO> getSupplyItemList(Collection<Long> ids);

    /**
     * 获得办公用品映射
     *
     * @param ids 编号集合
     * @return 办公用品映射
     */
    default Map<Long, OaSupplyItemDO> getSupplyItemMap(Collection<Long> ids) {
        List<OaSupplyItemDO> items = getSupplyItemList(ids);
        return convertMap(items, OaSupplyItemDO::getId);
    }

    /**
     * 办公用品入库
     *
     * @param reqVO 请求参数
     */
    void stockInSupplyItem(OaSupplyItemStockInReqVO reqVO);

    /**
     * 增减办公用品库存
     *
     * @param id 编号
     * @param quantity 库存增量，扣减时为负数
     */
    void updateSupplyItemStockQuantity(Long id, Integer quantity);

}
