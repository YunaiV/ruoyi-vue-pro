package cn.iocoder.yudao.module.product.service.spu;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.ProductPropertyRespVO;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.ProductPropertyViewRespVO;
import cn.iocoder.yudao.module.product.controller.admin.propertyvalue.vo.ProductPropertyValueRespVO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuBaseVO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuCreateOrUpdateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuRespVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuCreateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuUpdateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.SpuPageReqVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.SpuRespVO;
import cn.iocoder.yudao.module.product.controller.app.spu.vo.AppSpuPageReqVO;
import cn.iocoder.yudao.module.product.controller.app.spu.vo.AppSpuPageRespVO;
import cn.iocoder.yudao.module.product.convert.sku.ProductSkuConvert;
import cn.iocoder.yudao.module.product.convert.spu.ProductSpuConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.dal.mysql.spu.ProductSpuMapper;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuSpecTypeEnum;
import cn.iocoder.yudao.module.product.service.category.ProductCategoryService;
import cn.iocoder.yudao.module.product.service.property.ProductPropertyService;
import cn.iocoder.yudao.module.product.service.sku.ProductSkuService;
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
    private ProductSpuMapper ProductSpuMapper;

    @Resource
    private ProductCategoryService categoryService;

    @Resource
    private ProductSkuService productSkuService;

    @Resource
    private ProductPropertyService productPropertyService;

    @Override
    @Transactional
    public Long createProductSpu(ProductSpuCreateReqVO createReqVO) {
        // 校验分类
        // TODO @luowenfeng：可以在这个类里加个方法，校验分类；商品必须挂在三级分类下；
        categoryService.validateProductCategory(createReqVO.getCategoryId());
        // TODO @luowenfeng：校验品牌
        // 校验SKU
        List<ProductSkuCreateOrUpdateReqVO> skuCreateReqList = createReqVO.getSkus();
        // 多规格才需校验
        // TODO @luowenfeng：可以把 type 传递到 productSkuService 里，通过它统一判断处理
        if(Objects.equals(createReqVO.getSpecType(), ProductSpuSpecTypeEnum.DISABLE.getType())) {
            productSkuService.validateProductSkus(skuCreateReqList);
        }

        // 插入 SPU
        ProductSpuDO spu = ProductSpuConvert.INSTANCE.convert(createReqVO);
        // TODO @luowenfeng：可以在 CollectionUtils 增加 getMaxValue 方法，增加一个 defaultValue 方法，如果为空，则返回 defaultValue
        spu.setMarketPrice(skuCreateReqList.stream().map(ProductSkuCreateOrUpdateReqVO::getMarketPrice).max(Integer::compare).orElse(0));
        spu.setMaxPrice(skuCreateReqList.stream().map(ProductSkuCreateOrUpdateReqVO::getPrice).max(Integer::compare).orElse(0));
        spu.setMinPrice(skuCreateReqList.stream().map(ProductSkuCreateOrUpdateReqVO::getPrice).min(Integer::compare).orElse(0));
        // TODO @luowenfeng：库存求和
        ProductSpuMapper.insert(spu);

        // 批量插入 SKU
        // TODO @luowenfeng：convert 逻辑，交给 createProductSkus 一起处理
        List<ProductSkuDO> skuDOList = ProductSkuConvert.INSTANCE.convertSkuDOList(skuCreateReqList);
        skuDOList.forEach(v->v.setSpuId(spu.getId()));
        productSkuService.createProductSkus(skuDOList);
        // 返回
        return spu.getId();
    }

    @Override
    @Transactional
    public void updateProductSpu(ProductSpuUpdateReqVO updateReqVO) {
        // 校验 SPU 是否存在
        validateSpuExists(updateReqVO.getId());
        // 校验分类
        categoryService.validateProductCategory(updateReqVO.getCategoryId());
        // TODO @luowenfeng：校验品牌
        // 校验SKU
        List<ProductSkuCreateOrUpdateReqVO> skuCreateReqList = updateReqVO.getSkus();
        // 多规格才需校验
        // TODO @luowenfeng：可以把 type 传递到 productSkuService 里，通过它统一判断处理
        if(updateReqVO.getSpecType().equals(ProductSpuSpecTypeEnum.DISABLE.getType())) {
            productSkuService.validateProductSkus(skuCreateReqList);
        }

        // 更新 SPU
        ProductSpuDO updateObj = ProductSpuConvert.INSTANCE.convert(updateReqVO);
        // TODO @计算各种字段
        ProductSpuMapper.updateById(updateObj);

        // 更新 SKU
        productSkuService.updateProductSkus(updateObj.getId(), updateReqVO.getSkus());
    }

    @Override
    @Transactional
    public void deleteSpu(Long id) {
        // 校验存在
        this.validateSpuExists(id);
        // 删除 SPU
        ProductSpuMapper.deleteById(id);
        // 删除关联的 SKU
        productSkuService.deleteSkuBySpuId(id);
    }

    private void validateSpuExists(Long id) {
        if (ProductSpuMapper.selectById(id) == null) {
            throw exception(SPU_NOT_EXISTS);
        }
    }

    @Override
    public SpuRespVO getSpu(Long id) {
        ProductSpuDO spu = ProductSpuMapper.selectById(id);
        SpuRespVO spuVO = ProductSpuConvert.INSTANCE.convert(spu);
        if (null != spuVO) {
            List<ProductSkuRespVO> skuReqs = ProductSkuConvert.INSTANCE.convertList(productSkuService.getSkusBySpuId(id));
            spuVO.setSkus(skuReqs);
            List<ProductSkuRespVO.Property> properties = new ArrayList<>();
            // 组合 sku 规格属性
            for (ProductSkuRespVO productSkuRespVO : skuReqs) {
                properties.addAll(productSkuRespVO.getProperties());
            }
            Map<Long, List<ProductSkuBaseVO.Property>> propertyMaps = properties.stream().collect(Collectors.groupingBy(ProductSkuBaseVO.Property::getPropertyId));
            List<ProductPropertyRespVO> propertyAndValueList = productPropertyService.selectByIds(new ArrayList<>(propertyMaps.keySet()));
            // 装载组装过后的属性
            List<ProductPropertyViewRespVO> productPropertyViews = new ArrayList<>();
            propertyAndValueList.forEach(p -> {
                ProductPropertyViewRespVO productPropertyViewRespVO = new ProductPropertyViewRespVO();
                productPropertyViewRespVO.setPropertyId(p.getId());
                productPropertyViewRespVO.setName(p.getName());
                Set<ProductPropertyViewRespVO.Tuple2> propertyValues = new HashSet<>();
                Map<Long, ProductPropertyValueRespVO> propertyValueMaps = p.getPropertyValueList().stream().collect(Collectors.toMap(ProductPropertyValueRespVO::getId, pv -> pv));
                propertyMaps.get(p.getId()).forEach(pv -> {
                    ProductPropertyViewRespVO.Tuple2 tuple2 = new ProductPropertyViewRespVO.Tuple2(pv.getValueId(), propertyValueMaps.get(pv.getValueId()).getName());
                    propertyValues.add(tuple2);
                });
                productPropertyViewRespVO.setPropertyValues(propertyValues);
                productPropertyViews.add(productPropertyViewRespVO);
            });
            spuVO.setProductPropertyViews(productPropertyViews);
            // 组合分类
            if (null != spuVO.getCategoryId()) {
                LinkedList<Long> categoryArray = new LinkedList<>();
                Long parentId = spuVO.getCategoryId();
                categoryArray.addFirst(parentId);
                while (parentId != 0) {
                    parentId = categoryService.getProductCategory(parentId).getParentId();
                    if (parentId > 0) {
                        categoryArray.addFirst(parentId);
                    }
                }
                spuVO.setCategoryIds(categoryArray);
            }
        }
        return spuVO;
    }

    @Override
    public List<ProductSpuDO> getSpuList(Collection<Long> ids) {
        return ProductSpuMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<SpuRespVO> getSpuPage(SpuPageReqVO pageReqVO) {
        PageResult<SpuRespVO> spuVOs = ProductSpuConvert.INSTANCE.convertPage(ProductSpuMapper.selectPage(pageReqVO));
        // 查询 sku 的信息
        List<Long> spuIds = spuVOs.getList().stream().map(SpuRespVO::getId).collect(Collectors.toList());
        List<ProductSkuRespVO> skus = ProductSkuConvert.INSTANCE.convertList(productSkuService.getSkusBySpuIds(spuIds));
        // TODO @franky：使用 CollUtil 里的方法替代哈
        // TODO 芋艿：临时注释
//        Map<Long, List<ProductSkuRespVO>> skuMap = skus.stream().collect(Collectors.groupingBy(ProductSkuRespVO::getSpuId));
//        // 将 spu 和 sku 进行组装
//        spuVOs.getList().forEach(p -> p.setSkus(skuMap.get(p.getId())));
        return spuVOs;
    }

    @Override
    public PageResult<AppSpuPageRespVO> getSpuPage(AppSpuPageReqVO pageReqVO) {
        PageResult<ProductSpuDO> productSpuDOPageResult = ProductSpuMapper.selectPage(ProductSpuConvert.INSTANCE.convert(pageReqVO));
        PageResult<AppSpuPageRespVO> pageResult = new PageResult<>();
        List<AppSpuPageRespVO> collect = productSpuDOPageResult.getList()
                .stream()
                .map(ProductSpuConvert.INSTANCE::convertAppResp)
                .collect(Collectors.toList());
        pageResult.setList(collect);
        pageResult.setTotal(productSpuDOPageResult.getTotal());
        return pageResult;
    }

}
