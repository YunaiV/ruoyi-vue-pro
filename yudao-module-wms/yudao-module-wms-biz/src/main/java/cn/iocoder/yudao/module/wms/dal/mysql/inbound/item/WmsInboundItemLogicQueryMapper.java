package cn.iocoder.yudao.module.wms.dal.mysql.inbound.item;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemLogicDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemQueryDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.WmsStockBinDO;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundStatus;
import org.apache.ibatis.annotations.Mapper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.module.wms.dal.mysql.inbound.item.WmsInboundItemQueryMapper.AGE_COL_EXPR;

/**
 * 入库单详情 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsInboundItemLogicQueryMapper extends BaseMapperX<WmsInboundItemLogicDO> {


    //    select
    //    t.product_id,t.company_id inbound_company_id,t.dept_id inbound_dept_id,
    //    t1.warehouse_id,  t1.company_id item_company_id,t1.dept_id item_dept_id,(DATEDIFF(NOW(),t1.inbound_time)+IFNULL(t1.init_age,0)) age,inbound_time from wms_inbound_item t
    //    join  wms_inbound t1 on t1.id=t.inbound_id
    //    and t.inbound_status in (1,2) and t1.inbound_status in (1,2) and product_id=189
    //    order by  t1.inbound_time asc

    /**
     * 按入库顺序获得第一个入库批次
     * @param warehouseId
     * @param productId
     * @param olderFirst 是否按入库时间升序
     **/
    default WmsInboundItemLogicDO getInboundItemLogic(Long warehouseId, Long productId, boolean olderFirst) {
        Map<Long, List<WmsInboundItemLogicDO>> map = selectInboundItemLogicGroupedMap(warehouseId, Arrays.asList(productId), olderFirst);
        if (map.isEmpty()) {
            return null;
        } else {
            if(CollectionUtils.isEmpty(map.get(productId))) {
                return null;
            } else {
                return map.get(productId).get(0);
            }
        }
    }

    /**
     * 按入库顺序获得第一个入库批次
     * @param warehouseId
     * @param productIds
     * @param olderFirst 是否按入库时间升序
     **/
    default Map<Long, WmsInboundItemLogicDO> selectInboundItemLogicMap(Long warehouseId, List<Long> productIds, boolean olderFirst) {
        Map<Long, List<WmsInboundItemLogicDO>> groupedMap = selectInboundItemLogicGroupedMap(warehouseId, productIds, olderFirst);
        Map<Long, WmsInboundItemLogicDO> map = new HashMap<>();
        for (Map.Entry<Long, List<WmsInboundItemLogicDO>> entry : groupedMap.entrySet()) {
            if(CollectionUtils.isEmpty(entry.getValue())) {
                continue;
            } else {
                map.put(entry.getKey(), entry.getValue().get(0));
            }
        }
        return map;
    }

    /**
     * 按入库顺序获得第一个入库批次
     * @param warehouseId
     * @param productIds
     * @param olderFirst 是否按入库时间升序
     **/
    default Map<Long, List<WmsInboundItemLogicDO>> selectInboundItemLogicGroupedMap(Long warehouseId, List<Long> productIds, boolean olderFirst) {

        // 主表
        MPJLambdaWrapperX<WmsInboundItemLogicDO> wrapper = new MPJLambdaWrapperX();
        // 主表条件
        wrapper.in(WmsInboundItemQueryDO::getProductId, productIds)
            .in(WmsInboundItemQueryDO::getInboundStatus, WmsInboundStatus.ALL.getValue(),WmsInboundStatus.PART.getValue())
        ;
        // 查询主表字段
        wrapper.select(WmsInboundItemDO::getProductId);
        wrapper.select(WmsInboundItemDO::getCompanyId);
        wrapper.select(WmsInboundItemDO::getDeptId);
        // 查询子表字段
        wrapper.innerJoin(WmsInboundDO.class,WmsInboundDO::getId, WmsInboundItemQueryDO::getInboundId).
            select(WmsInboundDO::getId).
                select(WmsInboundDO::getWarehouseId).
                select(WmsInboundDO::getInboundTime).
                select(AGE_COL_EXPR).
                eq(WmsInboundDO::getWarehouseId, warehouseId);

        // 控制顺序
        if(olderFirst) {
            wrapper.orderByAsc(WmsInboundDO::getInboundTime);
        } else {
            wrapper.orderByDesc(WmsInboundDO::getInboundTime);
        }

        List<WmsInboundItemLogicDO> list = selectList(wrapper);

        return StreamX.from(list).groupBy(WmsInboundItemLogicDO::getProductId);
    }

    /**
     * 获得入库批次列表
     *
     * @param warehouseId 仓库编号
     * @param productId   产品编号
     * @param olderFirst  是否按入库时间升序
     * @return 入库批次列表
     */
    default List<WmsInboundItemLogicDO> getInboundItemLogicList(Long warehouseId, Long productId, Long deptId, boolean olderFirst) {
        // 主表
        MPJLambdaWrapperX<WmsInboundItemLogicDO> wrapper = new MPJLambdaWrapperX();
        // 主表条件
        wrapper.eq(WmsInboundItemQueryDO::getProductId, productId)
            .in(WmsInboundItemQueryDO::getInboundStatus, WmsInboundStatus.ALL.getValue(), WmsInboundStatus.PART.getValue())
            .eqIfExists(WmsInboundItemQueryDO::getDeptId, deptId)
        ;
        // 查询主表字段
        wrapper.select(WmsInboundItemDO::getProductId);
        wrapper.select(WmsInboundItemDO::getCompanyId);
        wrapper.select(WmsInboundItemDO::getDeptId);
        // 查询子表字段
        wrapper.innerJoin(WmsInboundDO.class, WmsInboundDO::getId, WmsInboundItemQueryDO::getInboundId).
            select(WmsInboundDO::getId).
            select(WmsInboundDO::getWarehouseId).
            select(WmsInboundDO::getInboundTime).
            select(AGE_COL_EXPR).
            eq(WmsInboundDO::getWarehouseId, warehouseId);

        wrapper.innerJoin(WmsStockBinDO.class, WmsStockBinDO::getWarehouseId, WmsInboundDO::getWarehouseId).
            eq(WmsStockBinDO::getProductId, productId).
            gt(WmsStockBinDO::getSellableQty, 0).
            select(WmsStockBinDO::getBinId).
            select(WmsStockBinDO::getSellableQty);

        // 控制顺序
        if (olderFirst) {
            wrapper.orderByAsc(WmsInboundDO::getInboundTime);
        } else {
            wrapper.orderByDesc(WmsInboundDO::getInboundTime);
        }

        return selectList(wrapper);
    }
}
