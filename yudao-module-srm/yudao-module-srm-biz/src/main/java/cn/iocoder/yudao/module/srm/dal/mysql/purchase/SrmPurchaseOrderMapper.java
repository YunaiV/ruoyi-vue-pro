package cn.iocoder.yudao.module.srm.dal.mysql.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.order.SrmPurchaseOrderPageReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderItemDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.bo.SrmPurchaseOrderBO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * ERP 采购订单 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface SrmPurchaseOrderMapper extends BaseMapperX<SrmPurchaseOrderDO> {

    default MPJLambdaWrapper<SrmPurchaseOrderDO> wrapper(SrmPurchaseOrderPageReqVO reqVO) {
        return new MPJLambdaWrapperX<SrmPurchaseOrderDO>().selectAll(SrmPurchaseOrderDO.class)
            .eqIfPresent(SrmPurchaseOrderDO::getStatus, reqVO.getStatus())
            .eqIfPresent(SrmPurchaseOrderDO::getSupplierId, reqVO.getSupplierId())
            .eqIfPresent(SrmPurchaseOrderDO::getAccountId, reqVO.getAccountId())
            .betweenIfPresent(SrmPurchaseOrderDO::getOrderTime, reqVO.getOrderTime())
            .eqIfPresent(SrmPurchaseOrderDO::getTotalCount, reqVO.getTotalCount())
            .eqIfPresent(SrmPurchaseOrderDO::getTotalPrice, reqVO.getTotalPrice())
            .eqIfPresent(SrmPurchaseOrderDO::getTotalProductPrice, reqVO.getTotalProductPrice())
            .eqIfPresent(SrmPurchaseOrderDO::getTotalTaxPrice, reqVO.getTotalTaxPrice())
            .eqIfPresent(SrmPurchaseOrderDO::getDiscountPercent, reqVO.getDiscountPercent())
            .eqIfPresent(SrmPurchaseOrderDO::getDiscountPrice, reqVO.getDiscountPrice())
            .eqIfPresent(SrmPurchaseOrderDO::getDepositPrice, reqVO.getDepositPrice())
            .eqIfPresent(SrmPurchaseOrderDO::getFileUrl, reqVO.getFileUrl())
            .eqIfPresent(SrmPurchaseOrderDO::getRemark, reqVO.getRemark())
            .eqIfPresent(SrmPurchaseOrderDO::getTotalInCount, reqVO.getTotalInCount())
            .eqIfPresent(SrmPurchaseOrderDO::getTotalReturnCount, reqVO.getTotalReturnCount())
            .betweenIfPresent(SrmPurchaseOrderDO::getNoTime, reqVO.getNoTime())
            .betweenIfPresent(SrmPurchaseOrderDO::getCreateTime, reqVO.getCreateTime())
            .betweenIfPresent(SrmPurchaseOrderDO::getSettlementDate, reqVO.getSettlementDate())
            .eqIfPresent(SrmPurchaseOrderDO::getAuditorId, reqVO.getAuditorId())
            .betweenIfPresent(SrmPurchaseOrderDO::getAuditTime, reqVO.getAuditTime())
            .eqIfPresent(SrmPurchaseOrderDO::getPurchaseEntityId, reqVO.getPurchaseEntityId())
            .likeIfPresent(SrmPurchaseOrderDO::getXCode, reqVO.getXCode())
            .likeIfPresent(SrmPurchaseOrderDO::getContainerRate, reqVO.getContainerRate())
            .eqIfPresent(SrmPurchaseOrderDO::getWarehouseId, reqVO.getWarehouseId())
            .eqIfPresent(SrmPurchaseOrderDO::getOffStatus, reqVO.getOffStatus())
            .eqIfPresent(SrmPurchaseOrderDO::getExecuteStatus, reqVO.getExecuteStatus())
            .eqIfPresent(SrmPurchaseOrderDO::getInStatus, reqVO.getInStatus())
            .eqIfPresent(SrmPurchaseOrderDO::getPayStatus, reqVO.getPayStatus())
            .eqIfPresent(SrmPurchaseOrderDO::getAuditStatus, reqVO.getAuditStatus())
            .likeIfPresent(SrmPurchaseOrderDO::getAddress, reqVO.getAddress())
            .likeIfPresent(SrmPurchaseOrderDO::getPaymentTerms, reqVO.getPaymentTerms())
            .eqIfPresent(SrmPurchaseOrderDO::getOrderStatus, reqVO.getOrderStatus());
    }

    //getBOWrapper
    default MPJLambdaWrapper<SrmPurchaseOrderDO> getBOWrapper(SrmPurchaseOrderPageReqVO vo) {
        return wrapper(vo).leftJoin(SrmPurchaseOrderItemDO.class, SrmPurchaseOrderItemDO::getOrderId,
                SrmPurchaseOrderDO::getId)// 关联表
            .likeIfExists(SrmPurchaseOrderItemDO::getErpPurchaseRequestItemNo,
                vo.getErpPurchaseRequestItemNo())//采购申请单的单号
            .selectCollection(SrmPurchaseOrderItemDO.class, SrmPurchaseOrderBO::getSrmPurchaseOrderItemBO);
        //            .selectAsClass(SrmPurchaseOrderDO.class, SrmPurchaseOrderBO.class);
    }

    default PageResult<SrmPurchaseOrderDO> selectPage(SrmPurchaseOrderPageReqVO reqVO, List<Long> orderIds) {
        MPJLambdaWrapper<SrmPurchaseOrderDO> wrapper = wrapper(reqVO);
        if(orderIds != null && !orderIds.isEmpty()) {
            wrapper.in(SrmPurchaseOrderDO::getId, orderIds);
        }
        return selectPage(reqVO, wrapper);
    }

    default int updateByIdAndStatus(Long id, Integer status, SrmPurchaseOrderDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<SrmPurchaseOrderDO>().eq(SrmPurchaseOrderDO::getId, id)
            .eq(SrmPurchaseOrderDO::getAuditStatus, status));
    }

    default SrmPurchaseOrderDO selectByNo(String no) {
        return selectOne(SrmPurchaseOrderDO::getNo, no);
    }

    //查询BO，根据订单项的erpPurchaseRequestItemNo查出对应的BO
    //嵌套结果方式会导致结果集被折叠，因此数量会少。
    //    default PageResult<SrmPurchaseOrderBO> selectBOByPageVO(SrmPurchaseOrderPageReqVO vo) {
    //        return selectJoinPage(vo, SrmPurchaseOrderBO.class, getBOWrapper(vo));
    //    }
}