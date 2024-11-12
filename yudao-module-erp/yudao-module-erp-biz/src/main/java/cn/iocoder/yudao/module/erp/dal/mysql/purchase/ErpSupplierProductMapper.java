package cn.iocoder.yudao.module.erp.dal.mysql.purchase;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.erp.dal.dataobject.logistic.customrule.ErpCustomRuleDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierProductDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.*;

/**
 * ERP 供应商产品 Mapper
 *
 * @author 索迈管理员
 */
@Mapper
public interface ErpSupplierProductMapper extends BaseMapperX<ErpSupplierProductDO> {

    default PageResult<ErpSupplierProductDO> selectPage(ErpSupplierProductPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpSupplierProductDO>()
                .eqIfPresent(ErpSupplierProductDO::getCode, reqVO.getCode())
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

    /**
     * @Author Wqh
     * @Description 根据供应商产品id获取产品的全量信息（海关规则，产品供应商）
     * @Date 10:40 2024/11/5
     * @Param [id]
     * @return java.util.List<cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO>
     **/
    default List<ErpProductDTO> selectProductAllInfoListBySupplierId(Long id){
        MPJLambdaWrapperX<ErpSupplierProductDO> wrapper = new MPJLambdaWrapperX<>();
        wrapper.selectAs(ErpSupplierProductDO::getCode, ErpProductDTO::getSupplierProductCode)
                .selectAs(ErpSupplierProductDO::getPackageWeight, ErpProductDTO::getPdNetWeight)
                .selectAs(ErpSupplierProductDO::getPackageLength, ErpProductDTO::getPdNetLength)
                .selectAs(ErpSupplierProductDO::getPackageWidth, ErpProductDTO::getPdNetWidth)
                .selectAs(ErpSupplierProductDO::getPackageHeight, ErpProductDTO::getPdNetHeight)
                .selectAs(ErpSupplierProductDO::getPurchasePriceCurrencyCode, ErpProductDTO::getCurrencyCode)
                .leftJoin(ErpProductDO.class, ErpProductDO::getId, ErpSupplierProductDO::getProductId)
                .selectAs(ErpProductDO::getName, ErpProductDTO::getProductTitle)
                .select(ErpProductDO::getImageUrl)
                .selectAs(ErpProductDO::getWeight, ErpProductDTO::getProductWeight)
                .selectAs(ErpProductDO::getLength, ErpProductDTO::getProductLength)
                .selectAs(ErpProductDO::getWidth, ErpProductDTO::getProductWidth)
                .selectAs(ErpProductDO::getHeight, ErpProductDTO::getProductHeight)
                .selectAs(ErpProductDO::getMaterial, ErpProductDTO::getProductMaterial)
                .select(ErpProductDO::getBarCode)
                .selectAs(ErpProductDO::getDeptId, ErpProductDTO::getProductDeptId)
                .select(ErpProductDO::getCreator)
                .leftJoin(ErpCustomRuleDO.class, ErpCustomRuleDO::getSupplierProductId, ErpSupplierProductDO::getId)
                .selectAs(ErpCustomRuleDO::getId, ErpProductDTO::getRuleId)
                .selectAs(ErpCustomRuleDO::getCountryCode, ErpProductDTO::getCountryCode)
                .select(ErpCustomRuleDO::getLogisticAttribute)
                .selectAs(ErpCustomRuleDO::getHscode, ErpProductDTO::getHsCode)
                .selectAs(ErpCustomRuleDO::getDeclaredValue, ErpProductDTO::getProductDeclaredValue)
                .selectAs(ErpCustomRuleDO::getDeclaredValueCurrencyCode, ErpProductDTO::getPdDeclareCurrencyCode)
                .selectAs(ErpCustomRuleDO::getDeclaredType, ErpProductDTO::getPdOverseaTypeCn)
                .selectAs(ErpCustomRuleDO::getDeclaredTypeEn, ErpProductDTO::getPdOverseaTypeEn)
                .selectAs(ErpCustomRuleDO::getTaxRate, ErpProductDTO::getFboTaxRate)
                .eq(ErpSupplierProductDO::getId, id);
        return selectJoinList(ErpProductDTO.class, wrapper);
    }
}