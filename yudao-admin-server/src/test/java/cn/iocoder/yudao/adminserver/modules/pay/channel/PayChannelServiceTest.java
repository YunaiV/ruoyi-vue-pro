package cn.iocoder.yudao.adminserver.modules.pay.channel;

import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayChannelDO;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;

import cn.iocoder.yudao.adminserver.BaseDbUnitTest;
import cn.iocoder.yudao.adminserver.modules.pay.service.channel.impl.PayChannelServiceImpl;
import cn.iocoder.yudao.adminserver.modules.pay.controller.channel.vo.*;
import cn.iocoder.yudao.adminserver.modules.pay.dal.mysql.channel.PayChannelMapper;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import java.util.*;

import static cn.iocoder.yudao.coreservice.modules.pay.enums.PayErrorCodeCoreConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.buildTime;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;

/**
* {@link PayChannelServiceImpl} 的单元测试类
*
* @author 芋艿
*/
@Import(PayChannelServiceImpl.class)
public class PayChannelServiceTest extends BaseDbUnitTest {

    @Resource
    private PayChannelServiceImpl channelService;

    @Resource
    private PayChannelMapper channelMapper;

    @Test
    public void testCreateChannel_success() {
        // 准备参数
        PayChannelCreateReqVO reqVO = randomPojo(PayChannelCreateReqVO.class);

        // 调用
        Long channelId = channelService.createChannel(reqVO);
        // 断言
        assertNotNull(channelId);
        // 校验记录的属性是否正确
        PayChannelDO channel = channelMapper.selectById(channelId);
        assertPojoEquals(reqVO, channel);
    }

    @Test
    public void testUpdateChannel_success() {
        // mock 数据
        PayChannelDO dbChannel = randomPojo(PayChannelDO.class);
        channelMapper.insert(dbChannel);// @Sql: 先插入出一条存在的数据
        // 准备参数
        PayChannelUpdateReqVO reqVO = randomPojo(PayChannelUpdateReqVO.class, o -> {
            o.setId(dbChannel.getId()); // 设置更新的 ID
        });

        // 调用
        channelService.updateChannel(reqVO);
        // 校验是否更新正确
        PayChannelDO channel = channelMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, channel);
    }

    @Test
    public void testUpdateChannel_notExists() {
        // 准备参数
        PayChannelUpdateReqVO reqVO = randomPojo(PayChannelUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> channelService.updateChannel(reqVO), CHANNEL_NOT_EXISTS);
    }

    @Test
    public void testDeleteChannel_success() {
        // mock 数据
        PayChannelDO dbChannel = randomPojo(PayChannelDO.class);
        channelMapper.insert(dbChannel);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbChannel.getId();

        // 调用
        channelService.deleteChannel(id);
       // 校验数据不存在了
       assertNull(channelMapper.selectById(id));
    }

    @Test
    public void testDeleteChannel_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> channelService.deleteChannel(id), CHANNEL_NOT_EXISTS);
    }

    @Test // TODO 请修改 null 为需要的值
    public void testGetChannelPage() {
       // mock 数据
       PayChannelDO dbChannel = randomPojo(PayChannelDO.class, o -> { // 等会查询到
           o.setCode(null);
           o.setStatus(null);
           o.setRemark(null);
           o.setFeeRate(null);
           o.setMerchantId(null);
           o.setAppId(null);
           o.setConfig(null);
           o.setCreateTime(null);
       });
       channelMapper.insert(dbChannel);
       // 测试 code 不匹配
       channelMapper.insert(ObjectUtils.clone(dbChannel, o -> o.setCode(null)));
       // 测试 status 不匹配
       channelMapper.insert(ObjectUtils.clone(dbChannel, o -> o.setStatus(null)));
       // 测试 remark 不匹配
       channelMapper.insert(ObjectUtils.clone(dbChannel, o -> o.setRemark(null)));
       // 测试 feeRate 不匹配
       channelMapper.insert(ObjectUtils.clone(dbChannel, o -> o.setFeeRate(null)));
       // 测试 merchantId 不匹配
       channelMapper.insert(ObjectUtils.clone(dbChannel, o -> o.setMerchantId(null)));
       // 测试 appId 不匹配
       channelMapper.insert(ObjectUtils.clone(dbChannel, o -> o.setAppId(null)));
       // 测试 config 不匹配
       channelMapper.insert(ObjectUtils.clone(dbChannel, o -> o.setConfig(null)));
       // 测试 createTime 不匹配
       channelMapper.insert(ObjectUtils.clone(dbChannel, o -> o.setCreateTime(null)));
       // 准备参数
       PayChannelPageReqVO reqVO = new PayChannelPageReqVO();
       reqVO.setCode(null);
       reqVO.setStatus(null);
       reqVO.setRemark(null);
       reqVO.setFeeRate(null);
       reqVO.setMerchantId(null);
       reqVO.setAppId(null);
       reqVO.setConfig(null);
       reqVO.setBeginCreateTime(null);
       reqVO.setEndCreateTime(null);

       // 调用
       PageResult<PayChannelDO> pageResult = channelService.getChannelPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbChannel, pageResult.getList().get(0));
    }

    @Test // TODO 请修改 null 为需要的值
    public void testGetChannelList() {
       // mock 数据
       PayChannelDO dbChannel = randomPojo(PayChannelDO.class, o -> { // 等会查询到
           o.setCode(null);
           o.setStatus(null);
           o.setRemark(null);
           o.setFeeRate(null);
           o.setMerchantId(null);
           o.setAppId(null);
           o.setConfig(null);
           o.setCreateTime(null);
       });
       channelMapper.insert(dbChannel);
       // 测试 code 不匹配
       channelMapper.insert(ObjectUtils.clone(dbChannel, o -> o.setCode(null)));
       // 测试 status 不匹配
       channelMapper.insert(ObjectUtils.clone(dbChannel, o -> o.setStatus(null)));
       // 测试 remark 不匹配
       channelMapper.insert(ObjectUtils.clone(dbChannel, o -> o.setRemark(null)));
       // 测试 feeRate 不匹配
       channelMapper.insert(ObjectUtils.clone(dbChannel, o -> o.setFeeRate(null)));
       // 测试 merchantId 不匹配
       channelMapper.insert(ObjectUtils.clone(dbChannel, o -> o.setMerchantId(null)));
       // 测试 appId 不匹配
       channelMapper.insert(ObjectUtils.clone(dbChannel, o -> o.setAppId(null)));
       // 测试 config 不匹配
       channelMapper.insert(ObjectUtils.clone(dbChannel, o -> o.setConfig(null)));
       // 测试 createTime 不匹配
       channelMapper.insert(ObjectUtils.clone(dbChannel, o -> o.setCreateTime(null)));
       // 准备参数
       PayChannelExportReqVO reqVO = new PayChannelExportReqVO();
       reqVO.setCode(null);
       reqVO.setStatus(null);
       reqVO.setRemark(null);
       reqVO.setFeeRate(null);
       reqVO.setMerchantId(null);
       reqVO.setAppId(null);
       reqVO.setConfig(null);
       reqVO.setBeginCreateTime(null);
       reqVO.setEndCreateTime(null);

       // 调用
       List<PayChannelDO> list = channelService.getChannelList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbChannel, list.get(0));
    }

}
