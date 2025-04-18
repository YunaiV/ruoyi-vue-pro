package cn.iocoder.yudao.module.wms.dal.mysql.inbound.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsPickupPendingPageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemQueryDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.pickup.item.WmsPickupItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.product.WmsProductDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse.WmsStockWarehouseDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 入库单详情 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsInboundItemQueryMapper extends BaseMapperX<WmsInboundItemQueryDO> {

    String AGE_EXPR = "(DATEDIFF(NOW(),t1.inbound_time)+IFNULL(t1.init_age,0))";

    default PageResult<WmsInboundItemQueryDO> selectPage(WmsInboundItemPageReqVO reqVO) {

        MPJLambdaWrapperX<WmsInboundItemQueryDO> wrapper = new MPJLambdaWrapperX();
        //
        wrapper.selectAll(WmsInboundItemDO.class);
        wrapper.select(WmsInboundDO::getWarehouseId);
        wrapper.select(WmsPickupItemDO::getBinId);
        wrapper.select(AGE_EXPR+" as age");

        //
        wrapper.innerJoin(WmsInboundDO.class,WmsInboundDO::getId, WmsInboundItemQueryDO::getInboundId)
            .likeIfExists(WmsInboundDO::getCode, reqVO.getInboundNo())
            .eqIfExists(WmsInboundDO::getWarehouseId, reqVO.getWarehouseId())
            .eqIfExists(WmsInboundDO::getDeptId, reqVO.getDeptId())
            .eqIfExists(WmsInboundDO::getCompanyId, reqVO.getCompanyId());

        wrapper.leftJoin(WmsPickupItemDO.class, WmsPickupItemDO::getInboundItemId, WmsInboundItemQueryDO::getId)
            .eqIfExists(WmsPickupItemDO::getBinId, reqVO.getBinId());

        // 连接产品视图
        if(reqVO.getProductCode()!=null) {
            wrapper.innerJoin(WmsProductDO.class, WmsProductDO::getId, WmsStockWarehouseDO::getProductId)
                .likeIfExists(WmsProductDO::getBarCode, reqVO.getProductCode());
        }
        wrapper.eqIfPresent(WmsInboundItemDO::getInboundId, reqVO.getInboundId());
        wrapper.eqIfPresent(WmsInboundItemDO::getInboundStatus, reqVO.getInboundStatus());
        wrapper.eqIfPresent(WmsInboundItemDO::getProductId, reqVO.getProductId());

        wrapper.betweenIfPresent(WmsInboundItemDO::getCreateTime, reqVO.getCreateTime());

        // 范围查询
        wrapper.betweenIfPresent(WmsInboundItemDO::getActualQty,reqVO.getActualQty());
        wrapper.betweenIfPresent(WmsInboundItemDO::getOutboundAvailableQty,reqVO.getOutboundAvailableQty());
        wrapper.betweenIfPresent(WmsInboundItemDO::getPlanQty,reqVO.getPlanQty());
        wrapper.betweenIfPresent(WmsInboundItemDO::getShelvedQty,reqVO.getShelvedQty());

        wrapper.betweenIfPresent(AGE_EXPR, reqVO.getAge());

        return selectPage(reqVO, wrapper);



    }

    default PageResult<WmsInboundItemQueryDO> getPickupPending(WmsPickupPendingPageReqVO reqVO) {
        MPJLambdaWrapperX<WmsInboundItemQueryDO> query = new MPJLambdaWrapperX<>();
        query.selectAll(WmsInboundItemDO.class).select(WmsInboundDO::getWarehouseId);
        query.gt(WmsInboundItemDO::getActualQty, WmsInboundItemDO::getShelvedQty).innerJoin(WmsInboundDO.class, WmsInboundDO::getId, WmsInboundItemDO::getInboundId).likeIfExists(WmsInboundDO::getCode, reqVO.getInboundNo())
            .orderByDesc(WmsInboundItemDO::getId);
        return selectPage(reqVO, query);
    }

}
