package cn.iocoder.yudao.module.wms.dal.mysql.warehouse.bin;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.bin.WmsWarehouseBinDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.bin.vo.*;

/**
 * 库位 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsWarehouseBinMapper extends BaseMapperX<WmsWarehouseBinDO> {

    default PageResult<WmsWarehouseBinDO> selectPage(WmsWarehouseBinPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsWarehouseBinDO>()
				.eqIfPresent(WmsWarehouseBinDO::getCode, reqVO.getCode())
				.likeIfPresent(WmsWarehouseBinDO::getName, reqVO.getName())
				.eqIfPresent(WmsWarehouseBinDO::getWarehouseId, reqVO.getWarehouseId())
				.eqIfPresent(WmsWarehouseBinDO::getZoneId, reqVO.getZoneId())
				.eqIfPresent(WmsWarehouseBinDO::getPickingOrder, reqVO.getPickingOrder())
				.eqIfPresent(WmsWarehouseBinDO::getStatus, reqVO.getStatus())
				.betweenIfPresent(WmsWarehouseBinDO::getCreateTime, reqVO.getCreateTime())
				.orderByDesc(WmsWarehouseBinDO::getId));
    }

    /**
     * 按 warehouseId 查询 WmsWarehouseBinDO
     */
    default List<WmsWarehouseBinDO> selectByWarehouseId(Long warehouseId, int limit) {
        WmsWarehouseBinPageReqVO reqVO = new WmsWarehouseBinPageReqVO();
        reqVO.setPageSize(limit);
        reqVO.setPageNo(1);
        LambdaQueryWrapperX<WmsWarehouseBinDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsWarehouseBinDO::getWarehouseId, warehouseId);
        return selectPage(reqVO, wrapper).getList();
    }

    /**
     * 按 zoneId 查询 WmsWarehouseBinDO
     */
    default List<WmsWarehouseBinDO> selectByZoneId(Long zoneId, int limit) {
        WmsWarehouseBinPageReqVO reqVO = new WmsWarehouseBinPageReqVO();
        reqVO.setPageSize(limit);
        reqVO.setPageNo(1);
        LambdaQueryWrapperX<WmsWarehouseBinDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsWarehouseBinDO::getZoneId, zoneId);
        return selectPage(reqVO, wrapper).getList();
    }

    /**
     * 按 warehouse_id,zone_id 查询 WmsWarehouseBinDO 清单
     */
    default List<WmsWarehouseBinDO> selectByWarehouseIdAndZoneId(Long warehouseId, Long zoneId) {
        return selectList(new LambdaQueryWrapperX<WmsWarehouseBinDO>().eq(WmsWarehouseBinDO::getWarehouseId, warehouseId).eq(WmsWarehouseBinDO::getZoneId, zoneId));
    }

    /**
     * 按 code 查询唯一的 WmsWarehouseBinDO
     */
    default WmsWarehouseBinDO getByCode(String code) {
        LambdaQueryWrapperX<WmsWarehouseBinDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsWarehouseBinDO::getCode, code);
        return selectOne(wrapper);
    }
}