package cn.iocoder.yudao.module.pay.service.merchant;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.pay.core.client.PayClientFactory;
import cn.iocoder.yudao.framework.pay.core.client.impl.alipay.AlipayPayClientConfig;
import cn.iocoder.yudao.framework.pay.core.client.impl.wx.WXPayClientConfig;
import cn.iocoder.yudao.framework.pay.core.enums.PayChannelEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.channel.PayChannelCreateReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.channel.PayChannelExportReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.channel.PayChannelPageReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.channel.PayChannelUpdateReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.merchant.PayChannelDO;
import cn.iocoder.yudao.module.pay.dal.mysql.merchant.PayChannelMapper;
import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import javax.validation.Validator;
import java.util.Date;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.CHANNEL_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

@Import({PayChannelServiceImpl.class})
public class PayChannelServiceTest extends BaseDbUnitTest {

    @Resource
    private PayChannelServiceImpl channelService;

    @Resource
    private PayChannelMapper channelMapper;

    @MockBean
    private PayClientFactory payClientFactory;
    @MockBean
    private Validator validator;

    @Test
    public void testCreateWechatVersion2Channel_success() {
        // 准备参数

        WXPayClientConfig v2Config = getV2Config();
        PayChannelCreateReqVO reqVO = randomPojo(PayChannelCreateReqVO.class, o -> {
            o.setCode(PayChannelEnum.WX_PUB.getCode());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setConfig(JSON.toJSONString(v2Config));
        });

        // 调用
        Long channelId = channelService.createChannel(reqVO);
        // 断言
        assertNotNull(channelId);
        // 校验记录的属性是否正确
        PayChannelDO channel = channelMapper.selectById(channelId);
        assertPojoEquals(reqVO, channel, "config");
        // 关于config 对象应该拿出来重新对比
        assertPojoEquals(v2Config, channel.getConfig());
    }

    @Test
    public void testCreateWechatVersion3Channel_success() {
        // 准备参数
        WXPayClientConfig v3Config = getV3Config();
        PayChannelCreateReqVO reqVO = randomPojo(PayChannelCreateReqVO.class, o -> {
            o.setCode(PayChannelEnum.WX_PUB.getCode());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setConfig(JSON.toJSONString(v3Config));
        });

        // 调用
        Long channelId = channelService.createChannel(reqVO);
        // 断言
        assertNotNull(channelId);
        // 校验记录的属性是否正确
        PayChannelDO channel = channelMapper.selectById(channelId);
        assertPojoEquals(reqVO, channel, "config");
        // 关于config 对象应该拿出来重新对比
        assertPojoEquals(v3Config, channel.getConfig());
    }

    @Test
    public void testCreateAliPayPublicKeyChannel_success() {
        // 准备参数

        AlipayPayClientConfig payClientConfig = getPublicKeyConfig();
        PayChannelCreateReqVO reqVO = randomPojo(PayChannelCreateReqVO.class, o -> {
            o.setCode(PayChannelEnum.ALIPAY_APP.getCode());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setConfig(JSON.toJSONString(payClientConfig));
        });

        // 调用
        Long channelId = channelService.createChannel(reqVO);
        // 断言
        assertNotNull(channelId);
        // 校验记录的属性是否正确
        PayChannelDO channel = channelMapper.selectById(channelId);
        assertPojoEquals(reqVO, channel, "config");
        // 关于config 对象应该拿出来重新对比
        assertPojoEquals(payClientConfig, channel.getConfig());

    }

    @Test
    public void testCreateAliPayCertificateChannel_success() {
        // 准备参数

        AlipayPayClientConfig payClientConfig = getCertificateConfig();
        PayChannelCreateReqVO reqVO = randomPojo(PayChannelCreateReqVO.class, o -> {
            o.setCode(PayChannelEnum.ALIPAY_APP.getCode());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setConfig(JSON.toJSONString(payClientConfig));
        });

        // 调用
        Long channelId = channelService.createChannel(reqVO);
        // 断言
        assertNotNull(channelId);
        // 校验记录的属性是否正确
        PayChannelDO channel = channelMapper.selectById(channelId);
        assertPojoEquals(reqVO, channel, "config");
        // 关于config 对象应该拿出来重新对比
        assertPojoEquals(payClientConfig, channel.getConfig());
    }

