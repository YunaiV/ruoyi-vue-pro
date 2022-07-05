package cn.iocoder.yudao.module.market.service.activity;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.market.controller.admin.activity.vo.*;
import cn.iocoder.yudao.module.market.dal.dataobject.activity.ActivityDO;
import cn.iocoder.yudao.module.market.dal.mysql.activity.ActivityMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.springframework.context.annotation.Import;

import static cn.iocoder.yudao.module.market.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
* {@link ActivityServiceImpl} 的单元测试类
*
* @author 芋道源码
*/
@Import(ActivityServiceImpl.class)
public class ActivityServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ActivityServiceImpl activityService;

    @Resource
    private ActivityMapper activityMapper;

    @Test
    public void testCreateActivity_success() {
        // 准备参数
        ActivityCreateReqVO reqVO = randomPojo(ActivityCreateReqVO.class);

        // 调用
        Long activityId = activityService.createActivity(reqVO);
        // 断言
        assertNotNull(activityId);
        // 校验记录的属性是否正确
        ActivityDO activity = activityMapper.selectById(activityId);
        assertPojoEquals(reqVO, activity);
    }

    @Test
    public void testUpdateActivity_success() {
        // mock 数据
        ActivityDO dbActivity = randomPojo(ActivityDO.class);
        activityMapper.insert(dbActivity);// @Sql: 先插入出一条存在的数据
        // 准备参数
        ActivityUpdateReqVO reqVO = randomPojo(ActivityUpdateReqVO.class, o -> {
            o.setId(dbActivity.getId()); // 设置更新的 ID
        });

        // 调用
        activityService.updateActivity(reqVO);
        // 校验是否更新正确
        ActivityDO activity = activityMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, activity);
    }

    @Test
    public void testUpdateActivity_notExists() {
        // 准备参数
        ActivityUpdateReqVO reqVO = randomPojo(ActivityUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> activityService.updateActivity(reqVO), ACTIVITY_NOT_EXISTS);
    }

    @Test
    public void testDeleteActivity_success() {
        // mock 数据
        ActivityDO dbActivity = randomPojo(ActivityDO.class);
        activityMapper.insert(dbActivity);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbActivity.getId();

        // 调用
        activityService.deleteActivity(id);
       // 校验数据不存在了
       assertNull(activityMapper.selectById(id));
    }

    @Test
    public void testDeleteActivity_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> activityService.deleteActivity(id), ACTIVITY_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetActivityPage() {
       // mock 数据
       ActivityDO dbActivity = randomPojo(ActivityDO.class, o -> { // 等会查询到
           o.setTitle(null);
           o.setActivityType(null);
           o.setStatus(null);
           o.setStartTime(null);
           o.setEndTime(null);
           o.setInvalidTime(null);
           o.setDeleteTime(null);
           o.setTimeLimitedDiscount(null);
           o.setFullPrivilege(null);
           o.setCreateTime(null);
       });
       activityMapper.insert(dbActivity);
       // 测试 title 不匹配
       activityMapper.insert(cloneIgnoreId(dbActivity, o -> o.setTitle(null)));
       // 测试 activityType 不匹配
       activityMapper.insert(cloneIgnoreId(dbActivity, o -> o.setActivityType(null)));
       // 测试 status 不匹配
       activityMapper.insert(cloneIgnoreId(dbActivity, o -> o.setStatus(null)));
       // 测试 startTime 不匹配
       activityMapper.insert(cloneIgnoreId(dbActivity, o -> o.setStartTime(null)));
       // 测试 endTime 不匹配
       activityMapper.insert(cloneIgnoreId(dbActivity, o -> o.setEndTime(null)));
       // 测试 invalidTime 不匹配
       activityMapper.insert(cloneIgnoreId(dbActivity, o -> o.setInvalidTime(null)));
       // 测试 deleteTime 不匹配
       activityMapper.insert(cloneIgnoreId(dbActivity, o -> o.setDeleteTime(null)));
       // 测试 timeLimitedDiscount 不匹配
       activityMapper.insert(cloneIgnoreId(dbActivity, o -> o.setTimeLimitedDiscount(null)));
       // 测试 fullPrivilege 不匹配
       activityMapper.insert(cloneIgnoreId(dbActivity, o -> o.setFullPrivilege(null)));
       // 测试 createTime 不匹配
       activityMapper.insert(cloneIgnoreId(dbActivity, o -> o.setCreateTime(null)));
       // 准备参数
       ActivityPageReqVO reqVO = new ActivityPageReqVO();
       reqVO.setTitle(null);
       reqVO.setActivityType(null);
       reqVO.setStatus(null);
       reqVO.setBeginStartTime(null);
       reqVO.setEndStartTime(null);
       reqVO.setBeginEndTime(null);
       reqVO.setEndEndTime(null);
       reqVO.setBeginInvalidTime(null);
       reqVO.setEndInvalidTime(null);
       reqVO.setBeginDeleteTime(null);
       reqVO.setEndDeleteTime(null);
       reqVO.setTimeLimitedDiscount(null);
       reqVO.setFullPrivilege(null);
       reqVO.setBeginCreateTime(null);
       reqVO.setEndCreateTime(null);

       // 调用
       PageResult<ActivityDO> pageResult = activityService.getActivityPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbActivity, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetActivityList() {
       // mock 数据
       ActivityDO dbActivity = randomPojo(ActivityDO.class, o -> { // 等会查询到
           o.setTitle(null);
           o.setActivityType(null);
           o.setStatus(null);
           o.setStartTime(null);
           o.setEndTime(null);
           o.setInvalidTime(null);
           o.setDeleteTime(null);
           o.setTimeLimitedDiscount(null);
           o.setFullPrivilege(null);
           o.setCreateTime(null);
       });
       activityMapper.insert(dbActivity);
       // 测试 title 不匹配
       activityMapper.insert(cloneIgnoreId(dbActivity, o -> o.setTitle(null)));
       // 测试 activityType 不匹配
       activityMapper.insert(cloneIgnoreId(dbActivity, o -> o.setActivityType(null)));
       // 测试 status 不匹配
       activityMapper.insert(cloneIgnoreId(dbActivity, o -> o.setStatus(null)));
       // 测试 startTime 不匹配
       activityMapper.insert(cloneIgnoreId(dbActivity, o -> o.setStartTime(null)));
       // 测试 endTime 不匹配
       activityMapper.insert(cloneIgnoreId(dbActivity, o -> o.setEndTime(null)));
       // 测试 invalidTime 不匹配
       activityMapper.insert(cloneIgnoreId(dbActivity, o -> o.setInvalidTime(null)));
       // 测试 deleteTime 不匹配
       activityMapper.insert(cloneIgnoreId(dbActivity, o -> o.setDeleteTime(null)));
       // 测试 timeLimitedDiscount 不匹配
       activityMapper.insert(cloneIgnoreId(dbActivity, o -> o.setTimeLimitedDiscount(null)));
       // 测试 fullPrivilege 不匹配
       activityMapper.insert(cloneIgnoreId(dbActivity, o -> o.setFullPrivilege(null)));
       // 测试 createTime 不匹配
       activityMapper.insert(cloneIgnoreId(dbActivity, o -> o.setCreateTime(null)));
    }

}
