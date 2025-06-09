package cn.iocoder.yudao.module.wms.dal.mysql.stock.logic.move.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wms.controller.admin.stock.logic.move.item.vo.WmsStockLogicMoveItemPageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.logic.move.item.WmsStockLogicMoveItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 逻辑库存移动详情 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsStockLogicMoveItemMapper extends BaseMapperX<WmsStockLogicMoveItemDO> {

    default PageResult<WmsStockLogicMoveItemDO> selectPage(WmsStockLogicMoveItemPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsStockLogicMoveItemDO>()
            .eqIfPresent(WmsStockLogicMoveItemDO::getLogicMoveId, reqVO.getLogicMoveId())
            .eqIfPresent(WmsStockLogicMoveItemDO::getProductId, reqVO.getProductId())
            .eqIfPresent(WmsStockLogicMoveItemDO::getFromCompanyId, reqVO.getFromCompanyId())
            .eqIfPresent(WmsStockLogicMoveItemDO::getFromDeptId, reqVO.getFromDeptId())
            .eqIfPresent(WmsStockLogicMoveItemDO::getToCompanyId, reqVO.getToCompanyId())
            .eqIfPresent(WmsStockLogicMoveItemDO::getToDeptId, reqVO.getToDeptId())
            .eqIfPresent(WmsStockLogicMoveItemDO::getQty, reqVO.getQty())
            .betweenIfPresent(WmsStockLogicMoveItemDO::getCreateTime, reqVO.getCreateTime())
            .orderByDesc(WmsStockLogicMoveItemDO::getId));
    }

    /**
     * 按 logic_move_id,product_id,from_company_id,from_dept_id,to_company_id,to_dept_id 查询唯一的 WmsStockLogicMoveItemDO
     */
    default WmsStockLogicMoveItemDO getByUk(Long logicMoveId, Long productId, Long fromCompanyId, Long fromDeptId, Long toCompanyId, Long toDeptId) {
        LambdaQueryWrapperX<WmsStockLogicMoveItemDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsStockLogicMoveItemDO::getLogicMoveId, logicMoveId);
        wrapper.eq(WmsStockLogicMoveItemDO::getProductId, productId);
        wrapper.eq(WmsStockLogicMoveItemDO::getFromCompanyId, fromCompanyId);
        wrapper.eq(WmsStockLogicMoveItemDO::getFromDeptId, fromDeptId);
        wrapper.eq(WmsStockLogicMoveItemDO::getToCompanyId, toCompanyId);
        wrapper.eq(WmsStockLogicMoveItemDO::getToDeptId, toDeptId);
        return selectOne(wrapper);
    }

    /**
     * 按 logic_move_id 查询 WmsStockLogicMoveItemDO 清单
     */
    default List<WmsStockLogicMoveItemDO> selectByLogicMoveId(Long logicMoveId) {
        return selectList(new LambdaQueryWrapperX<WmsStockLogicMoveItemDO>().eq(WmsStockLogicMoveItemDO::getLogicMoveId, logicMoveId));
    }
}
