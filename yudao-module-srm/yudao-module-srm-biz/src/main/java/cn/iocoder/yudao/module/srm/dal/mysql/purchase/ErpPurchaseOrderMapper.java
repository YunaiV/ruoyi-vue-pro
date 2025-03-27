package cn.iocoder.yudao.module.srm.dal.mysql.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.order.ErpPurchaseOrderPageReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.bo.ErpPurchaseOrderBO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * ERP 采购订单 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ErpPurchaseOrderMapper extends BaseMapperX<ErpPurchaseOrderDO> {

    default MPJLambdaWrapper<ErpPurchaseOrderDO> wrapper(ErpPurchaseOrderPageReqVO reqVO) {
        return new MPJLambdaWrapperX<ErpPurchaseOrderDO>().selectAll(ErpPurchaseOrderDO.class).eqIfPresent(ErpPurchaseOrderDO::getStatus, reqVO.getStatus()).eqIfPresent(ErpPurchaseOrderDO::getSupplierId, reqVO.getSupplierId()).eqIfPresent(ErpPurchaseOrderDO::getAccountId, reqVO.getAccountId()).betweenIfPresent(ErpPurchaseOrderDO::getOrderTime, reqVO.getOrderTime()).eqIfPresent(ErpPurchaseOrderDO::getTotalCount, reqVO.getTotalCount()).eqIfPresent(ErpPurchaseOrderDO::getTotalPrice, reqVO.getTotalPrice()).eqIfPresent(ErpPurchaseOrderDO::getTotalProductPrice, reqVO.getTotalProductPrice()).eqIfPresent(ErpPurchaseOrderDO::getTotalTaxPrice, reqVO.getTotalTaxPrice()).eqIfPresent(ErpPurchaseOrderDO::getDiscountPercent, reqVO.getDiscountPercent()).eqIfPresent(ErpPurchaseOrderDO::getDiscountPrice, reqVO.getDiscountPrice()).eqIfPresent(ErpPurchaseOrderDO::getDepositPrice, reqVO.getDepositPrice()).eqIfPresent(ErpPurchaseOrderDO::getFileUrl, reqVO.getFileUrl()).eqIfPresent(ErpPurchaseOrderDO::getRemark, reqVO.getRemark()).eqIfPresent(ErpPurchaseOrderDO::getTotalInCount, reqVO.getTotalInCount()).eqIfPresent(ErpPurchaseOrderDO::getTotalReturnCount, reqVO.getTotalReturnCount()).betweenIfPresent(ErpPurchaseOrderDO::getNoTime, reqVO.getNoTime()).betweenIfPresent(ErpPurchaseOrderDO::getCreateTime, reqVO.getCreateTime()).betweenIfPresent(ErpPurchaseOrderDO::getSettlementDate, reqVO.getSettlementDate()).eqIfPresent(ErpPurchaseOrderDO::getAuditorId, reqVO.getAuditorId()).betweenIfPresent(ErpPurchaseOrderDO::getAuditTime, reqVO.getAuditTime()).eqIfPresent(ErpPurchaseOrderDO::getPurchaseEntityId, reqVO.getPurchaseEntityId()).eqIfPresent(ErpPurchaseOrderDO::getInspectionJson, reqVO.getInspectionJson()).eqIfPresent(ErpPurchaseOrderDO::getCompletionJson, reqVO.getCompletionJson()).likeIfPresent(ErpPurchaseOrderDO::getXCode, reqVO.getXCode()).likeIfPresent(ErpPurchaseOrderDO::getContainerRate, reqVO.getContainerRate()).eqIfPresent(ErpPurchaseOrderDO::getWarehouseId, reqVO.getWarehouseId()).eqIfPresent(ErpPurchaseOrderDO::getOffStatus, reqVO.getOffStatus()).eqIfPresent(ErpPurchaseOrderDO::getExecuteStatus, reqVO.getExecuteStatus()).eqIfPresent(ErpPurchaseOrderDO::getInStatus, reqVO.getInStatus()).eqIfPresent(ErpPurchaseOrderDO::getPayStatus, reqVO.getPayStatus()).eqIfPresent(ErpPurchaseOrderDO::getAuditStatus, reqVO.getAuditStatus()).likeIfPresent(ErpPurchaseOrderDO::getAddress, reqVO.getAddress()).likeIfPresent(ErpPurchaseOrderDO::getPaymentTerms, reqVO.getPaymentTerms()).eqIfPresent(ErpPurchaseOrderDO::getOrderStatus, reqVO.getOrderStatus()).eqIfPresent(ErpPurchaseOrderDO::getTotalInspectionPassCount, reqVO.getTotalInspectionPassCount()).orderByDesc(ErpPurchaseOrderDO::getId);
    }

    //getBOWrapper
    default MPJLambdaWrapper<ErpPurchaseOrderDO> getBOWrapper(ErpPurchaseOrderPageReqVO vo) {
        return wrapper(vo).leftJoin(ErpPurchaseOrderItemDO.class, ErpPurchaseOrderItemDO::getOrderId, ErpPurchaseOrderDO::getId)// 关联表
            .likeIfExists(ErpPurchaseOrderItemDO::getErpPurchaseRequestItemNo, vo.getErpPurchaseRequestItemNo())//采购申请单的单号
            .selectCollection(ErpPurchaseOrderItemDO.class, ErpPurchaseOrderBO::getErpPurchaseOrderItemBO);
//            .selectAsClass(ErpPurchaseOrderDO.class, ErpPurchaseOrderBO.class);
    }

    default PageResult<ErpPurchaseOrderDO> selectPage(ErpPurchaseOrderPageReqVO reqVO, List<Long> orderIds) {
        MPJLambdaWrapper<ErpPurchaseOrderDO> wrapper = wrapper(reqVO);
        if (orderIds != null && !orderIds.isEmpty()) {
            wrapper.in(ErpPurchaseOrderDO::getId, orderIds);
        }
        return selectPage(reqVO, wrapper);
    }

    default int updateByIdAndStatus(Long id, Integer status, ErpPurchaseOrderDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<ErpPurchaseOrderDO>().eq(ErpPurchaseOrderDO::getId, id).eq(ErpPurchaseOrderDO::getAuditStatus, status));
    }

    default ErpPurchaseOrderDO selectByNo(String no) {
        return selectOne(ErpPurchaseOrderDO::getNo, no);
    }

    //查询BO，根据订单项的erpPurchaseRequestItemNo查出对应的BO
    //嵌套结果方式会导致结果集被折叠，因此数量会少。
//    default PageResult<ErpPurchaseOrderBO> selectBOByPageVO(ErpPurchaseOrderPageReqVO vo) {
//        return selectJoinPage(vo, ErpPurchaseOrderBO.class, getBOWrapper(vo));
//    }
}