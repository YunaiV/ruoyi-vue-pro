package cn.iocoder.yudao.module.wms.dal.mysql.inbound.item;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemOwnershipDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemQueryDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.pickup.item.WmsPickupItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

import static cn.iocoder.yudao.module.wms.dal.mysql.inbound.item.WmsInboundItemQueryMapper.AGE_EXPR;

/**
 * 入库单详情 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsInboundItemOwnershipQueryMapper extends BaseMapperX<WmsInboundItemOwnershipDO> {



    default PageResult<WmsInboundItemOwnershipDO> selectInboundItemOwnershipList(Long productId) {


        MPJLambdaWrapperX<WmsInboundItemOwnershipDO> wrapper = new MPJLambdaWrapperX();
        //
        wrapper.selectAll(WmsInboundItemDO.class);
        wrapper.select(WmsInboundDO::getWarehouseId);
        wrapper.select(WmsPickupItemDO::getBinId);
        wrapper.select(AGE_EXPR+" as age");

        //
        wrapper.innerJoin(WmsInboundDO.class,WmsInboundDO::getId, WmsInboundItemQueryDO::getInboundId);



        PageResult<WmsInboundItemOwnershipDO> pageResult =selectPage(new PageParam(),wrapper);

        List<WmsInboundItemOwnershipDO> list= selectList(wrapper);

        return selectPage(new PageParam(), wrapper);



    }

}
