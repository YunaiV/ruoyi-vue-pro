package cn.iocoder.yudao.module.erp.dal.mysql.sale;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOutItemDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * ERP 销售出库项 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ErpSaleOutItemMapper extends BaseMapperX<ErpSaleOutItemDO> {

    default List<ErpSaleOutItemDO> selectListByOutId(Long orderId) {
        return selectList(ErpSaleOutItemDO::getOutId, orderId);
    }

    default List<ErpSaleOutItemDO> selectListByOutIds(Collection<Long> orderIds) {
        return selectList(ErpSaleOutItemDO::getOutId, orderIds);
    }

    default int deleteByOutId(Long orderId) {
        return delete(ErpSaleOutItemDO::getOutId, orderId);
    }

    /**
     * 基于销售订单编号，查询每个销售订单项的出库数量之和
     *
     * @param outIds 出库订单项编号数组
     * @return key：销售订单项编号；value：出库数量之和
     */
    default Map<Long, BigDecimal> selectOrderItemCountSumMapByOutIds(Collection<Long> outIds) {
        if (CollUtil.isEmpty(outIds)) {
            return Collections.emptyMap();
        }
        // SQL sum 查询
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<ErpSaleOutItemDO>()
                .select("order_item_id, SUM(count) AS sumCount")
                .groupBy("order_item_id")
                .in("out_id", outIds));
        // 获得数量
        return convertMap(result, obj -> (Long) obj.get("order_item_id"), obj -> (BigDecimal) obj.get("sumCount"));
    }

}