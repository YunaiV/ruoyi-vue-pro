package cn.iocoder.dashboard.modules.system.service.test.impl;

import cn.iocoder.dashboard.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.test.vo.SysTestDemoCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.test.vo.SysTestDemoPageReqVO;
import cn.iocoder.dashboard.modules.system.controller.test.vo.SysTestDemoUpdateReqVO;
import cn.iocoder.dashboard.modules.system.convert.test.SysTestDemoConvert;
import cn.iocoder.dashboard.modules.system.dal.mysql.test.SysTestDemoMapper;
import cn.iocoder.dashboard.modules.system.dal.dataobject.test.SysTestDemoDO;
import cn.iocoder.dashboard.modules.system.service.test.SysTestDemoService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.dashboard.modules.system.enums.SysErrorCodeConstants.TEST_DEMO_NOT_EXISTS;

/**
* 字典类型 Service 实现类
*
* @author 芋艿
*/
@Service
@Validated
public class SysTestDemoServiceImpl implements SysTestDemoService {

    @Resource
    private SysTestDemoMapper testDemoMapper;

    @Override
    public Long createTestDemo(SysTestDemoCreateReqVO createReqVO) {
        // 插入
        SysTestDemoDO testDemo = SysTestDemoConvert.INSTANCE.convert(createReqVO);
        testDemoMapper.insert(testDemo);
        // 返回
        return testDemo.getId();
    }

    @Override
    public void updateTestDemo(SysTestDemoUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateTestDemoExists(updateReqVO.getId());
        // 更新
        SysTestDemoDO updateObj = SysTestDemoConvert.INSTANCE.convert(updateReqVO);
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
    public SysTestDemoDO getTestDemo(Long id) {
        return testDemoMapper.selectById(id);
    }

    @Override
    public List<SysTestDemoDO> getTestDemoList(Collection<Long> ids) {
        return testDemoMapper.selectBatchIds(ids);
    }

	@Override
    public PageResult<SysTestDemoDO> getTestDemoPage(SysTestDemoPageReqVO pageReqVO) {
		return testDemoMapper.selectPage(pageReqVO);
    }

}
