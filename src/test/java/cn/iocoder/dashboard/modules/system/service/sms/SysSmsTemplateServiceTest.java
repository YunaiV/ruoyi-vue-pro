package cn.iocoder.dashboard.modules.system.service.sms;

import cn.iocoder.dashboard.BaseDbUnitTest;
import cn.iocoder.dashboard.common.enums.CommonStatusEnum;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.template.SysSmsTemplateCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.template.SysSmsTemplateExportReqVO;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.template.SysSmsTemplatePageReqVO;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.template.SysSmsTemplateUpdateReqVO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsChannelDO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsTemplateDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.sms.SysSmsTemplateMapper;
import cn.iocoder.dashboard.modules.system.enums.sms.SysSmsTemplateTypeEnum;
import cn.iocoder.dashboard.modules.system.service.sms.impl.SysSmsTemplateServiceImpl;
import cn.iocoder.dashboard.util.collection.ArrayUtils;
import cn.iocoder.dashboard.util.object.ObjectUtils;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.List;
import java.util.function.Consumer;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.dashboard.modules.system.enums.SysErrorCodeConstants.SMS_TEMPLATE_NOT_EXISTS;
import static cn.iocoder.dashboard.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.dashboard.util.AssertUtils.assertServiceException;
import static cn.iocoder.dashboard.util.RandomUtils.randomLongId;
import static cn.iocoder.dashboard.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
* {@link SysSmsTemplateServiceImpl} 的单元测试类
*
* @author 芋道源码
*/
@Import(SysSmsTemplateServiceImpl.class)
public class SysSmsTemplateServiceTest extends BaseDbUnitTest {

    @Resource
    private SysSmsTemplateServiceImpl smsTemplateService;

    @Resource
    private SysSmsTemplateMapper smsTemplateMapper;

    @MockBean
    private SysSmsChannelService smsChannelService;

    @Test
    public void testParseTemplateContentParams() {
        // 准备参数
        String content = "正在进行登录操作{operation}，您的验证码是{code}";
        // mock 方法

        // 调用
        List<String> params = smsTemplateService.parseTemplateContentParams(content);
        // 断言
        assertEquals(Lists.newArrayList("operation", "code"), params);
    }

    @Test
    public void testCreateSmsTemplate_success() {
        // 准备参数
        SysSmsTemplateCreateReqVO reqVO = randomPojo(SysSmsTemplateCreateReqVO.class, o -> {
            o.setContent("正在进行登录操作{operation}，您的验证码是{code}");
            o.setStatus(randomEle(CommonStatusEnum.values()).getStatus()); // 保证 status 的范围
            o.setType(randomEle(SysSmsTemplateTypeEnum.values()).getType()); // 保证 type 的泛微
        });
        // mock 方法
        SysSmsChannelDO channelDO = randomPojo(SysSmsChannelDO.class, o -> {
            o.setId(reqVO.getChannelId());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus()); // 保证 status 开启，创建必须处于这个状态
        });
        when(smsChannelService.getSmsChannel(eq(channelDO.getId()))).thenReturn(channelDO);

