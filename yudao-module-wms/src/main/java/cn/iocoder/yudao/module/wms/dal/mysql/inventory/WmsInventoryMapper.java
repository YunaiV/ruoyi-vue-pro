package cn.iocoder.yudao.module.wms.dal.mysql.inventory;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.WmsInventoryListReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.WmsInventoryPageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemSkuDO;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * WMS 库存 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface WmsInventoryMapper extends BaseMapperX<WmsInventoryDO> {

    default PageResult<WmsInventoryDO> selectPage(WmsInventoryPageReqVO reqVO) {
        MPJLambdaWrapperX<WmsInventoryDO> query = new MPJLambdaWrapperX<WmsInventoryDO>()
                .selectAll(WmsInventoryDO.class)
                .innerJoin(WmsItemSkuDO.class, WmsItemSkuDO::getId, WmsInventoryDO::getSkuId)
                .innerJoin(WmsItemDO.class, WmsItemDO::getId, WmsItemSkuDO::getItemId)
                .likeIfPresent(WmsItemDO::getCode, reqVO.getItemCode())
                .likeIfPresent(WmsItemDO::getName, reqVO.getItemName())
                .eqIfPresent(WmsInventoryDO::getSkuId, reqVO.getSkuId())
                .likeIfPresent(WmsItemSkuDO::getCode, reqVO.getSkuCode())
                .likeIfPresent(WmsItemSkuDO::getName, reqVO.getSkuName())
                .eqIfPresent(WmsInventoryDO::getWarehouseId, reqVO.getWarehouseId())
                .geIfPresent(WmsInventoryDO::getQuantity, reqVO.getMinQuantity());
        if (Boolean.TRUE.equals(reqVO.getOnlyPositiveQuantity())) {
            query.gt(WmsInventoryDO::getQuantity, BigDecimal.ZERO);
        }
        appendDimensionOrder(query, reqVO.getType());
        return selectJoinPage(reqVO, WmsInventoryDO.class, query);
    }

    default Long selectCountBySkuId(Long skuId) {
        return selectCount(WmsInventoryDO::getSkuId, skuId);
    }

    default Long selectCountByWarehouseId(Long warehouseId) {
        return selectCount(WmsInventoryDO::getWarehouseId, warehouseId);
    }

    default List<WmsInventoryDO> selectList(WmsInventoryListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<WmsInventoryDO>()
                .eq(WmsInventoryDO::getWarehouseId, reqVO.getWarehouseId())
                .orderByAsc(WmsInventoryDO::getSkuId)
                .orderByAsc(WmsInventoryDO::getId));
    }

    default WmsInventoryDO selectBySkuIdAndWarehouseId(Long skuId, Long warehouseId) {
        return selectOne(WmsInventoryDO::getSkuId, skuId,
                WmsInventoryDO::getWarehouseId, warehouseId);
    }

    default WmsInventoryDO selectByIdForUpdate(Long id) {
        return selectOne(new LambdaQueryWrapperX<WmsInventoryDO>()
                .eq(WmsInventoryDO::getId, id)
                .last("FOR UPDATE"));
    }

    /**
     * 根据多个唯一键，批量查询库存列表
     *
     * @param keys 唯一键列表：由 SKU 编号 + 仓库编号组成
     * @return 库存列表
     */
    default List<WmsInventoryDO> selectListByKeys(Collection<WmsInventoryDO> keys) {
        if (CollUtil.isEmpty(keys)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<WmsInventoryDO>()
                .and(query -> {
                    boolean first = true;
                    for (WmsInventoryDO key : keys) {
                        if (!first) {
                            query.or();
                        }
                        query.eq(WmsInventoryDO::getSkuId, key.getSkuId())
                                .eq(WmsInventoryDO::getWarehouseId, key.getWarehouseId());
                        first = false;
                    }
                }));
    }

    default List<WmsInventoryDO> selectListByIdsForUpdate(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<WmsInventoryDO>()
                .in(WmsInventoryDO::getId, ids)
                .orderByAsc(WmsInventoryDO::getId)
                .last("FOR UPDATE"));
    }

    static void appendDimensionOrder(MPJLambdaWrapperX<WmsInventoryDO> query, String type) {
        if (StrUtil.equals(WmsInventoryPageReqVO.TYPE_WAREHOUSE, type)) {
            query.orderByAsc(WmsInventoryDO::getWarehouseId)
                    .orderByAsc(WmsItemSkuDO::getItemId)
                    .orderByAsc(WmsInventoryDO::getSkuId)
                    .orderByAsc(WmsInventoryDO::getId);
            return;
        }
        if (StrUtil.equals(WmsInventoryPageReqVO.TYPE_ITEM, type)) {
            query.orderByAsc(WmsItemSkuDO::getItemId)
                    .orderByAsc(WmsInventoryDO::getSkuId)
                    .orderByAsc(WmsInventoryDO::getWarehouseId)
                    .orderByAsc(WmsInventoryDO::getId);
            return;
        }
        throw new IllegalArgumentException("未知库存统计维度：" + type);
    }

}
