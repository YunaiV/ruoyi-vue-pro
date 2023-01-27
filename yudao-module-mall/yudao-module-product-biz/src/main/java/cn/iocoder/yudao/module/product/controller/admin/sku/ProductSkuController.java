package cn.iocoder.yudao.module.product.controller.admin.sku;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuOptionRespVO;
import cn.iocoder.yudao.module.product.convert.sku.ProductSkuConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.service.sku.ProductSkuService;
import cn.iocoder.yudao.module.product.service.spu.ProductSpuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Api(tags = "管理后台 - 商品 sku")
@RestController
@RequestMapping("/product/sku")
@Validated
public class ProductSkuController {

    @Resource
    private ProductSkuService productSkuService;
    @Resource
    private ProductSpuService productSpuService;

    @GetMapping("/get-option-list")
    @ApiOperation("获得商品 SKU 选项的列表")
//    @PreAuthorize("@ss.hasPermission('product:sku:query')")
    public CommonResult<List<ProductSkuOptionRespVO>> getSkuOptionList() {
        // 获得 SKU 列表
        List<ProductSkuDO> skus = productSkuService.getSkuList();
        if (CollUtil.isEmpty(skus)) {
            return success(Collections.emptyList());
        }

        // 获得对应的 SPU 映射
        Map<Long, ProductSpuDO> spuMap = productSpuService.getSpuMap(convertSet(skus, ProductSkuDO::getSpuId));
        // 转换为返回结果
        List<ProductSkuOptionRespVO> skuVOs = ProductSkuConvert.INSTANCE.convertList05(skus);
        skuVOs.forEach(sku -> MapUtils.findAndThen(spuMap, sku.getSpuId(),
                spu -> sku.setSpuId(spu.getId()).setSpuName(spu.getName())));
        return success(skuVOs);
    }

}
