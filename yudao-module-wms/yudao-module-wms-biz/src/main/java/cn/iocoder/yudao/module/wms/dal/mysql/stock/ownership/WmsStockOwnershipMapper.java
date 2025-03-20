package cn.iocoder.yudao.module.wms.dal.mysql.stock.ownership;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership.WmsStockOwnershipDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.vo.*;

/**
 * 所有者库存 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsStockOwnershipMapper extends BaseMapperX<WmsStockOwnershipDO> {

    default PageResult<WmsStockOwnershipDO> selectPage(WmsStockOwnershipPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsStockOwnershipDO>()
				.eqIfPresent(WmsStockOwnershipDO::getWarehouseId, reqVO.getWarehouseId())
				.eqIfPresent(WmsStockOwnershipDO::getProductId, reqVO.getProductId())
				.eqIfPresent(WmsStockOwnershipDO::getCompanyId, reqVO.getCompanyId())
				.eqIfPresent(WmsStockOwnershipDO::getDeptId, reqVO.getDeptId())
				.eqIfPresent(WmsStockOwnershipDO::getAvailableQuantity, reqVO.getAvailableQuantity())
				.eqIfPresent(WmsStockOwnershipDO::getOutboundPendingQuantity, reqVO.getOutboundPendingQuantity())
				.betweenIfPresent(WmsStockOwnershipDO::getCreateTime, reqVO.getCreateTime())
				.orderByDesc(WmsStockOwnershipDO::getId));
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
}