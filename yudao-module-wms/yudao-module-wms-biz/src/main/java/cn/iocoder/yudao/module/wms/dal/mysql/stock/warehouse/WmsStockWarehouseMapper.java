package cn.iocoder.yudao.module.wms.dal.mysql.stock.warehouse;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.product.WmsProductDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse.WmsStockWarehouseDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.*;

/**
 * 仓库库存 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsStockWarehouseMapper extends BaseMapperX<WmsStockWarehouseDO> {

    default PageResult<WmsStockWarehouseDO> selectPage(WmsStockWarehousePageReqVO reqVO) {

        MPJLambdaWrapperX<WmsStockWarehouseDO> wrapper = new MPJLambdaWrapperX();
        wrapper.innerJoin(WmsProductDO.class,WmsProductDO::getId,WmsStockWarehouseDO::getProductId);
        // 仓库
        wrapper.eqIfPresent(WmsStockWarehouseDO::getWarehouseId, reqVO.getWarehouseId())
        // 产品
        .eqIfPresent(WmsStockWarehouseDO::getProductId, reqVO.getProductId())
            ;




        return selectPage(reqVO, new LambdaQueryWrapperX<WmsStockWarehouseDO>()



				.eqIfPresent(WmsStockWarehouseDO::getPurchasePlanQty, reqVO.getPurchasePlanQty())
				.eqIfPresent(WmsStockWarehouseDO::getPurchaseTransitQty, reqVO.getPurchaseTransitQty())
				.eqIfPresent(WmsStockWarehouseDO::getReturnTransitQty, reqVO.getReturnTransitQty())
				.eqIfPresent(WmsStockWarehouseDO::getAvailableQty, reqVO.getAvailableQty())
				.eqIfPresent(WmsStockWarehouseDO::getSellableQty, reqVO.getSellableQty())
				.eqIfPresent(WmsStockWarehouseDO::getDefectiveQty, reqVO.getDefectiveQty())
				.betweenIfPresent(WmsStockWarehouseDO::getCreateTime, reqVO.getCreateTime())
				.orderByDesc(WmsStockWarehouseDO::getId));
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
}
