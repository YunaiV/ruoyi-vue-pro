package com.somle.eccang.service;

import com.somle.eccang.model.EccangOrderVO;
import com.somle.eccang.model.EccangToken;
import com.somle.eccang.repository.EccangTokenRepository;
import com.somle.framework.common.util.json.JSONObject;
import com.somle.framework.common.util.json.JsonUtils;
import com.somle.framework.test.core.ut.BaseMockitoUnitTest;
import com.somle.framework.test.core.ut.BaseSpringTest;
import com.somle.framework.test.core.ut.BaseSpringUnitTest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.MessageChannel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

        postData = JsonUtils.newObject();
        postData.put("app_key", "bd40a2c63fb34dd8");
        postData.put("biz_content", JsonUtils.toJsonString(Map.of("action_type", "ADD")));
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
}