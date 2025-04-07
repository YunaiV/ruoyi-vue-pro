package cn.iocoder.yudao.module.wms.dal.mysql.stock.bin.move.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.item.vo.WmsStockBinMoveItemPageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.move.item.WmsStockBinMoveItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 库位移动详情 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsStockBinMoveItemMapper extends BaseMapperX<WmsStockBinMoveItemDO> {

    default PageResult<WmsStockBinMoveItemDO> selectPage(WmsStockBinMoveItemPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsStockBinMoveItemDO>()
				.eqIfPresent(WmsStockBinMoveItemDO::getBinMoveId, reqVO.getBinMoveId())
				.eqIfPresent(WmsStockBinMoveItemDO::getProductId, reqVO.getProductId())
				.eqIfPresent(WmsStockBinMoveItemDO::getFromBinId, reqVO.getFromBinId())
				.eqIfPresent(WmsStockBinMoveItemDO::getToBinId, reqVO.getToBinId())
				.eqIfPresent(WmsStockBinMoveItemDO::getQty, reqVO.getQty())
				.betweenIfPresent(WmsStockBinMoveItemDO::getCreateTime, reqVO.getCreateTime())
				.orderByDesc(WmsStockBinMoveItemDO::getId));
    }

    /**
     * 按 bin_move_id 查询 WmsStockBinMoveItemDO 清单
     */
    default List<WmsStockBinMoveItemDO> selectByBinMoveId(Long binMoveId) {
        return selectList(new LambdaQueryWrapperX<WmsStockBinMoveItemDO>().eq(WmsStockBinMoveItemDO::getBinMoveId, binMoveId));
    }

    /**
     * 按 bin_move_id,product_id,from_bin_id,to_bin_id 查询唯一的 WmsStockBinMoveItemDO
     */
    default WmsStockBinMoveItemDO getByUk(Long binMoveId, Integer productId, Long fromBinId, Long toBinId) {
        LambdaQueryWrapperX<WmsStockBinMoveItemDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsStockBinMoveItemDO::getBinMoveId, binMoveId);
        wrapper.eq(WmsStockBinMoveItemDO::getProductId, productId);
        wrapper.eq(WmsStockBinMoveItemDO::getFromBinId, fromBinId);
        wrapper.eq(WmsStockBinMoveItemDO::getToBinId, toBinId);
        return selectOne(wrapper);
    }
}
