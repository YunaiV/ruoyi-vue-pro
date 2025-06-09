package cn.iocoder.yudao.module.wms.dal.mysql.stock.warehouse;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.WmsStockWarehousePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.WmsWarehouseProductVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.product.WmsProductDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse.WmsStockWarehouseDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 仓库库存 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsStockWarehouseMapper extends BaseMapperX<WmsStockWarehouseDO> {

    default PageResult<WmsStockWarehouseDO> selectPage(WmsStockWarehousePageReqVO reqVO) {
        MPJLambdaWrapperX<WmsStockWarehouseDO> wrapper = new MPJLambdaWrapperX();
        // 连接产品视图
        wrapper.innerJoin(WmsProductDO.class, WmsProductDO::getId, WmsStockWarehouseDO::getProductId).likeIfExists(WmsProductDO::getCode, reqVO.getProductCode()).eqIfExists(WmsProductDO::getDeptId, reqVO.getProductDeptId());
        // 按仓库
        // 按产品ID
        wrapper.eqIfPresent(WmsStockWarehouseDO::getWarehouseId, reqVO.getWarehouseId())
				.eqIfPresent(WmsStockWarehouseDO::getProductId, reqVO.getProductId());
        wrapper.betweenIfPresent(WmsStockWarehouseDO::getAvailableQty, reqVO.getAvailableQty());
        wrapper.betweenIfPresent(WmsStockWarehouseDO::getDefectiveQty, reqVO.getDefectiveQty());
        wrapper.betweenIfPresent(WmsStockWarehouseDO::getOutboundPendingQty, reqVO.getOutboundPendingQty());
        wrapper.betweenIfPresent(WmsStockWarehouseDO::getMakePendingQty, reqVO.getMakePendingQty());
        wrapper.betweenIfPresent(WmsStockWarehouseDO::getTransitQty, reqVO.getTransitQty());
        wrapper.betweenIfPresent(WmsStockWarehouseDO::getReturnTransitQty, reqVO.getReturnTransitQty());
        wrapper.betweenIfPresent(WmsStockWarehouseDO::getSellableQty, reqVO.getSellableQty());
        wrapper.betweenIfPresent(WmsStockWarehouseDO::getShelvingPendingQty, reqVO.getShelvingPendingQty());
        return selectPage(reqVO, wrapper);
    }

    /**
     * 按 warehouse_id,product_id 查询唯一的 WmsStockWarehouseDO
     */
    default WmsStockWarehouseDO getByWarehouseIdAndProductId(Long warehouseId, String productId) {
        LambdaQueryWrapperX<WmsStockWarehouseDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsStockWarehouseDO::getWarehouseId, warehouseId);
        wrapper.eq(WmsStockWarehouseDO::getProductId, productId);
        return selectOne(wrapper);
    }

    /**
     * 按 warehouse_id,product_id 查询唯一的 WmsStockWarehouseDO
     */
    default WmsStockWarehouseDO getByWarehouseIdAndProductId(Long warehouseId, Long productId) {
        LambdaQueryWrapperX<WmsStockWarehouseDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsStockWarehouseDO::getWarehouseId, warehouseId);
        wrapper.eq(WmsStockWarehouseDO::getProductId, productId);
        return selectOne(wrapper);
    }

    /**
     * 按仓库查询库存
     */
    default List<WmsStockWarehouseDO> selectByWarehouse(Long warehouseId) {
        LambdaQueryWrapperX<WmsStockWarehouseDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsStockWarehouseDO::getWarehouseId, warehouseId);
        return selectList(wrapper);
    }

    default List<WmsStockWarehouseDO> getByProductIds(Long warehouseId, List<Long> productIds) {
        LambdaQueryWrapperX<WmsStockWarehouseDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eqIfPresent(WmsStockWarehouseDO::getWarehouseId, warehouseId);
        wrapper.in(WmsStockWarehouseDO::getProductId, productIds);
        return selectList(wrapper);
    }

    default List<WmsStockWarehouseDO> selectStockWarehouse(List<WmsWarehouseProductVO> warehouseProductVOList) {
        LambdaQueryWrapperX<WmsStockWarehouseDO> wrapper = new LambdaQueryWrapperX<>();
        List<Object> params = new ArrayList<>();
        List<String> units = new ArrayList<>();
        int index = 0;
        for (WmsWarehouseProductVO vo : warehouseProductVOList) {
            params.add(vo.getWarehouseId());
            params.add(vo.getProductId());
            units.add("({" + index + "},{" + (index + 1) + "})");
            index += 2;
        }
        wrapper.apply("(warehouse_id, product_id) IN (" + StrUtils.join(units, ",") + ")", params.toArray());
        return selectList(wrapper);
    }

    default List<WmsStockWarehouseDO> selectByProductIds(Set<Long> productIds) {
        LambdaQueryWrapperX<WmsStockWarehouseDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.in(WmsStockWarehouseDO::getProductId, productIds);
        return selectList(wrapper);
    }

}
