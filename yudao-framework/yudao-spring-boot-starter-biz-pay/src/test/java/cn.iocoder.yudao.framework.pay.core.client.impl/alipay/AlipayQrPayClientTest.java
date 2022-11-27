package cn.iocoder.yudao.framework.pay.core.client.impl.alipay;
import cn.hutool.core.util.ReflectUtil;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.pay.core.client.PayCommonResult;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import com.alipay.api.AlipayApiException;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

public class AlipayQrPayClientTest extends BaseMockitoUnitTest {

    private final AlipayPayClientConfig config = new AlipayPayClientConfig()
        .setAppId("2021000118634035")
        .setServerUrl(AlipayPayClientConfig.SERVER_URL_SANDBOX)
        .setSignType(AlipayPayClientConfig.SIGN_TYPE_DEFAULT)
        // TODO @tina：key 可以随机就好，简洁一点哈。
        .setPrivateKey("MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCHsEV1cDupwJ" +
                "v890x84qbppUtRIfhaKSwSVN0thCcsDCaAsGR5MZslDkO8NCT9V4r2SVXjyY7eJUZlZd1M0C8T" +
                "01Tg4UOx5LUbic0O3A1uJMy6V1n9IyYwbAW3AEZhBd5bSbPgrqvmv3NeWSTQT6Anxnllf+2iDH" +
                "6zyA2fPl7cYyQtbZoDJQFGqr4F+cGh2R6akzRKNoBkAeMYwoY6es2lX8sJxCVPWUmxNUoL3tScw" +
                "lSpd7Bxw0q9c/X01jMwuQ0+Va358zgFiGERTE6yD01eu40OBDXOYO3z++y+TAYHlQQ2toMO63tr" +
                "epo88X3xV3R44/1DH+k2pAm2IF5ixiLrAgMBAAECggEAPx3SoXcseaD7rmcGcE0p4SMfbsUDdk" +
                "USmBBbtfF0GzwnqNLkWa+mgE0rWt9SmXngTQH97vByAYmLPl1s3G82ht1V7Sk7yQMe74lhFllr" +
                "8eEyTjeVx3dTK1EEM4TwN+936DTXdFsr4TELJEcJJdD0KaxcCcfBLRDs2wnitEFZ9N+GoZybVmY8w" +
                "0e0MI7PLObUZ2l0X4RurQnfG9ZxjXjC7PkeMVv7cGGylpNFi3BbvkRhdhLPDC2E6wqnr9e7zk+hiENi" +
                "vAezXrtxtwKovzCtnWJ1r0IO14Rh47H509Ic0wFnj+o5YyUL4LdmpL7yaaH6fM7zcSLFjNZPHvZCKPw" +
                "YcQKBgQDQFho98QvnL8ex4v6cry4VitGpjSXm1qP3vmMQk4rTsn8iPWtcxPjqGEqOQJjdi4Mi0VZKQO" +
                "LFwlH0kl95wNrD/isJ4O1yeYfX7YAXApzHqYNINzM79HemO3Yx1qLMW3okRFJ9pPRzbQ9qkTpsaegsm" +
                "yX316zOBhzGRYjKbutTYwKBgQCm7phr9XdFW5Vh+XR90mVs483nrLmMiDKg7YKxSLJ8amiDjzPejCn7i9" +
                "5Hah08P+2MIZLIPbh2VLacczR6ltRRzN5bg5etFuqSgfkuHyxpoDmpjbe08+Q2h8JBYqcC5Nhv1AKU4iOU" +
                "hVLHo/FBAQliMcGc/J3eiYTFC7EsNx382QKBgClb20doe7cttgFTXswBvaUmfFm45kmla924B7SpvrQpDD" +
                "/f+VDtDZRp05fGmxuduSjYdtA3aVtpLiTwWu22OUUvZZqHDGruYOO4Hvdz23mL5b4ayqImCwoNU4bAZIc9v1" +
                "8p/UNf3/55NNE3oGcf/bev9rH2OjCQ4nM+Ktwhg8CFAoGACSgvbkShzUkv0ZcIf9ppu+ZnJh1AdGgINvGwaJ" +
                "8vQ0nm/8h8NOoFZ4oNoGc+wU5Ubops7dUM6FjPR5e+OjdJ4E7Xp7d5O4J1TaIZlCEbo5OpdhaTDDcQvrkFu+Z4e" +
                "N0qzj+YAKjDAOOrXc4tbr5q0FsgXscwtcNfaBuzFVTUrUkCgYEAwzPnMNhWG3zOWLUs2QFA2GP4Y+J8cpUYfj6p" +
                "bKKzeLwyG9qBwF1NJpN8m+q9q7V9P2LY+9Lp9e1mGsGeqt5HMEA3P6vIpcqLJLqE/4PBLLRzfccTcmqb1m71+erx" +
                "TRhHBRkGS+I7dZEb3olQfnS1Y1tpMBxiwYwR3LW4oXuJwj8=")
        .setAlipayPublicKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnq90KnF4dTnlzzmxpujbI05OYqi5WxAS6cL0" +
                "gnZFv2gK51HExF8v/BaP7P979PhFMgWTqmOOI+Dtno5s+yD09XTY1WkshbLk6i4g2Xlr8fyW9ODnkU88RI2w9UdPhQU4cPPwBN" +
                "lrsYhKkVK2OxwM3kFqjoBBY0CZoZCsSQ3LDH5WeZqPArlsS6xa2zqJBuuoKjMrdpELl3eXSjP8K54eDJCbeetCZNKWLL3DPahTPB7LZ" +
                "ikfYmslb0QUvCgGapD0xkS7eVq70NaL1G57MWABs4tbfWgxike4Daj3EfUrzIVspQxj7w8HEj9WozJPgL88kSJSits0pqD3n5r8HSuseQIDAQAB");

    @InjectMocks
    AlipayQrPayClient client = new AlipayQrPayClient(10L,config);

    @Mock
    private DefaultAlipayClient defaultAlipayClient;

    @Test
    public void testDoInit(){
        client.doInit();
        assertNotSame(defaultAlipayClient, ReflectUtil.getFieldValue(client, "defaultAlipayClient"));
    }

    @Test
    @Disabled // TODO 芋艿：临时禁用
    public void create() throws AlipayApiException {
        // TODO @tina：参数可以尽量随机一点，使用随机方法。这样的好处是，避免对固定参数的依赖，导致可能仅仅满足固定参数的结果
        // 这里，设置可以直接随机整个对象。
        Long shopOrderId = System.currentTimeMillis();
        PayOrderUnifiedReqDTO reqDTO=new PayOrderUnifiedReqDTO();
        reqDTO.setMerchantOrderId(String.valueOf(System.currentTimeMillis()));
        reqDTO.setAmount(1);
        reqDTO.setBody("内容：" + shopOrderId);
        reqDTO.setSubject("标题："+shopOrderId);
        String notify="http://niubi.natapp1.cc/api/pay/order/notify";
        reqDTO.setNotifyUrl(notify);

        AlipayTradePrecreateResponse response=randomPojo(AlipayTradePrecreateResponse.class,o->o.setQrCode("success"));

        when(defaultAlipayClient.execute(argThat((ArgumentMatcher<AlipayTradePrecreateRequest>) request ->{
            assertEquals(notify,request.getNotifyUrl());
            return true;
        }))).thenReturn(response);


        PayCommonResult<AlipayTradePrecreateResponse> result = client.doUnifiedOrder(reqDTO);
        // 断言
        assertEquals(response.getCode(), result.getApiCode());
        assertEquals(response.getMsg(), result.getApiMsg());
        // TODO @tina：这个断言木有过？
        assertEquals(GlobalErrorCodeConstants.SUCCESS.getCode(), result.getCode());
        assertEquals(GlobalErrorCodeConstants.SUCCESS.getMsg(), result.getMsg());

    }
}
