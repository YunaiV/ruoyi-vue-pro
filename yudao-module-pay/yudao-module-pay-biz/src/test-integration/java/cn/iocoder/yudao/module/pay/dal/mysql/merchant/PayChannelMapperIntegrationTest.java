package cn.iocoder.yudao.module.pay.dal.mysql.merchant;

import cn.hutool.core.io.IoUtil;
import cn.iocoder.yudao.module.pay.dal.dataobject.merchant.PayChannelDO;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.pay.core.client.impl.alipay.AlipayPayClientConfig;
import cn.iocoder.yudao.framework.pay.core.client.impl.weixin.WXPayClientConfig;
import cn.iocoder.yudao.framework.pay.core.enums.PayChannelEnum;
import cn.iocoder.yudao.module.pay.test.BaseDbIntegrationTest;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

@Resource
public class PayChannelMapperIntegrationTest extends BaseDbIntegrationTest {

    @Resource
    private PayChannelMapper payChannelMapper;

    /**
     * 插入 {@link PayChannelEnum#WX_PUB} 初始配置
     */
    @Test
    public void testInsertWxPub() throws FileNotFoundException {
        PayChannelDO payChannelDO = new PayChannelDO();
        payChannelDO.setCode(PayChannelEnum.WX_PUB.getCode());
        payChannelDO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        payChannelDO.setFeeRate(1D);
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
        payChannelMapper.insert(payChannelDO);
    }

    // TODO @ouyang：Zfb 改成 AlipayQr
    /**
     * 插入 {@link PayChannelEnum#ALIPAY_QR} 初始配置
     */
    @Test
    public void testInsertZfb() {
        PayChannelDO payChannelDO = new PayChannelDO();
        payChannelDO.setCode(PayChannelEnum.ALIPAY_QR.getCode());
        payChannelDO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        payChannelDO.setFeeRate(1D);
        payChannelDO.setAppId(6L);
        // 配置
        AlipayPayClientConfig config = new AlipayPayClientConfig();
        config.setAppId("2021000118634035");
        config.setServerUrl(AlipayPayClientConfig.SERVER_URL_SANDBOX);
        config.setSignType(AlipayPayClientConfig.SIGN_TYPE_DEFAULT);
        config.setPrivateKey("MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCHsEV1cDupwJv890x84qbppUtRIfhaKSwSVN0thCcsDCaAsGR5MZslDkO8NCT9V4r2SVXjyY7eJUZlZd1M0C8T01Tg4UOx5LUbic0O3A1uJMy6V1n9IyYwbAW3AEZhBd5bSbPgrqvmv3NeWSTQT6Anxnllf+2iDH6zyA2fPl7cYyQtbZoDJQFGqr4F+cGh2R6akzRKNoBkAeMYwoY6es2lX8sJxCVPWUmxNUoL3tScwlSpd7Bxw0q9c/X01jMwuQ0+Va358zgFiGERTE6yD01eu40OBDXOYO3z++y+TAYHlQQ2toMO63trepo88X3xV3R44/1DH+k2pAm2IF5ixiLrAgMBAAECggEAPx3SoXcseaD7rmcGcE0p4SMfbsUDdkUSmBBbtfF0GzwnqNLkWa+mgE0rWt9SmXngTQH97vByAYmLPl1s3G82ht1V7Sk7yQMe74lhFllr8eEyTjeVx3dTK1EEM4TwN+936DTXdFsr4TELJEcJJdD0KaxcCcfBLRDs2wnitEFZ9N+GoZybVmY8w0e0MI7PLObUZ2l0X4RurQnfG9ZxjXjC7PkeMVv7cGGylpNFi3BbvkRhdhLPDC2E6wqnr9e7zk+hiENivAezXrtxtwKovzCtnWJ1r0IO14Rh47H509Ic0wFnj+o5YyUL4LdmpL7yaaH6fM7zcSLFjNZPHvZCKPwYcQKBgQDQFho98QvnL8ex4v6cry4VitGpjSXm1qP3vmMQk4rTsn8iPWtcxPjqGEqOQJjdi4Mi0VZKQOLFwlH0kl95wNrD/isJ4O1yeYfX7YAXApzHqYNINzM79HemO3Yx1qLMW3okRFJ9pPRzbQ9qkTpsaegsmyX316zOBhzGRYjKbutTYwKBgQCm7phr9XdFW5Vh+XR90mVs483nrLmMiDKg7YKxSLJ8amiDjzPejCn7i95Hah08P+2MIZLIPbh2VLacczR6ltRRzN5bg5etFuqSgfkuHyxpoDmpjbe08+Q2h8JBYqcC5Nhv1AKU4iOUhVLHo/FBAQliMcGc/J3eiYTFC7EsNx382QKBgClb20doe7cttgFTXswBvaUmfFm45kmla924B7SpvrQpDD/f+VDtDZRp05fGmxuduSjYdtA3aVtpLiTwWu22OUUvZZqHDGruYOO4Hvdz23mL5b4ayqImCwoNU4bAZIc9v18p/UNf3/55NNE3oGcf/bev9rH2OjCQ4nM+Ktwhg8CFAoGACSgvbkShzUkv0ZcIf9ppu+ZnJh1AdGgINvGwaJ8vQ0nm/8h8NOoFZ4oNoGc+wU5Ubops7dUM6FjPR5e+OjdJ4E7Xp7d5O4J1TaIZlCEbo5OpdhaTDDcQvrkFu+Z4eN0qzj+YAKjDAOOrXc4tbr5q0FsgXscwtcNfaBuzFVTUrUkCgYEAwzPnMNhWG3zOWLUs2QFA2GP4Y+J8cpUYfj6pbKKzeLwyG9qBwF1NJpN8m+q9q7V9P2LY+9Lp9e1mGsGeqt5HMEA3P6vIpcqLJLqE/4PBLLRzfccTcmqb1m71+erxTRhHBRkGS+I7dZEb3olQfnS1Y1tpMBxiwYwR3LW4oXuJwj8=");
        config.setAlipayPublicKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnq90KnF4dTnlzzmxpujbI05OYqi5WxAS6cL0gnZFv2gK51HExF8v/BaP7P979PhFMgWTqmOOI+Dtno5s+yD09XTY1WkshbLk6i4g2Xlr8fyW9ODnkU88RI2w9UdPhQU4cPPwBNlrsYhKkVK2OxwM3kFqjoBBY0CZoZCsSQ3LDH5WeZqPArlsS6xa2zqJBuuoKjMrdpELl3eXSjP8K54eDJCbeetCZNKWLL3DPahTPB7LZikfYmslb0QUvCgGapD0xkS7eVq70NaL1G57MWABs4tbfWgxike4Daj3EfUrzIVspQxj7w8HEj9WozJPgL88kSJSits0pqD3n5r8HSuseQIDAQAB");
        // 创建客户端
        payChannelDO.setConfig(config);
        // 执行插入
        payChannelMapper.insert(payChannelDO);
    }

    /**
     * 查询所有支付配置，看看是否都是 ok 的
     */
    @Test
    public void testSelectList() {
        List<PayChannelDO> payChannels = payChannelMapper.selectList();
        System.out.println(payChannels.size());
    }

}
