package cn.iocoder.yudao.module.srm.dal.mysql.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.returns.SrmPurchaseReturnPageReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseReturnDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseReturnItemDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collections;
import java.util.List;

/**
 * ERP 采购退货 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface SrmPurchaseReturnMapper extends BaseMapperX<SrmPurchaseReturnDO> {

    default PageResult<SrmPurchaseReturnDO> selectPage(SrmPurchaseReturnPageReqVO reqVO) {
        MPJLambdaWrapperX<SrmPurchaseReturnDO> query = new MPJLambdaWrapperX<SrmPurchaseReturnDO>()
                .likeIfPresent(SrmPurchaseReturnDO::getNo, reqVO.getNo())
                .eqIfPresent(SrmPurchaseReturnDO::getSupplierId, reqVO.getSupplierId())
                .betweenIfPresent(SrmPurchaseReturnDO::getReturnTime, reqVO.getReturnTime())
                .eqIfPresent(SrmPurchaseReturnDO::getAuditStatus, reqVO.getStatus())
                .likeIfPresent(SrmPurchaseReturnDO::getRemark, reqVO.getRemark())
                .eqIfPresent(SrmPurchaseReturnDO::getCreator, reqVO.getCreator())
                .eqIfPresent(SrmPurchaseReturnDO::getAccountId, reqVO.getAccountId())
//                .likeIfPresent(SrmPurchaseReturnDO::getOrderNo, reqVO.getOrderNo())
                .orderByDesc(SrmPurchaseReturnDO::getId);
        // 退款状态。为什么需要 t. 的原因，是因为联表查询时，需要指定表名，不然会报字段不存在的错误
//        if (Objects.equals(reqVO.getRefundStatus(), SrmPurchaseReturnPageReqVO.REFUND_STATUS_NONE)) {
//            query.eq(SrmPurchaseReturnDO::getRefundPrice, 0);
//        } else if (Objects.equals(reqVO.getRefundStatus(), SrmPurchaseReturnPageReqVO.REFUND_STATUS_PART)) {
//            query.gt(SrmPurchaseReturnDO::getRefundPrice, 0).apply("t.refund_price < t.total_price");
//        } else if (Objects.equals(reqVO.getRefundStatus(), SrmPurchaseReturnPageReqVO.REFUND_STATUS_ALL)) {
//            query.apply("t.refund_price = t.total_price");
//        }
//        if (Boolean.TRUE.equals(reqVO.getRefundEnable())) {
//            query.eq(SrmPurchaseInDO::getAuditStatus, SrmAuditStatus.APPROVED.getCode())
//                    .apply("t.refund_price < t.total_price");
//        }
        if (reqVO.getWarehouseId() != null || reqVO.getProductId() != null) {
            query.leftJoin(SrmPurchaseReturnItemDO.class, SrmPurchaseReturnItemDO::getReturnId, SrmPurchaseReturnDO::getId)
                    .eq(reqVO.getWarehouseId() != null, SrmPurchaseReturnItemDO::getWarehouseId, reqVO.getWarehouseId())
                    .eq(reqVO.getProductId() != null, SrmPurchaseReturnItemDO::getProductId, reqVO.getProductId())
                    .groupBy(SrmPurchaseReturnDO::getId); // 避免 1 对多查询，产生相同的 1
        }
        return selectJoinPage(reqVO, SrmPurchaseReturnDO.class, query);
    }

    default int updateByIdAndStatus(Long id, Integer status, SrmPurchaseReturnDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<SrmPurchaseReturnDO>()
                .eq(SrmPurchaseReturnDO::getId, id).eq(SrmPurchaseReturnDO::getAuditStatus, status));
    }

    default SrmPurchaseReturnDO selectByNo(String no) {
        return selectOne(SrmPurchaseReturnDO::getNo, no);
    }

//    default List<SrmPurchaseReturnDO> selectListByOrderId(Long orderId) {
//        return selectList(SrmPurchaseReturnDO::getOrderId, orderId);
//    }

    //通过ids查询,如果ids是空，则返回空集合
    default List<SrmPurchaseReturnDO> selectListByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapper<SrmPurchaseReturnDO>().in(SrmPurchaseReturnDO::getId, ids));
    }
}