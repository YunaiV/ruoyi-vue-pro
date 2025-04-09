package cn.iocoder.yudao.module.wms.dal.mysql.inventory.bin;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.bin.WmsInventoryBinDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.bin.vo.*;

/**
 * 库位盘点 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsInventoryBinMapper extends BaseMapperX<WmsInventoryBinDO> {

    default PageResult<WmsInventoryBinDO> selectPage(WmsInventoryBinPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsInventoryBinDO>()
                .eqIfPresent(WmsInventoryBinDO::getInventoryId, reqVO.getInventoryId())
                .eqIfPresent(WmsInventoryBinDO::getProductId, reqVO.getProductId())
                .eqIfPresent(WmsInventoryBinDO::getExpectedQty, reqVO.getExpectedQty())
                .eqIfPresent(WmsInventoryBinDO::getActualQuantity, reqVO.getActualQuantity())
                .eqIfPresent(WmsInventoryBinDO::getNotes, reqVO.getNotes())
                .betweenIfPresent(WmsInventoryBinDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(WmsInventoryBinDO::getId));
    }

}