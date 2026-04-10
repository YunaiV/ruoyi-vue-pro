package cn.iocoder.yudao.module.promotion.service.combination;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity.CombinationActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity.CombinationActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity.CombinationActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationActivityDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.combination.CombinationActivityMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import jakarta.annotation.Resource;

import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.COMBINATION_ACTIVITY_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

// TODO 芋艿：等完成后，在补全单测
/**
 * {@link CombinationActivityServiceImpl} 的单元测试类
 *
 * @author HUIHUI
 */
@Disabled // TODO 芋艿：后续 fix 补充的单测
@Import(CombinationActivityServiceImpl.class)
public class CombinationActivityServiceImplTest extends BaseDbUnitTest {

    @Resource
    private CombinationActivityServiceImpl combinationActivityService;

    @Resource
    private CombinationActivityMapper combinationActivityMapper;

    @Test
    public void testCreateCombinationActivity_success() {
        // 准备参数
        CombinationActivityCreateReqVO reqVO = randomPojo(CombinationActivityCreateReqVO.class);

        // 调用
        Long combinationActivityId = combinationActivityService.createCombinationActivity(reqVO);
        // 断言
        assertNotNull(combinationActivityId);
        // 校验记录的属性是否正确
        CombinationActivityDO combinationActivity = combinationActivityMapper.selectById(combinationActivityId);
        assertPojoEquals(reqVO, combinationActivity);
    }

