package cn.iocoder.yudao.module.wms.dal.mysql.stock.bin.move;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.move.WmsStockBinMoveDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.vo.*;

/**
 * 库位移动 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsStockBinMoveMapper extends BaseMapperX<WmsStockBinMoveDO> {

    default PageResult<WmsStockBinMoveDO> selectPage(WmsStockBinMovePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsStockBinMoveDO>()
                .eqIfPresent(WmsStockBinMoveDO::getNo, reqVO.getNo())
                .eqIfPresent(WmsStockBinMoveDO::getWarehouseId, reqVO.getWarehouseId())
                .betweenIfPresent(WmsStockBinMoveDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(WmsStockBinMoveDO::getId));
    }

}