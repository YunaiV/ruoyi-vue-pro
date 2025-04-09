package cn.iocoder.yudao.module.wms.dal.mysql.inventory.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.product.vo.WmsInventoryProductPageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.product.WmsInventoryProductDO;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 库存盘点产品 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsInventoryProductMapper extends BaseMapperX<WmsInventoryProductDO> {

    default PageResult<WmsInventoryProductDO> selectPage(WmsInventoryProductPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsInventoryProductDO>()
				.eqIfPresent(WmsInventoryProductDO::getInventoryId, reqVO.getInventoryId())
				.eqIfPresent(WmsInventoryProductDO::getProductId, reqVO.getProductId())
				.eqIfPresent(WmsInventoryProductDO::getExpectedQty, reqVO.getExpectedQty())
				.eqIfPresent(WmsInventoryProductDO::getActualQty, reqVO.getActualQty())
				.eqIfPresent(WmsInventoryProductDO::getNotes, reqVO.getNotes())
				.betweenIfPresent(WmsInventoryProductDO::getCreateTime, reqVO.getCreateTime())
				.orderByDesc(WmsInventoryProductDO::getId));
    }

    /**
     * 按 inventory_id,product_id 查询唯一的 WmsInventoryProductDO
     */
    default WmsInventoryProductDO getByInventoryIdAndProductId(Long inventoryId, Long productId) {
        LambdaQueryWrapperX<WmsInventoryProductDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsInventoryProductDO::getInventoryId, inventoryId);
        wrapper.eq(WmsInventoryProductDO::getProductId, productId);
        return selectOne(wrapper);
    }

    default List<WmsInventoryProductDO> selectByInventoryId(Long id) {
        return selectList(WmsInventoryProductDO::getInventoryId, id);
    }
}