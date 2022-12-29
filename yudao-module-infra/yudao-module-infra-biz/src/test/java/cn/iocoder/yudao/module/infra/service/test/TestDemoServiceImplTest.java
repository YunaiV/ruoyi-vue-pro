package cn.iocoder.yudao.module.infra.service.test;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.infra.controller.admin.test.vo.TestDemoCreateReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.test.vo.TestDemoExportReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.test.vo.TestDemoPageReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.test.vo.TestDemoUpdateReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.test.TestDemoDO;
import cn.iocoder.yudao.module.infra.dal.mysql.test.TestDemoMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.TEST_DEMO_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link TestDemoServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(TestDemoServiceImpl.class)
public class TestDemoServiceImplTest extends BaseDbUnitTest {

    @Resource
    private TestDemoServiceImpl testDemoService;

    @Resource
    private TestDemoMapper testDemoMapper;

    @Test
    public void testCreateTestDemo_success() {
        // 准备参数
        TestDemoCreateReqVO reqVO = randomPojo(TestDemoCreateReqVO.class);

        // 调用
        Long testDemoId = testDemoService.createTestDemo(reqVO);
        // 断言
        assertNotNull(testDemoId);
        // 校验记录的属性是否正确
        TestDemoDO testDemo = testDemoMapper.selectById(testDemoId);
        assertPojoEquals(reqVO, testDemo);
    }

    @Test
    public void testUpdateTestDemo_success() {
        // mock 数据
        TestDemoDO dbTestDemo = randomPojo(TestDemoDO.class);
        testDemoMapper.insert(dbTestDemo);// @Sql: 先插入出一条存在的数据
        // 准备参数
        TestDemoUpdateReqVO reqVO = randomPojo(TestDemoUpdateReqVO.class, o -> {
            o.setId(dbTestDemo.getId()); // 设置更新的 ID
        });

        // 调用
        testDemoService.updateTestDemo(reqVO);
        // 校验是否更新正确
        TestDemoDO testDemo = testDemoMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, testDemo);
    }

    @Test
    public void testUpdateTestDemo_notExists() {
        // 准备参数
        TestDemoUpdateReqVO reqVO = randomPojo(TestDemoUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> testDemoService.updateTestDemo(reqVO), TEST_DEMO_NOT_EXISTS);
    }

    @Test
    public void testDeleteTestDemo_success() {
        // mock 数据
        TestDemoDO dbTestDemo = randomPojo(TestDemoDO.class);
        testDemoMapper.insert(dbTestDemo);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbTestDemo.getId();

        // 调用
        testDemoService.deleteTestDemo(id);
        // 校验数据不存在了
        assertNull(testDemoMapper.selectById(id));
    }

    @Test
    public void testDeleteTestDemo_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> testDemoService.deleteTestDemo(id), TEST_DEMO_NOT_EXISTS);
    }

    @Test
    public void testGetTestDemoPage() {
        // mock 数据
        TestDemoDO dbTestDemo = randomPojo(TestDemoDO.class, o -> { // 等会查询到
            o.setName("芋道源码");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setType(1);
            o.setCategory(2);
            o.setRemark("哈哈哈");
            o.setCreateTime(buildTime(2021, 11, 11));
        });
        testDemoMapper.insert(dbTestDemo);
        // 测试 name 不匹配
        testDemoMapper.insert(cloneIgnoreId(dbTestDemo, o -> o.setName("不匹配")));
        // 测试 status 不匹配
        testDemoMapper.insert(cloneIgnoreId(dbTestDemo, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        // 测试 type 不匹配
        testDemoMapper.insert(cloneIgnoreId(dbTestDemo, o -> o.setType(2)));
        // 测试 category 不匹配
        testDemoMapper.insert(cloneIgnoreId(dbTestDemo, o -> o.setCategory(1)));
        // 测试 remark 不匹配
        testDemoMapper.insert(cloneIgnoreId(dbTestDemo, o -> o.setRemark("呵呵呵")));
        // 测试 createTime 不匹配
        testDemoMapper.insert(cloneIgnoreId(dbTestDemo, o -> o.setCreateTime(buildTime(2021, 12, 12))));
        // 准备参数
        TestDemoPageReqVO reqVO = new TestDemoPageReqVO();
        reqVO.setName("芋道");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setType(1);
        reqVO.setCategory(2);
        reqVO.setRemark("哈哈哈");
        reqVO.setCreateTime((new LocalDateTime[]{buildTime(2021, 11, 10),buildTime(2021, 11, 12)}));

        // 调用
        PageResult<TestDemoDO> pageResult = testDemoService.getTestDemoPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbTestDemo, pageResult.getList().get(0));
    }

    @Test
    public void testGetTestDemoList() {
        // mock 数据
        TestDemoDO dbTestDemo = randomPojo(TestDemoDO.class, o -> { // 等会查询到
            o.setName("芋道源码");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setType(1);
            o.setCategory(2);
            o.setRemark("哈哈哈");
            o.setCreateTime(buildTime(2021, 11, 11));
        });
        testDemoMapper.insert(dbTestDemo);
        // 测试 name 不匹配
        testDemoMapper.insert(cloneIgnoreId(dbTestDemo, o -> o.setName("不匹配")));
        // 测试 status 不匹配
        testDemoMapper.insert(cloneIgnoreId(dbTestDemo, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        // 测试 type 不匹配
        testDemoMapper.insert(cloneIgnoreId(dbTestDemo, o -> o.setType(2)));
        // 测试 category 不匹配
        testDemoMapper.insert(cloneIgnoreId(dbTestDemo, o -> o.setCategory(1)));
        // 测试 remark 不匹配
        testDemoMapper.insert(cloneIgnoreId(dbTestDemo, o -> o.setRemark("呵呵呵")));
        // 测试 createTime 不匹配
        testDemoMapper.insert(cloneIgnoreId(dbTestDemo, o -> o.setCreateTime(buildTime(2021, 12, 12))));
        // 准备参数
        TestDemoExportReqVO reqVO = new TestDemoExportReqVO();
        reqVO.setName("芋道");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setType(1);
        reqVO.setCategory(2);
        reqVO.setRemark("哈哈哈");
        reqVO.setCreateTime((new LocalDateTime[]{buildTime(2021, 11, 10),buildTime(2021, 11, 12)}));

        // 调用
        List<TestDemoDO> list = testDemoService.getTestDemoList(reqVO);
        // 断言
        assertEquals(1, list.size());
        assertPojoEquals(dbTestDemo, list.get(0));
    }

}
