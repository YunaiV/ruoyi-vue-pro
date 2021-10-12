package cn.iocoder.yudao.coreservice.modules.system.service.sms;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.coreservice.BaseDbUnitTest;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.sms.SysSmsLogDO;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.sms.SysSmsTemplateDO;
import cn.iocoder.yudao.coreservice.modules.system.dal.mysql.sms.SysSmsLogCoreMapper;
import cn.iocoder.yudao.coreservice.modules.system.enums.sms.SysSmsReceiveStatusEnum;
import cn.iocoder.yudao.coreservice.modules.system.enums.sms.SysSmsSendStatusEnum;
import cn.iocoder.yudao.coreservice.modules.system.enums.sms.SysSmsTemplateTypeEnum;
import cn.iocoder.yudao.coreservice.modules.system.service.sms.impl.SysSmsLogCoreServiceImpl;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;
import java.util.function.Consumer;

import static cn.hutool.core.util.RandomUtil.randomBoolean;
import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
* {@link SysSmsLogCoreServiceImpl} 的单元测试类
*
* @author 芋道源码
*/
@Import(SysSmsLogCoreServiceImpl.class)
public class SysSmsLogCoreServiceTest extends BaseDbUnitTest {

    @Resource
    private SysSmsLogCoreServiceImpl smsLogCoreService;

    @Resource
    private SysSmsLogCoreMapper smsLogCoreMapper;

    @Test
    public void testCreateSmsLog() {
        // 准备参数
        String mobile = randomString();
        Long userId = randomLongId();
        Integer userType = randomEle(UserTypeEnum.values()).getValue();
        Boolean isSend = randomBoolean();
        SysSmsTemplateDO templateDO = randomPojo(SysSmsTemplateDO.class,
                o -> o.setType(randomEle(SysSmsTemplateTypeEnum.values()).getType()));
        String templateContent = randomString();
        Map<String, Object> templateParams = randomTemplateParams();
        // mock 方法

        // 调用
        Long logId = smsLogCoreService.createSmsLog(mobile, userId, userType, isSend,
                templateDO, templateContent, templateParams);
        // 断言
        SysSmsLogDO logDO = smsLogCoreMapper.selectById(logId);
        assertEquals(isSend ? SysSmsSendStatusEnum.INIT.getStatus() : SysSmsSendStatusEnum.IGNORE.getStatus(),
                logDO.getSendStatus());
        assertEquals(mobile, logDO.getMobile());
        assertEquals(userType, logDO.getUserType());
        assertEquals(userId, logDO.getUserId());
        assertEquals(templateDO.getId(), logDO.getTemplateId());
        assertEquals(templateDO.getCode(), logDO.getTemplateCode());
        assertEquals(templateDO.getType(), logDO.getTemplateType());
        assertEquals(templateDO.getChannelId(), logDO.getChannelId());
        assertEquals(templateDO.getChannelCode(), logDO.getChannelCode());
        assertEquals(templateContent, logDO.getTemplateContent());
        assertEquals(templateParams, logDO.getTemplateParams());
        assertEquals(SysSmsReceiveStatusEnum.INIT.getStatus(), logDO.getReceiveStatus());
    }

    @Test
    public void testUpdateSmsSendResult() {
        // mock 数据
        SysSmsLogDO dbSmsLog = randomSmsLogDO(
                o -> o.setSendStatus(SysSmsSendStatusEnum.IGNORE.getStatus()));
        smsLogCoreMapper.insert(dbSmsLog);
        // 准备参数
        Long id = dbSmsLog.getId();
        Integer sendCode = randomInteger();
        String sendMsg = randomString();
        String apiSendCode = randomString();
        String apiSendMsg = randomString();
        String apiRequestId = randomString();
        String apiSerialNo = randomString();

        // 调用
        smsLogCoreService.updateSmsSendResult(id, sendCode, sendMsg,
                apiSendCode, apiSendMsg, apiRequestId, apiSerialNo);
        // 断言
        dbSmsLog = smsLogCoreMapper.selectById(id);
        assertEquals(CommonResult.isSuccess(sendCode) ? SysSmsSendStatusEnum.SUCCESS.getStatus()
                : SysSmsSendStatusEnum.FAILURE.getStatus(), dbSmsLog.getSendStatus());
        assertNotNull(dbSmsLog.getSendTime());
        assertEquals(sendMsg, dbSmsLog.getSendMsg());
        assertEquals(apiSendCode, dbSmsLog.getApiSendCode());
        assertEquals(apiSendMsg, dbSmsLog.getApiSendMsg());
        assertEquals(apiRequestId, dbSmsLog.getApiRequestId());
        assertEquals(apiSerialNo, dbSmsLog.getApiSerialNo());
    }

    @Test
    public void testUpdateSmsReceiveResult() {
        // mock 数据
        SysSmsLogDO dbSmsLog = randomSmsLogDO(
                o -> o.setReceiveStatus(SysSmsReceiveStatusEnum.INIT.getStatus()));
        smsLogCoreMapper.insert(dbSmsLog);
        // 准备参数
        Long id = dbSmsLog.getId();
        Boolean success = randomBoolean();
        Date receiveTime = randomDate();
        String apiReceiveCode = randomString();
        String apiReceiveMsg = randomString();

        // 调用
        smsLogCoreService.updateSmsReceiveResult(id, success, receiveTime, apiReceiveCode, apiReceiveMsg);
        // 断言
        dbSmsLog = smsLogCoreMapper.selectById(id);
        assertEquals(success ? SysSmsReceiveStatusEnum.SUCCESS.getStatus()
                : SysSmsReceiveStatusEnum.FAILURE.getStatus(), dbSmsLog.getReceiveStatus());
        assertEquals(receiveTime, dbSmsLog.getReceiveTime());
        assertEquals(apiReceiveCode, dbSmsLog.getApiReceiveCode());
        assertEquals(apiReceiveMsg, dbSmsLog.getApiReceiveMsg());
    }

    // ========== 随机对象 ==========

    @SafeVarargs
    private static SysSmsLogDO randomSmsLogDO(Consumer<SysSmsLogDO>... consumers) {
        Consumer<SysSmsLogDO> consumer = (o) -> {
            o.setTemplateParams(randomTemplateParams());
            o.setTemplateType(randomEle(SysSmsTemplateTypeEnum.values()).getType()); // 保证 templateType 的范围
            o.setUserType(randomEle(UserTypeEnum.values()).getValue()); // 保证 userType 的范围
            o.setSendStatus(randomEle(SysSmsSendStatusEnum.values()).getStatus()); // 保证 sendStatus 的范围
            o.setReceiveStatus(randomEle(SysSmsReceiveStatusEnum.values()).getStatus()); // 保证 receiveStatus 的范围
        };
        return randomPojo(SysSmsLogDO.class, ArrayUtils.append(consumer, consumers));
    }


    private static Map<String, Object> randomTemplateParams() {
        return MapUtil.<String, Object>builder().put(randomString(), randomString())
                .put(randomString(), randomString()).build();
    }
}
