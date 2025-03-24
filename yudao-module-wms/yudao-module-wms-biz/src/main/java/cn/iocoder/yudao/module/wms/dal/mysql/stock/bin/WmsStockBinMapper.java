package cn.iocoder.yudao.module.wms.dal.mysql.stock.bin;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.WmsStockBinDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.*;

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
				.eqIfPresent(WmsStockBinDO::getAvailableQuantity, reqVO.getAvailableQuantity())
				.eqIfPresent(WmsStockBinDO::getSellableQuantity, reqVO.getSellableQuantity())
				.eqIfPresent(WmsStockBinDO::getOutboundPendingQuantity, reqVO.getOutboundPendingQuantity())
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

    default List<WmsStockBinDO> selectStockBin(Long warehouseId, Long productId) {
        LambdaQueryWrapperX<WmsStockBinDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsStockBinDO::getWarehouseId, warehouseId);
        wrapper.eq(WmsStockBinDO::getProductId, productId);
        return selectList(wrapper);
    }

    default List<WmsStockBinDO> selectStockBinList(Collection<Long> binIds, Collection<Long> productIds) {
        if(CollectionUtils.isEmpty(binIds) && CollectionUtils.isEmpty(productIds)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapperX<WmsStockBinDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.in(WmsStockBinDO::getBinId, binIds);
        wrapper.in(WmsStockBinDO::getProductId, productIds);
        return selectList(wrapper);
    }
}
