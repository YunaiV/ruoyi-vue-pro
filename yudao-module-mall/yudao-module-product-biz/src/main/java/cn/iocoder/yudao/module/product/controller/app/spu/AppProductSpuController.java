package cn.iocoder.yudao.module.product.controller.app.spu;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.app.spu.vo.AppSpuDetailRespVO;
import cn.iocoder.yudao.module.product.controller.app.spu.vo.AppSpuPageReqVO;
import cn.iocoder.yudao.module.product.controller.app.spu.vo.AppSpuPageRespVO;
import cn.iocoder.yudao.module.product.convert.sku.ProductSkuConvert;
import cn.iocoder.yudao.module.product.convert.spu.ProductSpuConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import cn.iocoder.yudao.module.product.service.property.ProductPropertyValueService;
import cn.iocoder.yudao.module.product.service.property.bo.ProductPropertyValueDetailRespBO;
import cn.iocoder.yudao.module.product.service.sku.ProductSkuService;
import cn.iocoder.yudao.module.product.service.spu.ProductSpuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SPU_NOT_ENABLE;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SPU_NOT_EXISTS;

@Api(tags = "用户 APP - 商品 SPU")
@RestController
@RequestMapping("/product/spu")
@Validated
public class AppProductSpuController {

    @Resource
    private ProductSpuService productSpuService;
    @Resource
    private ProductSkuService productSkuService;
    @Resource
    private ProductPropertyValueService productPropertyValueService;

    @GetMapping("/page")
    @ApiOperation("获得商品spu分页")
    public CommonResult<PageResult<AppSpuPageRespVO>> getSpuPage(@Valid AppSpuPageReqVO pageVO) {
        return success(productSpuService.getSpuPage(pageVO));
    }

    @GetMapping("/get-detail")
    @ApiOperation("获得商品 SPU 明细")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    public CommonResult<AppSpuDetailRespVO> getSpuDetail(@RequestParam("id") Long id) {
        // 获得商品 SPU
        ProductSpuDO spu = productSpuService.getSpu(id);
        if (spu == null) {
            throw exception(SPU_NOT_EXISTS);
        }
        if (!ProductSpuStatusEnum.isEnable(spu.getStatus())) {
            throw exception(SPU_NOT_ENABLE);
        }

        // 查询商品 SKU
        List<ProductSkuDO> skus = productSkuService.getSkuListBySpuIdAndStatus(spu.getId(),
                CommonStatusEnum.ENABLE.getStatus());
        // 查询商品属性
        List<ProductPropertyValueDetailRespBO> propertyValues = productPropertyValueService
                .getPropertyValueDetailList(ProductSkuConvert.INSTANCE.convertPropertyValueIds(skus));
        // 拼接
        return success(ProductSpuConvert.INSTANCE.convert(spu, skus, propertyValues));
    }

}
