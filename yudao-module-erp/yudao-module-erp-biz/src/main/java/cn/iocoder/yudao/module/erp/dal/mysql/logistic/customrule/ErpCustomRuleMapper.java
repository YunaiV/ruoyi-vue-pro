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
        wrapper.selectAs(ErpCustomRuleDO::getId, "rule_id")
                .selectAs(ErpCustomRuleDO::getCountryCode, "country_code")
                .select(ErpCustomRuleDO::getLogisticAttribute)
                .selectAs(ErpCustomRuleDO::getHscode, "hs_code")
                .selectAs(ErpCustomRuleDO::getDeclaredValue, "product_declared_value")
                .selectAs(ErpCustomRuleDO::getDeclaredValueCurrencyCode, "pd_declare_currency_code")
                .selectAs(ErpCustomRuleDO::getDeclaredType, "pd_oversea_type_cn")
                .selectAs(ErpCustomRuleDO::getDeclaredTypeEn, "pd_oversea_type_en")
                .selectAs(ErpCustomRuleDO::getTaxRate, "fbo_tax_rate")
                .leftJoin(ErpSupplierProductDO.class, ErpSupplierProductDO::getId,ErpCustomRuleDO::getSupplierProductId)
                .selectAs(ErpSupplierProductDO::getCode, "supplier_product_code")
                .selectAs(ErpSupplierProductDO::getPackageWeight, "pd_net_weight")
                .selectAs(ErpSupplierProductDO::getPackageLength, "pd_net_length")
                .selectAs(ErpSupplierProductDO::getPackageWidth, "pd_net_width")
                .selectAs(ErpSupplierProductDO::getPackageHeight, "pd_net_height")
                .selectAs(ErpSupplierProductDO::getPurchasePriceCurrencyCode, "currency_code")
                .leftJoin(ErpProductDO.class, ErpProductDO::getId,ErpSupplierProductDO::getProductId)
                .selectAs(ErpProductDO::getName, "product_title")
                .select(ErpProductDO::getImageUrl)
                .selectAs(ErpProductDO::getWeight, "product_weight")
                .selectAs(ErpProductDO::getLength, "product_length")
                .selectAs(ErpProductDO::getWidth, "product_width")
                .selectAs(ErpProductDO::getHeight, "product_height")
                .selectAs(ErpProductDO::getMaterial, "product_material")
                .select(ErpProductDO::getBarCode)
                .selectAs(ErpProductDO::getDeptId, "product_dept_id")
                .select(ErpProductDO::getCreator)
                .eq(ErpCustomRuleDO::getId, id);
        return selectJoinList(ErpProductDTO.class, wrapper);
    }
}