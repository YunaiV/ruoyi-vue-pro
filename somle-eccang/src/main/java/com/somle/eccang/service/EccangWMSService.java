package com.somle.eccang.service;

import cn.iocoder.yudao.framework.common.util.general.CoreUtils;
import cn.iocoder.yudao.framework.common.util.json.JSONObject;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import com.somle.eccang.model.EccangResponse;
import com.somle.eccang.model.EccangWMSResponse;
import com.somle.eccang.model.EccangWMSToken;
import com.somle.eccang.model.req.EccangSpecialOrdersReqVo;
import com.somle.eccang.repository.EccangWMSTokenRepository;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.io.ByteArrayInputStream;
import java.util.Objects;
import java.util.stream.Stream;


@Slf4j
@Service
public class EccangWMSService {

    private OkHttpClient client;

    private int pageSize = 20;
    private  String appToken;

    private  String appKey;

    private final String url = "http://somle.yunwms.com/default/svc/web-service";

    @Autowired
    private EccangWMSTokenRepository eccangWMSTokenRepository;
    @PostConstruct
    public void init() {
        client = new OkHttpClient();
        EccangWMSToken eccangWMSToken = eccangWMSTokenRepository.findAll().get(0);
        appToken = eccangWMSToken.getAppToken();
        appKey = eccangWMSToken.getAppKey();
    }


    public Stream<EccangResponse.EccangPage> getSpecialOrdersList(EccangSpecialOrdersReqVo eccangSpecialOrdersReqVo) {
        return getAllPage(JsonUtilsX.toJSONObject(eccangSpecialOrdersReqVo), "getSpecialOrdersList");
    }

    private Stream<EccangResponse.EccangPage> getAllPage(JSONObject payload, String endpoint) {
        payload.put("page", 1);
        payload.put("page_size", pageSize);
        return Stream.iterate(
                getPage(payload, endpoint), Objects::nonNull,
                bizContent -> {
                    if (bizContent.hasNext()) {
                        log.debug("have next,endpoint:{}当前进度：{}/{}", endpoint, (bizContent.getPage() - 1) * pageSize + bizContent.getData().size(), bizContent.getTotal());
                        payload.put("page", bizContent.getPage() + 1);
                        return getPage(payload, endpoint);
                    } else {
                        log.debug("no next page");
                        return null;
                    }
                }
        );
        // );
    }

    private EccangResponse.EccangPage getPage(JSONObject payload, String endpoint) {
        EccangWMSResponse response = getResponse(payload, endpoint);
        EccangResponse.EccangPage page = response.getEccangPage();
        return page;
    }



    @SneakyThrows
    private EccangWMSResponse getResponse(JSONObject payload, String endpoint) {
        EccangWMSResponse responseFinal = CoreUtils.retry(ctx -> {
            var requestBody = requestBody(payload, endpoint);
            // 请求体
            RequestBody body = RequestBody.create(requestBody, MediaType.get("text/xml; charset=utf-8"));

            // 创建请求
            Request request = new Request.Builder()
                    .url(url) // 替换为实际的服务URL
                    .post(body)
                    .addHeader("Content-Type", "text/xml; charset=utf-8")
                    .build();

            int retryCount = ctx.getRetryCount();
            // 记录每次重试的日志
            log.debug("正在请求url= {},第 {} 次重试。endpoint = {}", request.url(), retryCount + 1, endpoint);
            try (var response = client.newCall(request).execute()) {
                switch (response.code()) {
                    case 200:
                        var responseBody = response.body().string();
                        String jsonObject= parseObjectByDom4j(responseBody).toString();
                        var responseOriginal = JsonUtilsX.parseObject(jsonObject, EccangWMSResponse.class);
                        return responseOriginal;
                    case 429:
                        throw new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS, "Too many requests, please try again later.");
                    default:
                        throw new RuntimeException("Unknown response code " + response);
                }
            }
        });
        return responseFinal;
    }

    protected String requestBody(JSONObject reqParams, String ecMethod) {

        String requestBody =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"http://www.example.org/Ec/\">\n" +
                        "    <SOAP-ENV:Body>\n" +
                        "        <ns1:callService>\n" +
                        "            <paramsJson>"+ reqParams.toString() +"</paramsJson>\n" +
                        "            <appToken>" + appToken + "</appToken>\n" +
                        "            <appKey>" + appKey +"</appKey>\n" +
                        "            <service>" + ecMethod + "</service>\n" +
                        "        </ns1:callService>\n" +
                        "    </SOAP-ENV:Body>\n" +
                        "</SOAP-ENV:Envelope>";
        return requestBody;
    }

    @SneakyThrows
    protected JSONObject parseObjectByDom4j(String responseBody) {
        // 创建一个Dom4J框架提供的解析器对象
        SAXReader saxReader = new SAXReader();
        //2.使用saxReader对象把需要解析的XML文件读成一个Document对象。
        Document document = saxReader.read(new ByteArrayInputStream(responseBody.getBytes()));
        // 3、获得根元素对象
        Element root = document.getRootElement();
        // 获取<reponse>标签中的内容，即返回结果
        String responseText = root.elements().get(0).elements().get(0).elements().get(0).getText();
        JSONObject result = JsonUtilsX.parseObject(responseText, JSONObject.class);
        return result;
    }
}
