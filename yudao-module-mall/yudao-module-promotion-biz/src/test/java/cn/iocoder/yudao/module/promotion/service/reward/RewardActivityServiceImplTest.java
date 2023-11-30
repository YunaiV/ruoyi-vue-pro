package cn.iocoder.yudao.module.promotion.service.reward;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.promotion.controller.admin.reward.vo.RewardActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.reward.vo.RewardActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.reward.vo.RewardActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.reward.RewardActivityDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.reward.RewardActivityMapper;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionActivityStatusEnum;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionConditionTypeEnum;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionProductScopeEnum;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Set;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.framework.common.util.collection.SetUtils.asSet;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.addTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.REWARD_ACTIVITY_NOT_EXISTS;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;

/**
* {@link RewardActivityServiceImpl} 的单元测试类
*
* @author 芋道源码
*/
@Disabled // TODO 芋艿：后续 fix 补充的单测
@Import(RewardActivityServiceImpl.class)
public class RewardActivityServiceImplTest extends BaseDbUnitTest {

    @Resource
    private RewardActivityServiceImpl rewardActivityService;

    @Resource
    private RewardActivityMapper rewardActivityMapper;

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
        Long rewardActivityId = rewardActivityService.createRewardActivity(reqVO);
        // 断言
        assertNotNull(rewardActivityId);
        // 校验记录的属性是否正确
        RewardActivityDO rewardActivity = rewardActivityMapper.selectById(rewardActivityId);
        assertPojoEquals(reqVO, rewardActivity, "rules");
        assertEquals(rewardActivity.getStatus(), PromotionActivityStatusEnum.WAIT.getStatus());
        for (int i = 0; i < reqVO.getRules().size(); i++) {
            assertPojoEquals(reqVO.getRules().get(i), rewardActivity.getRules().get(i));
        }
    }

    @Test
    public void testUpdateRewardActivity_success() {
        // mock 数据
        RewardActivityDO dbRewardActivity = randomPojo(RewardActivityDO.class, o -> o.setStatus(PromotionActivityStatusEnum.WAIT.getStatus()));
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
        rewardActivityService.updateRewardActivity(reqVO);
        // 校验是否更新正确
        RewardActivityDO rewardActivity = rewardActivityMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, rewardActivity, "rules");
        assertEquals(rewardActivity.getStatus(), PromotionActivityStatusEnum.WAIT.getStatus());
        for (int i = 0; i < reqVO.getRules().size(); i++) {
            assertPojoEquals(reqVO.getRules().get(i), rewardActivity.getRules().get(i));
        }
    }

    @Test
    public void testCloseRewardActivity() {
        // mock 数据
        RewardActivityDO dbRewardActivity = randomPojo(RewardActivityDO.class, o -> o.setStatus(PromotionActivityStatusEnum.WAIT.getStatus()));
        rewardActivityMapper.insert(dbRewardActivity);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbRewardActivity.getId();

        // 调用
        rewardActivityService.closeRewardActivity(id);
        // 校验状态
        RewardActivityDO rewardActivity = rewardActivityMapper.selectById(id);
        assertEquals(rewardActivity.getStatus(), PromotionActivityStatusEnum.CLOSE.getStatus());
    }

    @Test
    public void testUpdateRewardActivity_notExists() {
        // 准备参数
        RewardActivityUpdateReqVO reqVO = randomPojo(RewardActivityUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> rewardActivityService.updateRewardActivity(reqVO), REWARD_ACTIVITY_NOT_EXISTS);
    }

    @Test
    public void testDeleteRewardActivity_success() {
        // mock 数据
        RewardActivityDO dbRewardActivity = randomPojo(RewardActivityDO.class, o -> o.setStatus(PromotionActivityStatusEnum.CLOSE.getStatus()));
        rewardActivityMapper.insert(dbRewardActivity);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbRewardActivity.getId();

        // 调用
        rewardActivityService.deleteRewardActivity(id);
       // 校验数据不存在了
       assertNull(rewardActivityMapper.selectById(id));
    }

    @Test
    public void testDeleteRewardActivity_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> rewardActivityService.deleteRewardActivity(id), REWARD_ACTIVITY_NOT_EXISTS);
    }

    @Test
    public void testGetRewardActivityPage() {
       // mock 数据
       RewardActivityDO dbRewardActivity = randomPojo(RewardActivityDO.class, o -> { // 等会查询到
           o.setName("芋艿");
           o.setStatus(PromotionActivityStatusEnum.CLOSE.getStatus());
       });
       rewardActivityMapper.insert(dbRewardActivity);
       // 测试 name 不匹配
       rewardActivityMapper.insert(cloneIgnoreId(dbRewardActivity, o -> o.setName("土豆")));
       // 测试 status 不匹配
       rewardActivityMapper.insert(cloneIgnoreId(dbRewardActivity, o -> o.setStatus(PromotionActivityStatusEnum.RUN.getStatus())));
       // 准备参数
       RewardActivityPageReqVO reqVO = new RewardActivityPageReqVO();
       reqVO.setName("芋艿");
       reqVO.setStatus(PromotionActivityStatusEnum.CLOSE.getStatus());

       // 调用
       PageResult<RewardActivityDO> pageResult = rewardActivityService.getRewardActivityPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbRewardActivity, pageResult.getList().get(0), "rules");
    }

    @Test
    public void testGetRewardActivities_all() {
        // mock 数据
        RewardActivityDO allActivity = randomPojo(RewardActivityDO.class, o -> o.setStatus(PromotionActivityStatusEnum.RUN.getStatus())
                .setProductScope(PromotionProductScopeEnum.ALL.getScope()));
        rewardActivityMapper.insert(allActivity);
        RewardActivityDO productActivity = randomPojo(RewardActivityDO.class, o -> o.setStatus(PromotionActivityStatusEnum.RUN.getStatus())
                .setProductScope(PromotionProductScopeEnum.SPU.getScope()).setProductSpuIds(asList(1L, 2L)));
        rewardActivityMapper.insert(productActivity);
        // 准备参数
        Set<Long> spuIds = asSet(1L, 2L);

        // 调用 TODO getMatchRewardActivities 没有这个方法，但是找到了 getMatchRewardActivityList
        //Map<RewardActivityDO, Set<Long>> matchRewardActivities = rewardActivityService.getMatchRewardActivities(spuIds);
        // 断言
        //assertEquals(matchRewardActivities.size(), 1);
        //Map.Entry<RewardActivityDO, Set<Long>> next = matchRewardActivities.entrySet().iterator().next();
        //assertPojoEquals(next.getKey(), allActivity);
        //assertEquals(next.getValue(), spuIds);
    }

    @Test
    public void testGetRewardActivities_product() {
        // mock 数据
        RewardActivityDO productActivity01 = randomPojo(RewardActivityDO.class, o -> o.setStatus(PromotionActivityStatusEnum.RUN.getStatus())
                .setProductScope(PromotionProductScopeEnum.SPU.getScope()).setProductSpuIds(asList(1L, 2L)));
        rewardActivityMapper.insert(productActivity01);
        RewardActivityDO productActivity02 = randomPojo(RewardActivityDO.class, o -> o.setStatus(PromotionActivityStatusEnum.RUN.getStatus())
                .setProductScope(PromotionProductScopeEnum.SPU.getScope()).setProductSpuIds(singletonList(3L)));
        rewardActivityMapper.insert(productActivity02);
        // 准备参数
        Set<Long> spuIds = asSet(1L, 2L, 3L);

        // 调用  TODO getMatchRewardActivities 没有这个方法，但是找到了 getMatchRewardActivityList
        //Map<RewardActivityDO, Set<Long>> matchRewardActivities = rewardActivityService.getMatchRewardActivities(spuIds);
        // 断言
        //assertEquals(matchRewardActivities.size(), 2);
        //matchRewardActivities.forEach((activity, activitySpuIds) -> {
        //    if (activity.getId().equals(productActivity01.getId())) {
        //        assertPojoEquals(activity, productActivity01);
        //        assertEquals(activitySpuIds, asSet(1L, 2L));
        //    } else if (activity.getId().equals(productActivity02.getId())) {
        //        assertPojoEquals(activity, productActivity02);
        //        assertEquals(activitySpuIds, asSet(3L));
        //    } else {
        //        fail();
        //    }
        //});
    }

}
