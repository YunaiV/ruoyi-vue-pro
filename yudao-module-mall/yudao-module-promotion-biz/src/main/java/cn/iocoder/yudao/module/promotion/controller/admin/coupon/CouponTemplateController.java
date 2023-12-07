package cn.iocoder.yudao.module.promotion.controller.admin.coupon;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.coupon.vo.template.*;
import cn.iocoder.yudao.module.promotion.convert.coupon.CouponTemplateConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.coupon.CouponTemplateDO;
import cn.iocoder.yudao.module.promotion.service.coupon.CouponTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 优惠劵模板")
@RestController
@RequestMapping("/promotion/coupon-template")
@Validated
public class CouponTemplateController {

    @Resource
    private CouponTemplateService couponTemplateService;

    @PostMapping("/create")
    @Operation(summary = "创建优惠劵模板")
    @PreAuthorize("@ss.hasPermission('promotion:coupon-template:create')")
    public CommonResult<Long> createCouponTemplate(@Valid @RequestBody CouponTemplateCreateReqVO createReqVO) {
        return success(couponTemplateService.createCouponTemplate(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新优惠劵模板")
    @PreAuthorize("@ss.hasPermission('promotion:coupon-template:update')")
    public CommonResult<Boolean> updateCouponTemplate(@Valid @RequestBody CouponTemplateUpdateReqVO updateReqVO) {
        couponTemplateService.updateCouponTemplate(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新优惠劵模板状态")
    @PreAuthorize("@ss.hasPermission('promotion:coupon-template:update')")
    public CommonResult<Boolean> updateCouponTemplateStatus(@Valid @RequestBody CouponTemplateUpdateStatusReqVO reqVO) {
        couponTemplateService.updateCouponTemplateStatus(reqVO.getId(), reqVO.getStatus());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除优惠劵模板")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('promotion:coupon-template:delete')")
    public CommonResult<Boolean> deleteCouponTemplate(@RequestParam("id") Long id) {
        couponTemplateService.deleteCouponTemplate(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得优惠劵模板")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('promotion:coupon-template:query')")
    public CommonResult<CouponTemplateRespVO> getCouponTemplate(@RequestParam("id") Long id) {
        CouponTemplateDO couponTemplate = couponTemplateService.getCouponTemplate(id);
        return success(CouponTemplateConvert.INSTANCE.convert(couponTemplate));
    }

    @GetMapping("/page")
    @Operation(summary = "获得优惠劵模板分页")
    @PreAuthorize("@ss.hasPermission('promotion:coupon-template:query')")
    public CommonResult<PageResult<CouponTemplateRespVO>> getCouponTemplatePage(@Valid CouponTemplatePageReqVO pageVO) {
        PageResult<CouponTemplateDO> pageResult = couponTemplateService.getCouponTemplatePage(pageVO);
        return success(CouponTemplateConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/list")
    @Operation(summary = "获得优惠劵模板列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('promotion:coupon-template:query')")
    public CommonResult<List<CouponTemplateRespVO>> getCouponTemplateList(@RequestParam("ids") Collection<Long> ids) {
        List<CouponTemplateDO> list = couponTemplateService.getCouponTemplateList(ids);
        return success(CouponTemplateConvert.INSTANCE.convertList(list));
    }

}
