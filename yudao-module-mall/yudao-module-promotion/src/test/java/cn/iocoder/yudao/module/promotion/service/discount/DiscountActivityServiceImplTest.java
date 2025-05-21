package cn.iocoder.yudao.module.promotion.service.discount;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.promotion.controller.admin.discount.vo.DiscountActivityBaseVO;
import cn.iocoder.yudao.module.promotion.controller.admin.discount.vo.DiscountActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.discount.vo.DiscountActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.discount.vo.DiscountActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.discount.DiscountActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.discount.DiscountProductDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.discount.DiscountActivityMapper;
import cn.iocoder.yudao.module.promotion.dal.mysql.discount.DiscountProductMapper;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionActivityStatusEnum;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionDiscountTypeEnum;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import jakarta.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.addTime;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.DISCOUNT_ACTIVITY_NOT_EXISTS;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

/**
* {@link DiscountActivityServiceImpl} 的单元测试类
*
* @author 芋道源码
*/
@Disabled // TODO 芋艿：后续 fix 补充的单测
@Import(DiscountActivityServiceImpl.class)
public class DiscountActivityServiceImplTest extends BaseDbUnitTest {

    @Resource
    private DiscountActivityServiceImpl discountActivityService;

    @Resource
    private DiscountActivityMapper discountActivityMapper;
    @Resource
    private DiscountProductMapper discountProductMapper;

    @Test
    public void testCreateDiscountActivity_success() {
        // 准备参数
        DiscountActivityCreateReqVO reqVO = randomPojo(DiscountActivityCreateReqVO.class, o -> {
            // 用于触发进行中的状态
            o.setStartTime(addTime(Duration.ofDays(1))).setEndTime(addTime(Duration.ofDays(2)));
            // 设置商品
            o.setProducts(asList(new DiscountActivityBaseVO.Product().setSpuId(1L).setSkuId(2L)
                            .setDiscountType(PromotionDiscountTypeEnum.PRICE.getType()).setDiscountPrice(3),
                    new DiscountActivityBaseVO.Product().setSpuId(10L).setSkuId(20L)
                            .setDiscountType(PromotionDiscountTypeEnum.PERCENT.getType()).setDiscountPercent(30)));
        });

        // 调用
        Long discountActivityId = discountActivityService.createDiscountActivity(reqVO);
        // 断言
        assertNotNull(discountActivityId);
        // 校验活动
        DiscountActivityDO discountActivity = discountActivityMapper.selectById(discountActivityId);
        assertPojoEquals(reqVO, discountActivity);
        assertEquals(discountActivity.getStatus(), PromotionActivityStatusEnum.WAIT.getStatus());
        // 校验商品
        List<DiscountProductDO> discountProducts = discountProductMapper.selectList(DiscountProductDO::getActivityId, discountActivity.getId());
        assertEquals(discountProducts.size(), reqVO.getProducts().size());
        for (int i = 0; i < reqVO.getProducts().size(); i++) {
            DiscountActivityBaseVO.Product product = reqVO.getProducts().get(i);
            DiscountProductDO discountProduct = discountProducts.get(i);
            assertEquals(discountProduct.getActivityId(), discountActivity.getId());
            assertEquals(discountProduct.getSpuId(), product.getSpuId());
            assertEquals(discountProduct.getSkuId(), product.getSkuId());
            assertEquals(discountProduct.getDiscountType(), product.getDiscountType());
            assertEquals(discountProduct.getDiscountPrice(), product.getDiscountPrice());
            assertEquals(discountProduct.getDiscountPercent(), product.getDiscountPercent());
        }
    }

