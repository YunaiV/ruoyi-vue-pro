package cn.iocoder.yudao.module.erp.dal.mysql.purchase;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInItemDO;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Objects;

/**
 * ERP 采购入库 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ErpPurchaseInMapper extends BaseMapperX<ErpPurchaseInDO> {

    default PageResult<ErpPurchaseInDO> selectPage(ErpPurchaseInPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpPurchaseInDO> query = new MPJLambdaWrapperX<ErpPurchaseInDO>()
                .likeIfPresent(ErpPurchaseInDO::getNo, reqVO.getNo())
                .eqIfPresent(ErpPurchaseInDO::getSupplierId, reqVO.getSupplierId())
                .betweenIfPresent(ErpPurchaseInDO::getInTime, reqVO.getInTime())
                .eqIfPresent(ErpPurchaseInDO::getStatus, reqVO.getStatus())
                .likeIfPresent(ErpPurchaseInDO::getRemark, reqVO.getRemark())
                .eqIfPresent(ErpPurchaseInDO::getCreator, reqVO.getCreator())
                .eqIfPresent(ErpPurchaseInDO::getAccountId, reqVO.getAccountId())
                .likeIfPresent(ErpPurchaseInDO::getOrderNo, reqVO.getOrderNo())
                .orderByDesc(ErpPurchaseInDO::getId);
        // 付款状态。为什么需要 t. 的原因，是因为联表查询时，需要指定表名，不然会报字段不存在的错误
        if (Objects.equals(reqVO.getPaymentStatus(), ErpPurchaseInPageReqVO.PAYMENT_STATUS_NONE)) {
            query.eq(ErpPurchaseInDO::getPaymentPrice, 0);
        } else if (Objects.equals(reqVO.getPaymentStatus(), ErpPurchaseInPageReqVO.PAYMENT_STATUS_PART)) {
            query.gt(ErpPurchaseInDO::getPaymentPrice, 0).apply("t.payment_price < t.total_price");
        } else if (Objects.equals(reqVO.getPaymentStatus(), ErpPurchaseInPageReqVO.PAYMENT_STATUS_ALL)) {
            query.apply("t.payment_price = t.total_price");
        }
        if (Boolean.TRUE.equals(reqVO.getPaymentEnable())) {
            query.eq(ErpPurchaseInDO::getStatus, ErpAuditStatus.APPROVE.getStatus())
                    .apply("t.payment_price < t.total_price");
        }
        if (reqVO.getWarehouseId() != null || reqVO.getProductId() != null) {
            query.leftJoin(ErpPurchaseInItemDO.class, ErpPurchaseInItemDO::getInId, ErpPurchaseInDO::getId)
                    .eq(reqVO.getWarehouseId() != null, ErpPurchaseInItemDO::getWarehouseId, reqVO.getWarehouseId())
                    .eq(reqVO.getProductId() != null, ErpPurchaseInItemDO::getProductId, reqVO.getProductId())
                    .groupBy(ErpPurchaseInDO::getId); // 避免 1 对多查询，产生相同的 1
        }
        return selectJoinPage(reqVO, ErpPurchaseInDO.class, query);
    }

    default int updateByIdAndStatus(Long id, Integer status, ErpPurchaseInDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<ErpPurchaseInDO>()
                .eq(ErpPurchaseInDO::getId, id).eq(ErpPurchaseInDO::getStatus, status));
    }

    default ErpPurchaseInDO selectByNo(String no) {
        return selectOne(ErpPurchaseInDO::getNo, no);
    }

    default List<ErpPurchaseInDO> selectListByOrderId(Long orderId) {
        return selectList(ErpPurchaseInDO::getOrderId, orderId);
    }

}