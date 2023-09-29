package cn.iocoder.yudao.module.promotion.controller.app.coupon;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.promotion.controller.app.coupon.vo.template.AppCouponTemplatePageReqVO;
import cn.iocoder.yudao.module.promotion.controller.app.coupon.vo.template.AppCouponTemplateRespVO;
import cn.iocoder.yudao.module.promotion.convert.coupon.CouponTemplateConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.coupon.CouponTemplateDO;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionProductScopeEnum;
import cn.iocoder.yudao.module.promotion.enums.coupon.CouponTakeTypeEnum;
import cn.iocoder.yudao.module.promotion.service.coupon.CouponService;
import cn.iocoder.yudao.module.promotion.service.coupon.CouponTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getLoginUserId;

@Tag(name = "用户 App - 优惠劵模板")
@RestController
@RequestMapping("/promotion/coupon-template")
@Validated
public class AppCouponTemplateController {

    @Resource
    private CouponTemplateService couponTemplateService;
    @Resource
    private CouponService couponService;

    @Resource
    private ProductSpuApi productSpuApi;

    @GetMapping("/page")
    @Operation(summary = "获得优惠劵模版分页")
    public CommonResult<PageResult<AppCouponTemplateRespVO>> getCouponTemplatePage(AppCouponTemplatePageReqVO pageReqVO) {
        // 1.1 处理查询条件：商品范围编号
        Long productScopeValue = getProductScopeValue(pageReqVO);
        // 1.2 处理查询条件：领取方式 = 直接领取
        List<Integer> canTakeTypes = Collections.singletonList(CouponTakeTypeEnum.USER.getValue());

        // 2. 分页查询
        PageResult<CouponTemplateDO> pageResult = couponTemplateService.getCouponTemplatePage(
                CouponTemplateConvert.INSTANCE.convert(pageReqVO, canTakeTypes, pageReqVO.getProductScope(), productScopeValue));

        // 3.1 领取数量
        Map<Long, Integer> couponTakeCountMap = new HashMap<>(0);
        Long userId = getLoginUserId();
        if (userId != null) {
            List<Long> templateIds = convertList(pageResult.getList(), CouponTemplateDO::getId,
                    t -> ObjUtil.notEqual(t.getTakeLimitCount(), -1)); // 只查有设置“每人限领个数”的
            couponTakeCountMap = couponService.getTakeCountMapByTemplateIds(templateIds, userId);
        }
        // 3.2 拼接返回
        return success(CouponTemplateConvert.INSTANCE.convertAppPage(pageResult, couponTakeCountMap));
    }

    /**
     * 获得分页查询的商品范围
     *
     * @param pageReqVO 分页查询
     * @return 商品范围
     */
    private Long getProductScopeValue(AppCouponTemplatePageReqVO pageReqVO) {
        // 通用券：清除商品范围
        if (pageReqVO.getProductScope() == null || ObjectUtils.equalsAny(pageReqVO.getProductScope(), PromotionProductScopeEnum.ALL.getScope(), null)) {
            return null;
        }
        // 品类券：查询商品的品类
        if (Objects.equals(pageReqVO.getProductScope(), PromotionProductScopeEnum.CATEGORY.getScope()) && pageReqVO.getSpuId() != null) {
            return Optional.ofNullable(productSpuApi.getSpu(pageReqVO.getSpuId()))
                    .map(ProductSpuRespDTO::getCategoryId).orElse(null);
        }
        // 商品卷：直接返回
        return pageReqVO.getSpuId();
    }

}
