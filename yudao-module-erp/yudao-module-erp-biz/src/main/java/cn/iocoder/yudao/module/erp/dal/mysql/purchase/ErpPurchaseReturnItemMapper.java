package cn.iocoder.yudao.module.erp.dal.mysql.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseReturnItemDO;
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
public interface ErpPurchaseReturnItemMapper extends BaseMapperX<ErpPurchaseReturnItemDO> {

    default List<ErpPurchaseReturnItemDO> selectListByReturnId(Long returnId) {
        return selectList(ErpPurchaseReturnItemDO::getReturnId, returnId);
    }

    default List<ErpPurchaseReturnItemDO> selectListByReturnIds(Collection<Long> returnIds) {
        return selectList(ErpPurchaseReturnItemDO::getReturnId, returnIds);
    }

    default int deleteByReturnId(Long returnId) {
        return delete(ErpPurchaseReturnItemDO::getReturnId, returnId);
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
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<ErpPurchaseReturnItemDO>()
                .select("order_item_id, SUM(count) AS sumCount")
                .groupBy("order_item_id")
                .in("return_id", returnIds));
        // 获得数量
        return convertMap(result, obj -> (Long) obj.get("order_item_id"), obj -> (BigDecimal) obj.get("sumCount"));
    }

}