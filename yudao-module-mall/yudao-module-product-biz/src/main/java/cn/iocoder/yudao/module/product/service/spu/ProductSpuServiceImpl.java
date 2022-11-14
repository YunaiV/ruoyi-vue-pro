package cn.iocoder.yudao.module.product.service.spu;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.ProductPropertyViewRespVO;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.property.ProductPropertyRespVO;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.value.ProductPropertyValueRespVO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuBaseVO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuCreateOrUpdateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuRespVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.*;
import cn.iocoder.yudao.module.product.controller.app.spu.vo.AppSpuPageReqVO;
import cn.iocoder.yudao.module.product.controller.app.spu.vo.AppSpuPageRespVO;
import cn.iocoder.yudao.module.product.convert.sku.ProductSkuConvert;
import cn.iocoder.yudao.module.product.convert.spu.ProductSpuConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.dal.mysql.spu.ProductSpuMapper;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuSpecTypeEnum;
import cn.iocoder.yudao.module.product.service.brand.ProductBrandService;
import cn.iocoder.yudao.module.product.service.category.ProductCategoryService;
import cn.iocoder.yudao.module.product.service.property.ProductPropertyService;
import cn.iocoder.yudao.module.product.service.property.ProductPropertyValueService;
import cn.iocoder.yudao.module.product.service.sku.ProductSkuService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SPU_NOT_EXISTS;

