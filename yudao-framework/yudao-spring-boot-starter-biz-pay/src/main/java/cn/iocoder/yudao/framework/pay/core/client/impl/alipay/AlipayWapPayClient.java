package cn.iocoder.yudao.framework.pay.core.client.impl.alipay;

import cn.hutool.core.bean.BeanUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.pay.core.client.PayCommonResult;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.impl.AbstractPayClient;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import lombok.SneakyThrows;

/**
 * 支付宝【手机网站】的 PayClient 实现类
 * 文档：https://opendocs.alipay.com/apis/api_1/alipay.trade.wap.pay
 *
 * @author 芋道源码
 */
public class AlipayWapPayClient extends AbstractPayClient<AlipayPayClientConfig> {

    private DefaultAlipayClient client;

    public AlipayWapPayClient(Long channelId, String channelCode, AlipayPayClientConfig config) {
        super(channelId, channelCode, config, new AlipayPayCodeMapping());
    }

    @Override
    @SneakyThrows
    protected void doInit() {
        AlipayConfig alipayConfig = new AlipayConfig();
        BeanUtil.copyProperties(config, alipayConfig, false);
        this.client = new DefaultAlipayClient(alipayConfig);
    }

    @Override
    public CommonResult<AlipayTradeWapPayResponse> unifiedOrder(PayOrderUnifiedReqDTO reqDTO) {
        // 构建 AlipayTradeWapPayModel 请求
        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        model.setOutTradeNo(reqDTO.getMerchantOrderId());
        model.setSubject(reqDTO.getSubject());
        model.setBody(reqDTO.getBody());
        model.setTotalAmount(calculateAmount(reqDTO.getAmount()).toString());
        model.setProductCode("QUICK_WAP_PAY"); // TODO 芋艿：这里咋整
        model.setSellerId("2088102147948060"); // TODO 芋艿：这里咋整
        // TODO 芋艿：clientIp + expireTime
        // 构建 AlipayTradeWapPayRequest
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        request.setBizModel(model);

        // 执行请求
        AlipayTradeWapPayResponse response;
        try {
            response = client.pageExecute(request);
        } catch (AlipayApiException e) {
            return PayCommonResult.build(e.getErrCode(), e.getErrMsg(), null, codeMapping);
        }

        System.out.println(response.getBody());

//         TODO 芋艿：sub Code
        return PayCommonResult.build(response.getCode(), response.getMsg(), response, codeMapping);
    }

