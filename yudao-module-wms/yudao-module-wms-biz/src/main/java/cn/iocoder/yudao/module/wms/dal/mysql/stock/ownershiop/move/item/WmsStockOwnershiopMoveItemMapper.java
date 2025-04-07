package cn.iocoder.yudao.module.wms.dal.mysql.stock.ownershiop.move.item;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownershiop.move.item.WmsStockOwnershiopMoveItemDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownershiop.move.item.vo.*;

/**
 * 所有者库存移动详情 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsStockOwnershiopMoveItemMapper extends BaseMapperX<WmsStockOwnershiopMoveItemDO> {

    default PageResult<WmsStockOwnershiopMoveItemDO> selectPage(WmsStockOwnershiopMoveItemPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsStockOwnershiopMoveItemDO>()
                .eqIfPresent(WmsStockOwnershiopMoveItemDO::getOwnershipMoveId, reqVO.getOwnershipMoveId())
                .eqIfPresent(WmsStockOwnershiopMoveItemDO::getProductId, reqVO.getProductId())
                .eqIfPresent(WmsStockOwnershiopMoveItemDO::getFromCompanyId, reqVO.getFromCompanyId())
                .eqIfPresent(WmsStockOwnershiopMoveItemDO::getFromDeptId, reqVO.getFromDeptId())
                .eqIfPresent(WmsStockOwnershiopMoveItemDO::getToCompanyId, reqVO.getToCompanyId())
                .eqIfPresent(WmsStockOwnershiopMoveItemDO::getToDeptId, reqVO.getToDeptId())
                .eqIfPresent(WmsStockOwnershiopMoveItemDO::getQty, reqVO.getQty())
                .betweenIfPresent(WmsStockOwnershiopMoveItemDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(WmsStockOwnershiopMoveItemDO::getId));
    }

}