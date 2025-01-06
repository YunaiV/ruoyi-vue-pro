package cn.iocoder.yudao.module.erp.dal.mysql.logistic.customrule;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpCustomRuleDTO;
import cn.iocoder.yudao.module.erp.dal.dataobject.logistic.customrule.ErpCustomRuleDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierProductDO;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import jakarta.validation.constraints.NotNull;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.erp.controller.admin.logistic.customrule.vo.*;

/**
 * ERP 海关规则 Mapper
 *
 * @author 索迈管理员
 */
@Mapper
public interface ErpCustomRuleMapper extends BaseMapperX<ErpCustomRuleDO> {
    default MPJLambdaWrapper<ErpCustomRuleDO> getWrapper() {
        return new MPJLambdaWrapperX<ErpCustomRuleDO>()
                .select(ErpCustomRuleDO::getId,
                        ErpCustomRuleDO::getCountryCode,
                        ErpCustomRuleDO::getLogisticAttribute,
                        ErpCustomRuleDO::getHscode,
                        ErpCustomRuleDO::getDeclaredValue,
                        ErpCustomRuleDO::getDeclaredValueCurrencyCode,
                        ErpCustomRuleDO::getDeclaredType,
                        ErpCustomRuleDO::getDeclaredTypeEn,
                        ErpCustomRuleDO::getTaxRate)
                .leftJoin(ErpSupplierProductDO.class, ErpSupplierProductDO::getId, ErpCustomRuleDO::getSupplierProductId)
                .selectAs(ErpSupplierProductDO::getCode, ErpCustomRuleDTO::getSupplierProductCode)
                .selectAs(ErpSupplierProductDO::getPurchasePrice,ErpCustomRuleDTO::getProductPurchaseValue)
                .select(ErpSupplierProductDO::getPackageWeight,
                        ErpSupplierProductDO::getPackageLength,
                        ErpSupplierProductDO::getPackageWidth,
                        ErpSupplierProductDO::getPackageHeight,
                        ErpSupplierProductDO::getPurchasePriceCurrencyCode)
                .leftJoin(ErpProductDO.class, ErpProductDO::getId, ErpSupplierProductDO::getProductId)
                .selectAs(ErpProductDO::getName, ErpCustomRuleDTO::getProductName)
                .selectAs(ErpProductDO::getPrimaryImageUrl, ErpCustomRuleDTO::getProductImageUrl)
                .selectAs(ErpProductDO::getWeight, ErpCustomRuleDTO::getProductWeight)
                .selectAs(ErpProductDO::getLength, ErpCustomRuleDTO::getProductLength)
                .selectAs(ErpProductDO::getWidth, ErpCustomRuleDTO::getProductWidth)
                .selectAs(ErpProductDO::getHeight, ErpCustomRuleDTO::getProductHeight)
                .selectAs(ErpProductDO::getMaterial, ErpCustomRuleDTO::getProductMaterial)
                .selectAs(ErpProductDO::getCreator, ErpCustomRuleDTO::getProductCreatorId)
                .select(ErpProductDO::getBarCode)
                .selectAs(ErpProductDO::getDeptId, ErpCustomRuleDTO::getProductDeptId);
    }

    default PageResult<ErpCustomRuleDO> selectPage(ErpCustomRulePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpCustomRuleDO>()
                .eqIfPresent(ErpCustomRuleDO::getCountryCode, reqVO.getCountryCode())
                .eqIfPresent(ErpCustomRuleDO::getType, reqVO.getType())
                .eqIfPresent(ErpCustomRuleDO::getSupplierProductId, reqVO.getSupplierProductId())
                .likeIfPresent(ErpCustomRuleDO::getDeclaredTypeEn, reqVO.getDeclaredTypeEn())
                .likeIfPresent(ErpCustomRuleDO::getDeclaredType, reqVO.getDeclaredType())
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
    default List<ErpCustomRuleDTO> selectProductAllInfoListByCustomRuleId(@NotNull(message = "海关规则id不能为空") Long id) {
        return selectJoinList(ErpCustomRuleDTO.class, getWrapper().eq(ErpCustomRuleDO::getId, id));
    }

    /**
     * @Author Wqh
     * @Description 根据产品id获取产品的全量信息（海关规则，产品供应商）
     * @Date 10:40 2024/11/5
     * @Param [id]
     * @return java.util.List<cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO>
     **/
    default List<ErpCustomRuleDTO> selectProductAllInfoListById(@NotNull(message = "产品id不能为空") Long id) {
        return selectJoinList(ErpCustomRuleDTO.class, getWrapper().eq(ErpProductDO::getId, id));
    }

    /**
     * @Author Wqh
     * @Description 根据供应商产品id获取产品的全量信息（海关规则，产品供应商）
     * @Date 10:40 2024/11/5
     * @Param [id]
     * @return java.util.List<cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO>
     **/
    default List<ErpCustomRuleDTO> selectProductAllInfoListBySupplierId(@NotNull(message = "供应商产品id不能为空") Long id) {
        return selectJoinList(ErpCustomRuleDTO.class, getWrapper().eq(ErpSupplierProductDO::getId, id));
    }

    default ErpCustomRuleDO selectByCountryCodeAndSupplierProductId(Integer countryCode, Long supplierProductId) {
        return selectOne(new LambdaQueryWrapperX<ErpCustomRuleDO>()
                .eq(ErpCustomRuleDO::getCountryCode, countryCode)
                .eq(ErpCustomRuleDO::getSupplierProductId, supplierProductId));
    }
}
