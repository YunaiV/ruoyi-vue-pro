package cn.iocoder.yudao.module.wms.dal.mysql.stock.ownershiop.move;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownershiop.move.WmsStockOwnershiopMoveDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownershiop.move.vo.*;

/**
 * 所有者库存移动 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsStockOwnershiopMoveMapper extends BaseMapperX<WmsStockOwnershiopMoveDO> {

    default PageResult<WmsStockOwnershiopMoveDO> selectPage(WmsStockOwnershiopMovePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsStockOwnershiopMoveDO>()
                .eqIfPresent(WmsStockOwnershiopMoveDO::getNo, reqVO.getNo())
                .eqIfPresent(WmsStockOwnershiopMoveDO::getWarehouseId, reqVO.getWarehouseId())
                .betweenIfPresent(WmsStockOwnershiopMoveDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(WmsStockOwnershiopMoveDO::getId));
    }

}