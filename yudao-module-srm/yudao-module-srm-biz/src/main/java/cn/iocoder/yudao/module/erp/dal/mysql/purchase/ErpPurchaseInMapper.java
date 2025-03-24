package cn.iocoder.yudao.module.erp.dal.mysql.purchase;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInItemDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * ERP 采购入库 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ErpPurchaseInMapper extends BaseMapperX<ErpPurchaseInDO> {

    //WrapperX 方法
    default MPJLambdaWrapper<ErpPurchaseInDO> bindQueryWrapper(ErpPurchaseInPageReqVO reqVO) {
        return new MPJLambdaWrapperX<ErpPurchaseInDO>()
            .selectAll(ErpPurchaseInDO.class)
            .likeIfPresent(ErpPurchaseInDO::getNo, reqVO.getNo())
            .eqIfPresent(ErpPurchaseInDO::getAuditStatus, reqVO.getAuditStatus())
            .eqIfPresent(ErpPurchaseInDO::getPayStatus, reqVO.getPayStatus())
            .eqIfPresent(ErpPurchaseInDO::getReconciliationStatus, reqVO.getReconciliationEnable())
            .eqIfPresent(ErpPurchaseInDO::getAuditorId, reqVO.getAuditorId())
            //
            .eqIfPresent(ErpPurchaseInDO::getAccountId, reqVO.getAccountId())
            .betweenIfPresent(ErpPurchaseInDO::getNoTime, reqVO.getNoTime())
            .betweenIfPresent(ErpPurchaseInDO::getAuditTime, reqVO.getAuditTime())
            .betweenIfPresent(ErpPurchaseInDO::getInTime, reqVO.getInTime());

    }

    default PageResult<ErpPurchaseInDO> selectPage(ErpPurchaseInPageReqVO reqVO) {
        MPJLambdaWrapper<ErpPurchaseInDO> queryWrapper = bindQueryWrapper(reqVO);

        if (reqVO.getWarehouseId() != null || reqVO.getProductId() != null) {
            queryWrapper.leftJoin(ErpPurchaseInItemDO.class, ErpPurchaseInItemDO::getInId, ErpPurchaseInDO::getId)
                .eq(reqVO.getWarehouseId() != null, ErpPurchaseInItemDO::getWarehouseId, reqVO.getWarehouseId())
                .eq(reqVO.getProductId() != null, ErpPurchaseInItemDO::getProductId, reqVO.getProductId())
                .groupBy(ErpPurchaseInDO::getId); // 避免 1 对多查询，产生相同的 1
        }
        return selectJoinPage(reqVO, ErpPurchaseInDO.class, queryWrapper);
    }

    default int updateByIdAndStatus(Long id, Integer status, ErpPurchaseInDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<ErpPurchaseInDO>()
            .eq(ErpPurchaseInDO::getId, id).eq(ErpPurchaseInDO::getAuditStatus, status));
    }

    default ErpPurchaseInDO selectByNo(String no) {
        return selectOne(ErpPurchaseInDO::getNo, no);
    }

//    default List<ErpPurchaseInDO> selectListByOrderId(Long orderId) {
//        return selectList(ErpPurchaseInDO::getOrderId, orderId);
//    }

}