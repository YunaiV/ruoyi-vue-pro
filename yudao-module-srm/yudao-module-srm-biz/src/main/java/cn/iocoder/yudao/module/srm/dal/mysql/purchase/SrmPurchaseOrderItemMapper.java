package cn.iocoder.yudao.module.srm.dal.mysql.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.order.req.SrmPurchaseOrderPageReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderItemDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.bo.SrmPurchaseOrderItemBO;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * ERP 采购订单明项目 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface SrmPurchaseOrderItemMapper extends BaseMapperX<SrmPurchaseOrderItemDO> {

    default MPJLambdaWrapperX<SrmPurchaseOrderItemDO> wrapper(SrmPurchaseOrderPageReqVO reqVO) {
        return new MPJLambdaWrapperX<SrmPurchaseOrderItemDO>().selectAll(SrmPurchaseOrderItemDO.class)
            .likeIfPresent(SrmPurchaseOrderItemDO::getErpPurchaseRequestItemNo, reqVO.getErpPurchaseRequestItemNo());
    }

    default MPJLambdaWrapper<SrmPurchaseOrderItemDO> getBOWrapper(SrmPurchaseOrderPageReqVO reqVO) {
        return wrapper(reqVO).leftJoin(SrmPurchaseOrderDO.class, SrmPurchaseOrderDO::getId,
                SrmPurchaseOrderItemDO::getOrderId).selectAll(SrmPurchaseOrderDO.class)
            .eqIfExists(SrmPurchaseOrderDO::getStatus, reqVO.getStatus())
            .eqIfExists(SrmPurchaseOrderDO::getSupplierId, reqVO.getSupplierId())
            .eqIfExists(SrmPurchaseOrderDO::getAccountId, reqVO.getAccountId())
            .eqIfExists(SrmPurchaseOrderDO::getTotalCount, reqVO.getTotalCount())
            .eqIfExists(SrmPurchaseOrderDO::getTotalPrice, reqVO.getTotalPrice())
            .eqIfExists(SrmPurchaseOrderDO::getTotalProductPrice, reqVO.getTotalProductPrice())
            .eqIfExists(SrmPurchaseOrderDO::getTotalTaxPrice, reqVO.getTotalTaxPrice())
            .eqIfExists(SrmPurchaseOrderDO::getDiscountPercent, reqVO.getDiscountPercent())
            .eqIfExists(SrmPurchaseOrderDO::getDiscountPrice, reqVO.getDiscountPrice())
            .eqIfExists(SrmPurchaseOrderDO::getDepositPrice, reqVO.getDepositPrice())
            .eqIfExists(SrmPurchaseOrderDO::getFileUrl, reqVO.getFileUrl())
            .eqIfExists(SrmPurchaseOrderDO::getRemark, reqVO.getRemark())
            .eqIfExists(SrmPurchaseOrderDO::getTotalInCount, reqVO.getTotalInCount())
            .eqIfExists(SrmPurchaseOrderDO::getTotalReturnCount, reqVO.getTotalReturnCount())
            //            .betweenIfPresent(SrmPurchaseOrderDO::getNoTime, reqVO.getNoTime())
            //            .betweenIfPresent(SrmPurchaseOrderDO::getCreateTime, reqVO.getCreateTime())
            //            .betweenIfPresent(SrmPurchaseOrderDO::getSettlementDate, reqVO.getSettlementDate())
            .eqIfExists(SrmPurchaseOrderDO::getAuditorId, reqVO.getAuditorId())
            //            .betweenIfPresent(SrmPurchaseOrderDO::getAuditTime, reqVO.getAuditTime())
            .eqIfExists(SrmPurchaseOrderDO::getPurchaseCompanyId, reqVO.getPurchaseCompanyId())
            //            .likeIfExists(SrmPurchaseOrderDO::getXCode, reqVO.getXCode())
            .likeIfExists(SrmPurchaseOrderDO::getContainerRate, reqVO.getContainerRate())
            .eqIfExists(SrmPurchaseOrderDO::getWarehouseId, reqVO.getWarehouseId())
            .eqIfExists(SrmPurchaseOrderDO::getOffStatus, reqVO.getOffStatus())
            .eqIfExists(SrmPurchaseOrderDO::getExecuteStatus, reqVO.getExecuteStatus())
            .eqIfExists(SrmPurchaseOrderDO::getInStatus, reqVO.getInStatus())
            .eqIfExists(SrmPurchaseOrderDO::getPayStatus, reqVO.getPayStatus())
            .eqIfExists(SrmPurchaseOrderDO::getAuditStatus, reqVO.getAuditStatus())
            .likeIfExists(SrmPurchaseOrderDO::getAddress, reqVO.getAddress())
            .likeIfExists(SrmPurchaseOrderDO::getPaymentTerms, reqVO.getPaymentTerms())
            .eqIfExists(SrmPurchaseOrderDO::getOrderStatus, reqVO.getOrderStatus());
    }

    //获得ErpPurchaseOrderItemBO分页查询
    default PageResult<SrmPurchaseOrderItemBO> selectErpPurchaseOrderItemBOPage(SrmPurchaseOrderPageReqVO reqVO) {
        MPJLambdaWrapper<SrmPurchaseOrderItemDO> wrapper =
            getBOWrapper(reqVO).selectAssociation(SrmPurchaseOrderDO.class,
                SrmPurchaseOrderItemBO::getSrmPurchaseOrderDO);//一对一关联
        return selectJoinPage(reqVO, SrmPurchaseOrderItemBO.class, wrapper);
    }

    //获得ErpPurchaseOrderItemBO 一个
    default SrmPurchaseOrderItemBO selectErpPurchaseOrderItemBOById(Long id) {
        MPJLambdaWrapper<SrmPurchaseOrderItemDO> wrapper =
            getBOWrapper(null).selectAssociation(SrmPurchaseOrderDO.class,
                    SrmPurchaseOrderItemBO::getSrmPurchaseOrderDO)//一对一关联
                .eq(SrmPurchaseOrderItemDO::getId, id);
        return selectJoinOne(SrmPurchaseOrderItemBO.class, wrapper);
    }

    //获得ErpPurchaseOrderItemBO列表查询
    default List<SrmPurchaseOrderItemBO> selectErpPurchaseOrderItemBOS(SrmPurchaseOrderPageReqVO reqVO) {
        MPJLambdaWrapper<SrmPurchaseOrderItemDO> wrapper =
            getBOWrapper(reqVO).selectAssociation(SrmPurchaseOrderDO.class,
                SrmPurchaseOrderItemBO::getSrmPurchaseOrderDO);//一对一关联
        return selectJoinList(SrmPurchaseOrderItemBO.class, wrapper);
    }

    //根据id来获得list
    default List<SrmPurchaseOrderItemDO> selectListByItemIds(Collection<Long> itemIds) {
        return selectList(SrmPurchaseOrderItemDO::getId, itemIds);
    }

    default List<SrmPurchaseOrderItemDO> selectListByOrderId(Long orderId) {
        return selectList(SrmPurchaseOrderItemDO::getOrderId, orderId);
    }

    default List<SrmPurchaseOrderItemDO> selectListByOrderIds(Collection<Long> orderIds) {
        return selectList(SrmPurchaseOrderItemDO::getOrderId, orderIds);
    }

    default int deleteByOrderId(Long orderId) {
        return delete(SrmPurchaseOrderItemDO::getOrderId, orderId);
    }

    //根据purchaseApplyItemId查找
    default List<SrmPurchaseOrderItemDO> selectListByPurchaseApplyItemIds(Collection<Long> purchaseApplyItemIds) {
        return selectList(SrmPurchaseOrderItemDO::getPurchaseApplyItemId, purchaseApplyItemIds);
    }

    //根据ApplyItemId 查询数量
    default Long selectCountByPurchaseApplyItemId(Long purchaseApplyItemId) {
        return selectCount(SrmPurchaseOrderItemDO::getPurchaseApplyItemId, purchaseApplyItemId);
    }

    default MPJLambdaWrapper<SrmPurchaseOrderItemDO> getDOWrapper() {
        return new MPJLambdaWrapperX<SrmPurchaseOrderItemDO>().selectAll(SrmPurchaseOrderItemDO.class);
    }

    default Collection<SrmPurchaseOrderItemDO> selectIdsByErpPurchaseRequestItemNo(String erpPurchaseRequestItemNo) {
        MPJLambdaWrapper<SrmPurchaseOrderItemDO> wrapper =
            new MPJLambdaWrapperX<SrmPurchaseOrderItemDO>().selectAll(SrmPurchaseOrderItemDO.class)
                .like(SrmPurchaseOrderItemDO::getErpPurchaseRequestItemNo, erpPurchaseRequestItemNo);
        return selectList(wrapper);
    }

    default List<SrmPurchaseOrderItemDO> selectListByApplyIds(Collection<Long> applyIds) {
        return selectList(SrmPurchaseOrderItemDO::getPurchaseApplyItemId, applyIds);
    }

    //    //BO
    //    default MPJLambdaWrapper<SrmPurchaseOrderItemDO> getBOWrapper() {
    //        return getDOWrapper()
    //            .leftJoin(SrmPurchaseOrderDO.class, SrmPurchaseOrderDO::getId, SrmPurchaseOrderItemDO::getOrderId)
    //            .selectAsClass(SrmPurchaseOrderDO.class, SrmPurchaseOrderItemBO.class);
    //    }
}