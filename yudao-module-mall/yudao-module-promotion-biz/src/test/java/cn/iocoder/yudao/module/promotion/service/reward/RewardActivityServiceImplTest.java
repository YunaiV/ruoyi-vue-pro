package cn.iocoder.yudao.module.promotion.service.reward;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.product.api.category.ProductCategoryApi;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.promotion.controller.admin.reward.vo.RewardActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.reward.vo.RewardActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.reward.vo.RewardActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.reward.RewardActivityDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.reward.RewardActivityMapper;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionConditionTypeEnum;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionProductScopeEnum;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.Duration;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.addTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.REWARD_ACTIVITY_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link RewardActivityServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Disabled // TODO 芋艿：后续 fix 补充的单测
public class RewardActivityServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private RewardActivityServiceImpl rewardActivityServiceImpl;

    @Mock
    private RewardActivityMapper rewardActivityMapper;
    @Mock
    private ProductCategoryApi productCategoryApi;
    @Mock
    private ProductSpuApi productSpuApi;

    @Test
    public void testCreateRewardActivity_success() {
        // 准备参数
        RewardActivityCreateReqVO reqVO = randomPojo(RewardActivityCreateReqVO.class, o -> {
            o.setConditionType(randomEle(PromotionConditionTypeEnum.values()).getType());
            o.setProductScope(randomEle(PromotionProductScopeEnum.values()).getScope());
            // 用于触发进行中的状态
            o.setStartTime(addTime(Duration.ofDays(1))).setEndTime(addTime(Duration.ofDays(2)));
        });

        // 调用
        Long rewardActivityId = rewardActivityServiceImpl.createRewardActivity(reqVO);
        // 断言
        assertNotNull(rewardActivityId);
        // 校验记录的属性是否正确
        RewardActivityDO rewardActivity = rewardActivityMapper.selectById(rewardActivityId);
        assertPojoEquals(reqVO, rewardActivity, "rules");
        assertEquals(rewardActivity.getStatus(), CommonStatusEnum.DISABLE.getStatus());
        for (int i = 0; i < reqVO.getRules().size(); i++) {
            assertPojoEquals(reqVO.getRules().get(i), rewardActivity.getRules().get(i));
        }
    }

    @Test
    public void testUpdateRewardActivity_success() {
        // mock 数据
        RewardActivityDO dbRewardActivity = randomPojo(RewardActivityDO.class, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus()));
        rewardActivityMapper.insert(dbRewardActivity);// @Sql: 先插入出一条存在的数据
        // 准备参数
        RewardActivityUpdateReqVO reqVO = randomPojo(RewardActivityUpdateReqVO.class, o -> {
            o.setId(dbRewardActivity.getId()); // 设置更新的 ID
            o.setConditionType(randomEle(PromotionConditionTypeEnum.values()).getType());
            o.setProductScope(randomEle(PromotionProductScopeEnum.values()).getScope());
            // 用于触发进行中的状态
            o.setStartTime(addTime(Duration.ofDays(1))).setEndTime(addTime(Duration.ofDays(2)));
        });

        // 调用
        rewardActivityServiceImpl.updateRewardActivity(reqVO);
        // 校验是否更新正确
        RewardActivityDO rewardActivity = rewardActivityMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, rewardActivity, "rules");
        assertEquals(rewardActivity.getStatus(), CommonStatusEnum.DISABLE.getStatus());
        for (int i = 0; i < reqVO.getRules().size(); i++) {
            assertPojoEquals(reqVO.getRules().get(i), rewardActivity.getRules().get(i));
        }
    }

    @Test
    public void testCloseRewardActivity() {
        // mock 数据
        RewardActivityDO dbRewardActivity = randomPojo(RewardActivityDO.class, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus()));
        rewardActivityMapper.insert(dbRewardActivity);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbRewardActivity.getId();

        // 调用
        rewardActivityServiceImpl.closeRewardActivity(id);
        // 校验状态
        RewardActivityDO rewardActivity = rewardActivityMapper.selectById(id);
        assertEquals(rewardActivity.getStatus(), CommonStatusEnum.DISABLE.getStatus());
    }

    @Test
    public void testUpdateRewardActivity_notExists() {
        // 准备参数
        RewardActivityUpdateReqVO reqVO = randomPojo(RewardActivityUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> rewardActivityServiceImpl.updateRewardActivity(reqVO), REWARD_ACTIVITY_NOT_EXISTS);
    }

    @Test
    public void testDeleteRewardActivity_success() {
        // mock 数据
        RewardActivityDO dbRewardActivity = randomPojo(RewardActivityDO.class, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus()));
        rewardActivityMapper.insert(dbRewardActivity);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbRewardActivity.getId();

        // 调用
        rewardActivityServiceImpl.deleteRewardActivity(id);
        // 校验数据不存在了
        assertNull(rewardActivityMapper.selectById(id));
    }

    @Test
    public void testDeleteRewardActivity_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> rewardActivityServiceImpl.deleteRewardActivity(id), REWARD_ACTIVITY_NOT_EXISTS);
    }

    @Test
    public void testGetRewardActivityPage() {
        // mock 数据
        RewardActivityDO dbRewardActivity = randomPojo(RewardActivityDO.class, o -> { // 等会查询到
            o.setName("芋艿");
            o.setStatus(CommonStatusEnum.DISABLE.getStatus());
        });
        rewardActivityMapper.insert(dbRewardActivity);
        // 测试 name 不匹配
        rewardActivityMapper.insert(cloneIgnoreId(dbRewardActivity, o -> o.setName("土豆")));
        // 测试 status 不匹配
        rewardActivityMapper.insert(cloneIgnoreId(dbRewardActivity, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus())));
        // 准备参数
        RewardActivityPageReqVO reqVO = new RewardActivityPageReqVO();
        reqVO.setName("芋艿");
        reqVO.setStatus(CommonStatusEnum.DISABLE.getStatus());

        // 调用
        PageResult<RewardActivityDO> pageResult = rewardActivityServiceImpl.getRewardActivityPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbRewardActivity, pageResult.getList().get(0), "rules");
    }

    // TODO 芋艿：后续完善单测
