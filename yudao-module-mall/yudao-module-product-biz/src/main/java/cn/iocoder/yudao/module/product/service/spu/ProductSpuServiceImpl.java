package cn.iocoder.yudao.module.product.service.spu;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.module.product.controller.admin.category.vo.ProductCategoryListReqVO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuCreateOrUpdateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuCreateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuPageReqVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuUpdateReqVO;
import cn.iocoder.yudao.module.product.controller.app.spu.vo.AppProductSpuPageReqVO;
import cn.iocoder.yudao.module.product.convert.spu.ProductSpuConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.category.ProductCategoryDO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.dal.mysql.spu.ProductSpuMapper;
import cn.iocoder.yudao.module.product.service.brand.ProductBrandService;
import cn.iocoder.yudao.module.product.service.category.ProductCategoryService;
import cn.iocoder.yudao.module.product.service.sku.ProductSkuService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SPU_NOT_EXISTS;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SPU_SAVE_FAIL_CATEGORY_LEVEL_ERROR;

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
    @Lazy // 循环依赖，避免报错
    private ProductSkuService productSkuService;
    @Resource
    private ProductBrandService brandService;
    @Resource
    private ProductCategoryService categoryService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSpu(ProductSpuCreateReqVO createReqVO) {
        // 校验分类
        validateCategory(createReqVO.getCategoryId());
        // 校验品牌
        brandService.validateProductBrand(createReqVO.getBrandId());
        // 校验SKU
        List<ProductSkuCreateOrUpdateReqVO> skuSaveReqList = createReqVO.getSkus();
        productSkuService.validateSkuList(skuSaveReqList, createReqVO.getSpecType());

        // 插入 SPU
        ProductSpuDO spu = ProductSpuConvert.INSTANCE.convert(createReqVO);
        initSpuFromSkus(spu, skuSaveReqList);
        productSpuMapper.insert(spu);
        // 插入 SKU
        productSkuService.createSkuList(spu.getId(), spu.getName(), skuSaveReqList);
        // 返回
        return spu.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSpu(ProductSpuUpdateReqVO updateReqVO) {
        // 校验 SPU 是否存在
        validateSpuExists(updateReqVO.getId());
        // 校验分类
        validateCategory(updateReqVO.getCategoryId());
        // 校验品牌
        brandService.validateProductBrand(updateReqVO.getBrandId());
        // 校验SKU
        List<ProductSkuCreateOrUpdateReqVO> skuSaveReqList = updateReqVO.getSkus();
        productSkuService.validateSkuList(skuSaveReqList, updateReqVO.getSpecType());

        // 更新 SPU
        ProductSpuDO updateObj = ProductSpuConvert.INSTANCE.convert(updateReqVO);
        initSpuFromSkus(updateObj, skuSaveReqList);
        productSpuMapper.updateById(updateObj);
        // 批量更新 SKU
        productSkuService.updateSkuList(updateObj.getId(), updateObj.getName(), updateReqVO.getSkus());
    }

    /**
     * 基于 SKU 的信息，初始化 SPU 的信息
     * 主要是计数相关的字段，例如说市场价、最大最小价、库存等等
     *
     * @param spu 商品 SPU
     * @param skus 商品 SKU 数组
     */
    private void initSpuFromSkus(ProductSpuDO spu, List<ProductSkuCreateOrUpdateReqVO> skus) {
        spu.setMarketPrice(getMaxValue(skus, ProductSkuCreateOrUpdateReqVO::getMarketPrice));
        // TODO ProductSpuDO中已没有相关属性
        //spu.setMaxPrice(getMaxValue(skus, ProductSkuCreateOrUpdateReqVO::getPrice));
        //spu.setMinPrice(getMinValue(skus, ProductSkuCreateOrUpdateReqVO::getPrice));
        //spu.setTotalStock(getSumValue(skus, ProductSkuCreateOrUpdateReqVO::getStock, Integer::sum));
    }

    /**
     * 校验商品分类是否合法
     *
     * @param id 商品分类编号
     */
    private void validateCategory(Long id) {
        categoryService.validateCategory(id);
        // 校验层级
        if (categoryService.getCategoryLevel(id) != 3) {
            throw exception(SPU_SAVE_FAIL_CATEGORY_LEVEL_ERROR);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
    public ProductSpuDO getSpu(Long id) {
        return productSpuMapper.selectById(id);
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
    public PageResult<ProductSpuDO> getSpuPage(ProductSpuPageReqVO pageReqVO) {
        // 库存告警的 SPU 编号的集合
        Set<Long> alarmStockSpuIds = null;
        if (Boolean.TRUE.equals(pageReqVO.getAlarmStock())) {
            alarmStockSpuIds = CollectionUtils.convertSet(productSkuService.getSkuListByAlarmStock(), ProductSkuDO::getSpuId);
            if (CollUtil.isEmpty(alarmStockSpuIds)) {
                return PageResult.empty();
            }
        }
        // 分页查询
        return productSpuMapper.selectPage(pageReqVO, alarmStockSpuIds);
    }

    @Override
    public PageResult<ProductSpuDO> getSpuPage(AppProductSpuPageReqVO pageReqVO) {
        // 查找时，如果查找某个分类编号，则包含它的子分类。因为顶级分类不包含商品
        Set<Long> categoryIds = new HashSet<>();
        if (pageReqVO.getCategoryId() != null && pageReqVO.getCategoryId() > 0) {
            categoryIds.add(pageReqVO.getCategoryId());
            List<ProductCategoryDO> categoryChildren = categoryService.getEnableCategoryList(new ProductCategoryListReqVO()
                    .setParentId(pageReqVO.getCategoryId()).setStatus(CommonStatusEnum.ENABLE.getStatus()));
            categoryIds.addAll(CollectionUtils.convertList(categoryChildren, ProductCategoryDO::getId));
        }
        // 分页查询
        return productSpuMapper.selectPage(pageReqVO, categoryIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSpuStock(Map<Long, Integer> stockIncrCounts) {
        stockIncrCounts.forEach((id, incCount) -> productSpuMapper.updateStock(id, incCount));
    }

}
