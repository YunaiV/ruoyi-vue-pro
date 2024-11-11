package cn.iocoder.yudao.module.erp.dal.mysql.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.logistic.customrule.ErpCustomRuleDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductCategoryDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierProductDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
    default List<ErpProductDTO> selectProductAllInfoListById(Long id){
        MPJLambdaWrapperX<ErpProductDO> wrapper = new MPJLambdaWrapperX<>();
        wrapper.selectAs(ErpProductDO::getName, "product_title")
                .select(ErpProductDO::getImageUrl)
                .selectAs(ErpProductDO::getWeight, "product_weight")
                .selectAs(ErpProductDO::getLength, "product_length")
                .selectAs(ErpProductDO::getWidth, "product_width")
                .selectAs(ErpProductDO::getHeight, "product_height")
                .selectAs(ErpProductDO::getMaterial, "product_material")
                .select(ErpProductDO::getBarCode)
                .selectAs(ErpProductDO::getDeptId, "product_dept_id")
                .select(ErpProductDO::getCreator)
                .leftJoin(ErpSupplierProductDO.class, ErpSupplierProductDO::getProductId, ErpProductDO::getId)
                .selectAs(ErpSupplierProductDO::getCode, "supplier_product_code")
                .selectAs(ErpSupplierProductDO::getPackageWeight, "pd_net_weight")
                .selectAs(ErpSupplierProductDO::getPackageLength, "pd_net_length")
                .selectAs(ErpSupplierProductDO::getPackageWidth, "pd_net_width")
                .selectAs(ErpSupplierProductDO::getPackageHeight, "pd_net_height")
                .selectAs(ErpSupplierProductDO::getPurchasePriceCurrencyCode, "currency_code")
                .leftJoin(ErpCustomRuleDO.class, ErpCustomRuleDO::getSupplierProductId, ErpSupplierProductDO::getId)
                .selectAs(ErpCustomRuleDO::getId, "rule_id")
                .selectAs(ErpCustomRuleDO::getCountryCode, "country_code")
                .select(ErpCustomRuleDO::getLogisticAttribute)
                .selectAs(ErpCustomRuleDO::getHscode, "hs_code")
                .selectAs(ErpCustomRuleDO::getDeclaredValue, "product_declared_value")
                .selectAs(ErpCustomRuleDO::getDeclaredValueCurrencyCode, "pd_declare_currency_code")
                .selectAs(ErpCustomRuleDO::getDeclaredType, "pd_oversea_type_cn")
                .selectAs(ErpCustomRuleDO::getDeclaredTypeEn, "pd_oversea_type_en")
                .selectAs(ErpCustomRuleDO::getTaxRate, "fbo_tax_rate")
                .eq(ErpProductDO::getId, id);
        return selectJoinList(ErpProductDTO.class, wrapper);
    }




}