//    @Test
//    public void testGetRewardActivities_all() {
//        LocalDateTime now = LocalDateTime.now();
//        // mock 数据
//        RewardActivityDO allActivity = randomPojo(RewardActivityDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus())
//                .setProductScope(PromotionProductScopeEnum.ALL.getScope()).setStartTime(now.minusDays(1)).setEndTime(now.plusDays(1)));
//        rewardActivityMapper.insert(allActivity);
//        RewardActivityDO productActivity = randomPojo(RewardActivityDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus())
//                .setProductScope(PromotionProductScopeEnum.SPU.getScope()).setProductScopeValues(asList(1L, 2L))
//                .setStartTime(now.minusDays(1)).setEndTime(now.plusDays(1)));
//        rewardActivityMapper.insert(productActivity);
//        // 准备参数
//        Set<Long> spuIds = asSet(1L, 2L);
//
//        // 调用
//        List<RewardActivityDO> activityList = rewardActivityServiceImpl.getRewardActivityListByStatusAndDateTimeLt(
//                CommonStatusEnum.ENABLE.getStatus(), now);
//        List<RewardActivityDO> matchRewardActivityList = filterMatchActivity(spuIds, activityList);
//        // 断言
//        assertEquals(matchRewardActivityList.size(), 1);
//        matchRewardActivityList.forEach((activity) -> {
//            if (activity.getId().equals(productActivity.getId())) {
//                assertPojoEquals(activity, productActivity);
//                assertEquals(activity.getProductScopeValues(), asList(1L, 2L));
//            } else {
//                fail();
//            }
//        });
//    }
//
//    @Test
//    public void testGetRewardActivities_product() {
//        LocalDateTime now = LocalDateTime.now();
//        // mock 数据
//        RewardActivityDO productActivity01 = randomPojo(RewardActivityDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus())
//                .setProductScope(PromotionProductScopeEnum.SPU.getScope()).setProductScopeValues(asList(1L, 2L))
//                .setStartTime(now.minusDays(1)).setEndTime(now.plusDays(1)));
//        rewardActivityMapper.insert(productActivity01);
//        RewardActivityDO productActivity02 = randomPojo(RewardActivityDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus())
//                .setProductScope(PromotionProductScopeEnum.SPU.getScope()).setProductScopeValues(singletonList(3L))
//                .setStartTime(now.minusDays(1)).setEndTime(now.plusDays(1)));
//        rewardActivityMapper.insert(productActivity02);
//        // 准备参数
//        Set<Long> spuIds = asSet(1L, 2L, 3L);
//
//        List<RewardActivityDO> activityList = rewardActivityServiceImpl.getRewardActivityListByStatusAndDateTimeLt(
//                CommonStatusEnum.ENABLE.getStatus(), now);
//        List<RewardActivityDO> matchRewardActivityList = filterMatchActivity(spuIds, activityList);
//        // 断言
//        assertEquals(matchRewardActivityList.size(), 2);
//        matchRewardActivityList.forEach((activity) -> {
//            if (activity.getId().equals(productActivity01.getId())) {
//                assertPojoEquals(activity, productActivity01);
//                assertEquals(activity.getProductScopeValues(), asList(1L, 2L));
//            } else if (activity.getId().equals(productActivity02.getId())) {
//                assertPojoEquals(activity, productActivity02);
//                assertEquals(activity.getProductScopeValues(), singletonList(3L));
//            } else {
//                fail();
//            }
//        });
//    }
//
//    /**
//     * 获得满减送的订单项（商品）列表
//     *
//     * @param spuIds       商品编号
//     * @param activityList 活动列表
//     * @return 订单项（商品）列表
//     */
//    private List<RewardActivityDO> filterMatchActivity(Collection<Long> spuIds, List<RewardActivityDO> activityList) {
//        List<RewardActivityDO> resultActivityList = new ArrayList<>();
//        for (RewardActivityDO activity : activityList) {
//            // 情况一：全部商品都可以参与
//            if (PromotionProductScopeEnum.isAll(activity.getProductScope())) {
//                resultActivityList.add(activity);
//            }
//            // 情况二：指定商品参与
//            if (PromotionProductScopeEnum.isSpu(activity.getProductScope()) &&
//                    !intersectionDistinct(activity.getProductScopeValues(), spuIds).isEmpty()) {
//                resultActivityList.add(activity);
//            }
//        }
//        return resultActivityList;
//    }

}
