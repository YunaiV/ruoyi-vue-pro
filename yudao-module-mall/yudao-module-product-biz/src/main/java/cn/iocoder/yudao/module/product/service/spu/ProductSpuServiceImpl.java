package cn.iocoder.yudao.module.product.service.spu;

import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuCreateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuRespVO;
import cn.iocoder.yudao.module.product.convert.sku.ProductSkuConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.service.category.CategoryService;
import cn.iocoder.yudao.module.product.service.sku.ProductSkuService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

import cn.iocoder.yudao.module.product.controller.admin.spu.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.product.convert.spu.ProductSpuConvert;
import cn.iocoder.yudao.module.product.dal.mysql.spu.ProductSpuMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.*;

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
    public void updateSpu(SpuUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateSpuExists(updateReqVO.getId());
        // 更新
        ProductSpuDO updateObj = ProductSpuConvert.INSTANCE.convert(updateReqVO);
        ProductSpuMapper.updateById(updateObj);
    }

    @Override
    public void deleteSpu(Long id) {
        // 校验存在
        this.validateSpuExists(id);
        // 删除
        ProductSpuMapper.deleteById(id);
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
        List<ProductSkuRespVO> skuReqs = ProductSkuConvert.INSTANCE.convertList( productSkuService.getSkusBySpuId(id));
        spuVO.setProductSkuRespVOS(skuReqs);
        return spuVO;
    }

    @Override
    public List<ProductSpuDO> getSpuList(Collection<Long> ids) {
        return ProductSpuMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<ProductSpuDO> getSpuPage(SpuPageReqVO pageReqVO) {
        return ProductSpuMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ProductSpuDO> getSpuList(SpuExportReqVO exportReqVO) {
        return ProductSpuMapper.selectList(exportReqVO);
    }

}
