package cn.iocoder.yudao.module.wms.dal.mysql.inventory;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.WmsInventoryPageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryDO;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 盘点 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsInventoryMapper extends BaseMapperX<WmsInventoryDO> {

    default PageResult<WmsInventoryDO> selectPage(WmsInventoryPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsInventoryDO>()
				.eqIfPresent(WmsInventoryDO::getCode, reqVO.getCode())
				.eqIfPresent(WmsInventoryDO::getWarehouseId, reqVO.getWarehouseId())
				.eqIfPresent(WmsInventoryDO::getAuditStatus, reqVO.getAuditStatus())
				.eqIfPresent(WmsInventoryDO::getCreatorRemark, reqVO.getCreatorRemark())
				.betweenIfPresent(WmsInventoryDO::getCreateTime, reqVO.getCreateTime())
				.orderByDesc(WmsInventoryDO::getId));
    }

    /**
     * 按 no 查询唯一的 WmsInventoryDO
     */
    default WmsInventoryDO getByNo(String no) {
        LambdaQueryWrapperX<WmsInventoryDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsInventoryDO::getCode, no);
        return selectOne(wrapper);
    }

    /**
     * 按 warehouse_id 查询 WmsInventoryDO 清单
     */
    default List<WmsInventoryDO> selectByWarehouseId(Long warehouseId) {
        return selectList(new LambdaQueryWrapperX<WmsInventoryDO>().eq(WmsInventoryDO::getWarehouseId, warehouseId));
    }

    /**
     * 按 code 查询唯一的 WmsInventoryDO
     */
    default WmsInventoryDO getByCode(String code) {
        LambdaQueryWrapperX<WmsInventoryDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsInventoryDO::getCode, code);
        return selectOne(wrapper);
    }
}
