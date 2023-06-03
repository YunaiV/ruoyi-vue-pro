package cn.iocoder.yudao.module.product.convert.spu;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuRespVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.*;
import cn.iocoder.yudao.module.product.controller.app.property.vo.value.AppProductPropertyValueDetailRespVO;
import cn.iocoder.yudao.module.product.controller.app.spu.vo.AppProductSpuDetailRespVO;
import cn.iocoder.yudao.module.product.controller.app.spu.vo.AppProductSpuPageItemRespVO;
import cn.iocoder.yudao.module.product.controller.app.spu.vo.AppProductSpuPageReqVO;
import cn.iocoder.yudao.module.product.convert.sku.ProductSkuConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.service.property.bo.ProductPropertyValueDetailRespBO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 商品 SPU Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface ProductSpuConvert {

    ProductSpuConvert INSTANCE = Mappers.getMapper(ProductSpuConvert.class);

    ProductSpuDO convert(ProductSpuCreateReqVO bean);

    ProductSpuDO convert(ProductSpuUpdateReqVO bean);

    List<ProductSpuDO> convertList(List<ProductSpuDO> list);

    PageResult<ProductSpuRespVO> convertPage(PageResult<ProductSpuDO> page);

    ProductSpuPageReqVO convert(AppProductSpuPageReqVO bean);

    List<ProductSpuRespDTO> convertList2(List<ProductSpuDO> list);

    List<ProductSpuSimpleRespVO> convertList02(List<ProductSpuDO> list);

    /**
     * 列表转字符串
     *
     * @param list 列表
     * @return 字符串
     */
    @Named("convertListToString")
    default String convertListToString(List<?> list) {
        return StrUtil.toString(list);
    }

    @Mapping(source = "sliderPicUrls", target = "sliderPicUrls", qualifiedByName = "convertListToString")
    @Mapping(source = "giveCouponTemplateIds", target = "giveCouponTemplateIds", qualifiedByName = "convertListToString")
    @Mapping(source = "activityOrders", target = "activityOrders", qualifiedByName = "convertListToString")
    @Mapping(target = "price", expression = "java(spu.getPrice() / 100)")
    @Mapping(target = "marketPrice", expression = "java(spu.getMarketPrice() / 100)")
    @Mapping(target = "costPrice", expression = "java(spu.getCostPrice() / 100)")
    ProductSpuExcelVO convert(ProductSpuDO spu);

    default List<ProductSpuExcelVO> convertList03(List<ProductSpuDO> list) {
        List<ProductSpuExcelVO> spuExcelVOs = new ArrayList<>();
        list.forEach(spu -> {
            ProductSpuExcelVO spuExcelVO = convert(spu);
            spuExcelVOs.add(spuExcelVO);
        });
        return spuExcelVOs;
    }

    ProductSpuDetailRespVO convert03(ProductSpuDO spu);

    // ========== 用户 App 相关 ==========

    default PageResult<AppProductSpuPageItemRespVO> convertPageForGetSpuPage(PageResult<ProductSpuDO> page) {
        // 累加虚拟销量
        page.getList().forEach(spu -> spu.setSalesCount(spu.getSalesCount() + spu.getVirtualSalesCount()));
        // 然后进行转换
        return convertPageForGetSpuPage0(page);
    }

    PageResult<AppProductSpuPageItemRespVO> convertPageForGetSpuPage0(PageResult<ProductSpuDO> page);

    default AppProductSpuDetailRespVO convertForGetSpuDetail(ProductSpuDO spu, List<ProductSkuDO> skus,
                                                             List<ProductPropertyValueDetailRespBO> propertyValues) {
        AppProductSpuDetailRespVO spuVO = convertForGetSpuDetail(spu)
                .setSalesCount(spu.getSalesCount() + defaultIfNull(spu.getVirtualSalesCount(), 0));
        spuVO.setSkus(convertListForGetSpuDetail(skus));
        // 处理商品属性
        Map<Long, ProductPropertyValueDetailRespBO> propertyValueMap = convertMap(propertyValues, ProductPropertyValueDetailRespBO::getValueId);
        for (int i = 0; i < skus.size(); i++) {
            List<ProductSkuDO.Property> properties = skus.get(i).getProperties();
            if (CollUtil.isEmpty(properties)) {
                continue;
            }
            AppProductSpuDetailRespVO.Sku sku = spuVO.getSkus().get(i);
            sku.setProperties(new ArrayList<>(properties.size()));
            // 遍历每个 properties，设置到 AppSpuDetailRespVO.Sku 中
            properties.forEach(property -> {
                ProductPropertyValueDetailRespBO propertyValue = propertyValueMap.get(property.getValueId());
                if (propertyValue == null) {
                    return;
                }
                sku.getProperties().add(convertForGetSpuDetail(propertyValue));
            });
        }
        return spuVO;
    }

    AppProductSpuDetailRespVO convertForGetSpuDetail(ProductSpuDO spu);

    List<AppProductSpuDetailRespVO.Sku> convertListForGetSpuDetail(List<ProductSkuDO> skus);

    AppProductPropertyValueDetailRespVO convertForGetSpuDetail(ProductPropertyValueDetailRespBO propertyValue);

    default ProductSpuDetailRespVO convertForSpuDetailRespVO(ProductSpuDO spu, List<ProductSkuDO> skus) {
        ProductSpuDetailRespVO productSpuDetailRespVO = convert03(spu);
        // skus 为空直接返回
        if (CollUtil.isEmpty(skus)) {
            return productSpuDetailRespVO;
        }
        List<ProductSkuRespVO> skuVOs = ProductSkuConvert.INSTANCE.convertList(skus);
        // fix: 因为现在已改为 sku 属性列表 属性 已包含 属性名字 属性值名字 所以不需要再额外处理，属性更新时更新 sku 中的属性相关冗余即可
        productSpuDetailRespVO.setSkus(skuVOs);
        return productSpuDetailRespVO;
    }

}
