package cn.iocoder.yudao.ssodemo.client;

import cn.iocoder.yudao.ssodemo.client.dto.CommonResult;
import cn.iocoder.yudao.ssodemo.client.dto.oauth2.OAuth2AccessTokenRespDTO;
import cn.iocoder.yudao.ssodemo.client.dto.oauth2.OAuth2CheckTokenRespDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

/**
 * OAuth 2.0 客户端
 *
 * 对应调用 OAuth2OpenController 接口
 */
@Component
public class OAuth2Client {

    private static final String BASE_URL = "http://127.0.0.1:48080/admin-api/system/oauth2";

    /**
     * 租户编号
     *
     * 默认使用 1；如果使用别的租户，可以调整
     */
    public static final Long TENANT_ID = 1L;

    private static final String CLIENT_ID = "yudao-sso-demo-by-code";
    private static final String CLIENT_SECRET = "test";


//    @Resource // 可优化，注册一个 RestTemplate Bean，然后注入
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 使用 code 授权码，获得访问令牌
     *
     * @param code        授权码
     * @param redirectUri 重定向 URI
     * @return 访问令牌
     */
    public CommonResult<OAuth2AccessTokenRespDTO> postAccessToken(String code, String redirectUri) {
        // 1.1 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("tenant-id", TENANT_ID.toString());
        addClientHeader(headers);
        // 1.2 构建请求参数
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", redirectUri);
//        body.add("state", ""); // 选填；填了会校验

        // 2. 执行请求
        ResponseEntity<CommonResult<OAuth2AccessTokenRespDTO>> exchange = restTemplate.exchange(
                BASE_URL + "/token",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                new ParameterizedTypeReference<CommonResult<OAuth2AccessTokenRespDTO>>() {}); // 解决 CommonResult 的泛型丢失
        Assert.isTrue(exchange.getStatusCode().is2xxSuccessful(), "响应必须是 200 成功");
        return exchange.getBody();
    }

    /**
     * 校验访问令牌，并返回它的基本信息
     *
     * @param token 访问令牌
     * @return 访问令牌的基本信息
     */
    public CommonResult<OAuth2CheckTokenRespDTO> checkToken(String token) {
        // 1.1 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("tenant-id", TENANT_ID.toString());
        addClientHeader(headers);
        // 1.2 构建请求参数
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("token", token);

        // 2. 执行请求
        ResponseEntity<CommonResult<OAuth2CheckTokenRespDTO>> exchange = restTemplate.exchange(
                BASE_URL + "/check-token",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                new ParameterizedTypeReference<CommonResult<OAuth2CheckTokenRespDTO>>() {}); // 解决 CommonResult 的泛型丢失
        Assert.isTrue(exchange.getStatusCode().is2xxSuccessful(), "响应必须是 200 成功");
        return exchange.getBody();
    }

    /**
     * 使用刷新令牌，获得（刷新）访问令牌
     *
     * @param refreshToken 刷新令牌
     * @return 访问令牌
     */
    public CommonResult<OAuth2AccessTokenRespDTO> refreshToken(String refreshToken) {
        // 1.1 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("tenant-id", TENANT_ID.toString());
        addClientHeader(headers);
        // 1.2 构建请求参数
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", refreshToken);

        // 2. 执行请求
        ResponseEntity<CommonResult<OAuth2AccessTokenRespDTO>> exchange = restTemplate.exchange(
                BASE_URL + "/token",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                new ParameterizedTypeReference<CommonResult<OAuth2AccessTokenRespDTO>>() {}); // 解决 CommonResult 的泛型丢失
        Assert.isTrue(exchange.getStatusCode().is2xxSuccessful(), "响应必须是 200 成功");
        return exchange.getBody();
    }

    /**
     * 删除访问令牌
     *
     * @param token 访问令牌
     * @return 成功
     */
    public CommonResult<Boolean> revokeToken(String token) {
        // 1.1 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("tenant-id", TENANT_ID.toString());
        addClientHeader(headers);
        // 1.2 构建请求参数
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("token", token);

        // 2. 执行请求
        ResponseEntity<CommonResult<Boolean>> exchange = restTemplate.exchange(
                BASE_URL + "/token",
                HttpMethod.DELETE,
                new HttpEntity<>(body, headers),
                new ParameterizedTypeReference<CommonResult<Boolean>>() {}); // 解决 CommonResult 的泛型丢失
        Assert.isTrue(exchange.getStatusCode().is2xxSuccessful(), "响应必须是 200 成功");
        return exchange.getBody();
    }

    private static void addClientHeader(HttpHeaders headers) {
        // client 拼接，需要 BASE64 编码
        String client = CLIENT_ID + ":" + CLIENT_SECRET;
        client = Base64Utils.encodeToString(client.getBytes(StandardCharsets.UTF_8));
        headers.add("Authorization", "Basic " + client);
    }

}
