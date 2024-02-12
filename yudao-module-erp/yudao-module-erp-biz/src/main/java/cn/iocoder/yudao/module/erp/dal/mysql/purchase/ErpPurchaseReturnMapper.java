package cn.iocoder.yudao.module.erp.dal.mysql.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseReturnDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseReturnItemDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * ERP 采购退货 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ErpPurchaseReturnMapper extends BaseMapperX<ErpPurchaseReturnDO> {

    default PageResult<ErpPurchaseReturnDO> selectPage(ErpPurchaseReturnPageReqVO reqVO) {
        MPJLambdaWrapperX<ErpPurchaseReturnDO> query = new MPJLambdaWrapperX<ErpPurchaseReturnDO>()
                .likeIfPresent(ErpPurchaseReturnDO::getNo, reqVO.getNo())
                .eqIfPresent(ErpPurchaseReturnDO::getSupplierId, reqVO.getSupplierId())
                .betweenIfPresent(ErpPurchaseReturnDO::getReturnTime, reqVO.getReturnTime())
                .eqIfPresent(ErpPurchaseReturnDO::getStatus, reqVO.getStatus())
                .likeIfPresent(ErpPurchaseReturnDO::getRemark, reqVO.getRemark())
                .eqIfPresent(ErpPurchaseReturnDO::getCreator, reqVO.getCreator())
                .eqIfPresent(ErpPurchaseReturnDO::getAccountId, reqVO.getAccountId())
                .likeIfPresent(ErpPurchaseReturnDO::getOrderNo, reqVO.getOrderNo())
                .orderByDesc(ErpPurchaseReturnDO::getId);
        if (reqVO.getWarehouseId() != null || reqVO.getProductId() != null) {
            query.leftJoin(ErpPurchaseReturnItemDO.class, ErpPurchaseReturnItemDO::getReturnId, ErpPurchaseReturnDO::getId)
                    .eq(reqVO.getWarehouseId() != null, ErpPurchaseReturnItemDO::getWarehouseId, reqVO.getWarehouseId())
                    .eq(reqVO.getProductId() != null, ErpPurchaseReturnItemDO::getProductId, reqVO.getProductId())
                    .groupBy(ErpPurchaseReturnDO::getId); // 避免 1 对多查询，产生相同的 1
        }
        return selectJoinPage(reqVO, ErpPurchaseReturnDO.class, query);
    }

    default int updateByIdAndStatus(Long id, Integer status, ErpPurchaseReturnDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<ErpPurchaseReturnDO>()
                .eq(ErpPurchaseReturnDO::getId, id).eq(ErpPurchaseReturnDO::getStatus, status));
    }

    default ErpPurchaseReturnDO selectByNo(String no) {
        return selectOne(ErpPurchaseReturnDO::getNo, no);
    }

    default List<ErpPurchaseReturnDO> selectListByOrderId(Long orderId) {
        return selectList(ErpPurchaseReturnDO::getOrderId, orderId);
    }

}