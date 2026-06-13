package cn.iocoder.yudao.module.wms.dal.mysql.order.movement;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wms.controller.admin.order.movement.vo.order.WmsMovementOrderPageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.movement.WmsMovementOrderDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * WMS 移库单 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface WmsMovementOrderMapper extends BaseMapperX<WmsMovementOrderDO> {

    default PageResult<WmsMovementOrderDO> selectPage(WmsMovementOrderPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsMovementOrderDO>()
                .likeIfPresent(WmsMovementOrderDO::getNo, reqVO.getNo())
                .eqIfPresent(WmsMovementOrderDO::getStatus, reqVO.getStatus())
                .eqIfPresent(WmsMovementOrderDO::getSourceWarehouseId, reqVO.getSourceWarehouseId())
                .eqIfPresent(WmsMovementOrderDO::getTargetWarehouseId, reqVO.getTargetWarehouseId())
                .betweenIfPresent(WmsMovementOrderDO::getOrderTime, reqVO.getOrderTime())
                .geIfPresent(WmsMovementOrderDO::getTotalQuantity, reqVO.getTotalQuantityMin())
                .leIfPresent(WmsMovementOrderDO::getTotalQuantity, reqVO.getTotalQuantityMax())
                .geIfPresent(WmsMovementOrderDO::getTotalPrice, reqVO.getTotalPriceMin())
                .leIfPresent(WmsMovementOrderDO::getTotalPrice, reqVO.getTotalPriceMax())
                .eqIfPresent(WmsMovementOrderDO::getCreator, reqVO.getCreator())
                .eqIfPresent(WmsMovementOrderDO::getUpdater, reqVO.getUpdater())
                .betweenIfPresent(WmsMovementOrderDO::getCreateTime, reqVO.getCreateTime())
                .betweenIfPresent(WmsMovementOrderDO::getUpdateTime, reqVO.getUpdateTime())
                .orderByDesc(WmsMovementOrderDO::getId));
    }

    default WmsMovementOrderDO selectByNo(String no) {
        return selectOne(WmsMovementOrderDO::getNo, no);
    }

    default Long selectCountByWarehouseId(Long warehouseId) {
        return selectCount(new LambdaQueryWrapperX<WmsMovementOrderDO>()
                .and(query -> query.eq(WmsMovementOrderDO::getSourceWarehouseId, warehouseId)
                        .or().eq(WmsMovementOrderDO::getTargetWarehouseId, warehouseId)));
    }

    default int updateByIdAndStatus(Long id, Integer status, WmsMovementOrderDO updateObj) {
        return update(updateObj, new LambdaQueryWrapper<WmsMovementOrderDO>()
                .eq(WmsMovementOrderDO::getId, id)
                .eq(WmsMovementOrderDO::getStatus, status));
    }

}
