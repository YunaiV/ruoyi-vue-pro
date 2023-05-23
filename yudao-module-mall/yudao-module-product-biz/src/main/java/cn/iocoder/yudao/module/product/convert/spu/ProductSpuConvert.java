package cn.iocoder.yudao.module.product.convert.spu;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.value.ProductPropertyValueDetailRespVO;
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
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    // TODO @puhui999：部分属性，可以通过 mapstruct 的 @Mapping(source = , target = , ) 映射转换，可以查下文档
    default List<ProductSpuExcelVO> convertList03(List<ProductSpuDO> list){
        ArrayList<ProductSpuExcelVO> spuExcelVOs = new ArrayList<>();
        list.forEach((spu)->{
            ProductSpuExcelVO spuExcelVO = new ProductSpuExcelVO();
            spuExcelVO.setId(spu.getId());
            spuExcelVO.setName(spu.getName());
            spuExcelVO.setKeyword(spu.getKeyword());
            spuExcelVO.setIntroduction(spu.getIntroduction());
            spuExcelVO.setDescription(spu.getDescription());
            spuExcelVO.setBarCode(spu.getBarCode());
            spuExcelVO.setCategoryId(spu.getCategoryId());
            spuExcelVO.setBrandId(spu.getBrandId());
            spuExcelVO.setPicUrl(spu.getPicUrl());
            spuExcelVO.setSliderPicUrls(StrUtil.toString(spu.getSliderPicUrls()));
            spuExcelVO.setVideoUrl(spu.getVideoUrl());
            spuExcelVO.setUnit(spu.getUnit());
            spuExcelVO.setSort(spu.getSort());
            spuExcelVO.setStatus(spu.getStatus());
            spuExcelVO.setSpecType(spu.getSpecType());
            spuExcelVO.setPrice(spu.getPrice()/100);
            spuExcelVO.setMarketPrice(spu.getMarketPrice()/100);
            spuExcelVO.setCostPrice(spu.getCostPrice()/100);
            spuExcelVO.setStock(spu.getStock());
            spuExcelVO.setDeliveryTemplateId(spu.getDeliveryTemplateId());
            spuExcelVO.setRecommendHot(spu.getRecommendHot());
            spuExcelVO.setRecommendBenefit(spu.getRecommendBenefit());
            spuExcelVO.setRecommendBest(spu.getRecommendBest());
            spuExcelVO.setRecommendNew(spu.getRecommendNew());
            spuExcelVO.setRecommendGood(spu.getRecommendGood());
            spuExcelVO.setGiveIntegral(spu.getGiveIntegral());
            spuExcelVO.setGiveCouponTemplateIds(StrUtil.toString(spu.getGiveCouponTemplateIds())); // TODO 暂定
            spuExcelVO.setSubCommissionType(spu.getSubCommissionType());
            spuExcelVO.setActivityOrders(StrUtil.toString(spu.getActivityOrders())); // TODO 暂定
            spuExcelVO.setSalesCount(spu.getSalesCount());
            spuExcelVO.setVirtualSalesCount(spu.getVirtualSalesCount());
            spuExcelVO.setBrowseCount(spu.getBrowseCount());
            spuExcelVO.setCreateTime(spu.getCreateTime());
            spuExcelVOs.add(spuExcelVO);
        });
        return spuExcelVOs;
    }
    ProductSpuDetailRespVO convert03(ProductSpuDO spu);

    // TODO @puhui999：下面两个没用到，是不是删除呀？
    List<ProductSkuRespVO> convertList04(List<ProductSkuDO> skus);

    ProductPropertyValueDetailRespVO convert04(ProductPropertyValueDetailRespBO propertyValue);

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

    default ProductSpuDetailRespVO convertForSpuDetailRespVO(ProductSpuDO spu, List<ProductSkuDO> skus,
                                                             Function<Set<Long>, List<ProductPropertyValueDetailRespBO>> func) {
        ProductSpuDetailRespVO productSpuDetailRespVO = convert03(spu);
        // TODO @puhui999：if return 哈，减少嵌套层数。
        if (CollUtil.isNotEmpty(skus)) {
            List<ProductSkuRespVO> skuVOs = ProductSkuConvert.INSTANCE.convertList(skus);
            // fix:统一模型，即使是单规格，也查询下，如若 Properties 为空报错则为单属性不做处理
            try {
                // 获取所有的属性值 id
                Set<Long> valueIds = skus.stream().flatMap(p -> p.getProperties().stream())
                        .map(ProductSkuDO.Property::getValueId)
                        .collect(Collectors.toSet());
                List<ProductPropertyValueDetailRespBO> valueDetailList = func.apply(valueIds);
                Map<Long, String> stringMap = valueDetailList.stream().collect(Collectors.toMap(ProductPropertyValueDetailRespBO::getValueId, ProductPropertyValueDetailRespBO::getValueName));
                // 设置属性值名称
                skuVOs.stream().flatMap(p -> p.getProperties().stream()).forEach(item -> item.setValueName(stringMap.get(item.getValueId())));
            } catch (Exception ignored) {
            }
            productSpuDetailRespVO.setSkus(skuVOs);
        }
        return productSpuDetailRespVO;
    }

}
