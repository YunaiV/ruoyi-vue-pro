package cn.iocoder.yudao.module.bpm.service.cc;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

@Import({BpmProcessInstanceCopyServiceImpl.class})
class BpmProcessInstanceCopyServiceTest extends BaseDbUnitTest {
    @Resource
    private BpmProcessInstanceCopyServiceImpl service;

    @Test
    void queryById() {
    }
}