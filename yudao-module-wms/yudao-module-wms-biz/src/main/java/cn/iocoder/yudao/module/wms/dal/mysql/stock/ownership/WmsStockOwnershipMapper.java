package cn.iocoder.yudao.module.wms.dal.mysql.stock.ownership;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership.WmsStockOwnershipDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.vo.*;

/**
 * 所有者库存 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsStockOwnershipMapper extends BaseMapperX<WmsStockOwnershipDO> {

    default PageResult<WmsStockOwnershipDO> selectPage(WmsStockOwnershipPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsStockOwnershipDO>()
                .eqIfPresent(WmsStockOwnershipDO::getWarehouseId, reqVO.getWarehouseId())
                .eqIfPresent(WmsStockOwnershipDO::getProductId, reqVO.getProductId())
                .eqIfPresent(WmsStockOwnershipDO::getProductSku, reqVO.getProductSku())
                .eqIfPresent(WmsStockOwnershipDO::getInventorySubjectId, reqVO.getInventorySubjectId())
                .eqIfPresent(WmsStockOwnershipDO::getInventoryOwnerId, reqVO.getInventoryOwnerId())
                .eqIfPresent(WmsStockOwnershipDO::getAvailableQuantity, reqVO.getAvailableQuantity())
                .eqIfPresent(WmsStockOwnershipDO::getPendingOutboundQuantity, reqVO.getPendingOutboundQuantity())
                .betweenIfPresent(WmsStockOwnershipDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(WmsStockOwnershipDO::getId));
    }

}