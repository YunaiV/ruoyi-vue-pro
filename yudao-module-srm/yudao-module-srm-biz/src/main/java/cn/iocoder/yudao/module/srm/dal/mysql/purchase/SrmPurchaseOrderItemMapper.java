package cn.iocoder.yudao.module.srm.dal.mysql.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.order.req.SrmPurchaseOrderPageReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderItemDO;
import cn.iocoder.yudao.module.srm.service.purchase.bo.order.SrmPurchaseOrderItemBO;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * ERP 采购订单明项目 Mapper
 *
 * @author wdy
 */
@Mapper
public interface SrmPurchaseOrderItemMapper extends BaseMapperX<SrmPurchaseOrderItemDO> {

    default MPJLambdaWrapperX<SrmPurchaseOrderItemDO> buildWrapper(SrmPurchaseOrderPageReqVO reqVO) {
        return new MPJLambdaWrapperX<SrmPurchaseOrderItemDO>()
            .selectAll(SrmPurchaseOrderItemDO.class)
            .eqIfPresent(SrmPurchaseOrderItemDO::getProductId, reqVO.getProductId()) // 产品ID
            .likeIfPresent(SrmPurchaseOrderItemDO::getProductCode, reqVO.getProductCode()) // 产品SKU
            .likeIfPresent(SrmPurchaseOrderItemDO::getProductName, reqVO.getProductName()) // 产品名称
            .likeIfPresent(SrmPurchaseOrderItemDO::getProductUnitName, reqVO.getProductUnitName()) // 产品单位名称
            .eqIfPresent(SrmPurchaseOrderItemDO::getApplicantId, reqVO.getApplicantId()) // 申请人ID
            .eqIfPresent(SrmPurchaseOrderItemDO::getApplicationDeptId, reqVO.getApplicationDeptId()) // 申请部门ID
            .eqIfPresent(SrmPurchaseOrderItemDO::getFbaCode, reqVO.getFbaCode()) // X码
            .likeIfPresent(SrmPurchaseOrderItemDO::getContainerRate, reqVO.getContainerRate()) // 箱率
            .likeIfPresent(SrmPurchaseOrderItemDO::getPurchaseApplyCode, reqVO.getPurchaseApplyCode()) // 原单单号
            .orderByDesc(SrmPurchaseOrderItemDO::getCreateTime) // 按时间降序排序
            ;
    }


