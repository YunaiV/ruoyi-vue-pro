package cn.iocoder.yudao.module.promotion.service.coupon;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.promotion.controller.admin.coupon.vo.CouponTemplateCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.coupon.vo.CouponTemplatePageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.coupon.vo.CouponTemplateUpdateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.coupon.CouponTemplateDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.coupon.CouponTemplateMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Date;

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
        CouponTemplateCreateReqVO reqVO = randomPojo(CouponTemplateCreateReqVO.class);

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
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetCouponTemplatePage() {
       // mock 数据
       CouponTemplateDO dbCouponTemplate = randomPojo(CouponTemplateDO.class, o -> { // 等会查询到
           o.setName(null);
           o.setStatus(null);
           o.setDiscountType(null);
           o.setCreateTime(null);
       });
       couponTemplateMapper.insert(dbCouponTemplate);
       // 测试 name 不匹配
       couponTemplateMapper.insert(cloneIgnoreId(dbCouponTemplate, o -> o.setName(null)));
       // 测试 status 不匹配
       couponTemplateMapper.insert(cloneIgnoreId(dbCouponTemplate, o -> o.setStatus(null)));
       // 测试 type 不匹配
       couponTemplateMapper.insert(cloneIgnoreId(dbCouponTemplate, o -> o.setDiscountType(null)));
       // 测试 createTime 不匹配
       couponTemplateMapper.insert(cloneIgnoreId(dbCouponTemplate, o -> o.setCreateTime(null)));
       // 准备参数
       CouponTemplatePageReqVO reqVO = new CouponTemplatePageReqVO();
       reqVO.setName(null);
       reqVO.setStatus(null);
       reqVO.setDiscountType(null);
       reqVO.setCreateTime((new Date[]{}));

       // 调用
       PageResult<CouponTemplateDO> pageResult = couponTemplateService.getCouponTemplatePage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbCouponTemplate, pageResult.getList().get(0));
    }

}