        // 调用
        Long smsTemplateId = smsTemplateService.createSmsTemplate(reqVO);
        // 断言
        assertNotNull(smsTemplateId);
        // 校验记录的属性是否正确
        SysSmsTemplateDO smsTemplate = smsTemplateMapper.selectById(smsTemplateId);
        assertPojoEquals(reqVO, smsTemplate);
        assertEquals(Lists.newArrayList("operation", "code"), smsTemplate.getParams());
        assertEquals(channelDO.getCode(), smsTemplate.getChannelCode());
    }

    @Test
    public void testUpdateSmsTemplate_success() {
        // mock 数据
        SysSmsTemplateDO dbSmsTemplate = randomPojo(SysSmsTemplateDO.class);
        smsTemplateMapper.insert(dbSmsTemplate);// @Sql: 先插入出一条存在的数据
        // 准备参数
        SysSmsTemplateUpdateReqVO reqVO = randomPojo(SysSmsTemplateUpdateReqVO.class, o -> {
            o.setId(dbSmsTemplate.getId()); // 设置更新的 ID
        });

        // 调用
        smsTemplateService.updateSmsTemplate(reqVO);
        // 校验是否更新正确
        SysSmsTemplateDO smsTemplate = smsTemplateMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, smsTemplate);
    }

    @Test
    public void testUpdateSmsTemplate_notExists() {
        // 准备参数
        SysSmsTemplateUpdateReqVO reqVO = randomPojo(SysSmsTemplateUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> smsTemplateService.updateSmsTemplate(reqVO), SMS_TEMPLATE_NOT_EXISTS);
    }

    @Test
    public void testDeleteSmsTemplate_success() {
        // mock 数据
        SysSmsTemplateDO dbSmsTemplate = randomSmsTemplateDO();
        smsTemplateMapper.insert(dbSmsTemplate);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbSmsTemplate.getId();

        // 调用
        smsTemplateService.deleteSmsTemplate(id);
       // 校验数据不存在了
       assertNull(smsTemplateMapper.selectById(id));
    }

    @Test
    public void testDeleteSmsTemplate_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> smsTemplateService.deleteSmsTemplate(id), SMS_TEMPLATE_NOT_EXISTS);
    }

    @Test // TODO 请修改 null 为需要的值
    public void testGetSmsTemplatePage() {
       // mock 数据
       SysSmsTemplateDO dbSmsTemplate = randomPojo(SysSmsTemplateDO.class, o -> { // 等会查询到
           o.setType(null);
           o.setStatus(null);
           o.setCode(null);
           o.setContent(null);
           o.setApiTemplateId(null);
           o.setChannelId(null);
           o.setCreateTime(null);
       });
       smsTemplateMapper.insert(dbSmsTemplate);
       // 测试 type 不匹配
       smsTemplateMapper.insert(ObjectUtils.clone(dbSmsTemplate, o -> o.setType(null)));
       // 测试 status 不匹配
       smsTemplateMapper.insert(ObjectUtils.clone(dbSmsTemplate, o -> o.setStatus(null)));
       // 测试 code 不匹配
       smsTemplateMapper.insert(ObjectUtils.clone(dbSmsTemplate, o -> o.setCode(null)));
       // 测试 content 不匹配
       smsTemplateMapper.insert(ObjectUtils.clone(dbSmsTemplate, o -> o.setContent(null)));
       // 测试 apiTemplateId 不匹配
       smsTemplateMapper.insert(ObjectUtils.clone(dbSmsTemplate, o -> o.setApiTemplateId(null)));
       // 测试 channelId 不匹配
       smsTemplateMapper.insert(ObjectUtils.clone(dbSmsTemplate, o -> o.setChannelId(null)));
       // 测试 createTime 不匹配
       smsTemplateMapper.insert(ObjectUtils.clone(dbSmsTemplate, o -> o.setCreateTime(null)));
       // 准备参数
       SysSmsTemplatePageReqVO reqVO = new SysSmsTemplatePageReqVO();
       reqVO.setType(null);
       reqVO.setStatus(null);
       reqVO.setCode(null);
       reqVO.setContent(null);
       reqVO.setApiTemplateId(null);
       reqVO.setChannelId(null);
       reqVO.setBeginCreateTime(null);
       reqVO.setEndCreateTime(null);

       // 调用
       PageResult<SysSmsTemplateDO> pageResult = smsTemplateService.getSmsTemplatePage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbSmsTemplate, pageResult.getList().get(0));
    }

    @Test // TODO 请修改 null 为需要的值
    public void testGetSmsTemplateList() {
       // mock 数据
       SysSmsTemplateDO dbSmsTemplate = randomPojo(SysSmsTemplateDO.class, o -> { // 等会查询到
           o.setType(null);
           o.setStatus(null);
           o.setCode(null);
           o.setContent(null);
           o.setApiTemplateId(null);
           o.setChannelId(null);
           o.setCreateTime(null);
       });
       smsTemplateMapper.insert(dbSmsTemplate);
       // 测试 type 不匹配
       smsTemplateMapper.insert(ObjectUtils.clone(dbSmsTemplate, o -> o.setType(null)));
       // 测试 status 不匹配
       smsTemplateMapper.insert(ObjectUtils.clone(dbSmsTemplate, o -> o.setStatus(null)));
       // 测试 code 不匹配
       smsTemplateMapper.insert(ObjectUtils.clone(dbSmsTemplate, o -> o.setCode(null)));
       // 测试 content 不匹配
       smsTemplateMapper.insert(ObjectUtils.clone(dbSmsTemplate, o -> o.setContent(null)));
       // 测试 apiTemplateId 不匹配
       smsTemplateMapper.insert(ObjectUtils.clone(dbSmsTemplate, o -> o.setApiTemplateId(null)));
       // 测试 channelId 不匹配
       smsTemplateMapper.insert(ObjectUtils.clone(dbSmsTemplate, o -> o.setChannelId(null)));
       // 测试 createTime 不匹配
       smsTemplateMapper.insert(ObjectUtils.clone(dbSmsTemplate, o -> o.setCreateTime(null)));
       // 准备参数
       SysSmsTemplateExportReqVO reqVO = new SysSmsTemplateExportReqVO();
       reqVO.setType(null);
       reqVO.setStatus(null);
       reqVO.setCode(null);
       reqVO.setContent(null);
       reqVO.setApiTemplateId(null);
       reqVO.setChannelId(null);
       reqVO.setBeginCreateTime(null);
       reqVO.setEndCreateTime(null);

       // 调用
       List<SysSmsTemplateDO> list = smsTemplateService.getSmsTemplateList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbSmsTemplate, list.get(0));
    }

    @SafeVarargs
    private static SysSmsTemplateDO randomSmsTemplateDO(Consumer<SysSmsTemplateDO>... consumers) {
        Consumer<SysSmsTemplateDO> consumer = (o) -> {
            o.setStatus(randomEle(CommonStatusEnum.values()).getStatus()); // 保证 status 的范围
            o.setType(randomEle(SysSmsTemplateTypeEnum.values()).getType()); // 保证 type 的泛微
        };
        return randomPojo(SysSmsTemplateDO.class, ArrayUtils.append(consumer, consumers));
    }

}