    default MPJLambdaWrapperX<SrmPurchaseOrderItemDO> buildBOWrapper(SrmPurchaseOrderPageReqVO reqVO) {
        if (reqVO == null) {
            reqVO = new SrmPurchaseOrderPageReqVO();
        }
        return buildWrapper(reqVO)
            .leftJoin(SrmPurchaseOrderDO.class, SrmPurchaseOrderDO::getId, SrmPurchaseOrderItemDO::getOrderId) // 采购订单ID关联
            .selectAll(SrmPurchaseOrderDO.class)
            .likeIfPresent(SrmPurchaseOrderDO::getCode, reqVO.getCode()) // 采购单编号
            .betweenIfPresent(SrmPurchaseOrderDO::getBillTime, reqVO.getBillTime()) // 单据日期
            .eqIfPresent(SrmPurchaseOrderDO::getSupplierId, reqVO.getSupplierId()) // 供应商ID
            .eqIfPresent(SrmPurchaseOrderDO::getAccountId, reqVO.getAccountId()) // 结算账户ID
            .eqIfPresent(SrmPurchaseOrderDO::getTotalCount, reqVO.getTotalCount()) // 合计数量
            .eqIfPresent(SrmPurchaseOrderDO::getTotalPrice, reqVO.getTotalPrice()) // 合计总价
            .eqIfPresent(SrmPurchaseOrderDO::getTotalProductPrice, reqVO.getTotalProductPrice()) // 合计产品价格
            .eqIfPresent(SrmPurchaseOrderDO::getTotalGrossPrice, reqVO.getTotalGrossPrice()) // 合计税额
            .eqIfPresent(SrmPurchaseOrderDO::getDiscountPercent, reqVO.getDiscountPercent()) // 优惠率
            .eqIfPresent(SrmPurchaseOrderDO::getDiscountPrice, reqVO.getDiscountPrice()) // 优惠金额
            .eqIfPresent(SrmPurchaseOrderDO::getDepositPrice, reqVO.getDepositPrice()) // 定金金额
            .likeIfPresent(SrmPurchaseOrderDO::getRemark, reqVO.getRemark()) // 备注
            .eqIfPresent(SrmPurchaseOrderDO::getTotalInboundCount, reqVO.getTotalInboundCount()) // 总入库数量
            .eqIfPresent(SrmPurchaseOrderDO::getTotalReturnCount, reqVO.getTotalReturnCount()) // 总退货数量
            .betweenIfPresent(SrmPurchaseOrderDO::getCreateTime, reqVO.getCreateTime()) // 创建时间
            .betweenIfPresent(SrmPurchaseOrderDO::getSettlementDate, reqVO.getSettlementDate()) // 结算日期
            .eqIfPresent(SrmPurchaseOrderDO::getAuditorId, reqVO.getAuditorId()) // 审核人ID
            .betweenIfPresent(SrmPurchaseOrderDO::getAuditTime, reqVO.getAuditTime()) // 审核时间
            .eqIfPresent(SrmPurchaseOrderDO::getPurchaseCompanyId, reqVO.getPurchaseCompanyId()) // 财务主体ID
//            .likeIfPresent(SrmPurchaseOrderDO::getContainerRate, reqVO.getContainerRate()) // 箱率
            .eqIfPresent(SrmPurchaseOrderDO::getWarehouseId, reqVO.getWarehouseId()) // 仓库ID
            .eqIfPresent(SrmPurchaseOrderDO::getOffStatus, reqVO.getOffStatus()) // 关闭状态
            .eqIfPresent(SrmPurchaseOrderDO::getExecuteStatus, reqVO.getExecuteStatus()) // 执行状态
            .inIfPresent(SrmPurchaseOrderDO::getInboundStatus, reqVO.getInboundStatusList()) // 入库状态
            .eqIfPresent(SrmPurchaseOrderDO::getPayStatus, reqVO.getPayStatus()) // 付款状态
            .eqIfPresent(SrmPurchaseOrderDO::getAuditStatus, reqVO.getAuditStatus()) // 审核状态
            .likeIfPresent(SrmPurchaseOrderDO::getAddress, reqVO.getAddress()) // 收货地址
            .likeIfPresent(SrmPurchaseOrderDO::getPaymentTerms, reqVO.getPaymentTerms()) // 付款条款
            .eqIfPresent(SrmPurchaseOrderDO::getOrderStatus, reqVO.getOrderStatus()) // 订单状态
            .eqIfPresent(SrmPurchaseOrderDO::getCreator, reqVO.getCreator())
            .orderByDesc(SrmPurchaseOrderDO::getCreateTime) // 按时间降序排序
            ;
    }

    //获得ErpPurchaseOrderItemBO分页查询
    default PageResult<SrmPurchaseOrderItemBO> selectErpPurchaseOrderItemBOPage(SrmPurchaseOrderPageReqVO reqVO) {
        MPJLambdaWrapper<SrmPurchaseOrderItemDO> wrapper = buildBOWrapper(reqVO).selectAssociation(SrmPurchaseOrderDO.class, SrmPurchaseOrderItemBO::getSrmPurchaseOrderDO);//一对一关联
        return selectJoinPage(reqVO, SrmPurchaseOrderItemBO.class, wrapper);
    }

    //获得ErpPurchaseOrderItemBO 一个
    default SrmPurchaseOrderItemBO selectErpPurchaseOrderItemBOById(Long id) {
        MPJLambdaWrapper<SrmPurchaseOrderItemDO> wrapper =
            buildBOWrapper(null).selectAssociation(SrmPurchaseOrderDO.class, SrmPurchaseOrderItemBO::getSrmPurchaseOrderDO)//一对一关联
                .eq(SrmPurchaseOrderItemDO::getId, id);
        return selectJoinOne(SrmPurchaseOrderItemBO.class, wrapper);
    }

    //获得ErpPurchaseOrderItemBO列表查询
    default List<SrmPurchaseOrderItemBO> selectErpPurchaseOrderItemBOS(SrmPurchaseOrderPageReqVO reqVO) {
        MPJLambdaWrapper<SrmPurchaseOrderItemDO> wrapper =
            buildBOWrapper(reqVO).selectAssociation(SrmPurchaseOrderDO.class, SrmPurchaseOrderItemBO::getSrmPurchaseOrderDO);//一对一关联
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