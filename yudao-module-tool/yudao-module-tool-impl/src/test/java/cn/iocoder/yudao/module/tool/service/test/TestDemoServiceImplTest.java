package cn.iocoder.yudao.module.tool.service.test;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.module.tool.test.BaseDbUnitTest;
import cn.iocoder.yudao.module.tool.controller.admin.test.vo.*;
import cn.iocoder.yudao.module.tool.dal.dataobject.test.TestDemoDO;
import cn.iocoder.yudao.module.tool.dal.mysql.test.TestDemoMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import javax.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;

import static cn.hutool.core.util.RandomUtil.*;
import static cn.iocoder.yudao.module.tool.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    @Test // TODO 请修改 null 为需要的值
    public void testGetTestDemoPage() {
       // mock 数据
       TestDemoDO dbTestDemo = randomPojo(TestDemoDO.class, o -> { // 等会查询到
           o.setName(null);
           o.setStatus(null);
           o.setType(null);
           o.setCategory(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       testDemoMapper.insert(dbTestDemo);
       // 测试 name 不匹配
       testDemoMapper.insert(cloneIgnoreId(dbTestDemo, o -> o.setName(null)));
       // 测试 status 不匹配
       testDemoMapper.insert(cloneIgnoreId(dbTestDemo, o -> o.setStatus(null)));
       // 测试 type 不匹配
       testDemoMapper.insert(cloneIgnoreId(dbTestDemo, o -> o.setType(null)));
       // 测试 category 不匹配
       testDemoMapper.insert(cloneIgnoreId(dbTestDemo, o -> o.setCategory(null)));
       // 测试 remark 不匹配
       testDemoMapper.insert(cloneIgnoreId(dbTestDemo, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       testDemoMapper.insert(cloneIgnoreId(dbTestDemo, o -> o.setCreateTime(null)));
       // 准备参数
       TestDemoPageReqVO reqVO = new TestDemoPageReqVO();
       reqVO.setName(null);
       reqVO.setStatus(null);
       reqVO.setType(null);
       reqVO.setCategory(null);
       reqVO.setRemark(null);
       reqVO.setBeginCreateTime(null);
       reqVO.setEndCreateTime(null);

       // 调用
       PageResult<TestDemoDO> pageResult = testDemoService.getTestDemoPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbTestDemo, pageResult.getList().get(0));
    }

    @Test // TODO 请修改 null 为需要的值
    public void testGetTestDemoList() {
       // mock 数据
       TestDemoDO dbTestDemo = randomPojo(TestDemoDO.class, o -> { // 等会查询到
           o.setName(null);
           o.setStatus(null);
           o.setType(null);
           o.setCategory(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       testDemoMapper.insert(dbTestDemo);
       // 测试 name 不匹配
       testDemoMapper.insert(cloneIgnoreId(dbTestDemo, o -> o.setName(null)));
       // 测试 status 不匹配
       testDemoMapper.insert(cloneIgnoreId(dbTestDemo, o -> o.setStatus(null)));
       // 测试 type 不匹配
       testDemoMapper.insert(cloneIgnoreId(dbTestDemo, o -> o.setType(null)));
       // 测试 category 不匹配
       testDemoMapper.insert(cloneIgnoreId(dbTestDemo, o -> o.setCategory(null)));
       // 测试 remark 不匹配
       testDemoMapper.insert(cloneIgnoreId(dbTestDemo, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       testDemoMapper.insert(cloneIgnoreId(dbTestDemo, o -> o.setCreateTime(null)));
       // 准备参数
       TestDemoExportReqVO reqVO = new TestDemoExportReqVO();
       reqVO.setName(null);
       reqVO.setStatus(null);
       reqVO.setType(null);
       reqVO.setCategory(null);
       reqVO.setRemark(null);
       reqVO.setBeginCreateTime(null);
       reqVO.setEndCreateTime(null);

       // 调用
       List<TestDemoDO> list = testDemoService.getTestDemoList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbTestDemo, list.get(0));
    }

}
