package cn.iocoder.yudao.module.tool.service.test;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.tool.controller.admin.test.vo.*;
import cn.iocoder.yudao.module.tool.dal.dataobject.test.TestDemoDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.tool.convert.test.TestDemoConvert;
import cn.iocoder.yudao.module.tool.dal.mysql.test.TestDemoMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.tool.enums.ErrorCodeConstants.*;

/**
 * 字典类型 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class TestDemoServiceImpl implements TestDemoService {

    @Resource
    private TestDemoMapper testDemoMapper;

    @Override
    public Long createTestDemo(TestDemoCreateReqVO createReqVO) {
        // 插入
        TestDemoDO testDemo = TestDemoConvert.INSTANCE.convert(createReqVO);
        testDemoMapper.insert(testDemo);
        // 返回
        return testDemo.getId();
    }

    @Override
    public void updateTestDemo(TestDemoUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateTestDemoExists(updateReqVO.getId());
        // 更新
        TestDemoDO updateObj = TestDemoConvert.INSTANCE.convert(updateReqVO);
        testDemoMapper.updateById(updateObj);
    }

    @Override
    public void deleteTestDemo(Long id) {
        // 校验存在
        this.validateTestDemoExists(id);
        // 删除
        testDemoMapper.deleteById(id);
    }

    private void validateTestDemoExists(Long id) {
        if (testDemoMapper.selectById(id) == null) {
            throw exception(TEST_DEMO_NOT_EXISTS);
        }
    }

    @Override
    public TestDemoDO getTestDemo(Long id) {
        return testDemoMapper.selectById(id);
    }

    @Override
    public List<TestDemoDO> getTestDemoList(Collection<Long> ids) {
        return testDemoMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<TestDemoDO> getTestDemoPage(TestDemoPageReqVO pageReqVO) {
        return testDemoMapper.selectPage(pageReqVO);
    }

    @Override
    public List<TestDemoDO> getTestDemoList(TestDemoExportReqVO exportReqVO) {
        return testDemoMapper.selectList(exportReqVO);
    }

}
