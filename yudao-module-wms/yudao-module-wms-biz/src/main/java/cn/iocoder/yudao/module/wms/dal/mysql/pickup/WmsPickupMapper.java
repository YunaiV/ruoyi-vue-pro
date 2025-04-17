package cn.iocoder.yudao.module.wms.dal.mysql.pickup;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wms.controller.admin.pickup.vo.WmsPickupPageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.pickup.WmsPickupDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 拣货单 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsPickupMapper extends BaseMapperX<WmsPickupDO> {

    default PageResult<WmsPickupDO> selectPage(WmsPickupPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsPickupDO>()
				.eqIfPresent(WmsPickupDO::getCode, reqVO.getCode())
				.eqIfPresent(WmsPickupDO::getWarehouseId, reqVO.getWarehouseId())
				.betweenIfPresent(WmsPickupDO::getCreateTime, reqVO.getCreateTime())
				.orderByDesc(WmsPickupDO::getId));
    }

    /**
     * 按 no 查询唯一的 WmsPickupDO
     */
    default WmsPickupDO getByNo(String no) {
        LambdaQueryWrapperX<WmsPickupDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsPickupDO::getCode, no);
        return selectOne(wrapper);
    }

    /**
     * 按 warehouse_id 查询 WmsPickupDO 清单
     */
    default List<WmsPickupDO> selectByWarehouseId(Long warehouseId) {
        return selectList(new LambdaQueryWrapperX<WmsPickupDO>().eq(WmsPickupDO::getWarehouseId, warehouseId));
    }

    /**
     * 按 code 查询唯一的 WmsPickupDO
     */
    default WmsPickupDO getByCode(String code) {
        LambdaQueryWrapperX<WmsPickupDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsPickupDO::getCode, code);
        return selectOne(wrapper);
    }
}
