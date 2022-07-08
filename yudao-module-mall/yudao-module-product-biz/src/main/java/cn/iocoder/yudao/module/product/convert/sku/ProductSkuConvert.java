package cn.iocoder.yudao.module.product.convert.sku;

import java.util.*;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import org.springframework.util.StringUtils;

/**
 * 商品sku Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface ProductSkuConvert {

    ProductSkuConvert INSTANCE = Mappers.getMapper(ProductSkuConvert.class);

    @Mapping(source = "properties", target = "properties", qualifiedByName = "translateStringFromList")
    ProductSkuDO convert(ProductSkuCreateReqVO bean);

    @Mapping(source = "properties", target = "properties", qualifiedByName = "translateStringFromList")
    ProductSkuDO convert(ProductSkuUpdateReqVO bean);

    @Mapping(source = "properties", target = "properties", qualifiedByName = "tokenizeToBeanArray")
    ProductSkuRespVO convert(ProductSkuDO bean);

    @Mapping(source = "properties", target = "properties", qualifiedByName = "tokenizeToExcelBeanArray")
    ProductSkuExcelVO convertToExcelVO(ProductSkuDO bean);

    List<ProductSkuRespVO> convertList(List<ProductSkuDO> list);

    List<ProductSkuDO> convertSkuDOList(List<ProductSkuCreateReqVO> list);

    PageResult<ProductSkuRespVO> convertPage(PageResult<ProductSkuDO> page);

    List<ProductSkuExcelVO> convertList02(List<ProductSkuDO> list);

    @Named("tokenizeToBeanArray")
    default List<ProductSkuBaseVO.Property> translatePropertyArrayFromString(String properties) {
        return JSONUtil.toList(properties, ProductSkuBaseVO.Property.class);
    }

    @Named("tokenizeToExcelBeanArray")
    default List<ProductSkuExcelVO.Property> translateExcelPropertyArrayFromString(String properties) {
        return JSONUtil.toList(properties, ProductSkuExcelVO.Property.class);
    }

    @Named("translateStringFromList")
    default String translatePropertyStringFromList(List<ProductSkuBaseVO.Property> properties) {
        return JSONUtil.toJsonStr(properties);
    }
}
