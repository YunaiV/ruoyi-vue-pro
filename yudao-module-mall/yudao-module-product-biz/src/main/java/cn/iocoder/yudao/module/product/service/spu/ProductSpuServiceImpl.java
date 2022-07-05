package cn.iocoder.yudao.module.product.service.spu;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuCreateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuRespVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.*;
import cn.iocoder.yudao.module.product.convert.sku.ProductSkuConvert;
import cn.iocoder.yudao.module.product.convert.spu.ProductSpuConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.category.CategoryDO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.dal.mysql.spu.ProductSpuMapper;
import cn.iocoder.yudao.module.product.service.category.CategoryService;
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
 * 商品spu Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ProductSpuServiceImpl implements ProductSpuService {

    @Resource
    private ProductSpuMapper ProductSpuMapper;

    @Resource
    private CategoryService categoryService;

    @Resource
    private ProductSkuService productSkuService;

    @Override
    @Transactional
    public Long createSpu(ProductSpuCreateReqVO createReqVO) {
        // 校验分类
        categoryService.validatedCategoryById(createReqVO.getCategoryId());
        // 校验SKU
        List<ProductSkuCreateReqVO> skuCreateReqList = createReqVO.getSkus();
        productSkuService.validateSkus(skuCreateReqList);
        // 插入SPU
        ProductSpuDO spu = ProductSpuConvert.INSTANCE.convert(createReqVO);
        ProductSpuMapper.insert(spu);
        // sku关联SPU属性
        skuCreateReqList.forEach(p -> {
            p.setSpuId(spu.getId());
        });
        List<ProductSkuDO> skuDOList = ProductSkuConvert.INSTANCE.convertSkuDOList(skuCreateReqList);
        // 批量插入sku
        productSkuService.createSkus(skuDOList);
        // 返回
        return spu.getId();
    }

    @Override
    @Transactional
    public void updateSpu(SpuUpdateReqVO updateReqVO) {
        // 校验 spu 是否存在
        this.validateSpuExists(updateReqVO.getId());
        // 校验分类
        categoryService.validatedCategoryById(updateReqVO.getCategoryId());
        // 校验SKU
        List<ProductSkuCreateReqVO> skuCreateReqList = updateReqVO.getSkus();
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
            // 组合分类
            if (null != spuVO.getCategoryId()) {
                LinkedList<Long> categoryArray = new LinkedList<>();
                Long parentId = spuVO.getCategoryId();
                categoryArray.addFirst(parentId);
                while (parentId != 0) {
                    parentId = categoryService.getCategory(parentId).getParentId();
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
        Map<Long, List<ProductSkuRespVO>> skuMap = skus.stream().collect(Collectors.groupingBy(ProductSkuRespVO::getSpuId));
        // 将 spu 和 sku 进行组装
        spuVOs.getList().forEach(p -> p.setSkus(skuMap.get(p.getId())));
        return spuVOs;
    }

    @Override
    public List<ProductSpuDO> getSpuList(SpuExportReqVO exportReqVO) {
        return ProductSpuMapper.selectList(exportReqVO);
    }

}
