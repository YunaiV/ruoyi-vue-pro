package cn.iocoder.yudao.module.erp.dal.mysql.product;

import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.logistic.customrule.ErpCustomRuleDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierProductDO;
import jakarta.validation.constraints.NotNull;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * ERP 产品 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ErpProductMapper extends BaseMapperX<ErpProductDO> {

    default PageResult<ErpProductDO> selectPage(ErpProductPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpProductDO>()
                .likeIfPresent(ErpProductDO::getName, reqVO.getName())
                .eqIfPresent(ErpProductDO::getCategoryId, reqVO.getCategoryId())
                .betweenIfPresent(ErpProductDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpProductDO::getId));
    }

    default Long selectCountByCategoryId(Long categoryId) {
        return selectCount(ErpProductDO::getCategoryId, categoryId);
    }

    default Long selectCountByUnitId(Long unitId) {
        return selectCount(ErpProductDO::getUnitId, unitId);
    }

    default ErpProductDO selectByCode(String code) {
        return selectOne(ErpProductDO::getBarCode, code);
    }

    default List<ErpProductDO> selectListByStatus(Integer status) {
        return selectList(ErpProductDO::getStatus, status);
    }



    /**
    * @Author Wqh
    * @Description 根据产品id获取产品的全量信息（海关规则，产品供应商）
    * @Date 10:40 2024/11/5
    * @Param [id]
    * @return java.util.List<cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO>
    **/
    default List<ErpProductDTO> selectProductAllInfoListById(@NotNull(message = "产品id不能为空") Long id){
        MPJLambdaWrapperX<ErpProductDO> wrapper = new MPJLambdaWrapperX<>();
        wrapper.select(
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
                .leftJoin(ErpSupplierProductDO.class, ErpSupplierProductDO::getProductId, ErpProductDO::getId)
                .selectAs(ErpSupplierProductDO::getCode, ErpProductDTO::getSupplierProductCode)
                .select( ErpSupplierProductDO::getPackageWeight,
                        ErpSupplierProductDO::getPackageLength,
                        ErpSupplierProductDO::getPackageWidth,
                        ErpSupplierProductDO::getPackageHeight,
                        ErpSupplierProductDO::getPurchasePriceCurrencyCode)
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
                .eq(ErpProductDO::getId, id);
        return selectJoinList(ErpProductDTO.class, wrapper);
    }




}