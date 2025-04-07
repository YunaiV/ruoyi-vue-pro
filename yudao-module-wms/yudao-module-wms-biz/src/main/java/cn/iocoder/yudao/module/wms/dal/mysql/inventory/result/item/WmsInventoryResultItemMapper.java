package cn.iocoder.yudao.module.wms.dal.mysql.inventory.result.item;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.result.item.WmsInventoryResultItemDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.result.item.vo.*;

/**
 * 库存盘点结果详情 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsInventoryResultItemMapper extends BaseMapperX<WmsInventoryResultItemDO> {

    default PageResult<WmsInventoryResultItemDO> selectPage(WmsInventoryResultItemPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsInventoryResultItemDO>()
                .eqIfPresent(WmsInventoryResultItemDO::getResultId, reqVO.getResultId())
                .eqIfPresent(WmsInventoryResultItemDO::getProductId, reqVO.getProductId())
                .eqIfPresent(WmsInventoryResultItemDO::getExpectedQty, reqVO.getExpectedQty())
                .eqIfPresent(WmsInventoryResultItemDO::getActualQty, reqVO.getActualQty())
                .betweenIfPresent(WmsInventoryResultItemDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(WmsInventoryResultItemDO::getId));
    }

}