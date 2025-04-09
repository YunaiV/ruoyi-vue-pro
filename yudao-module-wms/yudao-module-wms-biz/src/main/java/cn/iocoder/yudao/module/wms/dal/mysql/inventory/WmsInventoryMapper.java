package cn.iocoder.yudao.module.wms.dal.mysql.inventory;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.*;

/**
 * 盘点 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsInventoryMapper extends BaseMapperX<WmsInventoryDO> {

    default PageResult<WmsInventoryDO> selectPage(WmsInventoryPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsInventoryDO>()
                .eqIfPresent(WmsInventoryDO::getNo, reqVO.getNo())
                .eqIfPresent(WmsInventoryDO::getWarehouseId, reqVO.getWarehouseId())
                .eqIfPresent(WmsInventoryDO::getAuditStatus, reqVO.getAuditStatus())
                .eqIfPresent(WmsInventoryDO::getCreatorNotes, reqVO.getCreatorNotes())
                .betweenIfPresent(WmsInventoryDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(WmsInventoryDO::getId));
    }

}