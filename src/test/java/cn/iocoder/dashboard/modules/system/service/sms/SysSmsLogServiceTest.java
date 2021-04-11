package cn.iocoder.dashboard.modules.system.service.sms;

import cn.iocoder.dashboard.BaseDbUnitTest;
import cn.iocoder.dashboard.common.enums.UserTypeEnum;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.log.SysSmsLogExportReqVO;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.log.SysSmsLogPageReqVO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsLogDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.sms.SysSmsLogMapper;
import cn.iocoder.dashboard.modules.system.enums.sms.SysSmsReceiveStatusEnum;
import cn.iocoder.dashboard.modules.system.enums.sms.SysSmsSendStatusEnum;
import cn.iocoder.dashboard.modules.system.enums.sms.SysSmsTemplateTypeEnum;
import cn.iocoder.dashboard.modules.system.service.sms.impl.SysSmsLogServiceImpl;
import cn.iocoder.dashboard.util.collection.ArrayUtils;
import cn.iocoder.dashboard.util.object.ObjectUtils;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.dashboard.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.dashboard.util.RandomUtils.randomPojo;
import static cn.iocoder.dashboard.util.RandomUtils.randomString;
import static cn.iocoder.dashboard.util.date.DateUtils.buildTime;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
* {@link SysSmsLogServiceImpl} 的单元测试类
*
* @author 芋道源码
*/
@Import(SysSmsLogServiceImpl.class)
public class SysSmsLogServiceTest extends BaseDbUnitTest {

    @Resource
    private SysSmsLogServiceImpl smsLogService;

    @Resource
    private SysSmsLogMapper smsLogMapper;

