package cn.iocoder.yudao.module.srm.dal.mysql.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.order.ErpPurchaseOrderPageReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseOrderDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * ERP 采购订单 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ErpPurchaseOrderMapper extends BaseMapperX<ErpPurchaseOrderDO> {
    default PageResult<ErpPurchaseOrderDO> selectPage(ErpPurchaseOrderPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpPurchaseOrderDO>()
            .eqIfPresent(ErpPurchaseOrderDO::getStatus, reqVO.getStatus())
            .eqIfPresent(ErpPurchaseOrderDO::getSupplierId, reqVO.getSupplierId())
            .eqIfPresent(ErpPurchaseOrderDO::getAccountId, reqVO.getAccountId())
            .betweenIfPresent(ErpPurchaseOrderDO::getOrderTime, reqVO.getOrderTime())
            .eqIfPresent(ErpPurchaseOrderDO::getTotalCount, reqVO.getTotalCount())
            .eqIfPresent(ErpPurchaseOrderDO::getTotalPrice, reqVO.getTotalPrice())
            .eqIfPresent(ErpPurchaseOrderDO::getTotalProductPrice, reqVO.getTotalProductPrice())
            .eqIfPresent(ErpPurchaseOrderDO::getTotalTaxPrice, reqVO.getTotalTaxPrice())
            .eqIfPresent(ErpPurchaseOrderDO::getDiscountPercent, reqVO.getDiscountPercent())
            .eqIfPresent(ErpPurchaseOrderDO::getDiscountPrice, reqVO.getDiscountPrice())
            .eqIfPresent(ErpPurchaseOrderDO::getDepositPrice, reqVO.getDepositPrice())
            .eqIfPresent(ErpPurchaseOrderDO::getFileUrl, reqVO.getFileUrl())
            .eqIfPresent(ErpPurchaseOrderDO::getRemark, reqVO.getRemark())
            .eqIfPresent(ErpPurchaseOrderDO::getTotalInCount, reqVO.getTotalInCount())
            .eqIfPresent(ErpPurchaseOrderDO::getTotalReturnCount, reqVO.getTotalReturnCount())
            .betweenIfPresent(ErpPurchaseOrderDO::getNoTime, reqVO.getNoTime())
            .betweenIfPresent(ErpPurchaseOrderDO::getCreateTime, reqVO.getCreateTime())
            .betweenIfPresent(ErpPurchaseOrderDO::getSettlementDate, reqVO.getSettlementDate())
            .eqIfPresent(ErpPurchaseOrderDO::getAuditorId, reqVO.getAuditorId())
            .betweenIfPresent(ErpPurchaseOrderDO::getAuditTime, reqVO.getAuditTime())
            .eqIfPresent(ErpPurchaseOrderDO::getPurchaseEntityId, reqVO.getPurchaseEntityId())
            .eqIfPresent(ErpPurchaseOrderDO::getInspectionJson, reqVO.getInspectionJson())
            .eqIfPresent(ErpPurchaseOrderDO::getCompletionJson, reqVO.getCompletionJson())
            .likeIfPresent(ErpPurchaseOrderDO::getXCode, reqVO.getXCode())
            .likeIfPresent(ErpPurchaseOrderDO::getContainerRate, reqVO.getContainerRate())
            .eqIfPresent(ErpPurchaseOrderDO::getWarehouseId, reqVO.getWarehouseId())
            .eqIfPresent(ErpPurchaseOrderDO::getOffStatus, reqVO.getOffStatus())
            .eqIfPresent(ErpPurchaseOrderDO::getExecuteStatus, reqVO.getExecuteStatus())
            .eqIfPresent(ErpPurchaseOrderDO::getInStatus, reqVO.getInStatus())
            .eqIfPresent(ErpPurchaseOrderDO::getPayStatus, reqVO.getPayStatus())
            .eqIfPresent(ErpPurchaseOrderDO::getAuditStatus, reqVO.getAuditStatus())
            .likeIfPresent(ErpPurchaseOrderDO::getAddress, reqVO.getAddress())
            .likeIfPresent(ErpPurchaseOrderDO::getPaymentTerms, reqVO.getPaymentTerms())
            .eqIfPresent(ErpPurchaseOrderDO::getOrderStatus, reqVO.getOrderStatus())
            .eqIfPresent(ErpPurchaseOrderDO::getTotalInspectionPassCount, reqVO.getTotalInspectionPassCount())
            .orderByDesc(ErpPurchaseOrderDO::getId));
    }

    default int updateByIdAndStatus(Long id, Integer status, ErpPurchaseOrderDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<ErpPurchaseOrderDO>()
            .eq(ErpPurchaseOrderDO::getId, id).eq(ErpPurchaseOrderDO::getAuditStatus, status));
    }

    default ErpPurchaseOrderDO selectByNo(String no) {
        return selectOne(ErpPurchaseOrderDO::getNo, no);
    }


}