/**
 * 商品 SPU Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ProductSpuServiceImpl implements ProductSpuService {

    @Resource
    private ProductSpuMapper productSpuMapper;

    @Resource
    private ProductCategoryService categoryService;

    @Resource
    @Lazy // 循环依赖，避免报错
    private ProductSkuService productSkuService;
    @Resource
    private ProductPropertyService productPropertyService;
    @Resource
    private ProductPropertyValueService productPropertyValueService;
    @Resource
    private ProductBrandService brandService;

    @Override
    @Transactional
    public Long createSpu(ProductSpuCreateReqVO createReqVO) {
        // 校验分类
        categoryService.validateCategoryLevel(createReqVO.getCategoryId());
        // 校验品牌
        brandService.validateProductBrand(createReqVO.getBrandId());
        // 校验SKU
        List<ProductSkuCreateOrUpdateReqVO> skuCreateReqList = createReqVO.getSkus();
        productSkuService.validateSkus(skuCreateReqList, createReqVO.getSpecType());
        // 插入 SPU
        ProductSpuDO spu = ProductSpuConvert.INSTANCE.convert(createReqVO);
        spu.setMarketPrice(CollectionUtils.getMaxValue(skuCreateReqList, ProductSkuCreateOrUpdateReqVO::getMarketPrice));
        spu.setMaxPrice(CollectionUtils.getMaxValue(skuCreateReqList, ProductSkuCreateOrUpdateReqVO::getPrice));
        spu.setMinPrice(CollectionUtils.getMinValue(skuCreateReqList, ProductSkuCreateOrUpdateReqVO::getPrice));
        spu.setTotalStock(CollectionUtils.getSumValue(skuCreateReqList, ProductSkuCreateOrUpdateReqVO::getStock, Integer::sum));
        productSpuMapper.insert(spu);
        // 插入 SKU
        productSkuService.createSkus(spu.getId(), skuCreateReqList);
        // 返回
        return spu.getId();
    }

    @Override
    @Transactional
    public void updateSpu(ProductSpuUpdateReqVO updateReqVO) {
        // 校验 SPU 是否存在
        validateSpuExists(updateReqVO.getId());
        // 校验分类
        categoryService.validateCategoryLevel(updateReqVO.getCategoryId());
        // 校验品牌
        brandService.validateProductBrand(updateReqVO.getBrandId());
        // 校验SKU
        List<ProductSkuCreateOrUpdateReqVO> skuCreateReqList = updateReqVO.getSkus();
        productSkuService.validateSkus(skuCreateReqList, updateReqVO.getSpecType());

        // 更新 SPU
        ProductSpuDO updateObj = ProductSpuConvert.INSTANCE.convert(updateReqVO);
        updateObj.setMarketPrice(CollectionUtils.getMaxValue(skuCreateReqList, ProductSkuCreateOrUpdateReqVO::getMarketPrice));
        updateObj.setMaxPrice(CollectionUtils.getMaxValue(skuCreateReqList, ProductSkuCreateOrUpdateReqVO::getPrice));
        updateObj.setMinPrice(CollectionUtils.getMinValue(skuCreateReqList, ProductSkuCreateOrUpdateReqVO::getPrice));
        updateObj.setTotalStock(CollectionUtils.getSumValue(skuCreateReqList, ProductSkuCreateOrUpdateReqVO::getStock, Integer::sum));
        productSpuMapper.updateById(updateObj);
        // 批量更新 SKU
        productSkuService.updateSkus(updateObj.getId(), updateReqVO.getSkus());
    }

    @Override
    @Transactional
    public void deleteSpu(Long id) {
        // 校验存在
        validateSpuExists(id);
        // 删除 SPU
        productSpuMapper.deleteById(id);
        // 删除关联的 SKU
        productSkuService.deleteSkuBySpuId(id);
    }

    private void validateSpuExists(Long id) {
        if (productSpuMapper.selectById(id) == null) {
            throw exception(SPU_NOT_EXISTS);
        }
    }

    @Override
    // TODO @芋艿：需要再 review 下
    public ProductSpuDetailRespVO getSpuDetail(Long id) {
        ProductSpuDO spu = productSpuMapper.selectById(id);
        ProductSpuDetailRespVO respVO = BeanUtil.copyProperties(spu, ProductSpuDetailRespVO.class);
        if (null != spu) {
            List<ProductSpuDetailRespVO.Sku> skuReqs = ProductSkuConvert.INSTANCE.convertList03(productSkuService.getSkusBySpuId(id));
            respVO.setSkus(skuReqs);
            // 组合 sku 规格属性
            if (spu.getSpecType().equals(ProductSpuSpecTypeEnum.DISABLE.getType())) {
                List<ProductSkuRespVO.Property> properties = new ArrayList<>();
                for (ProductSpuDetailRespVO.Sku productSkuRespVO : skuReqs) {
                    properties.addAll(productSkuRespVO.getProperties());
                }
                Map<Long, List<ProductSkuBaseVO.Property>> propertyMaps = properties.stream().collect(Collectors.groupingBy(ProductSkuBaseVO.Property::getPropertyId));

                List<ProductPropertyValueRespVO> propertyValueList = productPropertyValueService.getPropertyValueListByPropertyId(new ArrayList<>(propertyMaps.keySet()));
                List<ProductPropertyRespVO> propertyList = productPropertyService.getPropertyList(new ArrayList<>(propertyMaps.keySet()));
                // 装载组装过后的属性
                List<ProductPropertyViewRespVO> productPropertyViews = new ArrayList<>();
                propertyList.forEach(p -> {
                    ProductPropertyViewRespVO productPropertyViewRespVO = new ProductPropertyViewRespVO();
                    productPropertyViewRespVO.setPropertyId(p.getId());
                    productPropertyViewRespVO.setName(p.getName());
                    List<ProductPropertyViewRespVO.Tuple2> propertyValues = new ArrayList<>();
                    // 转换成map是为了能快速获取
                    Map<Long, ProductPropertyValueRespVO> propertyValueMaps = CollectionUtils.convertMap(propertyValueList, ProductPropertyValueRespVO::getId);
                    propertyMaps.get(p.getId()).forEach(pv -> {
                        ProductPropertyViewRespVO.Tuple2 tuple2 = new ProductPropertyViewRespVO.Tuple2(pv.getValueId(), propertyValueMaps.get(pv.getValueId()).getName());
                        propertyValues.add(tuple2);
                    });
                    productPropertyViewRespVO.setPropertyValues(propertyValues.stream().distinct().collect(Collectors.toList()));
                    productPropertyViews.add(productPropertyViewRespVO);
                });
                respVO.setProductPropertyViews(productPropertyViews);
            }
        }
        return respVO;
    }

    @Override
    public ProductSpuRespVO getSpu(Long id) {
        return ProductSpuConvert.INSTANCE.convert(productSpuMapper.selectById(id));
    }

    @Override
    public List<ProductSpuDO> getSpuList(Collection<Long> ids) {
        return productSpuMapper.selectBatchIds(ids);
    }

    @Override
    public List<ProductSpuDO> getSpuList() {
        return productSpuMapper.selectList();
    }

    @Override
    public PageResult<ProductSpuRespVO> getSpuPage(ProductSpuPageReqVO pageReqVO) {
        // 库存告警的 SPU 编号的集合
        Set<Long> alarmStockSpuIds = null;
        if (Boolean.TRUE.equals(pageReqVO.getAlarmStock())) {
            alarmStockSpuIds = CollectionUtils.convertSet(productSkuService.getSkusByAlarmStock(), ProductSkuDO::getSpuId);
            if (CollUtil.isEmpty(alarmStockSpuIds)) {
                return PageResult.empty();
            }
        }
        // 分页查询
        return ProductSpuConvert.INSTANCE.convertPage(productSpuMapper.selectPage(pageReqVO, alarmStockSpuIds));
    }

    @Override
    public PageResult<AppSpuPageRespVO> getSpuPage(AppSpuPageReqVO pageReqVO) {
        // TODO 芋艿：貌似实现不太合理
        PageResult<ProductSpuDO> productSpuDOPageResult = productSpuMapper.selectPage(ProductSpuConvert.INSTANCE.convert(pageReqVO));
        PageResult<AppSpuPageRespVO> pageResult = new PageResult<>();
        // TODO @芋艿 这里用convert如何解决
        List<AppSpuPageRespVO> collect = productSpuDOPageResult.getList()
                .stream()
                .map(ProductSpuConvert.INSTANCE::convertAppResp)
                .collect(Collectors.toList());
        pageResult.setList(collect);
        pageResult.setTotal(productSpuDOPageResult.getTotal());
        return pageResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSpuStock(Map<Long, Integer> stockIncrCounts) {
        stockIncrCounts.forEach((id, incCount) -> productSpuMapper.updateStock(id, incCount));
    }

}
