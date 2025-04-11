package cn.iocoder.yudao.module.srm.dal.mysql.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseReturnItemDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * ERP 采购退货项 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface SrmPurchaseReturnItemMapper extends BaseMapperX<SrmPurchaseReturnItemDO> {

    default List<SrmPurchaseReturnItemDO> selectListByReturnId(Long returnId) {
        return selectList(SrmPurchaseReturnItemDO::getReturnId, returnId);
    }

    default List<SrmPurchaseReturnItemDO> selectListByReturnIds(Collection<Long> returnIds) {
        return selectList(SrmPurchaseReturnItemDO::getReturnId, returnIds);
    }

    default int deleteByReturnId(Long returnId) {
        return delete(SrmPurchaseReturnItemDO::getReturnId, returnId);
    }

    /**
     * 基于采购订单编号，查询每个采购订单项的退货数量之和
     *
     * @param returnIds 入库订单项编号数组
     * @return key：采购订单项编号；value：退货数量之和
     */
    default Map<Long, BigDecimal> selectOrderItemCountSumMapByReturnIds(Collection<Long> returnIds) {
        if (CollUtil.isEmpty(returnIds)) {
            return Collections.emptyMap();
        }
        // SQL sum 查询
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<SrmPurchaseReturnItemDO>()
            .select("order_item_id, SUM(count) AS sumCount")
            .groupBy("order_item_id")
            .in("return_id", returnIds));
        // 获得数量
        return convertMap(result, obj -> (Long) obj.get("order_item_id"), obj -> (BigDecimal) obj.get("sumCount"));
    }

    //通过ids查询,如果ids是空，则返回空集合
    default List<SrmPurchaseReturnItemDO> selectListByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapper<SrmPurchaseReturnItemDO>().in(SrmPurchaseReturnItemDO::getId, ids));
    }

    //通过入库项id查找对应的采购退货项
    default List<SrmPurchaseReturnItemDO> selectListByInItemId(Long inItemId) {
        return selectList(new LambdaQueryWrapper<SrmPurchaseReturnItemDO>().eq(SrmPurchaseReturnItemDO::getInItemId, inItemId));
    }

    //入库项id存在对应的采购退货项
    default boolean existsByInItemId(Long inItemId) {
        return selectCount(new LambdaQueryWrapper<SrmPurchaseReturnItemDO>().eq(SrmPurchaseReturnItemDO::getInItemId, inItemId)) > 0;
    }
}