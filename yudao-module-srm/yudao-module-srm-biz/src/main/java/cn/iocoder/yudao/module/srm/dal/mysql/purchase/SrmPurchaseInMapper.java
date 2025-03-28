package cn.iocoder.yudao.module.srm.dal.mysql.purchase;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.SrmPurchaseInPageReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInItemDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * ERP 采购入库 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface SrmPurchaseInMapper extends BaseMapperX<SrmPurchaseInDO> {

    //WrapperX 方法
    default MPJLambdaWrapper<SrmPurchaseInDO> bindQueryWrapper(SrmPurchaseInPageReqVO reqVO) {
        return new MPJLambdaWrapperX<SrmPurchaseInDO>()
            .selectAll(SrmPurchaseInDO.class)
            .likeIfPresent(SrmPurchaseInDO::getNo, reqVO.getNo())
            .eqIfPresent(SrmPurchaseInDO::getAuditStatus, reqVO.getAuditStatus())
            .eqIfPresent(SrmPurchaseInDO::getPayStatus, reqVO.getPayStatus())
            .eqIfPresent(SrmPurchaseInDO::getReconciliationStatus, reqVO.getReconciliationEnable())
            .eqIfPresent(SrmPurchaseInDO::getAuditorId, reqVO.getAuditorId())
            //
            .eqIfPresent(SrmPurchaseInDO::getAccountId, reqVO.getAccountId())
            .betweenIfPresent(SrmPurchaseInDO::getNoTime, reqVO.getNoTime())
            .betweenIfPresent(SrmPurchaseInDO::getAuditTime, reqVO.getAuditTime())
            .betweenIfPresent(SrmPurchaseInDO::getInTime, reqVO.getInTime());

    }

    default PageResult<SrmPurchaseInDO> selectPage(SrmPurchaseInPageReqVO reqVO) {
        MPJLambdaWrapper<SrmPurchaseInDO> queryWrapper = bindQueryWrapper(reqVO);

        if (reqVO.getWarehouseId() != null || reqVO.getProductId() != null) {
            queryWrapper.leftJoin(SrmPurchaseInItemDO.class, SrmPurchaseInItemDO::getInId, SrmPurchaseInDO::getId)
                .eq(reqVO.getWarehouseId() != null, SrmPurchaseInItemDO::getWarehouseId, reqVO.getWarehouseId())
                .eq(reqVO.getProductId() != null, SrmPurchaseInItemDO::getProductId, reqVO.getProductId())
                .groupBy(SrmPurchaseInDO::getId); // 避免 1 对多查询，产生相同的 1
        }
        return selectJoinPage(reqVO, SrmPurchaseInDO.class, queryWrapper);
    }

    default int updateByIdAndStatus(Long id, Integer status, SrmPurchaseInDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<SrmPurchaseInDO>()
            .eq(SrmPurchaseInDO::getId, id).eq(SrmPurchaseInDO::getAuditStatus, status));
    }

    default SrmPurchaseInDO selectByNo(String no) {
        return selectOne(SrmPurchaseInDO::getNo, no);
    }

//    default List<SrmPurchaseInDO> selectListByOrderId(Long orderId) {
//        return selectList(SrmPurchaseInDO::getOrderId, orderId);
//    }

}