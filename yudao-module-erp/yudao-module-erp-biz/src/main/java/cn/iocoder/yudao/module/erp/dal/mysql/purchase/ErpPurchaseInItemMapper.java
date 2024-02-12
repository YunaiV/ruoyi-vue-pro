package cn.iocoder.yudao.module.erp.dal.mysql.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInItemDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * ERP 采购入库项 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ErpPurchaseInItemMapper extends BaseMapperX<ErpPurchaseInItemDO> {

    default List<ErpPurchaseInItemDO> selectListByInId(Long inId) {
        return selectList(ErpPurchaseInItemDO::getInId, inId);
    }

    default List<ErpPurchaseInItemDO> selectListByInIds(Collection<Long> inIds) {
        return selectList(ErpPurchaseInItemDO::getInId, inIds);
    }

    default int deleteByInId(Long inId) {
        return delete(ErpPurchaseInItemDO::getInId, inId);
    }

    /**
     * 基于采购订单编号，查询每个采购订单项的入库数量之和
     *
     * @param inIds 入库订单项编号数组
     * @return key：采购订单项编号；value：入库数量之和
     */
    default Map<Long, BigDecimal> selectOrderItemCountSumMapByInIds(Collection<Long> inIds) {
        if (CollUtil.isEmpty(inIds)) {
            return Collections.emptyMap();
        }
        // SQL sum 查询
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<ErpPurchaseInItemDO>()
                .select("order_item_id, SUM(count) AS sumCount")
                .groupBy("order_item_id")
                .in("in_id", inIds));
        // 获得数量
        return convertMap(result, obj -> (Long) obj.get("order_item_id"), obj -> (BigDecimal) obj.get("sumCount"));
    }

}