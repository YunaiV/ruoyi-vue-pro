package cn.iocoder.yudao.module.wms.dal.mysql.stock.ownership;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.vo.WmsStockOwnershipPageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.product.WmsProductDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership.WmsStockOwnershipDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 所有者库存 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsStockOwnershipMapper extends BaseMapperX<WmsStockOwnershipDO> {

    default PageResult<WmsStockOwnershipDO> selectPage(WmsStockOwnershipPageReqVO reqVO) {

        MPJLambdaWrapperX<WmsStockOwnershipDO> wrapper = new MPJLambdaWrapperX();
        // 连接产品视图
        wrapper.innerJoin(WmsProductDO.class, WmsProductDO::getId, WmsStockOwnershipDO::getProductId)
            .likeIfExists(WmsProductDO::getBarCode, reqVO.getProductCode())
            .eqIfExists(WmsProductDO::getDeptId, reqVO.getDeptId());
        // 按仓库
        wrapper.eqIfPresent(WmsStockOwnershipDO::getWarehouseId, reqVO.getWarehouseId())
            // 按产品ID
            .eqIfPresent(WmsStockOwnershipDO::getProductId, reqVO.getProductId());

        wrapper.betweenIfPresent(WmsStockOwnershipDO::getAvailableQty,reqVO.getAvailableQty());
        wrapper.betweenIfPresent(WmsStockOwnershipDO::getOutboundPendingQty,reqVO.getOutboundPendingQty());
        wrapper.betweenIfPresent(WmsStockOwnershipDO::getShelvingPendingQty,reqVO.getShelvingPendingQty());

        return selectPage(reqVO, wrapper);


    }


    /**
     * 按 warehouse_id,dept_id,product_id 查询唯一的 WmsStockOwnershipDO
     */
    default WmsStockOwnershipDO getByUkProductOwner(Long warehouseId, Long companyId, Long deptId, Long productId) {
        LambdaQueryWrapperX<WmsStockOwnershipDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsStockOwnershipDO::getWarehouseId, warehouseId);
        wrapper.eq(WmsStockOwnershipDO::getCompanyId, companyId);
        wrapper.eq(WmsStockOwnershipDO::getDeptId, deptId);
        wrapper.eq(WmsStockOwnershipDO::getProductId, productId);
        return selectOne(wrapper);
    }

    default List<WmsStockOwnershipDO> selectStockOwnership(Long warehouseId, Long productId) {
        LambdaQueryWrapperX<WmsStockOwnershipDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsStockOwnershipDO::getWarehouseId, warehouseId);
        wrapper.eq(WmsStockOwnershipDO::getProductId, productId);
        return selectList(wrapper);
    }

    default List<WmsStockOwnershipDO> selectStockOwnership(Long warehouseId, List<Long> productIdList) {
        LambdaQueryWrapperX<WmsStockOwnershipDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsStockOwnershipDO::getWarehouseId, warehouseId);
        wrapper.in(WmsStockOwnershipDO::getProductId, productIdList);
        return selectList(wrapper);
    }
}
