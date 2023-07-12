package cn.iocoder.yudao.module.pay.service.channel;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.pay.core.client.PayClientFactory;
import cn.iocoder.yudao.framework.pay.core.client.impl.alipay.AlipayPayClientConfig;
import cn.iocoder.yudao.framework.pay.core.client.impl.weixin.WxPayClientConfig;
import cn.iocoder.yudao.framework.pay.core.enums.channel.PayChannelEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pay.controller.admin.channel.vo.PayChannelCreateReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.channel.vo.PayChannelUpdateReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.channel.PayChannelDO;
import cn.iocoder.yudao.module.pay.dal.mysql.channel.PayChannelMapper;
import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import javax.validation.Validator;

import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.CHANNEL_EXIST_SAME_CHANNEL_ERROR;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.CHANNEL_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

@Import({PayChannelServiceImpl.class})
public class PayChannelServiceTest extends BaseDbUnitTest {

    private static final String ALIPAY_SERVER_URL = "https://openapi.alipay.com/gateway.do";

    @Resource
    private PayChannelServiceImpl channelService;

    @Resource
    private PayChannelMapper channelMapper;

    @MockBean
    private PayClientFactory payClientFactory;
    @MockBean
    private Validator validator;

    @BeforeEach
    public void setUp() {
        channelService.setChannelCache(null);
    }

    @Test
    public void testCreateChannel_success() {
        // 准备参数
        WxPayClientConfig config = randomWxPayClientConfig();
        PayChannelCreateReqVO reqVO = randomPojo(PayChannelCreateReqVO.class, o -> {
            o.setStatus(randomCommonStatus());
            o.setCode(PayChannelEnum.WX_PUB.getCode());
            o.setConfig(JsonUtils.toJsonString(config));
        });

        // 调用
        Long channelId = channelService.createChannel(reqVO);
        // 校验记录的属性是否正确
        PayChannelDO channel = channelMapper.selectById(channelId);
        assertPojoEquals(reqVO, channel, "config");
        assertPojoEquals(config, channel.getConfig());
        // 校验缓存
        assertEquals(1, channelService.getChannelCache().size());
        assertEquals(channel, channelService.getChannelCache().get(0));
    }

    @Test
    public void testCreateChannel_exists() {
        // mock 数据
        PayChannelDO dbChannel = randomPojo(PayChannelDO.class,
                o -> o.setConfig(randomWxPayClientConfig()));
        channelMapper.insert(dbChannel);// @Sql: 先插入出一条存在的数据
        // 准备参数
        PayChannelCreateReqVO reqVO = randomPojo(PayChannelCreateReqVO.class, o -> {
            o.setAppId(dbChannel.getAppId());
            o.setCode(dbChannel.getCode());
        });

        // 调用, 并断言异常
        assertServiceException(() -> channelService.createChannel(reqVO), CHANNEL_EXIST_SAME_CHANNEL_ERROR);
    }

