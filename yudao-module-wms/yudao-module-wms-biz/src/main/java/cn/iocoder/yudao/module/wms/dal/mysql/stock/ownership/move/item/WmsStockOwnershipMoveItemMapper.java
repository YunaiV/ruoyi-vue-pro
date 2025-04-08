package cn.iocoder.yudao.module.wms.dal.mysql.stock.ownership.move.item;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership.move.item.WmsStockOwnershipMoveItemDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.move.item.vo.*;

/**
 * 所有者库存移动详情 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsStockOwnershipMoveItemMapper extends BaseMapperX<WmsStockOwnershipMoveItemDO> {

    default PageResult<WmsStockOwnershipMoveItemDO> selectPage(WmsStockOwnershipMoveItemPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsStockOwnershipMoveItemDO>()
				.eqIfPresent(WmsStockOwnershipMoveItemDO::getOwnershipMoveId, reqVO.getOwnershipMoveId())
				.eqIfPresent(WmsStockOwnershipMoveItemDO::getProductId, reqVO.getProductId())
				.eqIfPresent(WmsStockOwnershipMoveItemDO::getFromCompanyId, reqVO.getFromCompanyId())
				.eqIfPresent(WmsStockOwnershipMoveItemDO::getFromDeptId, reqVO.getFromDeptId())
				.eqIfPresent(WmsStockOwnershipMoveItemDO::getToCompanyId, reqVO.getToCompanyId())
				.eqIfPresent(WmsStockOwnershipMoveItemDO::getToDeptId, reqVO.getToDeptId())
				.eqIfPresent(WmsStockOwnershipMoveItemDO::getQty, reqVO.getQty())
				.betweenIfPresent(WmsStockOwnershipMoveItemDO::getCreateTime, reqVO.getCreateTime())
				.orderByDesc(WmsStockOwnershipMoveItemDO::getId));
    }

    /**
     * 按 ownership_move_id,product_id,from_company_id,from_dept_id,to_company_id,to_dept_id 查询唯一的 WmsStockOwnershipMoveItemDO
     */
    default WmsStockOwnershipMoveItemDO getByUk(Long ownershipMoveId, Long productId, Long fromCompanyId, Long fromDeptId, Long toCompanyId, Long toDeptId) {
        LambdaQueryWrapperX<WmsStockOwnershipMoveItemDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsStockOwnershipMoveItemDO::getOwnershipMoveId, ownershipMoveId);
        wrapper.eq(WmsStockOwnershipMoveItemDO::getProductId, productId);
        wrapper.eq(WmsStockOwnershipMoveItemDO::getFromCompanyId, fromCompanyId);
        wrapper.eq(WmsStockOwnershipMoveItemDO::getFromDeptId, fromDeptId);
        wrapper.eq(WmsStockOwnershipMoveItemDO::getToCompanyId, toCompanyId);
        wrapper.eq(WmsStockOwnershipMoveItemDO::getToDeptId, toDeptId);
        return selectOne(wrapper);
    }

    /**
     * 按 ownership_move_id 查询 WmsStockOwnershipMoveItemDO 清单
     */
    default List<WmsStockOwnershipMoveItemDO> selectByOwnershipMoveId(Long ownershipMoveId) {
        return selectList(new LambdaQueryWrapperX<WmsStockOwnershipMoveItemDO>().eq(WmsStockOwnershipMoveItemDO::getOwnershipMoveId, ownershipMoveId));
    }
}