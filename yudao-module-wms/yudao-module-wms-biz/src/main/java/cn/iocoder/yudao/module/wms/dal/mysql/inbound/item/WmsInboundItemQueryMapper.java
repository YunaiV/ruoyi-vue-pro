package cn.iocoder.yudao.module.wms.dal.mysql.inbound.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemListForTmsReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsPickupPendingPageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemQueryDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.pickup.item.WmsPickupItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.product.WmsProductDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse.WmsStockWarehouseDO;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundStatus;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 入库单详情 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsInboundItemQueryMapper extends BaseMapperX<WmsInboundItemQueryDO> {

    String AGE_EXPR = "(DATEDIFF(NOW(),t1.inbound_time)+IFNULL(t1.init_age,0))";
    String AGE_COL = "age";
    String AGE_COL_EXPR = AGE_EXPR+" as "+AGE_COL;

    default PageResult<WmsInboundItemQueryDO> selectPage(WmsInboundItemPageReqVO reqVO) {

        MPJLambdaWrapperX<WmsInboundItemQueryDO> wrapper = new MPJLambdaWrapperX();
        //
        wrapper.selectAll(WmsInboundItemDO.class);
        wrapper.select(WmsInboundDO::getWarehouseId);
//        wrapper.select(WmsPickupItemDO::getBinId);
        wrapper.select(AGE_COL_EXPR);

        //
        wrapper.distinct();
        wrapper.innerJoin(WmsInboundDO.class,WmsInboundDO::getId, WmsInboundItemQueryDO::getInboundId)
            .likeIfExists(WmsInboundDO::getCode, reqVO.getInboundNo())
            .eqIfExists(WmsInboundDO::getWarehouseId, reqVO.getWarehouseId())
            //.eqIfExists(WmsInboundDO::getDeptId, reqVO.getDeptId())
            //.eqIfExists(WmsInboundDO::getCompanyId, reqVO.getCompanyId())
         ;

        wrapper.leftJoin(WmsPickupItemDO.class, WmsPickupItemDO::getInboundItemId, WmsInboundItemQueryDO::getId)
            .eqIfExists(WmsPickupItemDO::getBinId, reqVO.getBinId());

        // 连接产品视图
        if(reqVO.getProductCode()!=null) {
            wrapper.innerJoin(WmsProductDO.class, WmsProductDO::getId, WmsStockWarehouseDO::getProductId)
                .likeIfExists(WmsProductDO::getCode, reqVO.getProductCode());
        }
        wrapper.eqIfPresent(WmsInboundItemDO::getInboundId, reqVO.getInboundId());
        wrapper.eqIfPresent(WmsInboundItemDO::getInboundStatus, reqVO.getInboundStatus());
        wrapper.eqIfPresent(WmsInboundItemDO::getProductId, reqVO.getProductId());

        wrapper.betweenIfPresent(WmsInboundItemDO::getCreateTime, reqVO.getCreateTime());

        // 范围查询
        wrapper.eqIfPresent(WmsInboundItemDO::getCompanyId, reqVO.getInboundCompanyId());
        wrapper.eqIfPresent(WmsInboundItemDO::getDeptId, reqVO.getInboundDeptId());
        wrapper.betweenIfPresent(WmsInboundItemDO::getActualQty,reqVO.getActualQty());
        wrapper.betweenIfPresent(WmsInboundItemDO::getOutboundAvailableQty,reqVO.getOutboundAvailableQty());
        wrapper.betweenIfPresent(WmsInboundItemDO::getPlanQty,reqVO.getPlanQty());
        wrapper.betweenIfPresent(WmsInboundItemDO::getShelveClosedQty, reqVO.getShelveClosedQty());

        wrapper.betweenIfPresent(AGE_COL, reqVO.getAge());

        return selectPage(reqVO, wrapper);



    }

    default List<WmsInboundItemQueryDO> selectListByCompany(Long companyId, List<Long> productIds){
        MPJLambdaWrapperX<WmsInboundItemQueryDO> wrapper = new MPJLambdaWrapperX<>();
        wrapper.in(WmsInboundItemDO::getProductId, productIds);
        wrapper.eq(WmsInboundItemDO::getCompanyId, companyId);
        wrapper.selectAll(WmsInboundItemDO.class);
        wrapper.select(WmsInboundDO::getWarehouseId);
        wrapper.leftJoin(WmsInboundDO.class,WmsInboundDO::getId, WmsInboundItemQueryDO::getInboundId);
        return selectList(wrapper);
    };

    default PageResult<WmsInboundItemQueryDO> getPickupPending(WmsPickupPendingPageReqVO reqVO) {
        MPJLambdaWrapperX<WmsInboundItemQueryDO> query = new MPJLambdaWrapperX<>();
        query.selectAll(WmsInboundItemDO.class).select(WmsInboundDO::getWarehouseId);
        query.eqIfPresent(WmsInboundItemDO::getProductId, reqVO.getProductId());
        // 已入库或部分入库
        query.in(WmsInboundItemDO::getInboundStatus, WmsInboundStatus.ALL.getValue(), WmsInboundStatus.PART.getValue());
        query.gt(WmsInboundItemDO::getActualQty, WmsInboundItemDO::getShelveClosedQty)
            .innerJoin(WmsInboundDO.class, WmsInboundDO::getId, WmsInboundItemDO::getInboundId).likeIfExists(WmsInboundDO::getCode, reqVO.getInboundCode())
            .orderByDesc(WmsInboundItemDO::getId);
        // 按仓库ID查询
        if(reqVO.getWarehouseId()!=null) {
            query.innerJoin(WmsInboundDO.class,WmsInboundDO::getId,WmsInboundItemQueryDO::getInboundId)
                .eq(WmsInboundDO::getWarehouseId,reqVO.getWarehouseId())
                .eqIfExists(WmsInboundDO::getInboundStatus, reqVO.getInboundStatus());
        }
        return selectPage(reqVO, query);
    }

    default List<WmsInboundItemQueryDO> getInboundItemListForTms(WmsInboundItemListForTmsReqVO listForTmsReqVO) {
        MPJLambdaWrapperX<WmsInboundItemQueryDO> wrapper1 = new MPJLambdaWrapperX<>();
        List<WmsInboundItemDO> itemList = listForTmsReqVO.getInboundDoList();
        wrapper1.eq(WmsInboundItemDO::getCompanyId, listForTmsReqVO.getCompanyId())
            .selectAll(WmsInboundItemDO.class)
            .select(WmsInboundDO::getWarehouseId)
            .and(wrapper -> {
                if (itemList != null && !itemList.isEmpty()) {
                    itemList.forEach(inboundItem -> {
                        wrapper.or(subWrapper -> {
                            subWrapper.eq(WmsInboundItemDO::getProductId, inboundItem.getProductId())
                                .eqIfExists(WmsInboundItemDO::getDeptId, inboundItem.getDeptId());
                        });
                    });
                }
            });
        wrapper1.leftJoin(WmsInboundDO.class, WmsInboundDO::getId, WmsInboundItemQueryDO::getInboundId)
            .eq(WmsInboundDO::getWarehouseId, listForTmsReqVO.getWarehouseId());
        return selectList(wrapper1);
    }
}
