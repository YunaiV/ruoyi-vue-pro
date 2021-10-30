package cn.iocoder.yudao.coreservice.modules.infra.service.logger;

import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.coreservice.BaseDbUnitTest;
import cn.iocoder.yudao.coreservice.modules.infra.dal.dataobject.logger.InfApiAccessLogDO;
import cn.iocoder.yudao.coreservice.modules.infra.dal.mysql.logger.InfApiAccessLogCoreMapper;
import cn.iocoder.yudao.coreservice.modules.infra.service.logger.impl.InfApiAccessLogCoreServiceImpl;
import cn.iocoder.yudao.framework.apilog.core.service.dto.ApiAccessLogCreateReqDTO;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.test.core.util.RandomUtils;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link InfApiAccessLogCoreServiceImpl} 单元测试
 */
@Import(InfApiAccessLogCoreServiceImpl.class)
public class InfApiAccessLogCoreServiceTest extends BaseDbUnitTest {

    @Resource
    private InfApiAccessLogCoreService apiAccessLogCoreService;

    @Resource
    private InfApiAccessLogCoreMapper apiAccessLogCoreMapper;

    @Test
    public void testCreateApiAccessLogAsync() {
        // 准备参数
        ApiAccessLogCreateReqDTO createDTO = RandomUtils.randomPojo(ApiAccessLogCreateReqDTO.class,
                dto -> dto.setUserType(RandomUtil.randomEle(UserTypeEnum.values()).getValue()));

        // 调用
        apiAccessLogCoreService.createApiAccessLogAsync(createDTO);
        // 断言
        InfApiAccessLogDO infApiAccessLogDO = apiAccessLogCoreMapper.selectOne(null);
        assertNotNull(infApiAccessLogDO);
        assertPojoEquals(createDTO, infApiAccessLogDO);
    }

}
