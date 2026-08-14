package cn.iocoder.yudao.module.hrm.service.config;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.dal.dataobject.config.HrmConfigDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.config.HrmConfigMapper;
import cn.iocoder.yudao.module.hrm.enums.config.HrmConfigTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link HrmConfigServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmConfigServiceImpl.class)
public class HrmConfigServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmConfigServiceImpl configService;
    @Resource
    private HrmConfigMapper hrmConfigMapper;

    @Test
    public void testReplaceConfigValueList_success() {
        // mock 数据
        hrmConfigMapper.insert(randomPojo(HrmConfigDO.class, config -> config.setId(null)
                .setType(HrmConfigTypeEnum.RECRUIT_ELIMINATE.getType()).setValue("旧配置").setSort(1)));

        // 准备参数
        List<String> newValues = Arrays.asList("第二项", "第一项");

        // 调用
        configService.replaceConfigValueList(HrmConfigTypeEnum.RECRUIT_ELIMINATE.getType(), newValues);

        // 断言
        assertEquals(newValues, configService.getConfigValueList(HrmConfigTypeEnum.RECRUIT_ELIMINATE.getType()));
        assertEquals(1, hrmConfigMapper.selectListByType(HrmConfigTypeEnum.RECRUIT_ELIMINATE.getType()).get(0).getSort());
        assertEquals(2, hrmConfigMapper.selectListByType(HrmConfigTypeEnum.RECRUIT_ELIMINATE.getType()).get(1).getSort());
    }

    @Test
    public void testReplaceConfigValueList_empty() {
        // mock 数据
        hrmConfigMapper.insert(randomPojo(HrmConfigDO.class, config -> config.setId(null)
                .setType(HrmConfigTypeEnum.RECRUIT_ELIMINATE.getType()).setValue("旧配置").setSort(1)));

        // 调用
        configService.replaceConfigValueList(HrmConfigTypeEnum.RECRUIT_ELIMINATE.getType(), Collections.emptyList());

        // 断言
        assertTrue(configService.getConfigValueList(HrmConfigTypeEnum.RECRUIT_ELIMINATE.getType()).isEmpty());
    }

}
