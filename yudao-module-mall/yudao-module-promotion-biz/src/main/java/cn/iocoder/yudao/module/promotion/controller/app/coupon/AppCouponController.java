package cn.iocoder.yudao.module.promotion.controller.app.coupon;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.module.promotion.controller.app.coupon.vo.coupon.AppCouponMatchReqVO;
import cn.iocoder.yudao.module.promotion.controller.app.coupon.vo.coupon.AppCouponMatchRespVO;
import cn.iocoder.yudao.module.promotion.controller.app.coupon.vo.coupon.AppCouponPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.app.coupon.vo.coupon.AppCouponRespVO;
import cn.iocoder.yudao.module.promotion.controller.app.coupon.vo.template.AppCouponTemplatePageReqVO;
import cn.iocoder.yudao.module.promotion.service.coupon.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    // TODO 芋艿：待实现
    @GetMapping("/match-list")
    @Operation(summary = "获得匹配指定商品的优惠劵列表")
    public CommonResult<List<AppCouponMatchRespVO>> getMatchCouponList(AppCouponMatchReqVO matchReqVO) {
        List<AppCouponMatchRespVO> list = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            AppCouponMatchRespVO vo = new AppCouponMatchRespVO();
            vo.setId(i + 1L);
            vo.setName("优惠劵" + (i + 1));
            vo.setUsePrice(random.nextInt(100) * 100);
            vo.setValidStartTime(LocalDateTime.now().plusDays(random.nextInt(10)));
            vo.setValidEndTime(LocalDateTime.now().plusDays(random.nextInt(20) + 10));
            vo.setDiscountType(random.nextInt(2) + 1);
            if (vo.getDiscountType() == 1) {
                vo.setDiscountPercent(null);
                vo.setDiscountPrice(random.nextInt(50) * 100);
                vo.setDiscountLimitPrice(null);
            } else {
                vo.setDiscountPercent(random.nextInt(90) + 10);
                vo.setDiscountPrice(null);
                vo.setDiscountLimitPrice(random.nextInt(200) * 100);
            }
            vo.setMatch(random.nextBoolean());
            if (!vo.getMatch()) {
                vo.setDescription("不符合条件噢");
            }
            list.add(vo);
        }
        return success(list);
    }

    // TODO 芋艿：待实现
    @GetMapping("/page")
    @Operation(summary = "优惠劵列表", description = "我的优惠劵")
    public CommonResult<PageResult<AppCouponRespVO>> takeCoupon(AppCouponPageReqVO pageReqVO) {
        List<AppCouponRespVO> list = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            AppCouponRespVO vo = new AppCouponRespVO();
            vo.setId(i + 1L);
            vo.setName("优惠劵" + (i + 1));
            vo.setStatus(pageReqVO.getStatus());
            vo.setUsePrice(random.nextInt(100) * 100);
            vo.setValidStartTime(LocalDateTime.now().plusDays(random.nextInt(10)));
            vo.setValidEndTime(LocalDateTime.now().plusDays(random.nextInt(20) + 10));
            vo.setDiscountType(random.nextInt(2) + 1);
            if (vo.getDiscountType() == 1) {
                vo.setDiscountPercent(null);
                vo.setDiscountPrice(random.nextInt(50) * 100);
                vo.setDiscountLimitPrice(null);
            } else {
                vo.setDiscountPercent(random.nextInt(90) + 10);
                vo.setDiscountPrice(null);
                vo.setDiscountLimitPrice(random.nextInt(200) * 100);
            }
            list.add(vo);
        }
        return success(new PageResult<>(list, 20L));
    }

    @GetMapping(value = "/get-unused-count")
    @Operation(summary = "获得未使用的优惠劵数量")
    @PreAuthenticated
    public CommonResult<Long> getUnusedCouponCount() {
        return success(couponService.getUnusedCouponCount(getLoginUserId()));
    }

}
