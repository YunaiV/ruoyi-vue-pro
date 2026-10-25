package cn.iocoder.yudao.module.promotion.service.coupon;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.product.api.category.ProductCategoryApi;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.promotion.dal.dataobject.coupon.CouponDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.coupon.CouponTemplateDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.coupon.CouponMapper;
import cn.iocoder.yudao.module.promotion.dal.mysql.coupon.CouponTemplateMapper;
import cn.iocoder.yudao.module.promotion.enums.coupon.CouponStatusEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import javax.sql.DataSource;
import java.util.function.Consumer;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.COUPON_DELETE_FAIL_USED;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.COUPON_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link CouponServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import({CouponServiceImpl.class, CouponTemplateServiceImpl.class})
public class CouponServiceImplTest extends BaseDbUnitTest {

    @Resource
    private CouponServiceImpl couponService;

    @Resource
    private CouponMapper couponMapper;
    @Resource
    private CouponTemplateMapper couponTemplateMapper;

    @Resource
    private DataSource dataSource;

    @MockitoBean
    private MemberUserApi memberUserApi;
    @MockitoBean
    private ProductCategoryApi productCategoryApi;
    @MockitoBean
    private ProductSpuApi productSpuApi;

    @Test
    public void testDeleteCoupon_success() {
        // mock 数据
        CouponTemplateDO dbTemplate = randomCouponTemplateDO(o -> o.setTakeCount(5));
        couponTemplateMapper.insert(dbTemplate);
        CouponDO dbCoupon = randomCouponDO(o -> o.setTemplateId(dbTemplate.getId())
                .setStatus(CouponStatusEnum.UNUSED.getStatus()));
        couponMapper.insert(dbCoupon);

        // 调用
        couponService.deleteCoupon(dbCoupon.getId());
        // 断言：优惠劵被删除，模板领取数量 -1
        assertNull(couponMapper.selectById(dbCoupon.getId()));
        assertEquals(4, couponTemplateMapper.selectById(dbTemplate.getId()).getTakeCount());
    }

    @Test
    public void testDeleteCoupon_templateDeleted() {
        // mock 数据（模板已删除）
        CouponTemplateDO dbTemplate = randomCouponTemplateDO(o -> o.setTakeCount(5));
        couponTemplateMapper.insert(dbTemplate);
        couponTemplateMapper.deleteById(dbTemplate.getId());
        CouponDO dbCoupon = randomCouponDO(o -> o.setTemplateId(dbTemplate.getId())
                .setStatus(CouponStatusEnum.UNUSED.getStatus()));
        couponMapper.insert(dbCoupon);

        // 调用
        couponService.deleteCoupon(dbCoupon.getId());
        // 断言：优惠劵被删除，已删除模板的领取数量不变
        assertNull(couponMapper.selectById(dbCoupon.getId()));
        assertEquals(5, selectTemplateTakeCount(dbTemplate.getId()));
    }

    @Test
    public void testDeleteCoupon_templateNotExists() {
        // mock 数据（模板不存在）
        CouponDO dbCoupon = randomCouponDO(o -> o.setTemplateId(randomLongId())
                .setStatus(CouponStatusEnum.EXPIRE.getStatus()));
        couponMapper.insert(dbCoupon);

        // 调用
        couponService.deleteCoupon(dbCoupon.getId());
        // 断言：优惠劵被删除
        assertNull(couponMapper.selectById(dbCoupon.getId()));
    }

    @Test
    public void testDeleteCoupon_used() {
        // mock 数据
        CouponTemplateDO dbTemplate = randomCouponTemplateDO(o -> o.setTakeCount(5));
        couponTemplateMapper.insert(dbTemplate);
        CouponDO dbCoupon = randomCouponDO(o -> o.setTemplateId(dbTemplate.getId())
                .setStatus(CouponStatusEnum.USED.getStatus()));
        couponMapper.insert(dbCoupon);

        // 调用, 并断言异常
        assertServiceException(() -> couponService.deleteCoupon(dbCoupon.getId()), COUPON_DELETE_FAIL_USED);
        // 断言：优惠劵未删除，模板领取数量不变
        CouponDO coupon = couponMapper.selectById(dbCoupon.getId());
        assertNotNull(coupon);
        assertEquals(CouponStatusEnum.USED.getStatus(), coupon.getStatus());
        assertEquals(5, couponTemplateMapper.selectById(dbTemplate.getId()).getTakeCount());
    }

    @Test
    public void testDeleteCoupon_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> couponService.deleteCoupon(id), COUPON_NOT_EXISTS);
    }

    /**
     * 查询模板的领取数量，包括已删除的模板
     *
     * @param id 模板编号
     * @return 领取数量
     */
    private Integer selectTemplateTakeCount(Long id) {
        return new JdbcTemplate(dataSource).queryForObject(
                "SELECT take_count FROM promotion_coupon_template WHERE id = ?", Integer.class, id);
    }

    // ========== 随机对象 ==========

    @SafeVarargs
    private static CouponTemplateDO randomCouponTemplateDO(Consumer<CouponTemplateDO>... consumers) {
        return randomPojo(CouponTemplateDO.class, consumers);
    }

    @SafeVarargs
    private static CouponDO randomCouponDO(Consumer<CouponDO>... consumers) {
        return randomPojo(CouponDO.class, consumers);
    }

}