    @Test
    public void testUpdateChannel_success() {
        // mock 数据
        PayChannelDO dbChannel = randomPojo(PayChannelDO.class, o -> {
            o.setCode(PayChannelEnum.ALIPAY_APP.getCode());
            o.setConfig(randomAlipayPayClientConfig());
        });
        channelMapper.insert(dbChannel);// @Sql: 先插入出一条存在的数据
        // 准备参数
        AlipayPayClientConfig config = randomAlipayPayClientConfig();
        PayChannelUpdateReqVO reqVO = randomPojo(PayChannelUpdateReqVO.class, o -> {
            o.setId(dbChannel.getId()); // 设置更新的 ID
            o.setStatus(randomCommonStatus());
            o.setConfig(JsonUtils.toJsonString(config));
        });

        // 调用
        channelService.updateChannel(reqVO);
        // 校验是否更新正确
        PayChannelDO channel = channelMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, channel, "config");
        assertPojoEquals(config, channel.getConfig());
        // 校验缓存
        assertEquals(1, channelService.getChannelCache().size());
        assertEquals(channel, channelService.getChannelCache().get(0));
    }

    @Test
    public void testUpdateChannel_notExists() {
        // 准备参数
        AlipayPayClientConfig payClientPublicKeyConfig = randomAlipayPayClientConfig();
        PayChannelUpdateReqVO reqVO = randomPojo(PayChannelUpdateReqVO.class, o -> {
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setConfig(JSON.toJSONString(payClientPublicKeyConfig));
        });

        // 调用, 并断言异常
        assertServiceException(() -> channelService.updateChannel(reqVO), CHANNEL_NOT_EXISTS);
    }

    @Test
    public void testDeleteChannel_success() {
        // mock 数据
        PayChannelDO dbChannel = randomPojo(PayChannelDO.class, o -> {
            o.setCode(PayChannelEnum.ALIPAY_APP.getCode());
            o.setConfig(randomAlipayPayClientConfig());
        });
        channelMapper.insert(dbChannel);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbChannel.getId();

        // 调用
        channelService.deleteChannel(id);
        // 校验数据不存在了
        assertNull(channelMapper.selectById(id));
        // 校验缓存
        assertEquals(0, channelService.getChannelCache().size());
    }

    @Test
    public void testDeleteChannel_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> channelService.deleteChannel(id), CHANNEL_NOT_EXISTS);
    }

    @Test
    public void testGetChannel() {
        // mock 数据
        PayChannelDO dbChannel = randomPojo(PayChannelDO.class, o -> {
            o.setCode(PayChannelEnum.ALIPAY_APP.getCode());
            o.setConfig(randomAlipayPayClientConfig());
        });
        channelMapper.insert(dbChannel);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbChannel.getId();

        // 调用
        PayChannelDO channel = channelService.getChannel(id);
        // 校验是否更新正确
        assertPojoEquals(dbChannel, channel);
    }

    @Test
    public void testGetChannelListByAppIds() {
        // mock 数据
        PayChannelDO dbChannel01 = randomPojo(PayChannelDO.class, o -> {
            o.setCode(PayChannelEnum.ALIPAY_APP.getCode());
            o.setConfig(randomAlipayPayClientConfig());
        });
        channelMapper.insert(dbChannel01);// @Sql: 先插入出一条存在的数据
        PayChannelDO dbChannel02 = randomPojo(PayChannelDO.class, o -> {
            o.setCode(PayChannelEnum.WX_PUB.getCode());
            o.setConfig(randomWxPayClientConfig());
        });
        channelMapper.insert(dbChannel02);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long appId = dbChannel01.getAppId();

        // 调用
        List<PayChannelDO> channels = channelService.getChannelListByAppIds(Collections.singleton(appId));
        // 校验是否更新正确
        assertEquals(1, channels.size());
        assertPojoEquals(dbChannel01, channels.get(0));
    }

    @Test
    public void testGetChannelByAppIdAndCode() {
        // mock 数据
        PayChannelDO dbChannel = randomPojo(PayChannelDO.class, o -> {
            o.setCode(PayChannelEnum.ALIPAY_APP.getCode());
            o.setConfig(randomAlipayPayClientConfig());
        });
        channelMapper.insert(dbChannel);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long appId = dbChannel.getAppId();
        String code = dbChannel.getCode();;

        // 调用
        PayChannelDO channel = channelService.getChannelByAppIdAndCode(appId, code);
        // 断言
        assertPojoEquals(channel, dbChannel);
    }

    public WxPayClientConfig randomWxPayClientConfig() {
        return new WxPayClientConfig()
                .setAppId(randomString())
                .setMchId(randomString())
                .setApiVersion(WxPayClientConfig.API_VERSION_V2)
                .setMchKey(randomString());
    }

    public AlipayPayClientConfig randomAlipayPayClientConfig() {
        return new AlipayPayClientConfig()
                .setServerUrl(randomURL())
                .setAppId(randomString())
                .setSignType(AlipayPayClientConfig.SIGN_TYPE_DEFAULT)
                .setMode(AlipayPayClientConfig.MODE_PUBLIC_KEY)
                .setPrivateKey(randomString())
                .setAlipayPublicKey(randomString());
    }

}