    @Test // TODO 请修改 null 为需要的值
    public void testGetSmsLogPage() {
       // mock 数据
       SysSmsLogDO dbSmsLog = randomSmsLogDO(o -> { // 等会查询到
           o.setChannelId(1L);
           o.setTemplateId(10L);
           o.setMobile("15601691300");
           o.setSendStatus(SysSmsSendStatusEnum.INIT.getStatus());
           o.setSendTime(buildTime(2020, 11, 11));
           o.setReceiveStatus(SysSmsReceiveStatusEnum.INIT.getStatus());
           o.setReceiveTime(buildTime(2021, 11, 11));
       });
       smsLogMapper.insert(dbSmsLog);
       // 测试 channelId 不匹配
       smsLogMapper.insert(ObjectUtils.clone(dbSmsLog, o -> o.setChannelId(2L)));
       // 测试 templateId 不匹配
       smsLogMapper.insert(ObjectUtils.clone(dbSmsLog, o -> o.setTemplateId(20L)));
       // 测试 mobile 不匹配
       smsLogMapper.insert(ObjectUtils.clone(dbSmsLog, o -> o.setMobile("18818260999")));
       // 测试 sendStatus 不匹配
       smsLogMapper.insert(ObjectUtils.clone(dbSmsLog, o -> o.setSendStatus(SysSmsSendStatusEnum.IGNORE.getStatus())));
       // 测试 sendTime 不匹配
       smsLogMapper.insert(ObjectUtils.clone(dbSmsLog, o -> o.setSendTime(buildTime(2020, 12, 12))));
       // 测试 receiveStatus 不匹配
       smsLogMapper.insert(ObjectUtils.clone(dbSmsLog, o -> o.setReceiveStatus(SysSmsReceiveStatusEnum.SUCCESS.getStatus())));
       // 测试 receiveTime 不匹配
       smsLogMapper.insert(ObjectUtils.clone(dbSmsLog, o -> o.setReceiveTime(buildTime(2021, 12, 12))));
       // 准备参数
       SysSmsLogPageReqVO reqVO = new SysSmsLogPageReqVO();
       reqVO.setChannelId(1L);
       reqVO.setTemplateId(10L);
       reqVO.setMobile("156");
       reqVO.setSendStatus(SysSmsSendStatusEnum.INIT.getStatus());
       reqVO.setBeginSendTime(buildTime(2020, 11, 1));
       reqVO.setEndSendTime(buildTime(2020, 11, 30));
       reqVO.setReceiveStatus(SysSmsReceiveStatusEnum.INIT.getStatus());
       reqVO.setBeginReceiveTime(buildTime(2021, 11, 1));
       reqVO.setEndReceiveTime(buildTime(2021, 11, 30));

       // 调用
       PageResult<SysSmsLogDO> pageResult = smsLogService.getSmsLogPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbSmsLog, pageResult.getList().get(0));
    }

    @Test
    public void testGetSmsLogList() {
        // mock 数据
        SysSmsLogDO dbSmsLog = randomSmsLogDO(o -> { // 等会查询到
            o.setChannelId(1L);
            o.setTemplateId(10L);
            o.setMobile("15601691300");
            o.setSendStatus(SysSmsSendStatusEnum.INIT.getStatus());
            o.setSendTime(buildTime(2020, 11, 11));
            o.setReceiveStatus(SysSmsReceiveStatusEnum.INIT.getStatus());
            o.setReceiveTime(buildTime(2021, 11, 11));
        });
        smsLogMapper.insert(dbSmsLog);
        // 测试 channelId 不匹配
        smsLogMapper.insert(ObjectUtils.clone(dbSmsLog, o -> o.setChannelId(2L)));
        // 测试 templateId 不匹配
        smsLogMapper.insert(ObjectUtils.clone(dbSmsLog, o -> o.setTemplateId(20L)));
        // 测试 mobile 不匹配
        smsLogMapper.insert(ObjectUtils.clone(dbSmsLog, o -> o.setMobile("18818260999")));
        // 测试 sendStatus 不匹配
        smsLogMapper.insert(ObjectUtils.clone(dbSmsLog, o -> o.setSendStatus(SysSmsSendStatusEnum.IGNORE.getStatus())));
        // 测试 sendTime 不匹配
        smsLogMapper.insert(ObjectUtils.clone(dbSmsLog, o -> o.setSendTime(buildTime(2020, 12, 12))));
        // 测试 receiveStatus 不匹配
        smsLogMapper.insert(ObjectUtils.clone(dbSmsLog, o -> o.setReceiveStatus(SysSmsReceiveStatusEnum.SUCCESS.getStatus())));
        // 测试 receiveTime 不匹配
        smsLogMapper.insert(ObjectUtils.clone(dbSmsLog, o -> o.setReceiveTime(buildTime(2021, 12, 12))));
        // 准备参数
        SysSmsLogExportReqVO reqVO = new SysSmsLogExportReqVO();
        reqVO.setChannelId(1L);
        reqVO.setTemplateId(10L);
        reqVO.setMobile("156");
        reqVO.setSendStatus(SysSmsSendStatusEnum.INIT.getStatus());
        reqVO.setBeginSendTime(buildTime(2020, 11, 1));
        reqVO.setEndSendTime(buildTime(2020, 11, 30));
        reqVO.setReceiveStatus(SysSmsReceiveStatusEnum.INIT.getStatus());
        reqVO.setBeginReceiveTime(buildTime(2021, 11, 1));
        reqVO.setEndReceiveTime(buildTime(2021, 11, 30));

       // 调用
       List<SysSmsLogDO> list = smsLogService.getSmsLogList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbSmsLog, list.get(0));
    }

    // ========== 随机对象 ==========

    @SafeVarargs
    private static SysSmsLogDO randomSmsLogDO(Consumer<SysSmsLogDO>... consumers) {
        Consumer<SysSmsLogDO> consumer = (o) -> {
            o.setTemplateParams(new HashMap<>());
            o.getTemplateParams().put(randomString(), randomString());
            o.getTemplateParams().put(randomString(), randomString());
            o.setTemplateType(randomEle(SysSmsTemplateTypeEnum.values()).getType()); // 保证 templateType 的范围
            o.setUserType(randomEle(UserTypeEnum.values()).getValue()); // 保证 userType 的范围
        };
        return randomPojo(SysSmsLogDO.class, ArrayUtils.append(consumer, consumers));
    }
}
