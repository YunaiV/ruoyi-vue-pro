package cn.iocoder.yudao.module.srm.dal.mysql.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.order.ErpPurchaseOrderPageReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.bo.ErpPurchaseOrderItemBO;
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
public interface ErpPurchaseOrderItemMapper extends BaseMapperX<ErpPurchaseOrderItemDO> {

    default MPJLambdaWrapperX<ErpPurchaseOrderItemDO> wrapper(ErpPurchaseOrderPageReqVO reqVO) {
        return new MPJLambdaWrapperX<ErpPurchaseOrderItemDO>()
            .selectAll(ErpPurchaseOrderItemDO.class)
            .likeIfPresent(ErpPurchaseOrderItemDO::getErpPurchaseRequestItemNo, reqVO.getErpPurchaseRequestItemNo());
    }

    default MPJLambdaWrapper<ErpPurchaseOrderItemDO> getBOWrapper(ErpPurchaseOrderPageReqVO reqVO) {
        return wrapper(reqVO)
            .leftJoin(ErpPurchaseOrderDO.class, ErpPurchaseOrderDO::getId, ErpPurchaseOrderItemDO::getOrderId)
            .selectAll(ErpPurchaseOrderDO.class)
            .eqIfExists(ErpPurchaseOrderDO::getStatus, reqVO.getStatus())
            .eqIfExists(ErpPurchaseOrderDO::getSupplierId, reqVO.getSupplierId())
            .eqIfExists(ErpPurchaseOrderDO::getAccountId, reqVO.getAccountId())
            .eqIfExists(ErpPurchaseOrderDO::getTotalCount, reqVO.getTotalCount())
            .eqIfExists(ErpPurchaseOrderDO::getTotalPrice, reqVO.getTotalPrice())
            .eqIfExists(ErpPurchaseOrderDO::getTotalProductPrice, reqVO.getTotalProductPrice())
            .eqIfExists(ErpPurchaseOrderDO::getTotalTaxPrice, reqVO.getTotalTaxPrice())
            .eqIfExists(ErpPurchaseOrderDO::getDiscountPercent, reqVO.getDiscountPercent())
            .eqIfExists(ErpPurchaseOrderDO::getDiscountPrice, reqVO.getDiscountPrice())
            .eqIfExists(ErpPurchaseOrderDO::getDepositPrice, reqVO.getDepositPrice())
            .eqIfExists(ErpPurchaseOrderDO::getFileUrl, reqVO.getFileUrl())
            .eqIfExists(ErpPurchaseOrderDO::getRemark, reqVO.getRemark())
            .eqIfExists(ErpPurchaseOrderDO::getTotalInCount, reqVO.getTotalInCount())
            .eqIfExists(ErpPurchaseOrderDO::getTotalReturnCount, reqVO.getTotalReturnCount())
//            .betweenIfPresent(ErpPurchaseOrderDO::getNoTime, reqVO.getNoTime())
//            .betweenIfPresent(ErpPurchaseOrderDO::getCreateTime, reqVO.getCreateTime())
//            .betweenIfPresent(ErpPurchaseOrderDO::getSettlementDate, reqVO.getSettlementDate())
            .eqIfExists(ErpPurchaseOrderDO::getAuditorId, reqVO.getAuditorId())
//            .betweenIfPresent(ErpPurchaseOrderDO::getAuditTime, reqVO.getAuditTime())
            .eqIfExists(ErpPurchaseOrderDO::getPurchaseEntityId, reqVO.getPurchaseEntityId())
            .eqIfExists(ErpPurchaseOrderDO::getInspectionJson, reqVO.getInspectionJson())
            .eqIfExists(ErpPurchaseOrderDO::getCompletionJson, reqVO.getCompletionJson())
            .likeIfExists(ErpPurchaseOrderDO::getXCode, reqVO.getXCode())
            .likeIfExists(ErpPurchaseOrderDO::getContainerRate, reqVO.getContainerRate())
            .eqIfExists(ErpPurchaseOrderDO::getWarehouseId, reqVO.getWarehouseId())
            .eqIfExists(ErpPurchaseOrderDO::getOffStatus, reqVO.getOffStatus())
            .eqIfExists(ErpPurchaseOrderDO::getExecuteStatus, reqVO.getExecuteStatus())
            .eqIfExists(ErpPurchaseOrderDO::getInStatus, reqVO.getInStatus())
            .eqIfExists(ErpPurchaseOrderDO::getPayStatus, reqVO.getPayStatus())
            .eqIfExists(ErpPurchaseOrderDO::getAuditStatus, reqVO.getAuditStatus())
            .likeIfExists(ErpPurchaseOrderDO::getAddress, reqVO.getAddress())
            .likeIfExists(ErpPurchaseOrderDO::getPaymentTerms, reqVO.getPaymentTerms())
            .eqIfExists(ErpPurchaseOrderDO::getOrderStatus, reqVO.getOrderStatus())
            .eqIfExists(ErpPurchaseOrderDO::getTotalInspectionPassCount, reqVO.getTotalInspectionPassCount())
            ;
    }

