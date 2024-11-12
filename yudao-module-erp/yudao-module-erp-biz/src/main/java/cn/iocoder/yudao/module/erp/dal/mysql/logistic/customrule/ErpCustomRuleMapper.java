package cn.iocoder.yudao.module.erp.dal.mysql.logistic.customrule;

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
import cn.iocoder.yudao.module.erp.controller.admin.logistic.customrule.vo.*;

/**
 * ERP 海关规则 Mapper
 *
 * @author 索迈管理员
 */
@Mapper
public interface ErpCustomRuleMapper extends BaseMapperX<ErpCustomRuleDO> {

    default PageResult<ErpCustomRuleDO> selectPage(ErpCustomRulePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpCustomRuleDO>()
                .eqIfPresent(ErpCustomRuleDO::getCountryCode, reqVO.getCountryCode())
                .eqIfPresent(ErpCustomRuleDO::getType, reqVO.getType())
                .eqIfPresent(ErpCustomRuleDO::getSupplierProductId, reqVO.getSupplierProductId())
                .eqIfPresent(ErpCustomRuleDO::getDeclaredTypeEn, reqVO.getDeclaredTypeEn())
                .eqIfPresent(ErpCustomRuleDO::getDeclaredType, reqVO.getDeclaredType())
                .eqIfPresent(ErpCustomRuleDO::getDeclaredValue, reqVO.getDeclaredValue())
                .eqIfPresent(ErpCustomRuleDO::getDeclaredValueCurrencyCode, reqVO.getDeclaredValueCurrencyCode())
                .eqIfPresent(ErpCustomRuleDO::getTaxRate, reqVO.getTaxRate())
                .eqIfPresent(ErpCustomRuleDO::getHscode, reqVO.getHscode())
                .eqIfPresent(ErpCustomRuleDO::getLogisticAttribute, reqVO.getLogisticAttribute())
                .betweenIfPresent(ErpCustomRuleDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpCustomRuleDO::getId));
    }
    /**
     * @Author Wqh
     * @Description 根据海关规则id获取产品的全量信息（海关规则，产品供应商）
     * @Date 10:40 2024/11/5
     * @Param [id]
     * @return java.util.List<cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO>
     **/
    default List<ErpProductDTO> selectProductAllInfoListByCustomRuleId(Long id){
        MPJLambdaWrapperX<ErpCustomRuleDO> wrapper = new MPJLambdaWrapperX<>();
        wrapper.selectAs(ErpCustomRuleDO::getId, ErpProductDTO::getRuleId)
                .selectAs(ErpCustomRuleDO::getCountryCode, ErpProductDTO::getCountryCode)
                .select(ErpCustomRuleDO::getLogisticAttribute)
                .selectAs(ErpCustomRuleDO::getHscode, ErpProductDTO::getHsCode)
                .selectAs(ErpCustomRuleDO::getDeclaredValue, ErpProductDTO::getProductDeclaredValue)
                .selectAs(ErpCustomRuleDO::getDeclaredValueCurrencyCode, ErpProductDTO::getPdDeclareCurrencyCode)
                .selectAs(ErpCustomRuleDO::getDeclaredType, ErpProductDTO::getPdOverseaTypeCn)
                .selectAs(ErpCustomRuleDO::getDeclaredTypeEn, ErpProductDTO::getPdOverseaTypeEn)
                .selectAs(ErpCustomRuleDO::getTaxRate, ErpProductDTO::getFboTaxRate)
                .leftJoin(ErpSupplierProductDO.class, ErpSupplierProductDO::getId, ErpCustomRuleDO::getSupplierProductId)
                .selectAs(ErpSupplierProductDO::getCode, ErpProductDTO::getSupplierProductCode)
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
                .eq(ErpCustomRuleDO::getId, id);
        return selectJoinList(ErpProductDTO.class, wrapper);
    }
}