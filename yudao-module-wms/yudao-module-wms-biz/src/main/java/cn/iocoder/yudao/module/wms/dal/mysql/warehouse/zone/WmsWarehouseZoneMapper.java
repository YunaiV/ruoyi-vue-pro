package cn.iocoder.yudao.module.wms.dal.mysql.warehouse.zone;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.zone.vo.WmsWarehouseZonePageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.zone.WmsWarehouseZoneDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 库区 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsWarehouseZoneMapper extends BaseMapperX<WmsWarehouseZoneDO> {

    default PageResult<WmsWarehouseZoneDO> selectPage(WmsWarehouseZonePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsWarehouseZoneDO>()
				.eqIfPresent(WmsWarehouseZoneDO::getCode, reqVO.getCode())
				.likeIfPresent(WmsWarehouseZoneDO::getName, reqVO.getName())
				.eqIfPresent(WmsWarehouseZoneDO::getWarehouseId, reqVO.getWarehouseId())
				.eqIfPresent(WmsWarehouseZoneDO::getStockType, reqVO.getStockType())
				.eqIfPresent(WmsWarehouseZoneDO::getPartitionType, reqVO.getPartitionType())
				.eqIfPresent(WmsWarehouseZoneDO::getStatus, reqVO.getStatus())
				.eqIfPresent(WmsWarehouseZoneDO::getPriority, reqVO.getPriority())
				.betweenIfPresent(WmsWarehouseZoneDO::getCreateTime, reqVO.getCreateTime())
				.orderByDesc(WmsWarehouseZoneDO::getId));
    }

    /**
     * 按 warehouseId 查询 WmsWarehouseZoneDO
     */
    default List<WmsWarehouseZoneDO> selectByWarehouseId(Long warehouseId, int limit) {
        WmsWarehouseZonePageReqVO reqVO = new WmsWarehouseZonePageReqVO();
        reqVO.setPageSize(limit);
        reqVO.setPageNo(1);
        LambdaQueryWrapperX<WmsWarehouseZoneDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsWarehouseZoneDO::getWarehouseId, warehouseId);
        return selectPage(reqVO, wrapper).getList();
    }

    /**
     * 按 warehouse_id 查询 WmsWarehouseZoneDO 清单
     */
    default List<WmsWarehouseZoneDO> selectByWarehouseId(Long warehouseId) {
        return selectList(new LambdaQueryWrapperX<WmsWarehouseZoneDO>().eq(WmsWarehouseZoneDO::getWarehouseId, warehouseId));
    }

    /**
     * 按 code 查询唯一的 WmsWarehouseZoneDO
     */
    default WmsWarehouseZoneDO getByCode(String code) {
        LambdaQueryWrapperX<WmsWarehouseZoneDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsWarehouseZoneDO::getCode, code);
        return selectOne(wrapper);
    }

    default List<WmsWarehouseZoneDO> getSimpleList(WmsWarehouseZonePageReqVO pageReqVO) {
        LambdaQueryWrapperX<WmsWarehouseZoneDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.likeIfPresent(WmsWarehouseZoneDO::getCode, pageReqVO.getCode());
        wrapper.likeIfPresent(WmsWarehouseZoneDO::getName, pageReqVO.getName());
        wrapper.eqIfPresent(WmsWarehouseZoneDO::getWarehouseId, pageReqVO.getWarehouseId());
        wrapper.eqIfPresent(WmsWarehouseZoneDO::getStockType, pageReqVO.getStockType());
        return selectList(wrapper);
    }
}
