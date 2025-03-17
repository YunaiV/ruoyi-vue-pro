package cn.iocoder.yudao.module.wms.dal.mysql.warehouse.area;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.area.WmsWarehouseAreaDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.area.vo.*;

/**
 * 库区 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsWarehouseAreaMapper extends BaseMapperX<WmsWarehouseAreaDO> {

    default PageResult<WmsWarehouseAreaDO> selectPage(WmsWarehouseAreaPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsWarehouseAreaDO>()
				.eqIfPresent(WmsWarehouseAreaDO::getCode, reqVO.getCode())
				.likeIfPresent(WmsWarehouseAreaDO::getName, reqVO.getName())
				.eqIfPresent(WmsWarehouseAreaDO::getWarehouseId, reqVO.getWarehouseId())
				.eqIfPresent(WmsWarehouseAreaDO::getStockType, reqVO.getStockType())
				.eqIfPresent(WmsWarehouseAreaDO::getPartitionType, reqVO.getPartitionType())
				.eqIfPresent(WmsWarehouseAreaDO::getStatus, reqVO.getStatus())
				.eqIfPresent(WmsWarehouseAreaDO::getPriority, reqVO.getPriority())
				.betweenIfPresent(WmsWarehouseAreaDO::getCreateTime, reqVO.getCreateTime())
				.orderByDesc(WmsWarehouseAreaDO::getId));
    }

    /**
     * 按 warehouseId 查询 WmsWarehouseAreaDO
     */
    default List<WmsWarehouseAreaDO> selectByWarehouseId(Long warehouseId, int limit) {
        WmsWarehouseAreaPageReqVO reqVO = new WmsWarehouseAreaPageReqVO();
        reqVO.setPageSize(limit);
        reqVO.setPageNo(1);
        LambdaQueryWrapperX<WmsWarehouseAreaDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsWarehouseAreaDO::getWarehouseId, warehouseId);
        return selectPage(reqVO, wrapper).getList();
    }

    /**
     * 按 warehouse_id 查询 WmsWarehouseAreaDO 清单
     */
    default List<WmsWarehouseAreaDO> selectByWarehouseId(Long warehouseId) {
        return selectList(new LambdaQueryWrapperX<WmsWarehouseAreaDO>().eq(WmsWarehouseAreaDO::getWarehouseId, warehouseId));
    }

    /**
     * 按 code 查询唯一的 WmsWarehouseAreaDO
     */
    default WmsWarehouseAreaDO getByCode(String code) {
        LambdaQueryWrapperX<WmsWarehouseAreaDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsWarehouseAreaDO::getCode, code);
        return selectOne(wrapper);
    }
}