package cn.iocoder.yudao.module.product.service.sku;

import cn.hutool.core.collection.CollectionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.ProductPropertyRespVO;
import cn.iocoder.yudao.module.product.controller.admin.propertyvalue.vo.ProductPropertyValueRespVO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuBaseVO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuCreateOrUpdateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuPageReqVO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuUpdateReqVO;
import cn.iocoder.yudao.module.product.convert.sku.ProductSkuConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.mysql.sku.ProductSkuMapper;
import cn.iocoder.yudao.module.product.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuSpecTypeEnum;
import cn.iocoder.yudao.module.product.service.property.ProductPropertyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.PROPERTY_NOT_EXISTS;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SKU_NOT_EXISTS;

/**
 * 商品sku Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ProductSkuServiceImpl implements ProductSkuService {

    @Resource
    private ProductSkuMapper productSkuMapper;

    @Resource
    private ProductPropertyService productPropertyService;

    @Override
    public Long createSku(ProductSkuCreateOrUpdateReqVO createReqVO) {
        // 插入
        ProductSkuDO sku = ProductSkuConvert.INSTANCE.convert(createReqVO);
        productSkuMapper.insert(sku);
        // 返回
        return sku.getId();
    }

    @Override
    public void updateSku(ProductSkuUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateSkuExists(updateReqVO.getId());
        // 更新
        ProductSkuDO updateObj = ProductSkuConvert.INSTANCE.convert(updateReqVO);
        productSkuMapper.updateById(updateObj);
    }

    @Override
    public void deleteSku(Long id) {
        // 校验存在
        this.validateSkuExists(id);
        // 删除
        productSkuMapper.deleteById(id);
    }

    private void validateSkuExists(Long id) {
        if (productSkuMapper.selectById(id) == null) {
            throw exception(SKU_NOT_EXISTS);
        }
    }

    @Override
    public ProductSkuDO getSku(Long id) {
        return productSkuMapper.selectById(id);
    }

    @Override
    public List<ProductSkuDO> getSkuList(Collection<Long> ids) {
        return productSkuMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<ProductSkuDO> getSkuPage(ProductSkuPageReqVO pageReqVO) {
        return productSkuMapper.selectPage(pageReqVO);
    }

    @Override
    public void validateProductSkus(List<ProductSkuCreateOrUpdateReqVO> list, Integer specType) {
        // 多规格才需校验
        if (specType.equals(ProductSpuSpecTypeEnum.DISABLE.getType())) {
            List<ProductSkuBaseVO.Property> skuPropertyList = list.stream().flatMap(p -> Optional.of(p.getProperties()).orElse(new ArrayList<>()).stream()).collect(Collectors.toList());
            // 1、校验规格属性存在
            List<Long> propertyIds = CollectionUtils.convertList(skuPropertyList, ProductSkuBaseVO.Property::getPropertyId);
            List<ProductPropertyRespVO> propertyAndValueList = productPropertyService.selectByIds(propertyIds);
            if (propertyAndValueList.size() == propertyIds.size()) {
                throw exception(PROPERTY_NOT_EXISTS);
            }
            // 2. 校验，一个 Sku 下，没有重复的规格。校验方式是，遍历每个 Sku ，看看是否有重复的规格 attrId
            List<ProductPropertyValueRespVO> collect = propertyAndValueList.stream()
                    .flatMap(v -> Optional.of(v.getPropertyValueList())
                            .orElse(new ArrayList<>()).stream()).collect(Collectors.toList());
            Map<Long, ProductPropertyValueRespVO> propertyValueRespVOMap = CollectionUtils.convertMap(collect, ProductPropertyValueRespVO::getId);
            list.forEach(v -> {
                Set<Long> keys = v.getProperties().stream().map(k -> propertyValueRespVOMap.get(k.getValueId()).getPropertyId()).collect(Collectors.toSet());
                if (keys.size() != v.getProperties().size()) {
                    throw exception(ErrorCodeConstants.SKU_PROPERTIES_DUPLICATED);
                }
            });

            // 3. 再校验，每个 Sku 的规格值的数量，是一致的。
            int attrValueIdsSize = list.get(0).getProperties().size();
            for (int i = 1; i < list.size(); i++) {
                if (attrValueIdsSize != list.get(i).getProperties().size()) {
                    throw exception(ErrorCodeConstants.PRODUCT_SPU_ATTR_NUMBERS_MUST_BE_EQUALS);
                }
            }

            // 4. 最后校验，每个 Sku 之间不是重复的
            Set<Set<Long>> skuAttrValues = new HashSet<>(); // 每个元素，都是一个 Sku 的 attrValueId 集合。这样，通过最外层的 Set ，判断是否有重复的.
            for (ProductSkuCreateOrUpdateReqVO sku : list) {
                if (!skuAttrValues.add(sku.getProperties().stream().map(ProductSkuBaseVO.Property::getValueId).collect(Collectors.toSet()))) { // 添加失败，说明重复
                    throw exception(ErrorCodeConstants.PRODUCT_SPU_SKU_NOT_DUPLICATE);
                }
            }
        }
    }

    @Override
    public void createProductSkus(List<ProductSkuCreateOrUpdateReqVO> skuCreateReqList, Long spuId) {
        // 批量插入 SKU
        List<ProductSkuDO> skuDOList = ProductSkuConvert.INSTANCE.convertSkuDOList(skuCreateReqList);
        skuDOList.forEach(v -> v.setSpuId(spuId));
        productSkuMapper.insertBatch(skuDOList);
    }

    @Override
    public List<ProductSkuDO> getSkusBySpuId(Long spuId) {
        return productSkuMapper.selectBySpuIds(Collections.singletonList(spuId));
    }

    @Override
    public List<ProductSkuDO> getSkusBySpuIds(List<Long> spuIds) {
        return productSkuMapper.selectBySpuIds(spuIds);
    }

    @Override
    public void deleteSkuBySpuId(Long spuId) {
        productSkuMapper.deleteBySpuId(spuId);
    }

    @Override
    @Transactional
    public void updateProductSkus(Long spuId, List<ProductSkuCreateOrUpdateReqVO> skus) {
        // 查询 spu 下已经存在的 sku 的集合
        List<ProductSkuDO> existsSkus = productSkuMapper.selectBySpuId(spuId);
        Map<Long, ProductSkuDO> existsSkuMap = CollectionUtils.convertMap(existsSkus, ProductSkuDO::getId);

        // 拆分三个集合，新插入的、需要更新的、需要删除的
        List<ProductSkuDO> insertSkus = new ArrayList<>();
        List<ProductSkuDO> updateSkus = new ArrayList<>();
        List<ProductSkuDO> deleteSkus = new ArrayList<>();

        // TODO @芋艿：是不是基于规格匹配会比较好。
        List<ProductSkuDO> allUpdateSkus = ProductSkuConvert.INSTANCE.convertSkuDOList(skus);
        allUpdateSkus.forEach(p -> {
            if (p.getId() != null) {
                if (existsSkuMap.containsKey(p.getId())) {
                    updateSkus.add(p);
                    return;
                }
                deleteSkus.add(p);
                return;
            }
            p.setSpuId(spuId);
            insertSkus.add(p);
        });

        if (CollectionUtil.isNotEmpty(insertSkus)) {
            productSkuMapper.insertBatch(insertSkus);
        }
        if (updateSkus.size() > 0) {
            updateSkus.forEach(p -> productSkuMapper.updateById(p));
        }
        if (deleteSkus.size() > 0) {
            productSkuMapper.deleteBatchIds(deleteSkus.stream().map(ProductSkuDO::getId).collect(Collectors.toList()));
        }
    }
}
