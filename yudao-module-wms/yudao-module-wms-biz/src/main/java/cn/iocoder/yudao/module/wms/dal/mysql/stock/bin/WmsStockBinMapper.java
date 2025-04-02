package cn.iocoder.yudao.module.wms.dal.mysql.stock.bin;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.WmsStockBinPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.WmsWarehouseProductVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.product.WmsProductDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.WmsStockBinDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse.WmsStockWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.bin.WmsWarehouseBinDO;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
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


        MPJLambdaWrapperX<WmsStockBinDO> wrapper = new MPJLambdaWrapperX();
        // 连接产品视图
        wrapper.innerJoin(WmsProductDO.class, WmsProductDO::getId, WmsStockWarehouseDO::getProductId)
            .likeIfExists(WmsProductDO::getBarCode, reqVO.getProductCode())
            .eqIfExists(WmsProductDO::getDeptId, reqVO.getProductDeptId());

        if(reqVO.getZoneId()!=null) {
            wrapper.innerJoin(WmsWarehouseBinDO.class, WmsWarehouseBinDO::getId, WmsStockBinDO::getBinId)
                .eqIfExists(WmsWarehouseBinDO::getZoneId, reqVO.getZoneId());
        }
        // 按仓库
        wrapper.eqIfPresent(WmsStockBinDO::getWarehouseId, reqVO.getWarehouseId())
            // 按产品ID
            .eqIfPresent(WmsStockBinDO::getProductId, reqVO.getProductId())
            .eqIfPresent(WmsStockBinDO::getBinId, reqVO.getBinId())
        ;


        betweenIf(wrapper,WmsStockBinDO::getAvailableQty,reqVO.getAvailableQty());
        betweenIf(wrapper,WmsStockBinDO::getOutboundPendingQty,reqVO.getOutboundPendingQty());
        betweenIf(wrapper,WmsStockBinDO::getSellableQty,reqVO.getSellableQty());

        return selectPage(reqVO, wrapper);


    }



    private static void betweenIf(MPJLambdaWrapperX<WmsStockBinDO> wrapper, SFunction<WmsStockBinDO, ?> column, Integer[] range) {
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
