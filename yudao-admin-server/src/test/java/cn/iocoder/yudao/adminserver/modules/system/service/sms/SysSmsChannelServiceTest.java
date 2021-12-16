package cn.iocoder.yudao.adminserver.modules.system.service.sms;

import cn.hutool.core.bean.BeanUtil;
import cn.iocoder.yudao.adminserver.BaseDbUnitTest;
import cn.iocoder.yudao.adminserver.modules.system.controller.sms.vo.channel.SysSmsChannelCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.sms.vo.channel.SysSmsChannelPageReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.sms.vo.channel.SysSmsChannelUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.system.dal.mysql.sms.SysSmsChannelMapper;
import cn.iocoder.yudao.adminserver.modules.system.mq.producer.sms.SysSmsProducer;
import cn.iocoder.yudao.adminserver.modules.system.service.sms.impl.SysSmsChannelServiceImpl;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.sms.SysSmsChannelDO;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.sms.core.client.SmsClientFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Date;
import java.util.function.Consumer;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.adminserver.modules.system.enums.SysErrorCodeConstants.SMS_CHANNEL_HAS_CHILDREN;
import static cn.iocoder.yudao.adminserver.modules.system.enums.SysErrorCodeConstants.SMS_CHANNEL_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.max;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
* {@link SysSmsChannelServiceImpl} 的单元测试类
*
* @author 芋道源码
*/
@Import(SysSmsChannelServiceImpl.class)
public class SysSmsChannelServiceTest extends BaseDbUnitTest {

    @Resource
    private SysSmsChannelServiceImpl smsChannelService;

    @Resource
    private SysSmsChannelMapper smsChannelMapper;

    @MockBean
    private SmsClientFactory smsClientFactory;
    @MockBean
    private SysSmsTemplateService smsTemplateService;
    @MockBean
    private SysSmsProducer smsProducer;

    @Test
    public void testInitLocalCache_success() {
        // mock 数据
        SysSmsChannelDO smsChannelDO01 = randomSmsChannelDO();
        smsChannelMapper.insert(smsChannelDO01);
        SysSmsChannelDO smsChannelDO02 = randomSmsChannelDO();
        smsChannelMapper.insert(smsChannelDO02);

        // 调用
        smsChannelService.initSmsClients();
        // 校验 maxUpdateTime 属性
        Date maxUpdateTime = (Date) BeanUtil.getFieldValue(smsChannelService, "maxUpdateTime");
        assertEquals(max(smsChannelDO01.getUpdateTime(), smsChannelDO02.getUpdateTime()), maxUpdateTime);
        // 校验调用
        verify(smsClientFactory, times(1)).createOrUpdateSmsClient(
                argThat(properties -> isPojoEquals(smsChannelDO01, properties)));
        verify(smsClientFactory, times(1)).createOrUpdateSmsClient(
                argThat(properties -> isPojoEquals(smsChannelDO02, properties)));
    }

    @Test
    public void testCreateSmsChannel_success() {
        // 准备参数
        SysSmsChannelCreateReqVO reqVO = randomPojo(SysSmsChannelCreateReqVO.class, o -> o.setStatus(randomCommonStatus()));

        // 调用
        Long smsChannelId = smsChannelService.createSmsChannel(reqVO);
        // 断言
        assertNotNull(smsChannelId);
        // 校验记录的属性是否正确
        SysSmsChannelDO smsChannel = smsChannelMapper.selectById(smsChannelId);
        assertPojoEquals(reqVO, smsChannel);
        // 校验调用
        verify(smsProducer, times(1)).sendSmsChannelRefreshMessage();
    }

