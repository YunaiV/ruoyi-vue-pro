package cn.iocoder.yudao.module.srm.dal.mysql.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.order.req.SrmPurchaseOrderPageReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * ERP 采购订单 Mapper
 *
 * @author wdy
 */
@Mapper
public interface SrmPurchaseOrderMapper extends BaseMapperX<SrmPurchaseOrderDO> {

    default MPJLambdaWrapper<SrmPurchaseOrderDO> wrapper(SrmPurchaseOrderPageReqVO vo) {
        return new MPJLambdaWrapperX<SrmPurchaseOrderDO>().selectAll(SrmPurchaseOrderDO.class)
            .eqIfPresent(SrmPurchaseOrderDO::getSupplierId, vo.getSupplierId()).eqIfPresent(SrmPurchaseOrderDO::getAccountId, vo.getAccountId())
            .eqIfPresent(SrmPurchaseOrderDO::getTotalCount, vo.getTotalCount()).eqIfPresent(SrmPurchaseOrderDO::getTotalPrice, vo.getTotalPrice())
            .eqIfPresent(SrmPurchaseOrderDO::getTotalProductPrice, vo.getTotalProductPrice())
            .eqIfPresent(SrmPurchaseOrderDO::getTotalGrossPrice, vo.getTotalGrossPrice())
            .eqIfPresent(SrmPurchaseOrderDO::getDiscountPercent, vo.getDiscountPercent())
            .eqIfPresent(SrmPurchaseOrderDO::getDiscountPrice, vo.getDiscountPrice())
            .eqIfPresent(SrmPurchaseOrderDO::getRemark, vo.getRemark()).eqIfPresent(SrmPurchaseOrderDO::getTotalInboundCount, vo.getTotalInboundCount())
            .eqIfPresent(SrmPurchaseOrderDO::getTotalReturnCount, vo.getTotalReturnCount())
            .betweenIfPresent(SrmPurchaseOrderDO::getBillTime, vo.getBillTime()).betweenIfPresent(SrmPurchaseOrderDO::getCreateTime, vo.getCreateTime())
            .betweenIfPresent(SrmPurchaseOrderDO::getSettlementDate, vo.getSettlementDate())
            .eqIfPresent(SrmPurchaseOrderDO::getAuditorId, vo.getAuditorId()).betweenIfPresent(SrmPurchaseOrderDO::getAuditTime, vo.getAuditTime())
            .eqIfPresent(SrmPurchaseOrderDO::getPurchaseCompanyId, vo.getPurchaseCompanyId())
//                        .likeIfPresent(SrmPurchaseOrderDO::getXCode, vo.getXCode())
            .eqIfPresent(SrmPurchaseOrderDO::getWarehouseId, vo.getWarehouseId()).eqIfPresent(SrmPurchaseOrderDO::getOffStatus, vo.getOffStatus())
            .inIfPresent(SrmPurchaseOrderDO::getExecuteStatus, vo.getExecuteStatus()).eqIfPresent(SrmPurchaseOrderDO::getInboundStatus, vo.getInboundStatusList())
            .eqIfPresent(SrmPurchaseOrderDO::getPayStatus, vo.getPayStatus()).eqIfPresent(SrmPurchaseOrderDO::getAuditStatus, vo.getAuditStatus())
            .likeIfPresent(SrmPurchaseOrderDO::getAddress, vo.getAddress()).likeIfPresent(SrmPurchaseOrderDO::getPaymentTerms, vo.getPaymentTerms())
            .eqIfPresent(SrmPurchaseOrderDO::getOrderStatus, vo.getOrderStatus());
    }


    //需要分页主表	主表单独查 + 子表用 IN 批量查
    default PageResult<SrmPurchaseOrderDO> selectPage(SrmPurchaseOrderPageReqVO reqVO) {
        return selectPage(reqVO, wrapper(reqVO));
    }

    default int updateByIdAndStatus(Long id, Integer status, SrmPurchaseOrderDO updateObj) {
        return update(updateObj,
            new LambdaUpdateWrapper<SrmPurchaseOrderDO>().eq(SrmPurchaseOrderDO::getId, id).eq(SrmPurchaseOrderDO::getAuditStatus, status));
    }

    default SrmPurchaseOrderDO selectByNo(String no) {
        return selectOne(SrmPurchaseOrderDO::getCode, no);
    }

    //查询BO，根据订单项的erpPurchaseRequestItemNo查出对应的BO
    //嵌套结果方式会导致结果集被折叠，因此数量会少。
    //    default PageResult<SrmPurchaseOrderBO> selectBOByPageVO(SrmPurchaseOrderPageReqVO vo) {
    //        return selectJoinPage(vo, SrmPurchaseOrderBO.class, getBOWrapper(vo));
    //    }
}