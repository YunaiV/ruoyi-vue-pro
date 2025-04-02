package cn.iocoder.yudao.module.wms.dal.mysql.stock.warehouse;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.WmsStockWarehousePageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.product.WmsProductDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse.WmsStockWarehouseDO;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.apache.ibatis.annotations.Mapper;

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
        wrapper.innerJoin(WmsProductDO.class, WmsProductDO::getId, WmsStockWarehouseDO::getProductId)
            .likeIfExists(WmsProductDO::getBarCode, reqVO.getProductCode())
            .eqIfExists(WmsProductDO::getDeptId, reqVO.getProductDeptId());
        // 按仓库
        wrapper.eqIfPresent(WmsStockWarehouseDO::getWarehouseId, reqVO.getWarehouseId())
            // 按产品ID
            .eqIfPresent(WmsStockWarehouseDO::getProductId, reqVO.getProductId());

        betweenIf(wrapper,WmsStockWarehouseDO::getAvailableQty,reqVO.getAvailableQty());
        betweenIf(wrapper,WmsStockWarehouseDO::getDefectiveQty,reqVO.getDefectiveQty());
        betweenIf(wrapper,WmsStockWarehouseDO::getOutboundPendingQty,reqVO.getOutboundPendingQty());
        betweenIf(wrapper,WmsStockWarehouseDO::getPurchasePlanQty,reqVO.getPurchasePlanQty());
        betweenIf(wrapper,WmsStockWarehouseDO::getPurchaseTransitQty,reqVO.getPurchaseTransitQty());
        betweenIf(wrapper,WmsStockWarehouseDO::getReturnTransitQty,reqVO.getReturnTransitQty());
        betweenIf(wrapper,WmsStockWarehouseDO::getSellableQty,reqVO.getSellableQty());
        betweenIf(wrapper,WmsStockWarehouseDO::getShelvingPendingQty,reqVO.getShelvingPendingQty());

        return selectPage(reqVO, wrapper);

    }

    private static void betweenIf(MPJLambdaWrapperX<WmsStockWarehouseDO> wrapper, SFunction<WmsStockWarehouseDO, ?> column,Integer[] range) {
        if(range!=null && range.length>0) {
            Integer[] array = new Integer[2];
            if(range.length==1) {
                array[0]= range[0];
                array[1]=Integer.MAX_VALUE;
            }
            if(range.length>=2) {
                array[0]= range[0];
                array[1]= range[1];
            }
            if(array[0]==null) {
                array[0]=Integer.MIN_VALUE;
            }
            if(array[1]==null) {
                array[1]=Integer.MAX_VALUE;
            }
            wrapper.betweenIfPresent(column, array[0], array[1]);
        }
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
