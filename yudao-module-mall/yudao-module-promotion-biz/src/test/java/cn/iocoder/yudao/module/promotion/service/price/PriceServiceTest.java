package cn.iocoder.yudao.module.promotion.service.price;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.promotion.api.price.dto.CouponMeetRespDTO;
import cn.iocoder.yudao.module.promotion.api.price.dto.PriceCalculateReqDTO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.coupon.CouponDO;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionProductScopeEnum;
import cn.iocoder.yudao.module.promotion.enums.coupon.CouponStatusEnum;
import cn.iocoder.yudao.module.promotion.service.coupon.CouponService;
import cn.iocoder.yudao.module.promotion.service.reward.RewardActivityService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.SetUtils.asSet;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.COUPON_VALID_TIME_NOT_NOW;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * {@link PriceServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
public class PriceServiceTest extends BaseMockitoUnitTest {

    @InjectMocks
    private PriceServiceImpl priceService;

    @Mock
    private RewardActivityService rewardActivityService;
    @Mock
    private CouponService couponService;
    @Mock
    private ProductSkuApi productSkuApi;

    /**
     * 测试满减送活动，不匹配的情况
     */
    @Test
    public void testCalculatePrice_rewardActivityNotMeet() {

    }

    @Test
    public void testGetMeetCouponList() {
        // 准备参数
        PriceCalculateReqDTO calculateReqDTO = new PriceCalculateReqDTO().setUserId(1024L)
                .setItems(singletonList(new PriceCalculateReqDTO.Item().setSkuId(10L).setCount(2)));
        // mock 方法(商品 SKU 信息)
        ProductSkuRespDTO productSku = randomPojo(ProductSkuRespDTO.class, o -> o.setId(10L).setPrice(100));
        when(productSkuApi.getSkuList(eq(asSet(10L)))).thenReturn(singletonList(productSku));
        // mock 方法(情况一：优惠劵未到使用时间)
        CouponDO coupon01 = randomPojo(CouponDO.class);
        doThrow(new ServiceException(COUPON_VALID_TIME_NOT_NOW)).when(couponService).validCoupon(coupon01);
        // mock 方法(情况二：所结算商品没有符合条件的商品)
        CouponDO coupon02 = randomPojo(CouponDO.class);
        // mock 方法(情况三：使用金额不足)
        CouponDO coupon03 = randomPojo(CouponDO.class, o -> o.setProductScope(PromotionProductScopeEnum.ALL.getScope())
                .setUsePrice(300));
        // mock 方法(情况五：满足条件)
        CouponDO coupon04 = randomPojo(CouponDO.class, o -> o.setProductScope(PromotionProductScopeEnum.ALL.getScope())
                .setUsePrice(190));
        // mock 方法（获得用户的待使用优惠劵）
        when(couponService.getCouponList(eq(1024L), eq(CouponStatusEnum.UNUSED.getStatus())))
                .thenReturn(asList(coupon01, coupon02, coupon03, coupon04));
        // 调用
        List<CouponMeetRespDTO> list = priceService.getMeetCouponList(calculateReqDTO);
        // 断言
        assertEquals(list.size(), 4);
        // 断言情况一：优惠劵未到使用时间
        CouponMeetRespDTO couponMeetRespDTO01 = list.get(0);
        assertPojoEquals(couponMeetRespDTO01, coupon01);
        assertFalse(couponMeetRespDTO01.getMeet());
        assertEquals(couponMeetRespDTO01.getMeetTip(), "优惠劵未到使用时间");
        // 断言情况二：所结算商品没有符合条件的商品
        CouponMeetRespDTO couponMeetRespDTO02 = list.get(1);
        assertPojoEquals(couponMeetRespDTO02, coupon02);
        assertFalse(couponMeetRespDTO02.getMeet());
        assertEquals(couponMeetRespDTO02.getMeetTip(), "所结算商品没有符合条件的商品");
        // 断言情况三：差 %s 元可用优惠劵
        CouponMeetRespDTO couponMeetRespDTO03 = list.get(2);
        assertPojoEquals(couponMeetRespDTO03, coupon03);
        assertFalse(couponMeetRespDTO03.getMeet());
        assertEquals(couponMeetRespDTO03.getMeetTip(), "所结算的商品中未满足使用的金额");
        // 断言情况四：满足条件
        CouponMeetRespDTO couponMeetRespDTO04 = list.get(3);
        assertPojoEquals(couponMeetRespDTO04, coupon04);
        assertTrue(couponMeetRespDTO04.getMeet());
        assertNull(couponMeetRespDTO04.getMeetTip());
    }

}
