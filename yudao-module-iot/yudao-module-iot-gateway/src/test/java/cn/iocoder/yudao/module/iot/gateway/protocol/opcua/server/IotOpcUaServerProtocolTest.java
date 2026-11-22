package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.server;

import org.eclipse.milo.opcua.sdk.server.EndpointConfig;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.util.EndpointUtil;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateBuilder;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateGenerator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IotOpcUaServerProtocol} 的单元测试
 *
 * @author 芋道源码
 */
public class IotOpcUaServerProtocolTest {

    /**
     * 加密端点要求服务端证书
     */
    private static X509Certificate certificate;

    @BeforeAll
    public static void setUpCertificate() throws Exception {
        KeyPair keyPair = SelfSignedCertificateGenerator.generateRsaKeyPair(2048);
        certificate = new SelfSignedCertificateBuilder(keyPair).setCommonName("endpoint test")
                .setApplicationUri("urn:yudao:test:server").addDnsName("opc.example.com").build();
    }

    @Test
    public void testNormalizeBasePath() {
        assertEquals("/yudao-iot", IotOpcUaServerProtocol.normalizeBasePath("/yudao-iot"));
        assertEquals("/yudao-iot", IotOpcUaServerProtocol.normalizeBasePath("/yudao-iot/"));
        assertEquals("/yudao-iot", IotOpcUaServerProtocol.normalizeBasePath(" //yudao-iot// "));
        assertEquals("/yudao-iot", IotOpcUaServerProtocol.normalizeBasePath("yudao-iot"));
        assertEquals("/a/b", IotOpcUaServerProtocol.normalizeBasePath("//a//b//"));
        // 断言：根路径规范化为空字符串
        assertEquals("", IotOpcUaServerProtocol.normalizeBasePath("/"));
        assertEquals("", IotOpcUaServerProtocol.normalizeBasePath("//"));
        assertEquals("", IotOpcUaServerProtocol.normalizeBasePath(""));
        assertEquals("", IotOpcUaServerProtocol.normalizeBasePath(null));
    }

    @Test
    public void testBuildDiscoveryPath() {
        assertEquals("/yudao-iot/discovery", IotOpcUaServerProtocol.buildDiscoveryPath("/yudao-iot"));
        // 断言：末尾的 /、连续的 / 不产生双斜杠
        assertEquals("/yudao-iot/discovery", IotOpcUaServerProtocol.buildDiscoveryPath("/yudao-iot/"));
        assertEquals("/a/b/discovery", IotOpcUaServerProtocol.buildDiscoveryPath("//a//b//"));
        assertEquals("/discovery", IotOpcUaServerProtocol.buildDiscoveryPath("/"));
        assertEquals("/discovery", IotOpcUaServerProtocol.buildDiscoveryPath(""));
        // 断言：缺少开头的 / 时补齐
        assertEquals("/yudao-iot/discovery", IotOpcUaServerProtocol.buildDiscoveryPath("yudao-iot"));
    }

    @Test
    public void testBuildEndpoints_normalizedPath() {
        for (String path : Arrays.asList("/yudao-iot", "/yudao-iot/", "//yudao-iot//", "yudao-iot", " /yudao-iot/ ")) {
            // 调用：开启 None 会话端点
            List<EndpointConfig> endpoints = buildEndpoints(path, true);

            // 断言：3 个 SignAndEncrypt 会话端点 + 1 个 None 会话端点 + 1 个 None 发现端点
            assertEquals(5, endpoints.size(), path);
            String sessionUrl = "opc.tcp://opc.example.com:4840/yudao-iot";
            String discoveryUrl = sessionUrl + "/discovery";
            List<SecurityPolicy> securePolicies = new ArrayList<>();
            int noneSession = 0;
            int discovery = 0;
            for (EndpointConfig endpoint : endpoints) {
                String url = endpoint.getEndpointUrl();
                assertFalse(url.substring("opc.tcp://".length()).contains("//"), url);
                assertEquals("0.0.0.0", endpoint.getBindAddress());
                assertEquals(4840, endpoint.getBindPort());
                assertEquals(1, endpoint.getTokenPolicies().size());
                if (discoveryUrl.equals(url)) {
                    discovery++;
                    assertEquals(SecurityPolicy.None, endpoint.getSecurityPolicy());
                    assertEquals(MessageSecurityMode.None, endpoint.getSecurityMode());
                    continue;
                }
                // 断言：会话端点（加密、None）使用同一个规范化路径；客户端回退拼接 /discovery 正好是发现端点
                assertEquals(sessionUrl, url, path);
                assertEquals(discoveryUrl, url + "/discovery");
                if (endpoint.getSecurityPolicy() == SecurityPolicy.None) {
                    noneSession++;
                    assertEquals(MessageSecurityMode.None, endpoint.getSecurityMode());
                } else {
                    securePolicies.add(endpoint.getSecurityPolicy());
                    assertEquals(MessageSecurityMode.SignAndEncrypt, endpoint.getSecurityMode());
                }
            }
            assertEquals(1, discovery, path);
            assertEquals(1, noneSession, path);
            assertEquals(Arrays.asList(SecurityPolicy.Basic256Sha256, SecurityPolicy.Aes128_Sha256_RsaOaep,
                    SecurityPolicy.Aes256_Sha256_RsaPss), securePolicies, path);
            // 断言：Milo 按路径注册服务：会话路径与发现路径不同，发现路径以 /discovery 结尾
            assertEquals("/yudao-iot", EndpointUtil.getPath(sessionUrl));
            assertEquals("/yudao-iot/discovery", EndpointUtil.getPath(discoveryUrl));
        }
    }

    @Test
    public void testBuildEndpoints_rootPathAndNoneDisabled() {
        for (String path : Arrays.asList("/", "", "//")) {
            // 调用：不开启 None 会话端点
            List<EndpointConfig> endpoints = buildEndpoints(path, false);

            // 断言：3 个加密会话端点 + 1 个发现端点，没有 None 会话端点
            assertEquals(4, endpoints.size(), path);
            for (EndpointConfig endpoint : endpoints) {
                if (endpoint.getSecurityPolicy() == SecurityPolicy.None) {
                    assertEquals("opc.tcp://opc.example.com:4840/discovery", endpoint.getEndpointUrl(), path);
                } else {
                    assertEquals("opc.tcp://opc.example.com:4840", endpoint.getEndpointUrl(), path);
                    assertEquals(MessageSecurityMode.SignAndEncrypt, endpoint.getSecurityMode());
                }
            }
        }
    }

    private static List<EndpointConfig> buildEndpoints(String path, boolean securityNoneAllowed) {
        IotOpcUaServerConfig config = new IotOpcUaServerConfig();
        config.setHostname("opc.example.com");
        config.setPath(path);
        config.setSecurityNoneAllowed(securityNoneAllowed);
        Set<EndpointConfig> endpoints = IotOpcUaServerProtocol.buildEndpoints(config, 4840, certificate);
        return new ArrayList<>(endpoints);
    }

}
