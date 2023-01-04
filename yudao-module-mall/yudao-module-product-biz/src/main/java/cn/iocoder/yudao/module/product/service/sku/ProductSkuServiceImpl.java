package cn.iocoder.yudao.module.product.service.sku;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuUpdateStockReqDTO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuBaseVO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuCreateOrUpdateReqVO;
import cn.iocoder.yudao.module.product.convert.sku.ProductSkuConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.property.ProductPropertyDO;
import cn.iocoder.yudao.module.product.dal.dataobject.property.ProductPropertyValueDO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.mysql.sku.ProductSkuMapper;
import cn.iocoder.yudao.module.product.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuSpecTypeEnum;
import cn.iocoder.yudao.module.product.service.property.ProductPropertyService;
import cn.iocoder.yudao.module.product.service.property.ProductPropertyValueService;
import cn.iocoder.yudao.module.product.service.spu.ProductSpuService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.*;

/**
 * 商品 SKU Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ProductSkuServiceImpl implements ProductSkuService {

    @Resource
    private ProductSkuMapper productSkuMapper;

    @Resource
    @Lazy // 循环依赖，避免报错
    private ProductSpuService productSpuService;
    @Resource
    private ProductPropertyService productPropertyService;
    @Resource
    private ProductPropertyValueService productPropertyValueService;

    @Override
    public void deleteSku(Long id) {
        // 校验存在
        validateSkuExists(id);
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
    public List<ProductSkuDO> getSkuList() {
        return productSkuMapper.selectList();
    }

    @Override
    public List<ProductSkuDO> getSkuList(Collection<Long> ids) {
        return productSkuMapper.selectBatchIds(ids);
    }

    @Override
    public void validateSkuList(List<ProductSkuCreateOrUpdateReqVO> skus, Integer specType) {
        // 非多规格，不需要校验
        if (ObjectUtil.notEqual(specType, ProductSpuSpecTypeEnum.DISABLE.getType())) {
            return;
        }

        // 1、校验属性项存在
        Set<Long> propertyIds = skus.stream().filter(p -> p.getProperties() != null)
                .flatMap(p -> p.getProperties().stream()) // 遍历多个 Property 属性
                .map(ProductSkuBaseVO.Property::getPropertyId) // 将每个 Property 转换成对应的 propertyId，最后形成集合
                .collect(Collectors.toSet());
        List<ProductPropertyDO> propertyList = productPropertyService.getPropertyList(propertyIds);
        if (propertyList.size() != propertyIds.size()) {
            throw exception(PROPERTY_NOT_EXISTS);
        }

        // 2. 校验，一个 SKU 下，没有重复的属性。校验方式是，遍历每个 SKU ，看看是否有重复的属性 propertyId
        Map<Long, ProductPropertyValueDO> propertyValueMap = convertMap(productPropertyValueService.getPropertyValueListByPropertyId(propertyIds), ProductPropertyValueDO::getId);
        skus.forEach(sku -> {
            Set<Long> skuPropertyIds = convertSet(sku.getProperties(), propertyItem -> propertyValueMap.get(propertyItem.getValueId()).getPropertyId());
            if (skuPropertyIds.size() != sku.getProperties().size()) {
                throw exception(SKU_PROPERTIES_DUPLICATED);
            }
        });

        // 3. 再校验，每个 Sku 的属性值的数量，是一致的。
        int attrValueIdsSize = skus.get(0).getProperties().size();
        for (int i = 1; i < skus.size(); i++) {
            if (attrValueIdsSize != skus.get(i).getProperties().size()) {
                throw exception(ErrorCodeConstants.SPU_ATTR_NUMBERS_MUST_BE_EQUALS);
            }
        }

        // 4. 最后校验，每个 Sku 之间不是重复的
        Set<Set<Long>> skuAttrValues = new HashSet<>(); // 每个元素，都是一个 Sku 的 attrValueId 集合。这样，通过最外层的 Set ，判断是否有重复的.
        for (ProductSkuCreateOrUpdateReqVO sku : skus) {
            if (!skuAttrValues.add(convertSet(sku.getProperties(), ProductSkuBaseVO.Property::getValueId))) { // 添加失败，说明重复
                throw exception(ErrorCodeConstants.SPU_SKU_NOT_DUPLICATE);
            }
        }
    }

    @Override
    public void createSkuList(Long spuId, String spuName, List<ProductSkuCreateOrUpdateReqVO> skuCreateReqList) {
        productSkuMapper.insertBatch(ProductSkuConvert.INSTANCE.convertList06(skuCreateReqList, spuId, spuName));
    }

    @Override
    public List<ProductSkuDO> getSkuListBySpuId(Long spuId) {
        return productSkuMapper.selectListBySpuId(spuId);
    }

    @Override
    public List<ProductSkuDO> getSkuListBySpuIdAndStatus(Long spuId, Integer status) {
        return productSkuMapper.selectListBySpuIdAndStatus(spuId, status);
    }

    @Override
    public List<ProductSkuDO> getSkuListBySpuId(List<Long> spuIds) {
        return productSkuMapper.selectListBySpuId(spuIds);
    }

    @Override
    public void deleteSkuBySpuId(Long spuId) {
        productSkuMapper.deleteBySpuId(spuId);
    }

    @Override
    public List<ProductSkuDO> getSkuListByAlarmStock() {
        return productSkuMapper.selectListByAlarmStock();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSkuList(Long spuId, String spuName, List<ProductSkuCreateOrUpdateReqVO> skus) {
        // 构建属性与 SKU 的映射关系;
        Map<String, Long> existsSkuMap = convertMap(productSkuMapper.selectListBySpuId(spuId),
                ProductSkuConvert.INSTANCE::buildPropertyKey, ProductSkuDO::getId);

        // 拆分三个集合，新插入的、需要更新的、需要删除的
        List<ProductSkuDO> insertSkus = new ArrayList<>();
        List<ProductSkuDO> updateSkus = new ArrayList<>();
        List<ProductSkuDO> allUpdateSkus = ProductSkuConvert.INSTANCE.convertList06(skus, null, spuName);
        allUpdateSkus.forEach(sku -> {
            String propertiesKey = ProductSkuConvert.INSTANCE.buildPropertyKey(sku);
            // 1、找得到的，进行更新
            Long existsSkuId = existsSkuMap.remove(propertiesKey);
            if (existsSkuId != null) {
                sku.setId(existsSkuId);
                updateSkus.add(sku);
                return;
            }
            // 2、找不到，进行插入
            sku.setSpuId(spuId);
            insertSkus.add(sku);
        });

        // 执行最终的批量操作
        if (CollUtil.isNotEmpty(insertSkus)) {
            productSkuMapper.insertBatch(insertSkus);
        }
        if (CollUtil.isNotEmpty(updateSkus)) {
            updateSkus.forEach(sku -> productSkuMapper.updateById(sku));
        }
        if (CollUtil.isNotEmpty(existsSkuMap)) {
            productSkuMapper.deleteBatchIds(existsSkuMap.values());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSkuStock(ProductSkuUpdateStockReqDTO updateStockReqDTO) {
        // 更新 SKU 库存
        updateStockReqDTO.getItems().forEach(item -> {
            if (item.getIncrCount() > 0) {
                productSkuMapper.updateStockIncr(item.getId(), item.getIncrCount());
            } else if (item.getIncrCount() < 0) {
                int updateStockIncr = productSkuMapper.updateStockDecr(item.getId(), item.getIncrCount());
                if (updateStockIncr == 0) {
                    throw exception(SKU_STOCK_NOT_ENOUGH);
                }
            }
        });

        // 更新 SPU 库存
        List<ProductSkuDO> skus = productSkuMapper.selectBatchIds(
                convertSet(updateStockReqDTO.getItems(), ProductSkuUpdateStockReqDTO.Item::getId));
        Map<Long, Integer> spuStockIncrCounts = ProductSkuConvert.INSTANCE.convertSpuStockMap(
                updateStockReqDTO.getItems(), skus);
        productSpuService.updateSpuStock(spuStockIncrCounts);
    }

}
