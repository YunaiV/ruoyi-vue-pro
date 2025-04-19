package cn.iocoder.yudao.module.oms.controller.admin.product;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.oms.controller.admin.product.vo.OmsShopProductPageReqVO;
import cn.iocoder.yudao.module.oms.controller.admin.product.vo.OmsShopProductRespVO;
import cn.iocoder.yudao.module.oms.controller.admin.product.vo.OmsShopProductSaveReqVO;
import cn.iocoder.yudao.module.oms.service.OmsShopProductService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - OMS 店铺产品")
@RestController
@RequestMapping("/oms/shop-product")
@Validated
public class OmsShopProductController {

    @Resource
    OmsShopProductService omsShopProductService;

    @Resource
    DeptApi deptApi;

    @GetMapping("/page")
    @Operation(summary = "获得OMS 店铺产品分页")
    @PreAuthorize("@ss.hasPermission('oms:shop-product:query')")
    public CommonResult<PageResult<OmsShopProductRespVO>> getShopProductPage(@Valid OmsShopProductPageReqVO pageReqVO) {
        return omsShopProductService.getShopProductPageVO(pageReqVO);

    }


    @GetMapping("/get")
    @Operation(summary = "获得OMS 店铺产品")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oms:shop-product:query')")
    public CommonResult<OmsShopProductRespVO> getShopProduct(@RequestParam("id") Long id) {
        OmsShopProductRespVO respVO = omsShopProductService.getShopProductVoModel(id);
        if (respVO.getDeptId() != null) {
            DeptRespDTO deptDTO = deptApi.getDept(respVO.getDeptId());
            if (deptDTO != null) {
                respVO.setDeptName(deptDTO.getName());
            }
        }

        return success(respVO);
    }

    @PostMapping("/create")
    @Operation(summary = "创建OMS 店铺产品")
    @PreAuthorize("@ss.hasPermission('oms:shop-product:create')")
    public CommonResult<Long> createShopProduct(@Valid @RequestBody OmsShopProductSaveReqVO createReqVO) {

        return success(omsShopProductService.createShopProductWithItems(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新OMS 店铺产品")
    @PreAuthorize("@ss.hasPermission('oms:shop-product:update')")
    public CommonResult<Boolean> updateShopProduct(@Valid @RequestBody OmsShopProductSaveReqVO updateReqVO) {
        omsShopProductService.updateShopProductWithItems(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除ERP 店铺产品")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('oms:shop-product:delete')")
    public CommonResult<Boolean> deleteShopProduct(@RequestParam("id") Long id) {
        omsShopProductService.deleteShopProduct(id);
        return success(true);
    }
}
