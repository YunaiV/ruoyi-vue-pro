package cn.iocoder.yudao.module.promotion.service.seckillactivity;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.SeckillActivityDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.seckill.seckillactivity.SeckillActivityMapper;
import cn.iocoder.yudao.module.promotion.service.seckill.SeckillActivityServiceImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.SECKILL_ACTIVITY_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

/**
* {@link SeckillActivityServiceImpl} 的单元测试类
*
* @author 芋道源码
*/
@Import(SeckillActivityServiceImpl.class)
@Disabled // TODO 芋艿：未来开启
public class SeckillActivityServiceImplTest extends BaseDbUnitTest {

    @Resource
    private SeckillActivityServiceImpl seckillActivityService;

    @Resource
    private SeckillActivityMapper seckillActivityMapper;

    @Test
    public void testCreateSeckillActivity_success() {
        // 准备参数
        SeckillActivityCreateReqVO reqVO = randomPojo(SeckillActivityCreateReqVO.class);

        // 调用
        Long seckillActivityId = seckillActivityService.createSeckillActivity(reqVO);
        // 断言
        assertNotNull(seckillActivityId);
        // 校验记录的属性是否正确
        SeckillActivityDO seckillActivity = seckillActivityMapper.selectById(seckillActivityId);
        assertPojoEquals(reqVO, seckillActivity);
    }

    @Test
    public void testUpdateSeckillActivity_success() {
        // mock 数据
        SeckillActivityDO dbSeckillActivity = randomPojo(SeckillActivityDO.class);
        seckillActivityMapper.insert(dbSeckillActivity);// @Sql: 先插入出一条存在的数据
        // 准备参数
        SeckillActivityUpdateReqVO reqVO = randomPojo(SeckillActivityUpdateReqVO.class, o -> {
            o.setId(dbSeckillActivity.getId()); // 设置更新的 ID
        });

        // 调用
        seckillActivityService.updateSeckillActivity(reqVO);
        // 校验是否更新正确
        SeckillActivityDO seckillActivity = seckillActivityMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, seckillActivity);
    }

    @Test
    public void testUpdateSeckillActivity_notExists() {
        // 准备参数
        SeckillActivityUpdateReqVO reqVO = randomPojo(SeckillActivityUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> seckillActivityService.updateSeckillActivity(reqVO), SECKILL_ACTIVITY_NOT_EXISTS);
    }

    @Test
    public void testDeleteSeckillActivity_success() {
        // mock 数据
        SeckillActivityDO dbSeckillActivity = randomPojo(SeckillActivityDO.class);
        seckillActivityMapper.insert(dbSeckillActivity);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbSeckillActivity.getId();

        // 调用
        seckillActivityService.deleteSeckillActivity(id);
       // 校验数据不存在了
       assertNull(seckillActivityMapper.selectById(id));
    }

    @Test
    public void testDeleteSeckillActivity_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> seckillActivityService.deleteSeckillActivity(id), SECKILL_ACTIVITY_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetSeckillActivityPage() {
       // mock 数据
       SeckillActivityDO dbSeckillActivity = randomPojo(SeckillActivityDO.class, o -> { // 等会查询到
           o.setName(null);
           o.setStatus(null);
           o.setConfigIds(null);
           o.setCreateTime(null);
       });
       seckillActivityMapper.insert(dbSeckillActivity);
       // 测试 name 不匹配
        seckillActivityMapper.insert(cloneIgnoreId(dbSeckillActivity, o -> o.setName(null)));
        // 测试 status 不匹配
        seckillActivityMapper.insert(cloneIgnoreId(dbSeckillActivity, o -> o.setStatus(null)));
        // 测试 timeId 不匹配
        seckillActivityMapper.insert(cloneIgnoreId(dbSeckillActivity, o -> o.setConfigIds(null)));
        // 测试 createTime 不匹配
        seckillActivityMapper.insert(cloneIgnoreId(dbSeckillActivity, o -> o.setCreateTime(null)));
        // 准备参数
        SeckillActivityPageReqVO reqVO = new SeckillActivityPageReqVO();
        reqVO.setName(null);
        reqVO.setStatus(null);
        reqVO.setConfigId(null);
        reqVO.setCreateTime((new LocalDateTime[]{}));

        // 调用
        PageResult<SeckillActivityDO> pageResult = seckillActivityService.getSeckillActivityPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbSeckillActivity, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetSeckillActivityList() {
       // mock 数据
       SeckillActivityDO dbSeckillActivity = randomPojo(SeckillActivityDO.class, o -> { // 等会查询到
           o.setName(null);
           o.setStatus(null);
           o.setConfigIds(null);
           o.setCreateTime(null);
       });
       seckillActivityMapper.insert(dbSeckillActivity);
       // 测试 name 不匹配
       seckillActivityMapper.insert(cloneIgnoreId(dbSeckillActivity, o -> o.setName(null)));
       // 测试 status 不匹配
       seckillActivityMapper.insert(cloneIgnoreId(dbSeckillActivity, o -> o.setStatus(null)));
       // 测试 timeId 不匹配
        seckillActivityMapper.insert(cloneIgnoreId(dbSeckillActivity, o -> o.setConfigIds(null)));
       // 测试 createTime 不匹配
       seckillActivityMapper.insert(cloneIgnoreId(dbSeckillActivity, o -> o.setCreateTime(null)));
       // 准备参数
//       SeckillActivityExportReqVO reqVO = new SeckillActivityExportReqVO();
//       reqVO.setName(null);
//       reqVO.setStatus(null);
//       reqVO.setTimeId(null);
//       reqVO.setCreateTime((new Date[]{}));
//
//       // 调用
//       List<SeckillActivityDO> list = seckillActivityService.getSeckillActivityList(reqVO);
//       // 断言
//       assertEquals(1, list.size());
//       assertPojoEquals(dbSeckillActivity, list.get(0));
    }

}
