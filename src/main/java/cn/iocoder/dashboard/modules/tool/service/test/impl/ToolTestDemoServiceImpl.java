package cn.iocoder.dashboard.modules.tool.service.test.impl;

import cn.iocoder.dashboard.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.tool.controller.test.vo.ToolTestDemoCreateReqVO;
import cn.iocoder.dashboard.modules.tool.controller.test.vo.ToolTestDemoExportReqVO;
import cn.iocoder.dashboard.modules.tool.controller.test.vo.ToolTestDemoPageReqVO;
import cn.iocoder.dashboard.modules.tool.controller.test.vo.ToolTestDemoUpdateReqVO;
import cn.iocoder.dashboard.modules.tool.convert.test.ToolTestDemoConvert;
import cn.iocoder.dashboard.modules.tool.dal.dataobject.test.ToolTestDemoDO;
import cn.iocoder.dashboard.modules.tool.dal.mysql.test.ToolTestDemoMapper;
import cn.iocoder.dashboard.modules.tool.service.test.ToolTestDemoService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.dashboard.modules.tool.enums.ToolErrorCodeConstants.TEST_DEMO_NOT_EXISTS;

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
        // 更新
        testDemoMapper.deleteById(id);
    }

    private void validateTestDemoExists(Long id) {
        if (testDemoMapper.selectById(id) == null) {
            throw ServiceExceptionUtil.exception(TEST_DEMO_NOT_EXISTS);
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