    public static void main(String[] args) {
        AlipayPayClientConfig config = new AlipayPayClientConfig();
        config.setAppId("2021000118634035");
        config.setServerUrl(AlipayPayClientConfig.SERVER_URL_SANDBOX);
        config.setSignType(AlipayPayClientConfig.SIGN_TYPE_DEFAULT);
        config.setPrivateKey("MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCHsEV1cDupwJv890x84qbppUtRIfhaKSwSVN0thCcsDCaAsGR5MZslDkO8NCT9V4r2SVXjyY7eJUZlZd1M0C8T01Tg4UOx5LUbic0O3A1uJMy6V1n9IyYwbAW3AEZhBd5bSbPgrqvmv3NeWSTQT6Anxnllf+2iDH6zyA2fPl7cYyQtbZoDJQFGqr4F+cGh2R6akzRKNoBkAeMYwoY6es2lX8sJxCVPWUmxNUoL3tScwlSpd7Bxw0q9c/X01jMwuQ0+Va358zgFiGERTE6yD01eu40OBDXOYO3z++y+TAYHlQQ2toMO63trepo88X3xV3R44/1DH+k2pAm2IF5ixiLrAgMBAAECggEAPx3SoXcseaD7rmcGcE0p4SMfbsUDdkUSmBBbtfF0GzwnqNLkWa+mgE0rWt9SmXngTQH97vByAYmLPl1s3G82ht1V7Sk7yQMe74lhFllr8eEyTjeVx3dTK1EEM4TwN+936DTXdFsr4TELJEcJJdD0KaxcCcfBLRDs2wnitEFZ9N+GoZybVmY8w0e0MI7PLObUZ2l0X4RurQnfG9ZxjXjC7PkeMVv7cGGylpNFi3BbvkRhdhLPDC2E6wqnr9e7zk+hiENivAezXrtxtwKovzCtnWJ1r0IO14Rh47H509Ic0wFnj+o5YyUL4LdmpL7yaaH6fM7zcSLFjNZPHvZCKPwYcQKBgQDQFho98QvnL8ex4v6cry4VitGpjSXm1qP3vmMQk4rTsn8iPWtcxPjqGEqOQJjdi4Mi0VZKQOLFwlH0kl95wNrD/isJ4O1yeYfX7YAXApzHqYNINzM79HemO3Yx1qLMW3okRFJ9pPRzbQ9qkTpsaegsmyX316zOBhzGRYjKbutTYwKBgQCm7phr9XdFW5Vh+XR90mVs483nrLmMiDKg7YKxSLJ8amiDjzPejCn7i95Hah08P+2MIZLIPbh2VLacczR6ltRRzN5bg5etFuqSgfkuHyxpoDmpjbe08+Q2h8JBYqcC5Nhv1AKU4iOUhVLHo/FBAQliMcGc/J3eiYTFC7EsNx382QKBgClb20doe7cttgFTXswBvaUmfFm45kmla924B7SpvrQpDD/f+VDtDZRp05fGmxuduSjYdtA3aVtpLiTwWu22OUUvZZqHDGruYOO4Hvdz23mL5b4ayqImCwoNU4bAZIc9v18p/UNf3/55NNE3oGcf/bev9rH2OjCQ4nM+Ktwhg8CFAoGACSgvbkShzUkv0ZcIf9ppu+ZnJh1AdGgINvGwaJ8vQ0nm/8h8NOoFZ4oNoGc+wU5Ubops7dUM6FjPR5e+OjdJ4E7Xp7d5O4J1TaIZlCEbo5OpdhaTDDcQvrkFu+Z4eN0qzj+YAKjDAOOrXc4tbr5q0FsgXscwtcNfaBuzFVTUrUkCgYEAwzPnMNhWG3zOWLUs2QFA2GP4Y+J8cpUYfj6pbKKzeLwyG9qBwF1NJpN8m+q9q7V9P2LY+9Lp9e1mGsGeqt5HMEA3P6vIpcqLJLqE/4PBLLRzfccTcmqb1m71+erxTRhHBRkGS+I7dZEb3olQfnS1Y1tpMBxiwYwR3LW4oXuJwj8=");
        config.setAlipayPublicKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnq90KnF4dTnlzzmxpujbI05OYqi5WxAS6cL0gnZFv2gK51HExF8v/BaP7P979PhFMgWTqmOOI+Dtno5s+yD09XTY1WkshbLk6i4g2Xlr8fyW9ODnkU88RI2w9UdPhQU4cPPwBNlrsYhKkVK2OxwM3kFqjoBBY0CZoZCsSQ3LDH5WeZqPArlsS6xa2zqJBuuoKjMrdpELl3eXSjP8K54eDJCbeetCZNKWLL3DPahTPB7LZikfYmslb0QUvCgGapD0xkS7eVq70NaL1G57MWABs4tbfWgxike4Daj3EfUrzIVspQxj7w8HEj9WozJPgL88kSJSits0pqD3n5r8HSuseQIDAQAB");
        AlipayWapPayClient client = new AlipayWapPayClient(1L, "biubiubiu", config);
        client.init();

        PayOrderUnifiedReqDTO reqDTO = new PayOrderUnifiedReqDTO();
        reqDTO.setAmount(123);
        reqDTO.setSubject("IPhone 13");
        reqDTO.setMerchantOrderId(String.valueOf(System.currentTimeMillis()));
        client.unifiedOrder(reqDTO);
    }


}