    @Test
    public void testUpdateDiscountActivity_success() {
        // mock 数据(商品)
        DiscountActivityDO dbDiscountActivity = randomPojo(DiscountActivityDO.class);
        discountActivityMapper.insert(dbDiscountActivity);// @Sql: 先插入出一条存在的数据
        // mock 数据(活动)
        DiscountProductDO dbDiscountProduct01 = randomPojo(DiscountProductDO.class, o -> o.setActivityId(dbDiscountActivity.getId())
                .setSpuId(1L).setSkuId(2L).setDiscountType(PromotionDiscountTypeEnum.PRICE.getType()).setDiscountPrice(3).setDiscountPercent(null));
        DiscountProductDO dbDiscountProduct02 = randomPojo(DiscountProductDO.class, o -> o.setActivityId(dbDiscountActivity.getId())
                .setSpuId(10L).setSkuId(20L).setDiscountType(PromotionDiscountTypeEnum.PERCENT.getType()).setDiscountPercent(30).setDiscountPrice(null));
        discountProductMapper.insert(dbDiscountProduct01);
        discountProductMapper.insert(dbDiscountProduct02);
        // 准备参数
        DiscountActivityUpdateReqVO reqVO = randomPojo(DiscountActivityUpdateReqVO.class, o -> {
            o.setId(dbDiscountActivity.getId()); // 设置更新的 ID
            // 用于触发进行中的状态
            o.setStartTime(addTime(Duration.ofDays(1))).setEndTime(addTime(Duration.ofDays(2)));
            // 设置商品
            o.setProducts(asList(new DiscountActivityBaseVO.Product().setSpuId(1L).setSkuId(2L)
                            .setDiscountType(PromotionDiscountTypeEnum.PRICE.getType()).setDiscountPrice(3).setDiscountPercent(null),
                    new DiscountActivityBaseVO.Product().setSpuId(100L).setSkuId(200L)
                            .setDiscountType(PromotionDiscountTypeEnum.PERCENT.getType()).setDiscountPercent(30).setDiscountPrice(null)));
        });

        // 调用
        discountActivityService.updateDiscountActivity(reqVO);
        // 校验活动
        DiscountActivityDO discountActivity = discountActivityMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, discountActivity);
        assertEquals(discountActivity.getStatus(), PromotionActivityStatusEnum.WAIT.getStatus());
        // 校验商品
        List<DiscountProductDO> discountProducts = discountProductMapper.selectList(DiscountProductDO::getActivityId, discountActivity.getId());
        assertEquals(discountProducts.size(), reqVO.getProducts().size());
        for (int i = 0; i < reqVO.getProducts().size(); i++) {
            DiscountActivityBaseVO.Product product = reqVO.getProducts().get(i);
            DiscountProductDO discountProduct = discountProducts.get(i);
            assertEquals(discountProduct.getActivityId(), discountActivity.getId());
            assertEquals(discountProduct.getSpuId(), product.getSpuId());
            assertEquals(discountProduct.getSkuId(), product.getSkuId());
            assertEquals(discountProduct.getDiscountType(), product.getDiscountType());
            assertEquals(discountProduct.getDiscountPrice(), product.getDiscountPrice());
            assertEquals(discountProduct.getDiscountPercent(), product.getDiscountPercent());
        }
    }

    @Test
    public void testCloseDiscountActivity() {
        // mock 数据
        DiscountActivityDO dbDiscountActivity = randomPojo(DiscountActivityDO.class,
                o -> o.setStatus(PromotionActivityStatusEnum.WAIT.getStatus()));
        discountActivityMapper.insert(dbDiscountActivity);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbDiscountActivity.getId();

        // 调用
        discountActivityService.closeDiscountActivity(id);
        // 校验状态
        DiscountActivityDO discountActivity = discountActivityMapper.selectById(id);
        assertEquals(discountActivity.getStatus(), PromotionActivityStatusEnum.CLOSE.getStatus());
    }

    @Test
    public void testUpdateDiscountActivity_notExists() {
        // 准备参数
        DiscountActivityUpdateReqVO reqVO = randomPojo(DiscountActivityUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> discountActivityService.updateDiscountActivity(reqVO), DISCOUNT_ACTIVITY_NOT_EXISTS);
    }

    @Test
    public void testDeleteDiscountActivity_success() {
        // mock 数据
        DiscountActivityDO dbDiscountActivity = randomPojo(DiscountActivityDO.class,
                o -> o.setStatus(PromotionActivityStatusEnum.CLOSE.getStatus()));
        discountActivityMapper.insert(dbDiscountActivity);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbDiscountActivity.getId();

        // 调用
        discountActivityService.deleteDiscountActivity(id);
       // 校验数据不存在了
       assertNull(discountActivityMapper.selectById(id));
    }

    @Test
    public void testDeleteDiscountActivity_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> discountActivityService.deleteDiscountActivity(id), DISCOUNT_ACTIVITY_NOT_EXISTS);
    }

    @Test
    public void testGetDiscountActivityPage() {
       // mock 数据
       DiscountActivityDO dbDiscountActivity = randomPojo(DiscountActivityDO.class, o -> { // 等会查询到
           o.setName("芋艿");
           o.setStatus(PromotionActivityStatusEnum.WAIT.getStatus());
           o.setCreateTime(buildTime(2021, 1, 15));
       });
       discountActivityMapper.insert(dbDiscountActivity);
       // 测试 name 不匹配
       discountActivityMapper.insert(cloneIgnoreId(dbDiscountActivity, o -> o.setName("土豆")));
       // 测试 status 不匹配
       discountActivityMapper.insert(cloneIgnoreId(dbDiscountActivity, o -> o.setStatus(PromotionActivityStatusEnum.END.getStatus())));
       // 测试 createTime 不匹配
       discountActivityMapper.insert(cloneIgnoreId(dbDiscountActivity, o -> o.setCreateTime(buildTime(2021, 2, 10))));
       // 准备参数
       DiscountActivityPageReqVO reqVO = new DiscountActivityPageReqVO();
       reqVO.setName("芋艿");
       reqVO.setStatus(PromotionActivityStatusEnum.WAIT.getStatus());
       reqVO.setCreateTime((new LocalDateTime[]{buildTime(2021, 1, 1), buildTime(2021, 1, 31)}));

       // 调用
       PageResult<DiscountActivityDO> pageResult = discountActivityService.getDiscountActivityPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbDiscountActivity, pageResult.getList().get(0));
    }

}
