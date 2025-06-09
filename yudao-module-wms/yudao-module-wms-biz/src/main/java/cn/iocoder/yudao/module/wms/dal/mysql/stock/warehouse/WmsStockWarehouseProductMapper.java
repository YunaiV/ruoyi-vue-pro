package cn.iocoder.yudao.module.wms.dal.mysql.stock.warehouse;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.WmsStockWarehousePageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.product.WmsProductDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse.WmsStockWarehouseDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 仓库库存 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsStockWarehouseProductMapper extends BaseMapperX<WmsProductDO> {


    default PageResult<WmsProductDO> getStockGroupedWarehousePage(WmsStockWarehousePageReqVO reqVO) {


        MPJLambdaWrapperX<WmsProductDO> wrapper = new MPJLambdaWrapperX();
        wrapper.distinct();
        wrapper.likeIfExists(WmsProductDO::getCode, reqVO.getProductCode())
            .eqIfExists(WmsProductDO::getDeptId, reqVO.getProductDeptId());

        // 连接产品视图
        wrapper.innerJoin(WmsStockWarehouseDO.class, WmsStockWarehouseDO::getProductId,WmsProductDO::getId)
        // 按仓库ID
        .eqIfExists(WmsStockWarehouseDO::getWarehouseId, reqVO.getWarehouseId())
        // 按产品ID
        .eqIfExists(WmsStockWarehouseDO::getProductId, reqVO.getProductId())
        .between(WmsStockWarehouseDO::getAvailableQty,getMin(reqVO.getAvailableQty()),getMax(reqVO.getAvailableQty()))
        .between(WmsStockWarehouseDO::getDefectiveQty,getMin(reqVO.getDefectiveQty()),getMax(reqVO.getDefectiveQty()))
        .between(WmsStockWarehouseDO::getOutboundPendingQty,getMin(reqVO.getOutboundPendingQty()),getMax(reqVO.getOutboundPendingQty()))
        .between(WmsStockWarehouseDO::getMakePendingQty,getMin(reqVO.getMakePendingQty()),getMax(reqVO.getMakePendingQty()))
        .between(WmsStockWarehouseDO::getTransitQty,getMin(reqVO.getTransitQty()),getMax(reqVO.getTransitQty()))
        .between(WmsStockWarehouseDO::getReturnTransitQty,getMin(reqVO.getReturnTransitQty()),getMax(reqVO.getReturnTransitQty()))
        .between(WmsStockWarehouseDO::getSellableQty,getMin(reqVO.getSellableQty()),getMax(reqVO.getSellableQty()))
        .between(WmsStockWarehouseDO::getShelvingPendingQty,getMin(reqVO.getShelvingPendingQty()),getMax(reqVO.getShelvingPendingQty()));

        return selectPage(reqVO, wrapper);
    }

    default Integer getMin(Integer[] range) {
        Integer min= ArrayUtils.get(range,0);
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
