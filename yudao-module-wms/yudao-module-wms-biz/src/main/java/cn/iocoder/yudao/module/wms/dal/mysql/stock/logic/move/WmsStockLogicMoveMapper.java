package cn.iocoder.yudao.module.wms.dal.mysql.stock.logic.move;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wms.controller.admin.stock.logic.move.vo.WmsStockLogicMovePageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.logic.move.WmsStockLogicMoveDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 逻辑库存移动 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsStockLogicMoveMapper extends BaseMapperX<WmsStockLogicMoveDO> {

    default PageResult<WmsStockLogicMoveDO> selectPage(WmsStockLogicMovePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsStockLogicMoveDO>()
            .eqIfPresent(WmsStockLogicMoveDO::getNo, reqVO.getNo())
            .eqIfPresent(WmsStockLogicMoveDO::getExecuteStatus, reqVO.getExecuteStatus())
            .eqIfPresent(WmsStockLogicMoveDO::getWarehouseId, reqVO.getWarehouseId())
            .betweenIfPresent(WmsStockLogicMoveDO::getCreateTime, reqVO.getCreateTime())
            .orderByDesc(WmsStockLogicMoveDO::getId));
    }

    /**
     * 按 no 查询唯一的 WmsStockLogicMoveDO
     */
    default WmsStockLogicMoveDO getByNo(String no) {
        LambdaQueryWrapperX<WmsStockLogicMoveDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsStockLogicMoveDO::getNo, no);
        return selectOne(wrapper);
    }

    /**
     * 按 warehouse_id 查询 WmsStockLogicMoveDO 清单
     */
    default List<WmsStockLogicMoveDO> selectByWarehouseId(Long warehouseId) {
        return selectList(new LambdaQueryWrapperX<WmsStockLogicMoveDO>().eq(WmsStockLogicMoveDO::getWarehouseId, warehouseId));
    }
}