    @Test
    public void testUpdateCombinationActivity_success() {
        // mock 数据
        CombinationActivityDO dbCombinationActivity = randomPojo(CombinationActivityDO.class);
        combinationActivityMapper.insert(dbCombinationActivity);// @Sql: 先插入出一条存在的数据
        // 准备参数
        CombinationActivityUpdateReqVO reqVO = randomPojo(CombinationActivityUpdateReqVO.class, o -> {
            o.setId(dbCombinationActivity.getId()); // 设置更新的 ID
        });

        // 调用
        combinationActivityService.updateCombinationActivity(reqVO);
        // 校验是否更新正确
        CombinationActivityDO combinationActivity = combinationActivityMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, combinationActivity);
    }

    @Test
    public void testUpdateCombinationActivity_notExists() {
        // 准备参数
        CombinationActivityUpdateReqVO reqVO = randomPojo(CombinationActivityUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> combinationActivityService.updateCombinationActivity(reqVO), COMBINATION_ACTIVITY_NOT_EXISTS);
    }

    @Test
    public void testDeleteCombinationActivity_success() {
        // mock 数据
        CombinationActivityDO dbCombinationActivity = randomPojo(CombinationActivityDO.class);
        combinationActivityMapper.insert(dbCombinationActivity);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbCombinationActivity.getId();

        // 调用
        combinationActivityService.deleteCombinationActivity(id);
        // 校验数据不存在了
        assertNull(combinationActivityMapper.selectById(id));
    }

    @Test
    public void testDeleteCombinationActivity_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> combinationActivityService.deleteCombinationActivity(id), COMBINATION_ACTIVITY_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetCombinationActivityPage() {
        // mock 数据
        CombinationActivityDO dbCombinationActivity = randomPojo(CombinationActivityDO.class, o -> { // 等会查询到
            o.setName(null);
            //o.setSpuId(null);
            o.setTotalLimitCount(null);
            o.setSingleLimitCount(null);
            o.setStartTime(null);
            o.setEndTime(null);
            o.setUserSize(null);
            o.setVirtualGroup(null);
            o.setStatus(null);
            o.setLimitDuration(null);
            o.setCreateTime(null);
        });
        combinationActivityMapper.insert(dbCombinationActivity);
        // 测试 name 不匹配
        combinationActivityMapper.insert(cloneIgnoreId(dbCombinationActivity, o -> o.setName(null)));
        // 测试 spuId 不匹配
        //combinationActivityMapper.insert(cloneIgnoreId(dbCombinationActivity, o -> o.setSpuId(null)));
        // 测试 totalLimitCount 不匹配
        combinationActivityMapper.insert(cloneIgnoreId(dbCombinationActivity, o -> o.setTotalLimitCount(null)));
        // 测试 singleLimitCount 不匹配
        combinationActivityMapper.insert(cloneIgnoreId(dbCombinationActivity, o -> o.setSingleLimitCount(null)));
        // 测试 startTime 不匹配
        combinationActivityMapper.insert(cloneIgnoreId(dbCombinationActivity, o -> o.setStartTime(null)));
        // 测试 endTime 不匹配
        combinationActivityMapper.insert(cloneIgnoreId(dbCombinationActivity, o -> o.setEndTime(null)));
        // 测试 userSize 不匹配
        combinationActivityMapper.insert(cloneIgnoreId(dbCombinationActivity, o -> o.setUserSize(null)));
        // 测试 virtualGroup 不匹配
        combinationActivityMapper.insert(cloneIgnoreId(dbCombinationActivity, o -> o.setVirtualGroup(null)));
        // 测试 status 不匹配
        combinationActivityMapper.insert(cloneIgnoreId(dbCombinationActivity, o -> o.setStatus(null)));
        // 测试 limitDuration 不匹配
        combinationActivityMapper.insert(cloneIgnoreId(dbCombinationActivity, o -> o.setLimitDuration(null)));
        // 测试 createTime 不匹配
        combinationActivityMapper.insert(cloneIgnoreId(dbCombinationActivity, o -> o.setCreateTime(null)));
        // 准备参数
        CombinationActivityPageReqVO reqVO = new CombinationActivityPageReqVO();
        reqVO.setName(null);
        reqVO.setStatus(null);

        // 调用
        PageResult<CombinationActivityDO> pageResult = combinationActivityService.getCombinationActivityPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbCombinationActivity, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetCombinationActivityList() {
        // mock 数据
        CombinationActivityDO dbCombinationActivity = randomPojo(CombinationActivityDO.class, o -> { // 等会查询到
            o.setName(null);
            //o.setSpuId(null);
            o.setTotalLimitCount(null);
            o.setSingleLimitCount(null);
            o.setStartTime(null);
            o.setEndTime(null);
            o.setUserSize(null);
            o.setVirtualGroup(null);
            o.setStatus(null);
            o.setLimitDuration(null);
            o.setCreateTime(null);
        });
        combinationActivityMapper.insert(dbCombinationActivity);
        // 测试 name 不匹配
        combinationActivityMapper.insert(cloneIgnoreId(dbCombinationActivity, o -> o.setName(null)));
        // 测试 spuId 不匹配
        //combinationActivityMapper.insert(cloneIgnoreId(dbCombinationActivity, o -> o.setSpuId(null)));
        // 测试 totalLimitCount 不匹配
        combinationActivityMapper.insert(cloneIgnoreId(dbCombinationActivity, o -> o.setTotalLimitCount(null)));
        // 测试 singleLimitCount 不匹配
        combinationActivityMapper.insert(cloneIgnoreId(dbCombinationActivity, o -> o.setSingleLimitCount(null)));
        // 测试 startTime 不匹配
        combinationActivityMapper.insert(cloneIgnoreId(dbCombinationActivity, o -> o.setStartTime(null)));
        // 测试 endTime 不匹配
        combinationActivityMapper.insert(cloneIgnoreId(dbCombinationActivity, o -> o.setEndTime(null)));
        // 测试 userSize 不匹配
        combinationActivityMapper.insert(cloneIgnoreId(dbCombinationActivity, o -> o.setUserSize(null)));
        // 测试 virtualGroup 不匹配
        combinationActivityMapper.insert(cloneIgnoreId(dbCombinationActivity, o -> o.setVirtualGroup(null)));
        // 测试 status 不匹配
        combinationActivityMapper.insert(cloneIgnoreId(dbCombinationActivity, o -> o.setStatus(null)));
        // 测试 limitDuration 不匹配
        combinationActivityMapper.insert(cloneIgnoreId(dbCombinationActivity, o -> o.setLimitDuration(null)));
        // 测试 createTime 不匹配
        combinationActivityMapper.insert(cloneIgnoreId(dbCombinationActivity, o -> o.setCreateTime(null)));

    }

}
