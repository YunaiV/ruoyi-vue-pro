package cn.iocoder.yudao.module.promotion.service.rewardactivity;

import cn.iocoder.yudao.module.promotion.service.reward.RewardActivityServiceImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.promotion.controller.admin.reward.vo.*;
import cn.iocoder.yudao.module.promotion.dal.dataobject.rewardactivity.RewardActivityDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.reward.RewardActivityMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.springframework.context.annotation.Import;
import java.util.*;

import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
* {@link RewardActivityServiceImpl} 的单元测试类
*
* @author 芋道源码
*/
@Import(RewardActivityServiceImpl.class)
public class RewardActivityServiceImplTest extends BaseDbUnitTest {

    @Resource
    private RewardActivityServiceImpl rewardActivityService;

    @Resource
    private RewardActivityMapper rewardActivityMapper;

    @Test
    public void testCreateRewardActivity_success() {
        // 准备参数
        RewardActivityCreateReqVO reqVO = randomPojo(RewardActivityCreateReqVO.class);

        // 调用
        Integer rewardActivityId = rewardActivityService.createRewardActivity(reqVO);
        // 断言
        assertNotNull(rewardActivityId);
        // 校验记录的属性是否正确
        RewardActivityDO rewardActivity = rewardActivityMapper.selectById(rewardActivityId);
        assertPojoEquals(reqVO, rewardActivity);
    }

    @Test
    public void testUpdateRewardActivity_success() {
        // mock 数据
        RewardActivityDO dbRewardActivity = randomPojo(RewardActivityDO.class);
        rewardActivityMapper.insert(dbRewardActivity);// @Sql: 先插入出一条存在的数据
        // 准备参数
        RewardActivityUpdateReqVO reqVO = randomPojo(RewardActivityUpdateReqVO.class, o -> {
            o.setId(dbRewardActivity.getId()); // 设置更新的 ID
        });

        // 调用
        rewardActivityService.updateRewardActivity(reqVO);
        // 校验是否更新正确
        RewardActivityDO rewardActivity = rewardActivityMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, rewardActivity);
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
        RewardActivityDO dbRewardActivity = randomPojo(RewardActivityDO.class);
        rewardActivityMapper.insert(dbRewardActivity);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Integer id = dbRewardActivity.getId();

        // 调用
        rewardActivityService.deleteRewardActivity(id);
       // 校验数据不存在了
       assertNull(rewardActivityMapper.selectById(id));
    }

    @Test
    public void testDeleteRewardActivity_notExists() {
        // 准备参数
        Integer id = randomIntegerId();

        // 调用, 并断言异常
        assertServiceException(() -> rewardActivityService.deleteRewardActivity(id), REWARD_ACTIVITY_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetRewardActivityPage() {
       // mock 数据
       RewardActivityDO dbRewardActivity = randomPojo(RewardActivityDO.class, o -> { // 等会查询到
           o.setName(null);
           o.setStatus(null);
       });
       rewardActivityMapper.insert(dbRewardActivity);
       // 测试 name 不匹配
       rewardActivityMapper.insert(cloneIgnoreId(dbRewardActivity, o -> o.setName(null)));
       // 测试 status 不匹配
       rewardActivityMapper.insert(cloneIgnoreId(dbRewardActivity, o -> o.setStatus(null)));
       // 准备参数
       RewardActivityPageReqVO reqVO = new RewardActivityPageReqVO();
       reqVO.setName(null);
       reqVO.setStatus(null);

       // 调用
       PageResult<RewardActivityDO> pageResult = rewardActivityService.getRewardActivityPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbRewardActivity, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetRewardActivityList() {
       // mock 数据
       RewardActivityDO dbRewardActivity = randomPojo(RewardActivityDO.class, o -> { // 等会查询到
           o.setName(null);
           o.setStatus(null);
       });
       rewardActivityMapper.insert(dbRewardActivity);
       // 测试 name 不匹配
       rewardActivityMapper.insert(cloneIgnoreId(dbRewardActivity, o -> o.setName(null)));
       // 测试 status 不匹配
       rewardActivityMapper.insert(cloneIgnoreId(dbRewardActivity, o -> o.setStatus(null)));
       // 准备参数
       RewardActivityExportReqVO reqVO = new RewardActivityExportReqVO();
       reqVO.setName(null);
       reqVO.setStatus(null);

       // 调用
       List<RewardActivityDO> list = rewardActivityService.getRewardActivityList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbRewardActivity, list.get(0));
    }

}
