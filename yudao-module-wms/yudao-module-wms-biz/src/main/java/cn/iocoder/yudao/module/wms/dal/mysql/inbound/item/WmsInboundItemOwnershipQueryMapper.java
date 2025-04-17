package cn.iocoder.yudao.module.wms.dal.mysql.inbound.item;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemOwnershipDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemQueryDO;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundStatus;
import org.apache.ibatis.annotations.Mapper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.module.wms.dal.mysql.inbound.item.WmsInboundItemQueryMapper.AGE_EXPR;

/**
 * 入库单详情 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsInboundItemOwnershipQueryMapper extends BaseMapperX<WmsInboundItemOwnershipDO> {


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
    default WmsInboundItemOwnershipDO getInboundItemOwnership(Long warehouseId, Long productId,boolean olderFirst) {
        Map<Long, List<WmsInboundItemOwnershipDO>> map = selectInboundItemOwnershipGroupedMap(warehouseId, Arrays.asList(productId), olderFirst);
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
    default Map<Long,WmsInboundItemOwnershipDO> selectInboundItemOwnershipMap(Long warehouseId, List<Long> productIds, boolean olderFirst) {
        Map<Long, List<WmsInboundItemOwnershipDO>> groupedMap = selectInboundItemOwnershipGroupedMap(warehouseId, productIds, olderFirst);
        Map<Long,WmsInboundItemOwnershipDO> map = new HashMap<>();
        for (Map.Entry<Long, List<WmsInboundItemOwnershipDO>> entry : groupedMap.entrySet()) {
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
    default Map<Long, List<WmsInboundItemOwnershipDO>> selectInboundItemOwnershipGroupedMap(Long warehouseId, List<Long> productIds, boolean olderFirst) {

        // 主表
        MPJLambdaWrapperX<WmsInboundItemOwnershipDO> wrapper = new MPJLambdaWrapperX();
        // 主表条件
        wrapper.in(WmsInboundItemQueryDO::getProductId, productIds)
            .in(WmsInboundItemQueryDO::getInboundStatus, WmsInboundStatus.ALL.getValue(),WmsInboundStatus.PART.getValue())
        ;
        // 查询主表字段
        wrapper.select(WmsInboundItemDO::getProductId);
        wrapper.selectAs(WmsInboundItemDO::getCompanyId,WmsInboundItemOwnershipDO::getInboundCompanyId);
        wrapper.selectAs(WmsInboundItemDO::getDeptId,WmsInboundItemOwnershipDO::getInboundDeptId);
        // 查询子表字段
        wrapper.select(WmsInboundDO::getWarehouseId);
        wrapper.selectAs(WmsInboundDO::getCompanyId,WmsInboundItemOwnershipDO::getItemCompanyId);
        wrapper.selectAs(WmsInboundDO::getDeptId,WmsInboundItemOwnershipDO::getItemDeptId);
        wrapper.select(WmsInboundDO::getInboundTime);
        wrapper.select(AGE_EXPR+" as age");


        //
        wrapper.innerJoin(WmsInboundDO.class,WmsInboundDO::getId, WmsInboundItemQueryDO::getInboundId);
        wrapper.eq(WmsInboundDO::getWarehouseId, warehouseId)
            .in(WmsInboundItemQueryDO::getInboundStatus, WmsInboundStatus.ALL.getValue(),WmsInboundStatus.PART.getValue());
        // 控制顺序
        if(olderFirst) {
            wrapper.orderByAsc(WmsInboundDO::getInboundTime);
        } else {
            wrapper.orderByDesc(WmsInboundDO::getInboundTime);
        }


        List<WmsInboundItemOwnershipDO> list = selectList(wrapper);


        for (WmsInboundItemOwnershipDO ownershipDO : list) {
            // 优先使用详情表的公司字段字段
            ownershipDO.setCompanyId(ownershipDO.getItemCompanyId());
            if (ownershipDO.getCompanyId() == null) {
                ownershipDO.setCompanyId(ownershipDO.getInboundCompanyId());
            }
            // 优先使用详情表的部门字段字段
            ownershipDO.setDeptId(ownershipDO.getItemDeptId());
            if (ownershipDO.getDeptId() == null) {
                ownershipDO.setDeptId(ownershipDO.getInboundDeptId());
            }
        }

        return StreamX.from(list).groupBy(WmsInboundItemOwnershipDO::getProductId);
    }

}
