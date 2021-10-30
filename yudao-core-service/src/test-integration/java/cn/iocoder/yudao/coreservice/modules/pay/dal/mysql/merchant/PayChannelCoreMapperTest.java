package cn.iocoder.yudao.coreservice.modules.pay.dal.mysql.merchant;

import cn.hutool.core.io.IoUtil;
import cn.iocoder.yudao.coreservice.BaseDbAndRedisIntegrationTest;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayChannelDO;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.pay.core.client.impl.wx.WXPayClientConfig;
import cn.iocoder.yudao.framework.pay.core.enums.PayChannelEnum;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

@Resource
public class PayChannelCoreMapperTest extends BaseDbAndRedisIntegrationTest {

    @Resource
    private PayChannelCoreMapper payChannelCoreMapper;

    /**
     * 插入初始配置
     */
    @Test
    public void testInsert() throws FileNotFoundException {
        PayChannelDO payChannelDO = new PayChannelDO();
        payChannelDO.setCode(PayChannelEnum.WX_PUB.getCode());
        payChannelDO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        payChannelDO.setFeeRate(1D);
        payChannelDO.setMerchantId(1L);
        payChannelDO.setAppId(6L);
        // 配置
        WXPayClientConfig config = new WXPayClientConfig();
        config.setAppId("wx041349c6f39b268b");
        config.setMchId("1545083881");
        config.setApiVersion(WXPayClientConfig.API_VERSION_V2);
        config.setMchKey("0alL64UDQdlCwiKZ73ib7ypaIjMns06p");
        config.setPrivateKeyContent(IoUtil.readUtf8(new FileInputStream("/Users/yunai/Downloads/wx_pay/apiclient_key.pem")));
        config.setPrivateCertContent(IoUtil.readUtf8(new FileInputStream("/Users/yunai/Downloads/wx_pay/apiclient_cert.pem")));
        config.setApiV3Key("joerVi8y5DJ3o4ttA0o1uH47Xz1u2Ase");
        payChannelDO.setConfig(config);
        // 执行插入
        payChannelCoreMapper.insert(payChannelDO);
    }

    /**
     * 查询所有支付配置，看看是否都是 ok 的
     */
    @Test
    public void testSelectList() {
        List<PayChannelDO> payChannels = payChannelCoreMapper.selectList();
        System.out.println(payChannels.size());
    }

}
