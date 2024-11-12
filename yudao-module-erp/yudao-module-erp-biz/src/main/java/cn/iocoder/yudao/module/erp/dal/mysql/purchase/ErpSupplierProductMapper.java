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
import jakarta.validation.constraints.NotNull;
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
    default List<ErpProductDTO> selectProductAllInfoListBySupplierId(@NotNull(message = "供应商产品id不能为空") Long id){
        MPJLambdaWrapperX<ErpSupplierProductDO> wrapper = new MPJLambdaWrapperX<>();
        wrapper.selectAs(ErpSupplierProductDO::getCode, ErpProductDTO::getSupplierProductCode)
                .select( ErpSupplierProductDO::getPackageWeight,
                        ErpSupplierProductDO::getPackageLength,
                        ErpSupplierProductDO::getPackageWidth,
                        ErpSupplierProductDO::getPackageHeight,
                        ErpSupplierProductDO::getPurchasePriceCurrencyCode)
                .leftJoin(ErpProductDO.class, ErpProductDO::getId, ErpSupplierProductDO::getProductId)
                .select(
                        ErpProductDO::getName,
                        ErpProductDO::getImageUrl,
                        ErpProductDO::getWeight,
                        ErpProductDO::getLength,
                        ErpProductDO::getWidth,
                        ErpProductDO::getHeight,
                        ErpProductDO::getMaterial,
                        ErpProductDO::getBarCode,
                        ErpProductDO::getCreator)
                .selectAs(ErpProductDO::getDeptId, ErpProductDTO::getProductDeptId)
                .leftJoin(ErpCustomRuleDO.class, ErpCustomRuleDO::getSupplierProductId, ErpSupplierProductDO::getId)
                .selectAs(ErpCustomRuleDO::getId, ErpProductDTO::getCustomRuleId)
                .select( ErpCustomRuleDO::getCountryCode,
                        ErpCustomRuleDO::getLogisticAttribute,
                        ErpCustomRuleDO::getHscode,
                        ErpCustomRuleDO::getDeclaredValue,
                        ErpCustomRuleDO::getDeclaredValueCurrencyCode,
                        ErpCustomRuleDO::getDeclaredType,
                        ErpCustomRuleDO::getDeclaredTypeEn,
                        ErpCustomRuleDO::getTaxRate)
                .eq(ErpSupplierProductDO::getId, id);
        return selectJoinList(ErpProductDTO.class, wrapper);
    }
}