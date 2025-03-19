package com.somle.eccang.service;

import cn.iocoder.yudao.framework.common.util.json.JSONObject;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import com.somle.eccang.model.EccangToken;
import com.somle.eccang.repository.EccangTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


@Slf4j
@Import({
    EccangService.class,
})
public class EccangServiceTest extends BaseMockitoUnitTest {
    static EccangService service;
    static JSONObject postData;


    @BeforeAll
    static void setup() {
        service = new EccangService();
        var tokenRepo =  Mockito.mock(EccangTokenRepository.class);
        List<EccangToken> tokens = new ArrayList<>();
        tokens.add(new EccangToken("bd40a2c63fb34dd8", ""));
        when(tokenRepo.findAll()).thenReturn(tokens);
        service.tokenRepo = tokenRepo;
        service.init();

        postData = JsonUtilsX.newObject();
        postData.put("app_key", "bd40a2c63fb34dd8");
        postData.put("biz_content", JsonUtilsX.toJsonString(Map.of("action_type", "ADD")));
        postData.put("charset", "UTF-8");
        postData.put("interface_method", "syncProduct");
        postData.put("nonce_str", "随机字符串");
        postData.put("service_id", "授权的服务ID");
        postData.put("sign_type", "MD5");
        postData.put("timestamp", "毫秒级时间戳");
        postData.put("version", "V1.0.0c2d81edc9d304cc6");
    }

    @BeforeEach
    void init() {
        log.info("run set up");
    }



    @Test
    void concateParams() {
        var expected = "app_key=bd40a2c63fb34dd8&biz_content={\"action_type\":\"ADD\"}&charset=UTF-8&interface_method=syncProduct&nonce_str=随机字符串&service_id=授权的服务ID&sign_type=MD5&timestamp=毫秒级时间戳&version=V1.0.0c2d81edc9d304cc6";
        assertEquals(expected, service.concatenateParams(postData));
    }

//    @Test
//    void requestBody() {
//        var expected = postData;
//        expected.put("sign", service.concatenateParams(postData));
//        var actual = service.requestBody(EccangOrderVO.builder().build(), "get");
//        assertEquals(expected, actual);
//    }

    @Test
    void getOrderArchivePages(){
//        EccangOrderVO.builder()
//            .condition(EccangOrderVO.Condition.builder()
//                .platformPaidDateStart(beforeYesterdayFirstSecond)
//                .platformPaidDateEnd(beforeYesterdayLastSecond)
//                .build())
//            .build(),
//            beforeYesterday.getYear()
//        LocalDateTimeUtils.getToday().minusDays(3)
//        EccangOrderVO.builder()
//            .condition(EccangOrderVO.Condition.builder()
//                .platformPaidDateStart()
//                .platformPaidDateEnd(beforeYesterdayLastSecond)
//                .build())
//            .build(),
//            beforeYesterday.getYear()
//        service.getOrderPlusArchivePages()
    }
}