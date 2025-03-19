package cn.iocoder.yudao.module.erp.dal.mysql.purchase;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * ERP采购申请单子 Mapper
 *
 * @author 索迈管理员
 */
@Mapper
public interface ErpPurchaseRequestItemsMapper extends BaseMapperX<ErpPurchaseRequestItemsDO> {
    default List<ErpPurchaseRequestItemsDO> selectListByRequestId(Long requestId) {
        return selectList(ErpPurchaseRequestItemsDO::getRequestId, requestId);
    }

    default int deleteByRequestId(Long requestId) {
        return delete(ErpPurchaseRequestItemsDO::getRequestId, requestId);
    }

    default List<ErpPurchaseRequestItemsDO> selectListByRequestIds(Collection<Long> requestIds) {
        return selectList(ErpPurchaseRequestItemsDO::getRequestId, requestIds);
    }
}