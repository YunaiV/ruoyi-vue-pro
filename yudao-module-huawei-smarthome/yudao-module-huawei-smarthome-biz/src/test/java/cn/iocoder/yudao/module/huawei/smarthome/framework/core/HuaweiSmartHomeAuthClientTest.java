package cn.iocoder.yudao.module.huawei.smarthome.framework.core;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.huawei.smarthome.config.HuaweiSmartHomeProperties;
import cn.iocoder.yudao.module.huawei.smarthome.util.HuaweiSmartHomeAuthUtils;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Spy;


import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

public class HuaweiSmartHomeAuthClientTest extends BaseMockitoUnitTest {

    private MockWebServer mockWebServer;
    private HuaweiSmartHomeAuthClient authClient;

    @Spy // 使用 @Spy 注入真实的 properties 实例，但可以修改其字段值
    private HuaweiSmartHomeProperties properties;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // 初始化 properties
        properties = new HuaweiSmartHomeProperties();
        properties.setEndpoint(mockWebServer.url("/").toString()); // http://localhost:xxxx/
        properties.setAccessKey("test-ak");
        properties.setSecretKey("test-sk-hex"); // 假设这是16进制的SK
        properties.setProjectId("test-project-id");
        properties.setConnectTimeout(1000);
        properties.setReadTimeout(1000);

        authClient = new HuaweiSmartHomeAuthClient(properties);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testSendRequest_Post_Success() throws IOException, InterruptedException {
        // Arrange
        String resourcePath = "/openapi/test/resource";
        String requestBodyJson = "{\"key\":\"value\"}";
        String mockResponseBody = "{\"status\":\"success\"}";
        String mockSign = "x-access-key,x-project-id,x-request-id,x-timestamp;mockedsignvalue";

        mockWebServer.enqueue(new MockResponse().setBody(mockResponseBody).setResponseCode(200));

        // Mock HuaweiSmartHomeAuthUtils.calculateSignWithHexSecret
        try (MockedStatic<HuaweiSmartHomeAuthUtils> mockedAuthUtils = mockStatic(HuaweiSmartHomeAuthUtils.class)) {
            mockedAuthUtils.when(() -> HuaweiSmartHomeAuthUtils.calculateSignWithHexSecret(
                    eq(properties.getAccessKey()),
                    eq(properties.getSecretKey()),
                    eq(properties.getProjectId()),
                    anyString(), // requestId is dynamic
                    anyString(), // timestamp is dynamic
                    eq(requestBodyJson)
            )).thenReturn(mockSign);

            // Act
            String actualResponse = authClient.post(resourcePath, requestBodyJson);

            // Assert
            assertEquals(mockResponseBody, actualResponse);

            RecordedRequest recordedRequest = mockWebServer.takeRequest();
            assertEquals("POST", recordedRequest.getMethod());
            assertEquals(resourcePath, recordedRequest.getPath());
            assertEquals(properties.getAccessKey(), recordedRequest.getHeader("x-access-key"));
            assertEquals(properties.getProjectId(), recordedRequest.getHeader("x-project-id"));
            assertNotNull(recordedRequest.getHeader("x-request-id"));
            assertNotNull(recordedRequest.getHeader("x-timestamp"));
            assertEquals(mockSign, recordedRequest.getHeader("x-sign"));
            assertEquals("application/json; charset=utf-8", recordedRequest.getHeader("Content-Type"));
            assertEquals(requestBodyJson, recordedRequest.getBody().readUtf8());
        }
    }

    @Test
    void testSendRequest_Get_Success() throws IOException, InterruptedException {
        // Arrange
        String resourcePath = "/openapi/test/resource";
        String mockResponseBody = "{\"data\":\"some data\"}";
        String mockSign = "x-access-key,x-project-id,x-request-id,x-timestamp;mockedsignvalueget";

        mockWebServer.enqueue(new MockResponse().setBody(mockResponseBody).setResponseCode(200));

        try (MockedStatic<HuaweiSmartHomeAuthUtils> mockedAuthUtils = mockStatic(HuaweiSmartHomeAuthUtils.class)) {
            mockedAuthUtils.when(() -> HuaweiSmartHomeAuthUtils.calculateSignWithHexSecret(
                    eq(properties.getAccessKey()),
                    eq(properties.getSecretKey()),
                    eq(properties.getProjectId()),
                    anyString(),
                    anyString(),
                    eq("") // GET request body for sign is empty string
            )).thenReturn(mockSign);

            // Act
            String actualResponse = authClient.get(resourcePath);

            // Assert
            assertEquals(mockResponseBody, actualResponse);

            RecordedRequest recordedRequest = mockWebServer.takeRequest();
            assertEquals("GET", recordedRequest.getMethod());
            assertEquals(resourcePath, recordedRequest.getPath());
            assertEquals(mockSign, recordedRequest.getHeader("x-sign"));
            assertTrue(recordedRequest.getBodySize() == 0);
        }
    }


    @Test
    void testSendRequest_ApiReturnsError_ThrowsIOException() {
        // Arrange
        String resourcePath = "/openapi/error/resource";
        String requestBodyJson = "{\"key\":\"value\"}";
        String errorResponseBody = "{\"errorCode\":\"123\", \"errorDesc\":\"Something went wrong\"}";
        String mockSign = "x-access-key,x-project-id,x-request-id,x-timestamp;mockederror";

        mockWebServer.enqueue(new MockResponse().setBody(errorResponseBody).setResponseCode(400));

        try (MockedStatic<HuaweiSmartHomeAuthUtils> mockedAuthUtils = mockStatic(HuaweiSmartHomeAuthUtils.class)) {
            mockedAuthUtils.when(HuaweiSmartHomeAuthUtils::calculateSignWithHexSecret).thenReturn(mockSign);

            // Act & Assert
            IOException exception = assertThrows(IOException.class, () -> {
                authClient.post(resourcePath, requestBodyJson);
            });
            assertTrue(exception.getMessage().contains("华为API请求失败"));
            assertTrue(exception.getMessage().contains("HTTP Status=400"));
            assertTrue(exception.getMessage().contains(errorResponseBody));
        }
    }

    @Test
    void testSendRequest_EndpointNotConfigured_ThrowsIOException() {
        // Arrange
        properties.setEndpoint(null); // Simulate endpoint not configured
        HuaweiSmartHomeAuthClient clientWithNoEndpoint = new HuaweiSmartHomeAuthClient(properties);
        String resourcePath = "/test";

        // Act & Assert
        IOException exception = assertThrows(IOException.class, () -> {
            clientWithNoEndpoint.get(resourcePath);
        });
        assertEquals("Huawei SmartHome API Endpoint is not configured.", exception.getMessage());
    }


    @Test
    void testSendRequest_AuthUtilThrowsException_PropagatesRuntimeException() {
        // Arrange
        String resourcePath = "/openapi/auth-fail";
        String requestBodyJson = "{}";

        try (MockedStatic<HuaweiSmartHomeAuthUtils> mockedAuthUtils = mockStatic(HuaweiSmartHomeAuthUtils.class)) {
            mockedAuthUtils.when(() -> HuaweiSmartHomeAuthUtils.calculateSignWithHexSecret(
                    anyString(), anyString(), anyString(), anyString(), anyString(), anyString()
            )).thenThrow(new RuntimeException("Signature calculation failed"));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                authClient.post(resourcePath, requestBodyJson);
            });
            assertEquals("Signature calculation failed", exception.getMessage());
        }
    }

}
