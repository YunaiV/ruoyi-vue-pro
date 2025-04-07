package cn.iocoder.yudao.module.wms.dal.mysql.stock.bin.move.item;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.move.item.WmsStockBinMoveItemDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.item.vo.*;

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

}