    //获得ErpPurchaseOrderItemBO分页查询
    default PageResult<ErpPurchaseOrderItemBO> selectErpPurchaseOrderItemBOPage(ErpPurchaseOrderPageReqVO reqVO) {
        MPJLambdaWrapper<ErpPurchaseOrderItemDO> wrapper = getBOWrapper(reqVO)
            .selectAssociation(ErpPurchaseOrderDO.class, ErpPurchaseOrderItemBO::getErpPurchaseOrderDO);//一对一关联
        return selectJoinPage(reqVO, ErpPurchaseOrderItemBO.class, wrapper);
    }

    //获得ErpPurchaseOrderItemBO 一个
    default ErpPurchaseOrderItemBO selectErpPurchaseOrderItemBOById(Long id) {
        MPJLambdaWrapper<ErpPurchaseOrderItemDO> wrapper = getBOWrapper(null)
            .selectAssociation(ErpPurchaseOrderDO.class, ErpPurchaseOrderItemBO::getErpPurchaseOrderDO)//一对一关联
            .eq(ErpPurchaseOrderItemDO::getId, id);
        return selectJoinOne(ErpPurchaseOrderItemBO.class, wrapper);
    }

    //获得ErpPurchaseOrderItemBO列表查询
    default List<ErpPurchaseOrderItemBO> selectErpPurchaseOrderItemBOS(ErpPurchaseOrderPageReqVO reqVO) {
        MPJLambdaWrapper<ErpPurchaseOrderItemDO> wrapper = getBOWrapper(reqVO)
            .selectAssociation(ErpPurchaseOrderDO.class, ErpPurchaseOrderItemBO::getErpPurchaseOrderDO);//一对一关联
        return selectJoinList(ErpPurchaseOrderItemBO.class, wrapper);
    }

    //根据id来获得list
    default List<ErpPurchaseOrderItemDO> selectListByItemIds(Collection<Long> itemIds) {
        return selectList(ErpPurchaseOrderItemDO::getId, itemIds);
    }

    default List<ErpPurchaseOrderItemDO> selectListByOrderId(Long orderId) {
        return selectList(ErpPurchaseOrderItemDO::getOrderId, orderId);
    }

    default List<ErpPurchaseOrderItemDO> selectListByOrderIds(Collection<Long> orderIds) {
        return selectList(ErpPurchaseOrderItemDO::getOrderId, orderIds);
    }

    default int deleteByOrderId(Long orderId) {
        return delete(ErpPurchaseOrderItemDO::getOrderId, orderId);
    }

    //根据purchaseApplyItemId查找
    default List<ErpPurchaseOrderItemDO> selectListByPurchaseApplyItemIds(Collection<Long> purchaseApplyItemIds) {
        return selectList(ErpPurchaseOrderItemDO::getPurchaseApplyItemId, purchaseApplyItemIds);
    }

    //根据ApplyItemId 查询数量
    default Long selectCountByPurchaseApplyItemId(Long purchaseApplyItemId) {
        return selectCount(ErpPurchaseOrderItemDO::getPurchaseApplyItemId, purchaseApplyItemId);
    }

    default MPJLambdaWrapper<ErpPurchaseOrderItemDO> getDOWrapper() {
        return new MPJLambdaWrapperX<ErpPurchaseOrderItemDO>()
            .selectAll(ErpPurchaseOrderItemDO.class);
    }


    default Collection<ErpPurchaseOrderItemDO> selectIdsByErpPurchaseRequestItemNo(String erpPurchaseRequestItemNo) {
        MPJLambdaWrapper<ErpPurchaseOrderItemDO> wrapper = new MPJLambdaWrapperX<ErpPurchaseOrderItemDO>()
            .selectAll(ErpPurchaseOrderItemDO.class)
            .like(ErpPurchaseOrderItemDO::getErpPurchaseRequestItemNo, erpPurchaseRequestItemNo);
        return selectList(wrapper);
    }

//    //BO
//    default MPJLambdaWrapper<ErpPurchaseOrderItemDO> getBOWrapper() {
//        return getDOWrapper()
//            .leftJoin(ErpPurchaseOrderDO.class, ErpPurchaseOrderDO::getId, ErpPurchaseOrderItemDO::getOrderId)
//            .selectAsClass(ErpPurchaseOrderDO.class, ErpPurchaseOrderItemBO.class);
//    }
}