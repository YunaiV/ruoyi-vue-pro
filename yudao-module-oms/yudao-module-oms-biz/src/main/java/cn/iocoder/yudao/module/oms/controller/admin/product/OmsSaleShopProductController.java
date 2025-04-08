package cn.iocoder.yudao.module.oms.controller.admin.product;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.oms.controller.admin.product.vo.OmsShopProductPageReqVO;
import cn.iocoder.yudao.module.oms.controller.admin.product.vo.OmsShopProductRespVO;
import cn.iocoder.yudao.module.oms.service.OmsShopProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理后台 - OMS 店铺产品")
@RestController
@RequestMapping("/oms/shop-product")
@Validated
public class OmsSaleShopProductController {

    @Resource
    OmsShopProductService omsShopProductService;

    @GetMapping("/page")
    @Operation(summary = "获得OMS 店铺产品分页")
    @PreAuthorize("@ss.hasPermission('oms:shop-product:query')")
    public CommonResult<PageResult<OmsShopProductRespVO>> getShopProductPage(@Valid OmsShopProductPageReqVO pageReqVO) {
        return omsShopProductService.getShopProductPageVO(pageReqVO);

    }
}
