package cn.iocoder.yudao.module.wms.dal.mysql.inbound.item;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemPageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemDO;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundStatus;
import org.apache.ibatis.annotations.Mapper;

import java.util.Arrays;
import java.util.List;

/**
 * 入库单详情 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsInboundItemMapper extends BaseMapperX<WmsInboundItemDO> {

    /**
     * 按 inbound_id,product_id 查询唯一的 WmsInboundItemDO
     */
    default WmsInboundItemDO getByInboundIdAndProductId(Long inboundId, Long productId) {
        LambdaQueryWrapperX<WmsInboundItemDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsInboundItemDO::getInboundId, inboundId);
        wrapper.eq(WmsInboundItemDO::getProductId, productId);
        return selectOne(wrapper);
    }

    /**
     * 按 inboundId 查询 WmsInboundItemDO
     */
    default List<WmsInboundItemDO> selectByInboundId(Long inboundId, int limit) {
        WmsInboundItemPageReqVO reqVO = new WmsInboundItemPageReqVO();
        reqVO.setPageSize(limit);
        reqVO.setPageNo(1);
        LambdaQueryWrapperX<WmsInboundItemDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsInboundItemDO::getInboundId, inboundId);
        return selectPage(reqVO, wrapper).getList();
    }

    default List<WmsInboundItemDO> selectItemListHasAvailableQty(Long warehouseId, Long productId) {
        MPJLambdaWrapperX<WmsInboundItemDO> query = new MPJLambdaWrapperX<>();
//        query.select(WmsInboundItemDO::getId, WmsInboundItemDO::getInboundId, WmsInboundItemDO::getProductId);
        query.selectAll(WmsInboundItemDO.class);
        query.innerJoin(WmsInboundDO.class, WmsInboundDO::getId, WmsInboundItemDO::getInboundId);
        query.in(WmsInboundDO::getInboundStatus, Arrays.asList(WmsInboundStatus.ALL.getValue(), WmsInboundStatus.PART.getValue()));
        query.eq(WmsInboundDO::getWarehouseId, warehouseId).eq(WmsInboundItemDO::getProductId, productId).last("ORDER BY DATEDIFF(t1.inbound_time, now())+t1.init_age desc");
        query.selectAll(WmsInboundItemDO.class);
        return selectList(query);
    }

    // default PageResult<WmsInboundItemLogicDO> selectInboundItemLogicList(Long productId) {
    // 
    // 
    // MPJLambdaWrapperX<WmsInboundItemLogicDO> wrapper = new MPJLambdaWrapperX();
    // //
    // wrapper.selectAll(WmsInboundItemDO.class);
    // wrapper.select(WmsInboundDO::getWarehouseId);
    // wrapper.select(WmsPickupItemDO::getBinId);
    // wrapper.select(AGE_EXPR+" as age");
    // 
    // //
    // wrapper.innerJoin(WmsInboundDO.class,WmsInboundDO::getId, WmsInboundItemQueryDO::getInboundId);
    // 
    // 
    // 
    // PageResult<WmsInboundItemLogicDO> pageResult =selectPage(new PageParam(),wrapper);
    // 
    // List<WmsInboundItemLogicDO> list= selectList(wrapper);
    // 
    // return selectPage(new PageParam(), wrapper);
    // 
    // 
    // 
    // }
    /**
     * 按 inbound_id,product_id 查询 WmsInboundItemDO 清单
     */
    default List<WmsInboundItemDO> selectByInboundIdAndProductId(Long inboundId, Long productId) {
        return selectList(new LambdaQueryWrapperX<WmsInboundItemDO>().eq(WmsInboundItemDO::getInboundId, inboundId).eq(WmsInboundItemDO::getProductId, productId));
    }
}
