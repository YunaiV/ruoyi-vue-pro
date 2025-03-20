package cn.iocoder.yudao.module.erp.dal.mysql.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
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
        MPJLambdaWrapperX<ErpPurchaseOrderDO> query = new MPJLambdaWrapperX<ErpPurchaseOrderDO>()
            .likeIfPresent(ErpPurchaseOrderDO::getNo, reqVO.getNo())
            .eqIfPresent(ErpPurchaseOrderDO::getSupplierId, reqVO.getSupplierId())
            .betweenIfPresent(ErpPurchaseOrderDO::getOrderTime, reqVO.getOrderTime())
            .eqIfPresent(ErpPurchaseOrderDO::getAuditStatus, reqVO.getStatus())
            .likeIfPresent(ErpPurchaseOrderDO::getRemark, reqVO.getRemark())
            .eqIfPresent(ErpPurchaseOrderDO::getCreator, reqVO.getCreator())
            .orderByDesc(ErpPurchaseOrderDO::getId);
//        // 入库状态。为什么需要 t. 的原因，是因为联表查询时，需要指定表名，不然会报 in_count 错误
//        if (Objects.equals(reqVO.getInStatus(), ErpPurchaseOrderPageReqVO.IN_STATUS_NONE)) {
//            query.eq(ErpPurchaseOrderDO::getTotalInCount, 0);
//        } else if (Objects.equals(reqVO.getInStatus(), ErpPurchaseOrderPageReqVO.IN_STATUS_PART)) {
//            query.gt(ErpPurchaseOrderDO::getTotalInCount, 0).apply("t.total_in_count < t.total_count");
//        } else if (Objects.equals(reqVO.getInStatus(), ErpPurchaseOrderPageReqVO.IN_STATUS_ALL)) {
//            query.apply("t.total_in_count = t.total_count");
//        }
//        // 退货状态
//        if (Objects.equals(reqVO.getReturnStatus(), ErpPurchaseOrderPageReqVO.RETURN_STATUS_NONE)) {
//            query.eq(ErpPurchaseOrderDO::getTotalReturnCount, 0);
//        } else if (Objects.equals(reqVO.getReturnStatus(), ErpPurchaseOrderPageReqVO.RETURN_STATUS_PART)) {
//            query.gt(ErpPurchaseOrderDO::getTotalReturnCount, 0).apply("t.total_return_count < t.total_count");
//        } else if (Objects.equals(reqVO.getReturnStatus(), ErpPurchaseOrderPageReqVO.RETURN_STATUS_ALL)) {
//            query.apply("t.total_return_count = t.total_count");
//        }
//        // 可采购入库
//        if (Boolean.TRUE.equals(reqVO.getInEnable())) {
//            query.eq(ErpPurchaseOrderDO::getAuditStatus, ErpAuditStatus.APPROVED.getCode())
//                .apply("t.total_in_count < t.total_count");
//        }
//        // 可采购退货
//        if (Boolean.TRUE.equals(reqVO.getReturnEnable())) {
//            query.eq(ErpPurchaseOrderDO::getAuditStatus, ErpAuditStatus.APPROVED.getCode())
//                .apply("t.total_return_count < t.total_in_count");
//        }
        if (reqVO.getProductId() != null) {
            query.leftJoin(ErpPurchaseOrderItemDO.class, ErpPurchaseOrderItemDO::getOrderId, ErpPurchaseOrderDO::getId)
                .eq(reqVO.getProductId() != null, ErpPurchaseOrderItemDO::getProductId, reqVO.getProductId())
                // 避免 1 对多查询，产生相同的 1
                .groupBy(ErpPurchaseOrderDO::getId);
        }
        return selectJoinPage(reqVO, ErpPurchaseOrderDO.class, query);
    }

    default int updateByIdAndStatus(Long id, Integer status, ErpPurchaseOrderDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<ErpPurchaseOrderDO>()
            .eq(ErpPurchaseOrderDO::getId, id).eq(ErpPurchaseOrderDO::getAuditStatus, status));
    }

    default ErpPurchaseOrderDO selectByNo(String no) {
        return selectOne(ErpPurchaseOrderDO::getNo, no);
    }


}