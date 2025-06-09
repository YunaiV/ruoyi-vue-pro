package cn.iocoder.yudao.module.wms.dal.mysql.stock.bin;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.WmsStockBinPageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.product.WmsProductDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.WmsStockBinDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse.WmsStockWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.bin.WmsWarehouseBinDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 仓位库存 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsStockBinProductMapper extends BaseMapperX<WmsProductDO> {

    default PageResult<WmsProductDO> getGroupedStockBinPage(WmsStockBinPageReqVO reqVO) {

        MPJLambdaWrapperX<WmsProductDO> wrapper = new MPJLambdaWrapperX();

        wrapper.distinct();
        wrapper.selectAll(WmsProductDO.class);

        wrapper.likeIfExists(WmsProductDO::getCode, reqVO.getProductCode())
            .eqIfExists(WmsProductDO::getDeptId, reqVO.getProductDeptId());

        // 连接仓位视图
        wrapper.innerJoin(WmsStockBinDO.class, WmsStockBinDO::getProductId,WmsProductDO::getId)
            // 按仓库ID
            .eqIfExists(WmsStockBinDO::getWarehouseId, reqVO.getWarehouseId())
            // 按产品ID
            .eqIfExists(WmsStockBinDO::getProductId, reqVO.getProductId())
            // 按仓位ID
            .eqIfExists(WmsStockBinDO::getBinId, reqVO.getBinId())
            // 按可用数量
            .between(WmsStockBinDO::getAvailableQty, getMin(reqVO.getAvailableQty()),getMax(reqVO.getAvailableQty()))
            // 按待出库数量
            .between(WmsStockBinDO::getOutboundPendingQty, getMin(reqVO.getOutboundPendingQty()),getMax(reqVO.getOutboundPendingQty()))
            // 按可售数量
            .between(WmsStockBinDO::getSellableQty, getMin(reqVO.getSellableQty()),getMax(reqVO.getSellableQty()))
        ;


        // 连接仓库库存视图
         wrapper.leftJoin(WmsStockWarehouseDO.class, WmsStockWarehouseDO::getWarehouseId, WmsStockBinDO::getWarehouseId)
         .between(WmsStockWarehouseDO::getAvailableQty,getMin(reqVO.getWarehouseAvailableQty()),getMax(reqVO.getWarehouseAvailableQty()))
            .between(WmsStockWarehouseDO::getDefectiveQty,getMin(reqVO.getWarehouseDefectiveQty()),getMax(reqVO.getWarehouseDefectiveQty()))
            .between(WmsStockWarehouseDO::getOutboundPendingQty,getMin(reqVO.getWarehouseOutboundPendingQty()),getMax(reqVO.getWarehouseOutboundPendingQty()))
            .between(WmsStockWarehouseDO::getMakePendingQty,getMin(reqVO.getWarehouseMakePendingQty()),getMax(reqVO.getWarehouseMakePendingQty()))
            .between(WmsStockWarehouseDO::getTransitQty,getMin(reqVO.getWarehouseTransitQty()),getMax(reqVO.getWarehouseTransitQty()))
            .between(WmsStockWarehouseDO::getReturnTransitQty,getMin(reqVO.getWarehouseReturnTransitQty()),getMax(reqVO.getWarehouseReturnTransitQty()))
            .between(WmsStockWarehouseDO::getSellableQty,getMin(reqVO.getWarehouseSellableQty()),getMax(reqVO.getWarehouseSellableQty()))
            .between(WmsStockWarehouseDO::getShelvingPendingQty,getMin(reqVO.getWarehouseShelvingPendingQty()),getMax(reqVO.getWarehouseShelvingPendingQty()));

        if(reqVO.getZoneId()!=null) {
            wrapper.innerJoin(WmsWarehouseBinDO.class, WmsWarehouseBinDO::getId, WmsStockBinDO::getBinId)
                .eqIfExists(WmsWarehouseBinDO::getZoneId, reqVO.getZoneId());
        }


        return selectPage(reqVO, wrapper);

    }



    default Integer getMin(Integer[] range) {
        Integer min=ArrayUtils.get(range,0);
        if(min==null) {
            min=0;
        }
        return min;
    }

    default Integer getMax(Integer[] range) {
        Integer max=ArrayUtils.get(range,1);
        if(max==null) {
            max=Integer.MAX_VALUE;
        }
        return max;
    }


}