    @Test
    public void testUpdateChannel_success() {
        // mock 数据
        AlipayPayClientConfig payClientConfig = getCertificateConfig();
        PayChannelDO dbChannel = randomPojo(PayChannelDO.class, o -> {
            o.setCode(PayChannelEnum.ALIPAY_APP.getCode());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setConfig(payClientConfig);
        });
        channelMapper.insert(dbChannel);// @Sql: 先插入出一条存在的数据
        // 准备参数
        AlipayPayClientConfig payClientPublicKeyConfig = getPublicKeyConfig();
        PayChannelUpdateReqVO reqVO = randomPojo(PayChannelUpdateReqVO.class, o -> {
            o.setCode(dbChannel.getCode());
            o.setStatus(dbChannel.getStatus());
            o.setConfig(JSON.toJSONString(payClientPublicKeyConfig));
            o.setId(dbChannel.getId()); // 设置更新的 ID
        });

        // 调用
        channelService.updateChannel(reqVO);
        // 校验是否更新正确
        PayChannelDO channel = channelMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, channel, "config");
        assertPojoEquals(payClientPublicKeyConfig, channel.getConfig());
    }

    @Test
    public void testUpdateChannel_notExists() {
        // 准备参数
        AlipayPayClientConfig payClientPublicKeyConfig = getPublicKeyConfig();
        PayChannelUpdateReqVO reqVO = randomPojo(PayChannelUpdateReqVO.class, o -> {
            o.setCode(PayChannelEnum.ALIPAY_APP.getCode());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setConfig(JSON.toJSONString(payClientPublicKeyConfig));
        });

        // 调用, 并断言异常
        assertServiceException(() -> channelService.updateChannel(reqVO), CHANNEL_NOT_EXISTS);
    }

    @Test
    public void testDeleteChannel_success() {
        // mock 数据
        AlipayPayClientConfig payClientConfig = getCertificateConfig();
        PayChannelDO dbChannel = randomPojo(PayChannelDO.class, o -> {
            o.setCode(PayChannelEnum.ALIPAY_APP.getCode());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setConfig(payClientConfig);
        });
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
        AlipayPayClientConfig payClientConfig = getCertificateConfig();
        PayChannelDO dbChannel = randomPojo(PayChannelDO.class, o -> { // 等会查询到
            o.setCode(PayChannelEnum.ALIPAY_APP.getCode());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setRemark("灿灿子的支付渠道");
            o.setFeeRate(0.03);
            o.setMerchantId(1L);
            o.setAppId(1L);
            o.setConfig(payClientConfig);
            o.setCreateTime(buildTime(2021,11,20));
        });
        channelMapper.insert(dbChannel);
        // 执行拷贝的时候会出现异常，所以在插入后要重置为null 后续在写入新的
        dbChannel.setConfig(null);
        // 测试 code 不匹配
        channelMapper.insert(cloneIgnoreId(dbChannel, o -> {
            o.setConfig(payClientConfig);
            o.setCode(PayChannelEnum.WX_PUB.getCode());
        }));
        // 测试 status 不匹配
        channelMapper.insert(cloneIgnoreId(dbChannel, o -> {
            o.setConfig(payClientConfig);
            o.setStatus(CommonStatusEnum.DISABLE.getStatus());
        }));
        // 测试 remark 不匹配
        channelMapper.insert(cloneIgnoreId(dbChannel, o ->{
            o.setConfig(payClientConfig);
            o.setRemark("敏敏子的渠道");
        }));
        // 测试 feeRate 不匹配
        channelMapper.insert(cloneIgnoreId(dbChannel, o -> {
            o.setConfig(payClientConfig);
            o.setFeeRate(1.23);
        }));
        // 测试 merchantId 不匹配
        channelMapper.insert(cloneIgnoreId(dbChannel, o -> {
            o.setConfig(payClientConfig);
            o.setMerchantId(2L);
        }));
        // 测试 appId 不匹配
        channelMapper.insert(cloneIgnoreId(dbChannel, o -> {
            o.setConfig(payClientConfig);
            o.setAppId(2L);
        }));
        // 测试 createTime 不匹配
        channelMapper.insert(cloneIgnoreId(dbChannel, o -> {
            o.setConfig(payClientConfig);
            o.setCreateTime(buildTime(2021, 10, 20));
        }));
        // 准备参数
        PayChannelPageReqVO reqVO = new PayChannelPageReqVO();
        reqVO.setCode(PayChannelEnum.ALIPAY_APP.getCode());
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setRemark("灿灿子的支付渠道");
        reqVO.setFeeRate(0.03);
        reqVO.setMerchantId(1L);
        reqVO.setAppId(1L);
        reqVO.setConfig(JSON.toJSONString(payClientConfig));
        reqVO.setCreateTime((new Date[]{buildTime(2021,11,19),buildTime(2021,11,21)}));

        // 调用
        PageResult<PayChannelDO> pageResult = channelService.getChannelPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbChannel, pageResult.getList().get(0), "config");
        assertPojoEquals(payClientConfig, pageResult.getList().get(0).getConfig());

    }

    @Test
    public void testGetChannelList() {
        // mock 数据
        AlipayPayClientConfig payClientConfig = getCertificateConfig();
        PayChannelDO dbChannel = randomPojo(PayChannelDO.class, o -> { // 等会查询到
            o.setCode(PayChannelEnum.ALIPAY_APP.getCode());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setRemark("灿灿子的支付渠道");
            o.setFeeRate(0.03);
            o.setMerchantId(1L);
            o.setAppId(1L);
            o.setConfig(payClientConfig);
            o.setCreateTime(buildTime(2021,11,20));
        });
        channelMapper.insert(dbChannel);
        // 执行拷贝的时候会出现异常，所以在插入后要重置为null 后续在写入新的
        dbChannel.setConfig(null);
        // 测试 code 不匹配
        channelMapper.insert(cloneIgnoreId(dbChannel, o -> {
            o.setConfig(payClientConfig);
            o.setCode(PayChannelEnum.WX_PUB.getCode());
        }));
        // 测试 status 不匹配
        channelMapper.insert(cloneIgnoreId(dbChannel, o -> {
            o.setConfig(payClientConfig);
            o.setStatus(CommonStatusEnum.DISABLE.getStatus());
        }));
        // 测试 remark 不匹配
        channelMapper.insert(cloneIgnoreId(dbChannel, o ->{
            o.setConfig(payClientConfig);
            o.setRemark("敏敏子的渠道");
        }));
        // 测试 feeRate 不匹配
        channelMapper.insert(cloneIgnoreId(dbChannel, o -> {
            o.setConfig(payClientConfig);
            o.setFeeRate(1.23);
        }));
        // 测试 merchantId 不匹配
        channelMapper.insert(cloneIgnoreId(dbChannel, o -> {
            o.setConfig(payClientConfig);
            o.setMerchantId(2L);
        }));
        // 测试 appId 不匹配
        channelMapper.insert(cloneIgnoreId(dbChannel, o -> {
            o.setConfig(payClientConfig);
            o.setAppId(2L);
        }));
        // 测试 createTime 不匹配
        channelMapper.insert(cloneIgnoreId(dbChannel, o -> {
            o.setConfig(payClientConfig);
            o.setCreateTime(buildTime(2021, 10, 20));
        }));
        // 准备参数
        PayChannelExportReqVO reqVO = new PayChannelExportReqVO();
        reqVO.setCode(PayChannelEnum.ALIPAY_APP.getCode());
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setRemark("灿灿子的支付渠道");
        reqVO.setFeeRate(0.03);
        reqVO.setMerchantId(1L);
        reqVO.setAppId(1L);
        reqVO.setConfig(JSON.toJSONString(payClientConfig));
        reqVO.setCreateTime((new Date[]{buildTime(2021,11,19),buildTime(2021,11,21)}));

        // 调用
        List<PayChannelDO> list = channelService.getChannelList(reqVO);
        // 断言
        assertEquals(1, list.size());
        assertPojoEquals(dbChannel, list.get(0), "config");
        assertPojoEquals(payClientConfig, list.get(0).getConfig());
    }

    public WXPayClientConfig getV2Config() {
        return new WXPayClientConfig()
                .setAppId("APP00001")
                .setMchId("MCH00001")
                .setApiVersion(WXPayClientConfig.API_VERSION_V2)
                .setMchKey("dsa1d5s6a1d6sa16d1sa56d15a61das6")
                .setApiV3Key("")
                .setPrivateCertContent("")
                .setPrivateKeyContent("");
    }

    public WXPayClientConfig getV3Config() {
        return new WXPayClientConfig()
                .setAppId("APP00001")
                .setMchId("MCH00001")
                .setApiVersion(WXPayClientConfig.API_VERSION_V3)
                .setMchKey("")
                .setApiV3Key("sdadasdsadadsa")
                .setPrivateKeyContent("dsa445das415d15asd16ad156as")
                .setPrivateCertContent("dsadasd45asd4s5a");

    }

    public AlipayPayClientConfig getPublicKeyConfig() {
        return new AlipayPayClientConfig()
                .setServerUrl(AlipayPayClientConfig.SERVER_URL_PROD)
                .setAppId("APP00001")
                .setSignType(AlipayPayClientConfig.SIGN_TYPE_DEFAULT)
                .setMode(AlipayPayClientConfig.MODE_PUBLIC_KEY)
                .setPrivateKey("13131321312")
                .setAlipayPublicKey("13321321321")
                .setAppCertContent("")
                .setAlipayPublicCertContent("")
                .setRootCertContent("");
    }

    public AlipayPayClientConfig getCertificateConfig() {
        return new AlipayPayClientConfig()
                .setServerUrl(AlipayPayClientConfig.SERVER_URL_PROD)
                .setAppId("APP00001")
                .setSignType(AlipayPayClientConfig.SIGN_TYPE_DEFAULT)
                .setMode(AlipayPayClientConfig.MODE_CERTIFICATE)
                .setPrivateKey("")
                .setAlipayPublicKey("")
                .setAppCertContent("13321321321sda")
                .setAlipayPublicCertContent("13321321321aqeqw")
                .setRootCertContent("13321321321dsad");
    }

}
