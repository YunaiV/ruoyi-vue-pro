package cn.iocoder.yudao.module.erp.dal.mysql.stock;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.stock.ErpStockPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * ERP 产品库存 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ErpStockMapper extends BaseMapperX<ErpStockDO> {

    default PageResult<ErpStockDO> selectPage(ErpStockPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpStockDO>()
                .eqIfPresent(ErpStockDO::getProductId, reqVO.getProductId())
                .eqIfPresent(ErpStockDO::getWarehouseId, reqVO.getWarehouseId())
                .orderByDesc(ErpStockDO::getId));
    }

}