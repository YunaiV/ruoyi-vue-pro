package cn.iocoder.yudao.module.erp.dal.mysql.sale;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.out.ErpSaleOutPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOutDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOutItemDO;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Objects;

/**
 * ERP 销售出库 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ErpSaleOutMapper extends BaseMapperX<ErpSaleOutDO> {

    default PageResult<ErpSaleOutDO> selectPage(ErpSaleOutPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpSaleOutDO> query = new MPJLambdaWrapperX<ErpSaleOutDO>()
                .likeIfPresent(ErpSaleOutDO::getNo, reqVO.getNo())
                .eqIfPresent(ErpSaleOutDO::getCustomerId, reqVO.getCustomerId())
                .betweenIfPresent(ErpSaleOutDO::getOutTime, reqVO.getOutTime())
                .eqIfPresent(ErpSaleOutDO::getStatus, reqVO.getStatus())
                .likeIfPresent(ErpSaleOutDO::getRemark, reqVO.getRemark())
                .eqIfPresent(ErpSaleOutDO::getCreator, reqVO.getCreator())
                .eqIfPresent(ErpSaleOutDO::getAccountId, reqVO.getAccountId())
                .likeIfPresent(ErpSaleOutDO::getOrderNo, reqVO.getOrderNo())
                .orderByDesc(ErpSaleOutDO::getId);
        // 收款状态。为什么需要 t. 的原因，是因为联表查询时，需要指定表名，不然会报字段不存在的错误
        if (Objects.equals(reqVO.getReceiptStatus(), ErpSaleOutPageReqVO.RECEIPT_STATUS_NONE)) {
            query.eq(ErpSaleOutDO::getReceiptPrice, 0);
        } else if (Objects.equals(reqVO.getReceiptStatus(), ErpSaleOutPageReqVO.RECEIPT_STATUS_PART)) {
            query.gt(ErpSaleOutDO::getReceiptPrice, 0).apply("t.receipt_price < t.total_price");
        } else if (Objects.equals(reqVO.getReceiptStatus(), ErpSaleOutPageReqVO.RECEIPT_STATUS_ALL)) {
            query.apply("t.receipt_price = t.total_price");
        }
        if (Boolean.TRUE.equals(reqVO.getReceiptEnable())) {
            query.eq(ErpSaleOutDO::getStatus, ErpAuditStatus.APPROVE.getStatus())
                    .apply("t.receipt_price < t.total_price");
        }
        if (reqVO.getWarehouseId() != null || reqVO.getProductId() != null) {
            query.leftJoin(ErpSaleOutItemDO.class, ErpSaleOutItemDO::getOutId, ErpSaleOutDO::getId)
                    .eq(reqVO.getWarehouseId() != null, ErpSaleOutItemDO::getWarehouseId, reqVO.getWarehouseId())
                    .eq(reqVO.getProductId() != null, ErpSaleOutItemDO::getProductId, reqVO.getProductId())
                    .groupBy(ErpSaleOutDO::getId); // 避免 1 对多查询，产生相同的 1
        }
        return selectJoinPage(reqVO, ErpSaleOutDO.class, query);
    }

    default int updateByIdAndStatus(Long id, Integer status, ErpSaleOutDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<ErpSaleOutDO>()
                .eq(ErpSaleOutDO::getId, id).eq(ErpSaleOutDO::getStatus, status));
    }

    default ErpSaleOutDO selectByNo(String no) {
        return selectOne(ErpSaleOutDO::getNo, no);
    }

    default List<ErpSaleOutDO> selectListByOrderId(Long orderId) {
        return selectList(ErpSaleOutDO::getOrderId, orderId);
    }

}