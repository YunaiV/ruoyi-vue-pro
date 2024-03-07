package cn.iocoder.yudao.module.erp.dal.mysql.sale;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderItemDO;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Objects;

/**
 * ERP 销售订单 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ErpSaleOrderMapper extends BaseMapperX<ErpSaleOrderDO> {

    default PageResult<ErpSaleOrderDO> selectPage(ErpSaleOrderPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpSaleOrderDO> query = new MPJLambdaWrapperX<ErpSaleOrderDO>()
                .likeIfPresent(ErpSaleOrderDO::getNo, reqVO.getNo())
                .eqIfPresent(ErpSaleOrderDO::getCustomerId, reqVO.getCustomerId())
                .betweenIfPresent(ErpSaleOrderDO::getOrderTime, reqVO.getOrderTime())
                .eqIfPresent(ErpSaleOrderDO::getStatus, reqVO.getStatus())
                .likeIfPresent(ErpSaleOrderDO::getRemark, reqVO.getRemark())
                .eqIfPresent(ErpSaleOrderDO::getCreator, reqVO.getCreator())
                .orderByDesc(ErpSaleOrderDO::getId);
        // 入库状态。为什么需要 t. 的原因，是因为联表查询时，需要指定表名，不然会报 out_count 错误
        if (Objects.equals(reqVO.getOutStatus(), ErpSaleOrderPageReqVO.OUT_STATUS_NONE)) {
            query.eq(ErpSaleOrderDO::getOutCount, 0);
        } else if (Objects.equals(reqVO.getOutStatus(), ErpSaleOrderPageReqVO.OUT_STATUS_PART)) {
            query.gt(ErpSaleOrderDO::getOutCount, 0).apply("t.out_count < t.total_count");
        } else if (Objects.equals(reqVO.getOutStatus(), ErpSaleOrderPageReqVO.OUT_STATUS_ALL)) {
            query.apply("t.out_count = t.total_count");
        }
        // 退货状态
        if (Objects.equals(reqVO.getReturnStatus(), ErpSaleOrderPageReqVO.RETURN_STATUS_NONE)) {
            query.eq(ErpSaleOrderDO::getReturnCount, 0);
        } else if (Objects.equals(reqVO.getReturnStatus(), ErpSaleOrderPageReqVO.RETURN_STATUS_PART)) {
            query.gt(ErpSaleOrderDO::getReturnCount, 0).apply("t.return_count < t.total_count");
        } else if (Objects.equals(reqVO.getReturnStatus(), ErpSaleOrderPageReqVO.RETURN_STATUS_ALL)) {
            query.apply("t.return_count = t.total_count");
        }
        // 可销售出库
        if (Boolean.TRUE.equals(reqVO.getOutEnable())) {
            query.eq(ErpSaleOrderDO::getStatus, ErpAuditStatus.APPROVE.getStatus())
                    .apply("t.out_count < t.total_count");
        }
        // 可销售退货
        if (Boolean.TRUE.equals(reqVO.getReturnEnable())) {
            query.eq(ErpSaleOrderDO::getStatus, ErpAuditStatus.APPROVE.getStatus())
                    .apply("t.return_count < t.out_count");
        }
        if (reqVO.getProductId() != null) {
            query.leftJoin(ErpSaleOrderItemDO.class, ErpSaleOrderItemDO::getOrderId, ErpSaleOrderDO::getId)
                    .eq(reqVO.getProductId() != null, ErpSaleOrderItemDO::getProductId, reqVO.getProductId())
                    .groupBy(ErpSaleOrderDO::getId); // 避免 1 对多查询，产生相同的 1
        }
        return selectJoinPage(reqVO, ErpSaleOrderDO.class, query);
    }

    default int updateByIdAndStatus(Long id, Integer status, ErpSaleOrderDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<ErpSaleOrderDO>()
                .eq(ErpSaleOrderDO::getId, id).eq(ErpSaleOrderDO::getStatus, status));
    }

    default ErpSaleOrderDO selectByNo(String no) {
        return selectOne(ErpSaleOrderDO::getNo, no);
    }

}