package cn.iocoder.yudao.module.promotion.service.coupon;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.promotion.controller.admin.coupon.vo.template.CouponTemplateCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.coupon.vo.template.CouponTemplatePageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.coupon.vo.template.CouponTemplateUpdateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.coupon.CouponTemplateDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.coupon.CouponTemplateMapper;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionDiscountTypeEnum;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionProductScopeEnum;
import cn.iocoder.yudao.module.promotion.enums.coupon.CouponTemplateValidityTypeEnum;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.COUPON_TEMPLATE_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

/**
* {@link CouponTemplateServiceImpl} 的单元测试类
*
* @author 芋道源码
*/
@Import(CouponTemplateServiceImpl.class)
public class CouponTemplateServiceImplTest extends BaseDbUnitTest {

    @Resource
    private CouponTemplateServiceImpl couponTemplateService;

    @Resource
    private CouponTemplateMapper couponTemplateMapper;

    @Test
    public void testCreateCouponTemplate_success() {
        // 准备参数
        CouponTemplateCreateReqVO reqVO = randomPojo(CouponTemplateCreateReqVO.class,
                o -> o.setProductScope(randomEle(PromotionProductScopeEnum.values()).getScope())
                        .setValidityType(randomEle(CouponTemplateValidityTypeEnum.values()).getType())
                        .setDiscountType(randomEle(PromotionDiscountTypeEnum.values()).getType()));

        // 调用
        Long couponTemplateId = couponTemplateService.createCouponTemplate(reqVO);
        // 断言
        assertNotNull(couponTemplateId);
        // 校验记录的属性是否正确
        CouponTemplateDO couponTemplate = couponTemplateMapper.selectById(couponTemplateId);
        assertPojoEquals(reqVO, couponTemplate);
    }

    @Test
    public void testUpdateCouponTemplate_success() {
        // mock 数据
        CouponTemplateDO dbCouponTemplate = randomPojo(CouponTemplateDO.class);
        couponTemplateMapper.insert(dbCouponTemplate);// @Sql: 先插入出一条存在的数据
        // 准备参数
        CouponTemplateUpdateReqVO reqVO = randomPojo(CouponTemplateUpdateReqVO.class, o -> {
            o.setId(dbCouponTemplate.getId()); // 设置更新的 ID
            // 其它通用字段
            o.setProductScope(randomEle(PromotionProductScopeEnum.values()).getScope())
                    .setValidityType(randomEle(CouponTemplateValidityTypeEnum.values()).getType())
                    .setDiscountType(randomEle(PromotionDiscountTypeEnum.values()).getType());
        });

        // 调用
        couponTemplateService.updateCouponTemplate(reqVO);
        // 校验是否更新正确
        CouponTemplateDO couponTemplate = couponTemplateMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, couponTemplate);
    }

    @Test
    public void testUpdateCouponTemplate_notExists() {
        // 准备参数
        CouponTemplateUpdateReqVO reqVO = randomPojo(CouponTemplateUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> couponTemplateService.updateCouponTemplate(reqVO), COUPON_TEMPLATE_NOT_EXISTS);
    }

    @Test
    public void testDeleteCouponTemplate_success() {
        // mock 数据
        CouponTemplateDO dbCouponTemplate = randomPojo(CouponTemplateDO.class);
        couponTemplateMapper.insert(dbCouponTemplate);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbCouponTemplate.getId();

        // 调用
        couponTemplateService.deleteCouponTemplate(id);
       // 校验数据不存在了
       assertNull(couponTemplateMapper.selectById(id));
    }

    @Test
    public void testDeleteCouponTemplate_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> couponTemplateService.deleteCouponTemplate(id), COUPON_TEMPLATE_NOT_EXISTS);
    }

    @Test
    public void testGetCouponTemplatePage() {
       // mock 数据
       CouponTemplateDO dbCouponTemplate = randomPojo(CouponTemplateDO.class, o -> { // 等会查询到
           o.setName("芋艿");
           o.setStatus(CommonStatusEnum.ENABLE.getStatus());
           o.setDiscountType(PromotionDiscountTypeEnum.PERCENT.getType());
           o.setCreateTime(buildTime(2022, 2, 2));
       });
       couponTemplateMapper.insert(dbCouponTemplate);
       // 测试 name 不匹配
       couponTemplateMapper.insert(cloneIgnoreId(dbCouponTemplate, o -> o.setName("土豆")));
       // 测试 status 不匹配
       couponTemplateMapper.insert(cloneIgnoreId(dbCouponTemplate, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
       // 测试 type 不匹配
       couponTemplateMapper.insert(cloneIgnoreId(dbCouponTemplate, o -> o.setDiscountType(PromotionDiscountTypeEnum.PRICE.getType())));
       // 测试 createTime 不匹配
       couponTemplateMapper.insert(cloneIgnoreId(dbCouponTemplate, o -> o.setCreateTime(buildTime(2022, 1, 1))));
       // 准备参数
       CouponTemplatePageReqVO reqVO = new CouponTemplatePageReqVO();
       reqVO.setName("芋艿");
       reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
       reqVO.setDiscountType(PromotionDiscountTypeEnum.PERCENT.getType());
       reqVO.setCreateTime((new LocalDateTime[]{buildTime(2022, 2, 1), buildTime(2022, 2, 3)}));

       // 调用
       PageResult<CouponTemplateDO> pageResult = couponTemplateService.getCouponTemplatePage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbCouponTemplate, pageResult.getList().get(0));
    }

}
