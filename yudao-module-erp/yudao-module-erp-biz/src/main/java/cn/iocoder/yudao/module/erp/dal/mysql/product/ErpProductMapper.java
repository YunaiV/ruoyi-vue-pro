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
        wrapper.selectAs(ErpProductDO::getName, ErpProductDTO::getProductTitle)
                .select(ErpProductDO::getImageUrl)
                .selectAs(ErpProductDO::getWeight, ErpProductDTO::getProductTitle)
                .selectAs(ErpProductDO::getLength, ErpProductDTO::getProductTitle)
                .selectAs(ErpProductDO::getWidth, ErpProductDTO::getProductTitle)
                .selectAs(ErpProductDO::getHeight, ErpProductDTO::getProductTitle)
                .selectAs(ErpProductDO::getMaterial, ErpProductDTO::getProductTitle)
                .select(ErpProductDO::getBarCode)
                .selectAs(ErpProductDO::getDeptId, ErpProductDTO::getProductTitle)
                .select(ErpProductDO::getCreator)
                .leftJoin(ErpSupplierProductDO.class, ErpSupplierProductDO::getProductId, ErpProductDO::getId)
                .selectAs(ErpSupplierProductDO::getCode, ErpProductDTO::getProductTitle)
                .selectAs(ErpSupplierProductDO::getPackageWeight, ErpProductDTO::getProductTitle)
                .selectAs(ErpSupplierProductDO::getPackageLength, ErpProductDTO::getProductTitle)
                .selectAs(ErpSupplierProductDO::getPackageWidth, ErpProductDTO::getProductTitle)
                .selectAs(ErpSupplierProductDO::getPackageHeight, ErpProductDTO::getProductTitle)
                .selectAs(ErpSupplierProductDO::getPurchasePriceCurrencyCode, ErpProductDTO::getProductTitle)
                .leftJoin(ErpCustomRuleDO.class, ErpCustomRuleDO::getSupplierProductId, ErpSupplierProductDO::getId)
                .selectAs(ErpCustomRuleDO::getId, ErpProductDTO::getProductTitle)
                .selectAs(ErpCustomRuleDO::getCountryCode, ErpProductDTO::getProductTitle)
                .select(ErpCustomRuleDO::getLogisticAttribute)
                .selectAs(ErpCustomRuleDO::getHscode, ErpProductDTO::getProductTitle)
                .selectAs(ErpCustomRuleDO::getDeclaredValue, ErpProductDTO::getProductTitle)
                .selectAs(ErpCustomRuleDO::getDeclaredValueCurrencyCode, ErpProductDTO::getProductTitle)
                .selectAs(ErpCustomRuleDO::getDeclaredType, ErpProductDTO::getProductTitle)
                .selectAs(ErpCustomRuleDO::getDeclaredTypeEn, ErpProductDTO::getProductTitle)
                .selectAs(ErpCustomRuleDO::getTaxRate, ErpProductDTO::getProductTitle)
                .eq(ErpProductDO::getId, id);
        return selectJoinList(ErpProductDTO.class, wrapper);
    }




}