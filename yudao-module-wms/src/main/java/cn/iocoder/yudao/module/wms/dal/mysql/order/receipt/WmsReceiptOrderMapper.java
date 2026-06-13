package cn.iocoder.yudao.module.wms.dal.mysql.order.receipt;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wms.controller.admin.order.receipt.vo.order.WmsReceiptOrderPageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.receipt.WmsReceiptOrderDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * WMS 入库单 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface WmsReceiptOrderMapper extends BaseMapperX<WmsReceiptOrderDO> {

    default PageResult<WmsReceiptOrderDO> selectPage(WmsReceiptOrderPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsReceiptOrderDO>()
                .likeIfPresent(WmsReceiptOrderDO::getNo, reqVO.getNo())
                .eqIfPresent(WmsReceiptOrderDO::getStatus, reqVO.getStatus())
                .eqIfPresent(WmsReceiptOrderDO::getWarehouseId, reqVO.getWarehouseId())
                .eqIfPresent(WmsReceiptOrderDO::getMerchantId, reqVO.getMerchantId())
                .betweenIfPresent(WmsReceiptOrderDO::getOrderTime, reqVO.getOrderTime())
                .geIfPresent(WmsReceiptOrderDO::getTotalQuantity, reqVO.getTotalQuantityMin())
                .leIfPresent(WmsReceiptOrderDO::getTotalQuantity, reqVO.getTotalQuantityMax())
                .geIfPresent(WmsReceiptOrderDO::getTotalPrice, reqVO.getTotalPriceMin())
                .leIfPresent(WmsReceiptOrderDO::getTotalPrice, reqVO.getTotalPriceMax())
                .eqIfPresent(WmsReceiptOrderDO::getType, reqVO.getType())
                .likeIfPresent(WmsReceiptOrderDO::getBizOrderNo, reqVO.getBizOrderNo())
                .eqIfPresent(WmsReceiptOrderDO::getCreator, reqVO.getCreator())
                .eqIfPresent(WmsReceiptOrderDO::getUpdater, reqVO.getUpdater())
                .betweenIfPresent(WmsReceiptOrderDO::getCreateTime, reqVO.getCreateTime())
                .betweenIfPresent(WmsReceiptOrderDO::getUpdateTime, reqVO.getUpdateTime())
                .orderByDesc(WmsReceiptOrderDO::getId));
    }

    default WmsReceiptOrderDO selectByNo(String no) {
        return selectOne(WmsReceiptOrderDO::getNo, no);
    }

    default Long selectCountByMerchantId(Long merchantId) {
        return selectCount(WmsReceiptOrderDO::getMerchantId, merchantId);
    }

    default Long selectCountByWarehouseId(Long warehouseId) {
        return selectCount(WmsReceiptOrderDO::getWarehouseId, warehouseId);
    }

    default int updateByIdAndStatus(Long id, Integer status, WmsReceiptOrderDO updateObj) {
        return update(updateObj, new LambdaQueryWrapper<WmsReceiptOrderDO>()
                .eq(WmsReceiptOrderDO::getId, id)
                .eq(WmsReceiptOrderDO::getStatus, status));
    }

}
