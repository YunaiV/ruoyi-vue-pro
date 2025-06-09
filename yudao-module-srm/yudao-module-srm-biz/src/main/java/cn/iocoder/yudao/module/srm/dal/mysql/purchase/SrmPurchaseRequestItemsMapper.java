package cn.iocoder.yudao.module.srm.dal.mysql.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.request.req.SrmPurchaseRequestPageReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.srm.service.purchase.bo.request.SrmPurchaseRequestItemsBO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ERP采购申请单子 Mapper
 *
 * @author wdy
 */
@Mapper
public interface SrmPurchaseRequestItemsMapper extends BaseMapperX<SrmPurchaseRequestItemsDO> {

    //buildWrapper
    default MPJLambdaWrapperX<SrmPurchaseRequestItemsDO> buildWrapper(SrmPurchaseRequestPageReqVO req) {
        return new MPJLambdaWrapperX<SrmPurchaseRequestItemsDO>()
            .selectAll(SrmPurchaseRequestItemsDO.class)
            .eqIfPresent(SrmPurchaseRequestItemsDO::getProductId, req.getProductId())
            .likeIfPresent(SrmPurchaseRequestItemsDO::getProductCode, req.getProductCode())
            .likeIfPresent(SrmPurchaseRequestItemsDO::getProductName, req.getProductName())
            .likeIfPresent(SrmPurchaseRequestItemsDO::getProductUnitName, req.getProductUnitName())
            .orderByDesc(SrmPurchaseRequestItemsDO::getCreateTime) // 按时间降序排序
            ;
    }

    //BO wrapper
    default MPJLambdaWrapperX<SrmPurchaseRequestItemsDO> buildBOWrapper(SrmPurchaseRequestPageReqVO req) {
        return buildWrapper(req)
            .leftJoin(SrmPurchaseRequestDO.class, SrmPurchaseRequestDO::getId, SrmPurchaseRequestItemsDO::getRequestId)
            .selectAll(SrmPurchaseRequestDO.class)
            .likeIfPresent(SrmPurchaseRequestDO::getCode, req.getCode())
            .eqIfPresent(SrmPurchaseRequestDO::getApplicantId, req.getApplicantId())
            .eqIfPresent(SrmPurchaseRequestDO::getApplicationDeptId, req.getApplicationDeptId())
            .betweenIfPresent(SrmPurchaseRequestDO::getBillTime, req.getBillTime())
            .eqIfPresent(SrmPurchaseRequestDO::getAuditorId, req.getAuditorId())
            .betweenIfPresent(SrmPurchaseRequestDO::getAuditTime, req.getAuditTime())
            .betweenIfPresent(SrmPurchaseRequestDO::getCreateTime, req.getCreateTime())
            .eqIfPresent(SrmPurchaseRequestDO::getSupplierId, req.getSupplierId())
            .eqIfPresent(SrmPurchaseRequestDO::getAuditStatus, req.getAuditStatus())
            .eqIfPresent(SrmPurchaseRequestDO::getOffStatus, req.getOffStatus())
            .eqIfPresent(SrmPurchaseRequestDO::getOrderStatus, req.getOrderStatus())
            .likeIfPresent(SrmPurchaseRequestDO::getTag, req.getTag())
            .likeIfPresent(SrmPurchaseRequestDO::getDelivery, req.getDelivery())
            .likeIfPresent(SrmPurchaseRequestDO::getAuditAdvice, req.getAuditAdvice())
            .eqIfPresent(SrmPurchaseRequestDO::getInboundStatus, req.getInboundStatus())
            .orderByDesc(SrmPurchaseOrderDO::getCreateTime) // 按时间降序排序
            ;
    }

    //分页查询
    default PageResult<SrmPurchaseRequestItemsBO> selectPageBO(SrmPurchaseRequestPageReqVO req) {
        return selectJoinPage(
            req,
            SrmPurchaseRequestItemsBO.class,
            buildBOWrapper(req).selectAssociation(SrmPurchaseRequestDO.class, SrmPurchaseRequestItemsBO::getPurchaseRequest)
        );
    }

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
        return selectByIds(itemIds);
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