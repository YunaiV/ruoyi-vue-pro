package cn.iocoder.yudao.coreservice.modules.system.service.sms;

import cn.iocoder.yudao.coreservice.BaseDbUnitTest;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.sms.SysSmsTemplateDO;
import cn.iocoder.yudao.coreservice.modules.system.dal.mysql.sms.SysSmsTemplateCoreMapper;
import cn.iocoder.yudao.coreservice.modules.system.enums.sms.SysSmsTemplateTypeEnum;
import cn.iocoder.yudao.coreservice.modules.system.service.sms.impl.SysSmsTemplateCoreServiceImpl;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;
import java.util.function.Consumer;

import static cn.hutool.core.bean.BeanUtil.getFieldValue;
import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.max;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
* {@link SysSmsTemplateCoreServiceImpl} 的单元测试类
*
* @author 芋道源码
*/
@Import(SysSmsTemplateCoreServiceImpl.class)
public class SysSmsTemplateServiceTest extends BaseDbUnitTest {

    @Resource
    private SysSmsTemplateCoreServiceImpl smsTemplateCoreService;

    @Resource
    private SysSmsTemplateCoreMapper smsTemplateCoreMapper;

    @Test
    @SuppressWarnings("unchecked")
    void testInitLocalCache() {
        // mock 数据
        SysSmsTemplateDO smsTemplate01 = randomSmsTemplateDO();
        smsTemplateCoreMapper.insert(smsTemplate01);
        SysSmsTemplateDO smsTemplate02 = randomSmsTemplateDO();
        smsTemplateCoreMapper.insert(smsTemplate02);

        // 调用
        smsTemplateCoreService.initLocalCache();
        // 断言 deptCache 缓存
        Map<String, SysSmsTemplateDO> smsTemplateCache = (Map<String, SysSmsTemplateDO>) getFieldValue(smsTemplateCoreService, "smsTemplateCache");
        assertEquals(2, smsTemplateCache.size());
        assertPojoEquals(smsTemplate01, smsTemplateCache.get(smsTemplate01.getCode()));
        assertPojoEquals(smsTemplate02, smsTemplateCache.get(smsTemplate02.getCode()));
        // 断言 maxUpdateTime 缓存
        Date maxUpdateTime = (Date) getFieldValue(smsTemplateCoreService, "maxUpdateTime");
        assertEquals(max(smsTemplate01.getUpdateTime(), smsTemplate02.getUpdateTime()), maxUpdateTime);
    }

    // ========== 随机对象 ==========

    @SafeVarargs
    private static SysSmsTemplateDO randomSmsTemplateDO(Consumer<SysSmsTemplateDO>... consumers) {
        Consumer<SysSmsTemplateDO> consumer = (o) -> {
            o.setStatus(randomEle(CommonStatusEnum.values()).getStatus()); // 保证 status 的范围
            o.setType(randomEle(SysSmsTemplateTypeEnum.values()).getType()); // 保证 type 的 范围
        };
        return randomPojo(SysSmsTemplateDO.class, ArrayUtils.append(consumer, consumers));
    }

}
