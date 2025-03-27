package cn.iocoder.yudao.module.srm.dal.mysql.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.ErpSupplierProductPageReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpSupplierProductDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * ERP 供应商产品 Mapper
 *
 * @author 索迈管理员
 */
@Mapper
public interface ErpSupplierProductMapper extends BaseMapperX<ErpSupplierProductDO> {

    default PageResult<ErpSupplierProductDO> selectPage(ErpSupplierProductPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpSupplierProductDO>()
                .likeIfPresent(ErpSupplierProductDO::getCode, reqVO.getCode())
                .eqIfPresent(ErpSupplierProductDO::getSupplierId, reqVO.getSupplierId())
                .eqIfPresent(ErpSupplierProductDO::getProductId, reqVO.getProductId())
                .eqIfPresent(ErpSupplierProductDO::getPackageHeight, reqVO.getPackageHeight())
                .eqIfPresent(ErpSupplierProductDO::getPackageLength, reqVO.getPackageLength())
                .eqIfPresent(ErpSupplierProductDO::getPackageWeight, reqVO.getPackageWeight())
                .eqIfPresent(ErpSupplierProductDO::getPackageWidth, reqVO.getPackageWidth())
                .eqIfPresent(ErpSupplierProductDO::getPurchasePrice, reqVO.getPurchasePrice())
                .eqIfPresent(ErpSupplierProductDO::getPurchasePriceCurrencyCode, reqVO.getPurchasePriceCurrencyCode())
                .betweenIfPresent(ErpSupplierProductDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpSupplierProductDO::getId));
    }

    default ErpSupplierProductDO selectByCode(String code) {
        return selectOne(ErpSupplierProductDO::getCode, code);
    }
}