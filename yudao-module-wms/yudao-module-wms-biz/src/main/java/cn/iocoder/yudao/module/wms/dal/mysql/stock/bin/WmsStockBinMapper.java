package cn.iocoder.yudao.module.wms.dal.mysql.stock.bin;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.WmsStockBinPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.WmsWarehouseProductVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.WmsStockBinDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 仓位库存 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsStockBinMapper extends BaseMapperX<WmsStockBinDO> {

    default PageResult<WmsStockBinDO> selectPage(WmsStockBinPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsStockBinDO>()
				.eqIfPresent(WmsStockBinDO::getWarehouseId, reqVO.getWarehouseId())
				.eqIfPresent(WmsStockBinDO::getBinId, reqVO.getBinId())
				.eqIfPresent(WmsStockBinDO::getProductId, reqVO.getProductId())
				.eqIfPresent(WmsStockBinDO::getAvailableQty, reqVO.getAvailableQty())
				.eqIfPresent(WmsStockBinDO::getSellableQty, reqVO.getSellableQty())
				.eqIfPresent(WmsStockBinDO::getOutboundPendingQty, reqVO.getOutboundPendingQty())
				.betweenIfPresent(WmsStockBinDO::getCreateTime, reqVO.getCreateTime())
				.orderByDesc(WmsStockBinDO::getId));
    }

    /**
     * 按 warehouse_id 查询 WmsStockBinDO 清单
     */
    default List<WmsStockBinDO> selectByWarehouseId(Long warehouseId) {
        return selectList(new LambdaQueryWrapperX<WmsStockBinDO>().eq(WmsStockBinDO::getWarehouseId, warehouseId));
    }

    /**
     * 按 bin_id,product_id 查询唯一的 WmsStockBinDO
     */
    default WmsStockBinDO getByBinIdAndProductId(Long binId, Long productId) {
        LambdaQueryWrapperX<WmsStockBinDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsStockBinDO::getBinId, binId);
        wrapper.eq(WmsStockBinDO::getProductId, productId);
        return selectOne(wrapper);
    }

    default List<WmsStockBinDO> selectStockBin(Long warehouseId,Long binId, Long productId) {
        LambdaQueryWrapperX<WmsStockBinDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eqIfPresent(WmsStockBinDO::getWarehouseId, warehouseId);
        wrapper.eqIfPresent(WmsStockBinDO::getProductId, productId);
        wrapper.eqIfPresent(WmsStockBinDO::getBinId, binId);
        return selectList(wrapper);
    }

    default List<WmsStockBinDO> selectStockBinListInBin(Collection<Long> binIds, Collection<Long> productIds) {
        if (CollectionUtils.isEmpty(binIds) && CollectionUtils.isEmpty(productIds)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapperX<WmsStockBinDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.in(WmsStockBinDO::getBinId, binIds);
        wrapper.in(WmsStockBinDO::getProductId, productIds);
        return selectList(wrapper);
    }

    default List<WmsStockBinDO> selectStockBinListInWarehouse(List<WmsWarehouseProductVO> warehouseProductList) {

        LambdaQueryWrapperX<WmsStockBinDO> wrapper = new LambdaQueryWrapperX<>();
        List<Object> params = new ArrayList<>();
        List<String> units = new ArrayList<>();
        int index=0;
        for (WmsWarehouseProductVO vo : warehouseProductList) {
            params.add(vo.getWarehouseId());
            params.add(vo.getProductId());
            units.add("({"+index+"},{"+(index+1)+"})");
            index+=2;
        }
        wrapper.apply("(warehouse_id, product_id) IN ("+ StrUtils.join(units,",")+")",params.toArray());
        return selectList(wrapper);


    }
}