    @Test
    public void testUpdateSmsChannel_success() {
        // mock 数据
        SysSmsChannelDO dbSmsChannel = randomSmsChannelDO();
        smsChannelMapper.insert(dbSmsChannel);// @Sql: 先插入出一条存在的数据
        // 准备参数
        SysSmsChannelUpdateReqVO reqVO = randomPojo(SysSmsChannelUpdateReqVO.class, o -> {
            o.setId(dbSmsChannel.getId()); // 设置更新的 ID
            o.setStatus(randomCommonStatus());
            o.setCallbackUrl(randomString());
        });

        // 调用
        smsChannelService.updateSmsChannel(reqVO);
        // 校验是否更新正确
        SysSmsChannelDO smsChannel = smsChannelMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, smsChannel);
        // 校验调用
        verify(smsProducer, times(1)).sendSmsChannelRefreshMessage();
    }

    @Test
    public void testUpdateSmsChannel_notExists() {
        // 准备参数
        SysSmsChannelUpdateReqVO reqVO = randomPojo(SysSmsChannelUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> smsChannelService.updateSmsChannel(reqVO), SMS_CHANNEL_NOT_EXISTS);
    }

    @Test
    public void testDeleteSmsChannel_success() {
        // mock 数据
        SysSmsChannelDO dbSmsChannel = randomSmsChannelDO();
        smsChannelMapper.insert(dbSmsChannel);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbSmsChannel.getId();

        // 调用
        smsChannelService.deleteSmsChannel(id);
       // 校验数据不存在了
       assertNull(smsChannelMapper.selectById(id));
        // 校验调用
        verify(smsProducer, times(1)).sendSmsChannelRefreshMessage();
    }

    @Test
    public void testDeleteSmsChannel_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> smsChannelService.deleteSmsChannel(id), SMS_CHANNEL_NOT_EXISTS);
    }

    @Test
    public void testDeleteSmsChannel_hasChildren() {
        // mock 数据
        SysSmsChannelDO dbSmsChannel = randomSmsChannelDO();
        smsChannelMapper.insert(dbSmsChannel);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbSmsChannel.getId();
        // mock 方法
        when(smsTemplateService.countByChannelId(eq(id))).thenReturn(10);

        // 调用, 并断言异常
        assertServiceException(() -> smsChannelService.deleteSmsChannel(id), SMS_CHANNEL_HAS_CHILDREN);
    }

    @Test
    public void testGetSmsChannelPage() {
       // mock 数据
       SysSmsChannelDO dbSmsChannel = randomPojo(SysSmsChannelDO.class, o -> { // 等会查询到
           o.setSignature("芋道源码");
           o.setStatus(CommonStatusEnum.ENABLE.getStatus());
           o.setCreateTime(buildTime(2020, 12, 12));
       });
       smsChannelMapper.insert(dbSmsChannel);
       // 测试 signature 不匹配
       smsChannelMapper.insert(ObjectUtils.cloneIgnoreId(dbSmsChannel, o -> o.setSignature("源码")));
       // 测试 status 不匹配
       smsChannelMapper.insert(ObjectUtils.cloneIgnoreId(dbSmsChannel, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
       // 测试 createTime 不匹配
       smsChannelMapper.insert(ObjectUtils.cloneIgnoreId(dbSmsChannel, o -> o.setCreateTime(buildTime(2020, 11, 11))));
       // 准备参数
       SysSmsChannelPageReqVO reqVO = new SysSmsChannelPageReqVO();
       reqVO.setSignature("芋道");
       reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
       reqVO.setBeginCreateTime(buildTime(2020, 12, 1));
       reqVO.setEndCreateTime(buildTime(2020, 12, 24));

       // 调用
       PageResult<SysSmsChannelDO> pageResult = smsChannelService.getSmsChannelPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbSmsChannel, pageResult.getList().get(0));
    }

    // ========== 随机对象 ==========

    @SafeVarargs
    private static SysSmsChannelDO randomSmsChannelDO(Consumer<SysSmsChannelDO>... consumers) {
        Consumer<SysSmsChannelDO> consumer = (o) -> {
            o.setStatus(randomEle(CommonStatusEnum.values()).getStatus()); // 保证 status 的范围
        };
        return randomPojo(SysSmsChannelDO.class, ArrayUtils.append(consumer, consumers));
    }

}
