package cn.iocoder.yudao.module.oms.controller.admin.shop;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.validation.ValidationGroup;
import cn.iocoder.yudao.module.oms.api.enums.shop.ShopTypeEnum;
import cn.iocoder.yudao.module.oms.controller.admin.shop.vo.OmsShopPageReqVO;
import cn.iocoder.yudao.module.oms.controller.admin.shop.vo.OmsShopRespVO;
import cn.iocoder.yudao.module.oms.controller.admin.shop.vo.OmsShopSaveReqVO;
import cn.iocoder.yudao.module.oms.service.OmsShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.module.oms.api.enums.OmsErrorCodeConstants.OMS_SHOP_CAN_NOT_CREATE_ONLINE_SHOP;

@Tag(name = "管理后台 - OMS 平台店铺")
@RestController
@RequestMapping("/oms/platform-shop")
@Validated
public class OmsShopController {
    @Resource
    private OmsShopService omsShopService;


    @GetMapping("/list")
    @Operation(summary = "获得OMS 店铺列表")
    @PreAuthorize("@ss.hasPermission('oms:shop:query')")
    public CommonResult<List<OmsShopRespVO>> getShopList(@Valid OmsShopPageReqVO pageReqVO) {
        List<OmsShopRespVO> pageResult = omsShopService.getShopList(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/page")
    @Operation(summary = "获得OMS 店铺分页")
    @PreAuthorize("@ss.hasPermission('oms:shop:query')")
    public CommonResult<PageResult<OmsShopRespVO>> getShopPage(@Valid OmsShopPageReqVO pageReqVO) {
        PageResult<OmsShopRespVO> pageResult = omsShopService.getShopPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/get")
    @Operation(summary = "获得oms 店铺")
    @Parameter(name = "id", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oms:shop:query')")
    public CommonResult<OmsShopRespVO> getShop(@RequestParam("id") Long id) {
        OmsShopRespVO omsShopRespVO = omsShopService.getShopById(id);
        return success(omsShopRespVO);
    }

    @PutMapping("/update")
    @Operation(summary = "更新OMS 店铺")
    @PreAuthorize("@ss.hasPermission('oms:shop:update')")
    public CommonResult<Boolean> updateShop(@Validated(value = {ValidationGroup.update.class}) @RequestBody OmsShopSaveReqVO updateReqVO) {
        return success(omsShopService.updateShopNameAndCode(updateReqVO.getId(), updateReqVO.getName(), updateReqVO.getCode()));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除OMS 店铺")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('oms:shop:delete')")
    public CommonResult<Boolean> deleteShop(@RequestParam("id") Long id) {
        omsShopService.deleteShop(id);
        return success(true);
    }

    @PostMapping("/create")
    @Operation(summary = "创建OMS 店铺")
    @PreAuthorize("@ss.hasPermission('oms:shop:create')")
    public CommonResult<Long> createShop(@Validated(value = {ValidationGroup.create.class}) @RequestBody OmsShopSaveReqVO createReqVO) {
        // 不允许用户创建在线店铺
        if (createReqVO.getType() == ShopTypeEnum.ONLINE.getType()) {
            return error(OMS_SHOP_CAN_NOT_CREATE_ONLINE_SHOP);
        }
        return success(omsShopService.createShop(createReqVO));
    }
}
