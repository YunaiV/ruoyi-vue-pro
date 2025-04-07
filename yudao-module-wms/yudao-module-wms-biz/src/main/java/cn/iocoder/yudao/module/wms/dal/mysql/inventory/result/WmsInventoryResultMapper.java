package cn.iocoder.yudao.module.wms.dal.mysql.inventory.result;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.result.WmsInventoryResultDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.result.vo.*;

/**
 * 库存盘点结果 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsInventoryResultMapper extends BaseMapperX<WmsInventoryResultDO> {

    default PageResult<WmsInventoryResultDO> selectPage(WmsInventoryResultPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsInventoryResultDO>()
                .eqIfPresent(WmsInventoryResultDO::getNo, reqVO.getNo())
                .eqIfPresent(WmsInventoryResultDO::getWarehouseId, reqVO.getWarehouseId())
                .eqIfPresent(WmsInventoryResultDO::getCreatorComment, reqVO.getCreatorComment())
                .betweenIfPresent(WmsInventoryResultDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(WmsInventoryResultDO::getId));
    }

}