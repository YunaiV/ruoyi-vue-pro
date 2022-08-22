package cn.iocoder.yudao.module.product.service.spu;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.ProductPropertyRespVO;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.ProductPropertyViewRespVO;
import cn.iocoder.yudao.module.product.controller.admin.propertyvalue.vo.ProductPropertyValueRespVO;
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
    public Long createSpu(ProductSpuCreateReqVO createReqVO) {
        // 校验分类
        categoryService.validateProductCategory(createReqVO.getCategoryId());
        // 校验SKU
        List<ProductSkuCreateOrUpdateReqVO> skuCreateReqList = createReqVO.getSkus();
        productSkuService.validateSkus(skuCreateReqList);
        // 插入SPU
        ProductSpuDO spu = ProductSpuConvert.INSTANCE.convert(createReqVO);
        ProductSpuMapper.insert(spu);
        List<ProductSkuDO> skuDOList = ProductSkuConvert.INSTANCE.convertSkuDOList(skuCreateReqList);
        // 批量插入sku
        productSkuService.createSkus(skuDOList);
        // 返回
        return spu.getId();
    }

    @Override
    @Transactional
    public void updateSpu(ProductSpuUpdateReqVO updateReqVO) {
        // 校验 spu 是否存在
        this.validateSpuExists(updateReqVO.getId());
        // 校验分类
        categoryService.validateProductCategory(updateReqVO.getCategoryId());
        // 校验SKU
        List<ProductSkuCreateOrUpdateReqVO> skuCreateReqList = updateReqVO.getSkus();
        productSkuService.validateSkus(skuCreateReqList);
        // 更新
        ProductSpuDO updateObj = ProductSpuConvert.INSTANCE.convert(updateReqVO);
        ProductSpuMapper.updateById(updateObj);
        // 更新 sku
        productSkuService.updateSkus(updateObj.getId(), updateReqVO.getSkus());
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
