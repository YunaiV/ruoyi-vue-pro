package cn.iocoder.yudao.module.wms.dal.mysql.inventory.product;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.product.WmsInventoryProductDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.product.vo.*;

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

}