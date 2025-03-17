package cn.iocoder.yudao.module.wms.dal.mysql.warehouse.location;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.location.WmsWarehouseLocationDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.location.vo.*;

/**
 * 库位 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsWarehouseLocationMapper extends BaseMapperX<WmsWarehouseLocationDO> {

    default PageResult<WmsWarehouseLocationDO> selectPage(WmsWarehouseLocationPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsWarehouseLocationDO>()
				.eqIfPresent(WmsWarehouseLocationDO::getCode, reqVO.getCode())
				.likeIfPresent(WmsWarehouseLocationDO::getName, reqVO.getName())
				.eqIfPresent(WmsWarehouseLocationDO::getWarehouseId, reqVO.getWarehouseId())
				.eqIfPresent(WmsWarehouseLocationDO::getAreaId, reqVO.getAreaId())
				.eqIfPresent(WmsWarehouseLocationDO::getPickingOrder, reqVO.getPickingOrder())
				.eqIfPresent(WmsWarehouseLocationDO::getStatus, reqVO.getStatus())
				.betweenIfPresent(WmsWarehouseLocationDO::getCreateTime, reqVO.getCreateTime())
				.orderByDesc(WmsWarehouseLocationDO::getId));
    }

    /**
     * 按 warehouseId 查询 WmsWarehouseLocationDO
     */
    default List<WmsWarehouseLocationDO> selectByWarehouseId(Long warehouseId, int limit) {
        WmsWarehouseLocationPageReqVO reqVO = new WmsWarehouseLocationPageReqVO();
        reqVO.setPageSize(limit);
        reqVO.setPageNo(1);
        LambdaQueryWrapperX<WmsWarehouseLocationDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsWarehouseLocationDO::getWarehouseId, warehouseId);
        return selectPage(reqVO, wrapper).getList();
    }

    /**
     * 按 areaId 查询 WmsWarehouseLocationDO
     */
    default List<WmsWarehouseLocationDO> selectByAreaId(Long areaId, int limit) {
        WmsWarehouseLocationPageReqVO reqVO = new WmsWarehouseLocationPageReqVO();
        reqVO.setPageSize(limit);
        reqVO.setPageNo(1);
        LambdaQueryWrapperX<WmsWarehouseLocationDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsWarehouseLocationDO::getAreaId, areaId);
        return selectPage(reqVO, wrapper).getList();
    }

    /**
     * 按 warehouse_id,area_id 查询 WmsWarehouseLocationDO 清单
     */
    default List<WmsWarehouseLocationDO> selectByWarehouseIdAndAreaId(Long warehouseId, Long areaId) {
        return selectList(new LambdaQueryWrapperX<WmsWarehouseLocationDO>().eq(WmsWarehouseLocationDO::getWarehouseId, warehouseId).eq(WmsWarehouseLocationDO::getAreaId, areaId));
    }

    /**
     * 按 code 查询唯一的 WmsWarehouseLocationDO
     */
    default WmsWarehouseLocationDO getByCode(String code) {
        LambdaQueryWrapperX<WmsWarehouseLocationDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsWarehouseLocationDO::getCode, code);
        return selectOne(wrapper);
    }
}