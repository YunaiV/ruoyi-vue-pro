package cn.iocoder.yudao.module.promotion.service.discount;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.promotion.controller.admin.discount.vo.DiscountActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.discount.vo.DiscountActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.discount.vo.DiscountActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.discount.DiscountActivityDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.discount.DiscountActivityMapper;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionActivityStatusEnum;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Date;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.addTime;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.DISCOUNT_ACTIVITY_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

/**
* {@link DiscountActivityServiceImpl} 的单元测试类
*
* @author 芋道源码
*/
@Import(DiscountActivityServiceImpl.class)
public class DiscountActivityServiceImplTest extends BaseDbUnitTest {

    @Resource
    private DiscountActivityServiceImpl discountActivityService;

    @Resource
    private DiscountActivityMapper discountActivityMapper;

    @Test
    public void testCreateDiscountActivity_success() {
        // 准备参数
        DiscountActivityCreateReqVO reqVO = randomPojo(DiscountActivityCreateReqVO.class, o -> {
            // 用于触发进行中的状态
            o.setStartTime(addTime(Duration.ofDays(1))).setEndTime(addTime(Duration.ofDays(2)));
        });

        // 调用
        Long discountActivityId = discountActivityService.createDiscountActivity(reqVO);
        // 断言
        assertNotNull(discountActivityId);
        // 校验记录的属性是否正确
        DiscountActivityDO discountActivity = discountActivityMapper.selectById(discountActivityId);
        assertPojoEquals(reqVO, discountActivity);
    }

    @Test
    public void testUpdateDiscountActivity_success() {
        // mock 数据
        DiscountActivityDO dbDiscountActivity = randomPojo(DiscountActivityDO.class);
        discountActivityMapper.insert(dbDiscountActivity);// @Sql: 先插入出一条存在的数据
        // 准备参数
        DiscountActivityUpdateReqVO reqVO = randomPojo(DiscountActivityUpdateReqVO.class, o -> {
            o.setId(dbDiscountActivity.getId()); // 设置更新的 ID
        });

        // 调用
        discountActivityService.updateDiscountActivity(reqVO);
        // 校验是否更新正确
        DiscountActivityDO discountActivity = discountActivityMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, discountActivity);
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
        DiscountActivityDO dbDiscountActivity = randomPojo(DiscountActivityDO.class);
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
       reqVO.setCreateTime((new Date[]{buildTime(2021, 1, 1), buildTime(2021, 1, 31)}));

       // 调用
       PageResult<DiscountActivityDO> pageResult = discountActivityService.getDiscountActivityPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbDiscountActivity, pageResult.getList().get(0));
    }

}
