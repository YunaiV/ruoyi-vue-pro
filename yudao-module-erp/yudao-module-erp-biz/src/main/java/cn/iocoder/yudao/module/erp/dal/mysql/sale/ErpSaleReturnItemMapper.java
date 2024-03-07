package cn.iocoder.yudao.module.erp.dal.mysql.sale;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleReturnItemDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * ERP 销售退货项 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ErpSaleReturnItemMapper extends BaseMapperX<ErpSaleReturnItemDO> {

    default List<ErpSaleReturnItemDO> selectListByReturnId(Long returnId) {
        return selectList(ErpSaleReturnItemDO::getReturnId, returnId);
    }

    default List<ErpSaleReturnItemDO> selectListByReturnIds(Collection<Long> returnIds) {
        return selectList(ErpSaleReturnItemDO::getReturnId, returnIds);
    }

    default int deleteByReturnId(Long returnId) {
        return delete(ErpSaleReturnItemDO::getReturnId, returnId);
    }

    /**
     * 基于销售订单编号，查询每个销售订单项的退货数量之和
     *
     * @param returnIds 出库订单项编号数组
     * @return key：销售订单项编号；value：退货数量之和
     */
    default Map<Long, BigDecimal> selectOrderItemCountSumMapByReturnIds(Collection<Long> returnIds) {
        if (CollUtil.isEmpty(returnIds)) {
            return Collections.emptyMap();
        }
        // SQL sum 查询
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<ErpSaleReturnItemDO>()
                .select("order_item_id, SUM(count) AS sumCount")
                .groupBy("order_item_id")
                .in("return_id", returnIds));
        // 获得数量
        return convertMap(result, obj -> (Long) obj.get("order_item_id"), obj -> (BigDecimal) obj.get("sumCount"));
    }

}