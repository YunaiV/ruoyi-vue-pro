package cn.iocoder.yudao.module.bpm.service.cc;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

@Import({BpmProcessInstanceCopyServiceImpl.class})
class BpmProcessInstanceCopyServiceTest extends BaseDbUnitTest {
    @Resource
    private BpmProcessInstanceCopyServiceImpl service;

    @Test
    void queryById() {
    }
}