package cn.iocoder.yudao.adminserver.modules.tool.service.test.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.adminserver.modules.tool.controller.test.vo.*;
import cn.iocoder.yudao.adminserver.modules.tool.dal.dataobject.test.ToolTestDemoDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.adminserver.modules.tool.convert.test.ToolTestDemoConvert;
import cn.iocoder.yudao.adminserver.modules.tool.dal.mysql.test.ToolTestDemoMapper;
import cn.iocoder.yudao.adminserver.modules.tool.service.test.ToolTestDemoService;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.adminserver.modules.tool.enums.ToolErrorCodeConstants.*;

/**
 * 字典类型 Service 实现类
 *
 * @author 芋艿
 */
@Service
@Validated
public class ToolTestDemoServiceImpl implements ToolTestDemoService {

    @Resource
    private ToolTestDemoMapper testDemoMapper;

    @Override
    public Long createTestDemo(ToolTestDemoCreateReqVO createReqVO) {
        // 插入
        ToolTestDemoDO testDemo = ToolTestDemoConvert.INSTANCE.convert(createReqVO);
        testDemoMapper.insert(testDemo);
        // 返回
        return testDemo.getId();
    }

    @Override
    public void updateTestDemo(ToolTestDemoUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateTestDemoExists(updateReqVO.getId());
        // 更新
        ToolTestDemoDO updateObj = ToolTestDemoConvert.INSTANCE.convert(updateReqVO);
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
    public ToolTestDemoDO getTestDemo(Long id) {
        return testDemoMapper.selectById(id);
    }

    @Override
    public List<ToolTestDemoDO> getTestDemoList(Collection<Long> ids) {
        return testDemoMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<ToolTestDemoDO> getTestDemoPage(ToolTestDemoPageReqVO pageReqVO) {
        return testDemoMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ToolTestDemoDO> getTestDemoList(ToolTestDemoExportReqVO exportReqVO) {
        return testDemoMapper.selectList(exportReqVO);
    }

}
