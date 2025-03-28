package cn.iocoder.yudao.module.srm.dal.mysql.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.SrmSupplierProductPageReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmSupplierProductDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * ERP 供应商产品 Mapper
 *
 * @author 索迈管理员
 */
@Mapper
public interface SrmSupplierProductMapper extends BaseMapperX<SrmSupplierProductDO> {

    default PageResult<SrmSupplierProductDO> selectPage(SrmSupplierProductPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SrmSupplierProductDO>()
                .likeIfPresent(SrmSupplierProductDO::getCode, reqVO.getCode())
                .eqIfPresent(SrmSupplierProductDO::getSupplierId, reqVO.getSupplierId())
                .eqIfPresent(SrmSupplierProductDO::getProductId, reqVO.getProductId())
                .eqIfPresent(SrmSupplierProductDO::getPackageHeight, reqVO.getPackageHeight())
                .eqIfPresent(SrmSupplierProductDO::getPackageLength, reqVO.getPackageLength())
                .eqIfPresent(SrmSupplierProductDO::getPackageWeight, reqVO.getPackageWeight())
                .eqIfPresent(SrmSupplierProductDO::getPackageWidth, reqVO.getPackageWidth())
                .eqIfPresent(SrmSupplierProductDO::getPurchasePrice, reqVO.getPurchasePrice())
                .eqIfPresent(SrmSupplierProductDO::getPurchasePriceCurrencyCode, reqVO.getPurchasePriceCurrencyCode())
                .betweenIfPresent(SrmSupplierProductDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SrmSupplierProductDO::getId));
    }

    default SrmSupplierProductDO selectByCode(String code) {
        return selectOne(SrmSupplierProductDO::getCode, code);
    }
}