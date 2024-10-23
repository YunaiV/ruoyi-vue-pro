package com.somle.kingdee.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.TreeMap;

import com.somle.framework.test.core.ut.BaseSpringTest;
import com.somle.kingdee.util.SignatureUtils;
import jakarta.annotation.Resource;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

//import org.mockito.MockitoAnnotations;
@Import(KingdeeService.class)
public class KingdeeServiceTest extends BaseSpringTest {
    @Resource
    KingdeeService service;



//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }

    @Test
    void getApiString() {
        // Arrange
        String reqMtd = "GET";
        String endUrl = "/jdyconnector/app_management/kingdee_auth_token";
        String nonce = "4427456950";
        String timestamp = "1670305063559";
        TreeMap<String, String> params = new TreeMap<>();
        params.put("app_key", "bVZgAZOv1");
        params.put("app_signature", "MzZlYTk0ODk4MWZlNjdiODNmNWU4YzViNzYxNGM5MTFlOGJkN2NjMzk0MTJkZGNhZGM0NzZhN2YxZDJmOTlkZA==");
        String expectedOutput = "GET\n" + //
            "%2Fjdyconnector%2Fapp_management%2Fkingdee_auth_token\n" + //
            "app_key=bVZgAZOv1&app_signature=MzZlYTk0ODk4MWZlNjdiODNmNWU4YzViNzYxNGM5MTFlOGJkN2NjMzk0MTJkZGNhZGM0NzZhN2YxZDJmOTlkZA%253D%253D\n" + //
            "x-api-nonce:4427456950\n" + //
            "x-api-timestamp:1670305063559\n";

        // Act
        String actualOutput = SignatureUtils.getApiString(reqMtd, endUrl, params, nonce, timestamp);

        // Assert
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void hmac256() {
        // Arrange
        String clientSecret = "f2adcfef73369bfc4e1384677d38a0ff";
        String apiString = "GET\n" + //
        "%2Fjdyconnector%2Fapp_management%2Fkingdee_auth_token\n" + //
        "app_key=bVZgAZOv1&app_signature=MzZlYTk0ODk4MWZlNjdiODNmNWU4YzViNzYxNGM5MTFlOGJkN2NjMzk0MTJkZGNhZGM0NzZhN2YxZDJmOTlkZA%253D%253D\n" + //
        "x-api-nonce:4427456950\n" + //
        "x-api-timestamp:1670305063559\n";
        String expectedOutput1 = "91be9b41b23da27c5a788028de71f5e09e9565e4c5a25f21cf9a07df68b50d51";
        String expectedOutput2 = "OTFiZTliNDFiMjNkYTI3YzVhNzg4MDI4ZGU3MWY1ZTA5ZTk1NjVlNGM1YTI1ZjIxY2Y5YTA3ZGY2OGI1MGQ1MQ==";

        // Act
        String actualOutput1 = Hex.encodeHexString(SignatureUtils.hmac256(clientSecret, apiString));
        String actualOutput2 = Base64.encodeBase64String(actualOutput1.getBytes());

        // Assert
        assertEquals(expectedOutput1, actualOutput1);
        assertEquals(expectedOutput2, actualOutput2);

    }

    // @Test
    // void getAppSignature() {
    //     // Arrange
    //     String appKey = "abc";
    //     String appSecret = "abc123";
    //     String expectedOutput1 = "ZDljMTI3NGIyNTE1MTRkYzlkNjc1MDNhYjUzMzgzNWMyY2M4YTdjMzdmNmM3YTVlNDkxMTkzNjdiOTFjNzUyZQ==";

    //     // Act
    //     String actualOutput1 = KingdeeClient.getAppSignature(appSecret, appKey);

    //     // Assert
    //     assertEquals(expectedOutput1, actualOutput1);

    // }

    @Test
    void refreshAuth() {
        service.refreshAuths();
    }
}
