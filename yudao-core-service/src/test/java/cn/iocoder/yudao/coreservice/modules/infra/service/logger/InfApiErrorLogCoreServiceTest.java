package cn.iocoder.yudao.coreservice.modules.infra.service.logger;

import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.coreservice.BaseDbUnitTest;
import cn.iocoder.yudao.coreservice.modules.infra.dal.dataobject.logger.InfApiErrorLogDO;
import cn.iocoder.yudao.coreservice.modules.infra.dal.mysql.logger.InfApiErrorLogCoreMapper;
import cn.iocoder.yudao.coreservice.modules.infra.service.logger.impl.InfApiErrorLogCoreServiceImpl;
import cn.iocoder.yudao.framework.apilog.core.service.dto.ApiErrorLogCreateDTO;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.test.core.util.RandomUtils;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link InfApiErrorLogCoreServiceImpl} 单元测试
 */
@Import(InfApiErrorLogCoreServiceImpl.class)
public class InfApiErrorLogCoreServiceTest extends BaseDbUnitTest {

    @Resource
    private InfApiErrorLogCoreService apiErrorLogCoreService;

    @Resource
    private InfApiErrorLogCoreMapper infApiErrorLogCoreMapper;

    @Test
    public void testCreateApiErrorLogAsync() {
        // 准备参数
        ApiErrorLogCreateDTO createDTO = RandomUtils.randomPojo(ApiErrorLogCreateDTO.class,
                dto -> dto.setUserType(RandomUtil.randomEle(UserTypeEnum.values()).getValue()));

        // 调用
        apiErrorLogCoreService.createApiErrorLogAsync(createDTO);
        // 断言
        InfApiErrorLogDO infApiErrorLogDO = infApiErrorLogCoreMapper.selectOne(null);
        assertNotNull(infApiErrorLogDO);
        assertPojoEquals(createDTO, infApiErrorLogDO);
    }

}
