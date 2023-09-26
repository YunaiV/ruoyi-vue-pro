package cn.iocoder.yudao.module.promotion.controller.app.coupon;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.module.promotion.controller.app.coupon.vo.coupon.AppCouponMatchReqVO;
import cn.iocoder.yudao.module.promotion.controller.app.coupon.vo.coupon.AppCouponMatchRespVO;
import cn.iocoder.yudao.module.promotion.controller.app.coupon.vo.coupon.AppCouponPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.app.coupon.vo.coupon.AppCouponRespVO;
import cn.iocoder.yudao.module.promotion.controller.app.coupon.vo.template.AppCouponTemplatePageReqVO;
import cn.iocoder.yudao.module.promotion.convert.coupon.CouponConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.coupon.CouponDO;
import cn.iocoder.yudao.module.promotion.service.coupon.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 App - 优惠劵")
@RestController
@RequestMapping("/promotion/coupon")
@Validated
public class AppCouponController {

    @Resource
    private CouponService couponService;

    // TODO 芋艿：待实现
    @PostMapping("/take")
    @Operation(summary = "领取优惠劵")
    public CommonResult<Long> takeCoupon(@RequestBody AppCouponTemplatePageReqVO pageReqVO) {
        return success(1L);
    }

    @GetMapping("/match-list")
    @Operation(summary = "获得匹配指定商品的优惠劵列表")
    public CommonResult<List<AppCouponMatchRespVO>> getMatchCouponList(AppCouponMatchReqVO matchReqVO) {
        // todo: 优惠金额倒序
        return success(CouponConvert.INSTANCE.convertList(couponService.getMatchCouponList(getLoginUserId(), matchReqVO)));
    }

    @GetMapping("/page")
    @Operation(summary = "优惠劵列表", description = "我的优惠劵")
    public CommonResult<PageResult<AppCouponRespVO>> takeCoupon(AppCouponPageReqVO pageReqVO) {
        PageResult<CouponDO> pageResult = couponService.getCouponPage(
                CouponConvert.INSTANCE.convert(pageReqVO, Collections.singleton(getLoginUserId())));
        return success(CouponConvert.INSTANCE.convertAppPage(pageResult));
    }

    @GetMapping(value = "/get-unused-count")
    @Operation(summary = "获得未使用的优惠劵数量")
    @PreAuthenticated
    public CommonResult<Long> getUnusedCouponCount() {
        return success(couponService.getUnusedCouponCount(getLoginUserId()));
    }

}
