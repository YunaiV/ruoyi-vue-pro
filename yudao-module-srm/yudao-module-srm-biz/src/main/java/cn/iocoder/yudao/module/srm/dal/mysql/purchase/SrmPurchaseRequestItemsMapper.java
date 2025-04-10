package cn.iocoder.yudao.module.srm.dal.mysql.purchase;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestItemsDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ERP采购申请单子 Mapper
 *
 * @author 索迈管理员
 */
@Mapper
public interface SrmPurchaseRequestItemsMapper extends BaseMapperX<SrmPurchaseRequestItemsDO> {
    default List<SrmPurchaseRequestItemsDO> selectListByRequestId(Long requestId) {
        return selectList(SrmPurchaseRequestItemsDO::getRequestId, requestId);
    }

    default void deleteByRequestId(Long requestId) {
        delete(SrmPurchaseRequestItemsDO::getRequestId, requestId);
    }

    default List<SrmPurchaseRequestItemsDO> selectListByRequestIds(Collection<Long> requestIds) {
        return selectList(SrmPurchaseRequestItemsDO::getRequestId, requestIds);
    }

    /**
     * 根据 itemIds 查询
     *
     * @param itemIds 子表产品项ids
     * @return 集合
     */
    default List<SrmPurchaseRequestItemsDO> selectListByIds(Collection<Long> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            // 如果 itemIds 为空，直接返回空列表，避免执行全表查询
            return Collections.emptyList();
        }
        return selectBatchIds(itemIds);
    }

    /**
     * 基于采购订单id，获得采购订单的产品项
     *
     * @param ids 主表采购订单ids
     * @return key：采购订单id；value：采购项集合
     */
    default Map<Long, List<SrmPurchaseRequestItemsDO>> findItemsGroupedByRequestId(Collection<Long> ids) {
        return selectList(new LambdaQueryWrapperX<SrmPurchaseRequestItemsDO>()
            .in(SrmPurchaseRequestItemsDO::getRequestId, ids))
            .stream().collect(Collectors.groupingBy(SrmPurchaseRequestItemsDO::getRequestId));
    }


}