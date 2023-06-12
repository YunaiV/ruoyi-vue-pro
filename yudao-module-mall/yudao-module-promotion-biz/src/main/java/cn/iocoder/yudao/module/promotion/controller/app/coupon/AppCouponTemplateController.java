package cn.iocoder.yudao.module.promotion.controller.app.coupon;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.app.coupon.vo.template.AppCouponTemplatePageReqVO;
import cn.iocoder.yudao.module.promotion.controller.app.coupon.vo.template.AppCouponTemplateRespVO;
import cn.iocoder.yudao.module.promotion.service.coupon.CouponTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 优惠劵模板")
@RestController
@RequestMapping("/promotion/coupon-template")
@Validated
public class AppCouponTemplateController {

    @Resource
    private CouponTemplateService couponTemplateService;

    // TODO 芋艿：待实现
    @GetMapping("/list")
    @Operation(summary = "获得优惠劵模版列表") // 目前主要给商品详情使用
    @Parameters({
            @Parameter(name = "spuId", description = "商品 SPU 编号", required = true),
            @Parameter(name = "useType", description = "使用类型"),
            @Parameter(name = "count", description = "数量", required = true)
    })
    public CommonResult<List<AppCouponTemplateRespVO>> getCouponTemplateList(@RequestParam("spuId") Long spuId,
                                                                             @RequestParam(value = "useType", required = false) Integer useType) {
        List<AppCouponTemplateRespVO> list = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            AppCouponTemplateRespVO vo = new AppCouponTemplateRespVO();
            vo.setId(i + 1L);
            vo.setName("优惠劵" + (i + 1));
            vo.setTakeLimitCount(random.nextInt(10) + 1);
            vo.setUsePrice(random.nextInt(100) * 100);
            vo.setValidityType(random.nextInt(2) + 1);
            if (vo.getValidityType() == 1) {
                vo.setValidStartTime(LocalDateTime.now().plusDays(random.nextInt(10)));
                vo.setValidEndTime(LocalDateTime.now().plusDays(random.nextInt(20) + 10));
            } else {
                vo.setFixedStartTerm(random.nextInt(10));
                vo.setFixedEndTerm(random.nextInt(10) + vo.getFixedStartTerm() + 1);
            }
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
            vo.setTakeStatus(random.nextBoolean());
            list.add(vo);
        }
        return success(list);
    }

    // TODO 芋艿：待实现
    @GetMapping("/page")
    @Operation(summary = "获得优惠劵模版分页")
    public CommonResult<PageResult<AppCouponTemplateRespVO>> getCouponTemplatePage(AppCouponTemplatePageReqVO pageReqVO) {
        return null;
    }

}
