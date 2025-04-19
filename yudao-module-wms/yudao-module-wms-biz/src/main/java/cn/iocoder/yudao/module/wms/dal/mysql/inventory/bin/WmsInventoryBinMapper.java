package cn.iocoder.yudao.module.wms.dal.mysql.inventory.bin;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.bin.vo.WmsInventoryBinPageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.bin.WmsInventoryBinDO;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

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
				.eqIfPresent(WmsInventoryBinDO::getActualQty, reqVO.getActualQty())
				.eqIfPresent(WmsInventoryBinDO::getRemark, reqVO.getRemark())
				.betweenIfPresent(WmsInventoryBinDO::getCreateTime, reqVO.getCreateTime())
				.orderByDesc(WmsInventoryBinDO::getId));
    }

    /**
     * 按 inventory_id,bin_id,product_id 查询唯一的 WmsInventoryBinDO
     */
    default WmsInventoryBinDO getByUkProductId(Long inventoryId, Long binId, Long productId) {
        LambdaQueryWrapperX<WmsInventoryBinDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsInventoryBinDO::getInventoryId, inventoryId);
        wrapper.eq(WmsInventoryBinDO::getBinId, binId);
        wrapper.eq(WmsInventoryBinDO::getProductId, productId);
        return selectOne(wrapper);
    }

    default List<WmsInventoryBinDO> selectByInventoryId(Long id) {
        return selectList(WmsInventoryBinDO::getInventoryId, id);
    }
}
