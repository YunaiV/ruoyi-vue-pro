package cn.iocoder.yudao.module.wms.dal.mysql.stockcheck.bin;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wms.controller.admin.stockcheck.bin.vo.WmsStockCheckBinPageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stockcheck.bin.WmsStockCheckBinDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 库位盘点 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsStockCheckBinMapper extends BaseMapperX<WmsStockCheckBinDO> {

    default PageResult<WmsStockCheckBinDO> selectPage(WmsStockCheckBinPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsStockCheckBinDO>()
            .eqIfPresent(WmsStockCheckBinDO::getStockCheckId, reqVO.getStockCheckId())
            .eqIfPresent(WmsStockCheckBinDO::getProductId, reqVO.getProductId())
            .eqIfPresent(WmsStockCheckBinDO::getExpectedQty, reqVO.getExpectedQty())
            .eqIfPresent(WmsStockCheckBinDO::getActualQty, reqVO.getActualQty())
            .eqIfPresent(WmsStockCheckBinDO::getRemark, reqVO.getRemark())
            .betweenIfPresent(WmsStockCheckBinDO::getCreateTime, reqVO.getCreateTime())
            .orderByDesc(WmsStockCheckBinDO::getId));
    }

    /**
     * 按 stockCheck_id,bin_id,product_id 查询唯一的 WmsStockCheckBinDO
     */
    default WmsStockCheckBinDO getByUkProductId(Long stockCheckId, Long binId, Long productId) {
        LambdaQueryWrapperX<WmsStockCheckBinDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsStockCheckBinDO::getStockCheckId, stockCheckId);
        wrapper.eq(WmsStockCheckBinDO::getBinId, binId);
        wrapper.eq(WmsStockCheckBinDO::getProductId, productId);
        return selectOne(wrapper);
    }

    default List<WmsStockCheckBinDO> selectByStockCheckId(Long id) {
        return selectList(WmsStockCheckBinDO::getStockCheckId, id);
    }
}
