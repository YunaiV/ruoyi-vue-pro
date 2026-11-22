package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.server.security;

import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.server.IotOpcUaServerConfig;
import org.eclipse.milo.opcua.stack.core.util.CertificateUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.security.cert.X509Certificate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IotOpcUaServerCertificateManager} 的单元测试：自签名证书的 SAN 按主机名类型生成
 *
 * @author 芋道源码
 */
public class IotOpcUaServerCertificateManagerTest {

    @TempDir
    Path tempDir;

    @Test
    public void testInitialize_ipv4HostnameUsesIpAddressSan() {
        X509Certificate certificate = generate("192.168.1.10");
        // 断言：IP 字面量生成 iPAddress，不生成 dNSName
        assertEquals(Collections.singletonList("192.168.1.10"), CertificateUtil.getSanIpAddresses(certificate));
        assertTrue(CertificateUtil.getSanDnsNames(certificate).isEmpty());
    }

    @Test
    public void testInitialize_ipv6HostnameUsesIpAddressSan() {
        X509Certificate certificate = generate("[::1]");
        // 断言：带方括号的 IPv6 字面量生成 iPAddress
        assertEquals(1, CertificateUtil.getSanIpAddresses(certificate).size());
        assertTrue(CertificateUtil.getSanDnsNames(certificate).isEmpty());
    }

    @Test
    public void testInitialize_domainHostnameUsesDnsNameSan() {
        X509Certificate certificate = generate("opc.example.com");
        // 断言：域名生成 dNSName，不生成 iPAddress
        assertEquals(Collections.singletonList("opc.example.com"), CertificateUtil.getSanDnsNames(certificate));
        assertTrue(CertificateUtil.getSanIpAddresses(certificate).isEmpty());
    }

    @Test
    public void testToIpLiteral() {
        assertEquals("127.0.0.1", IotOpcUaServerCertificateManager.toIpLiteral("127.0.0.1"));
        assertEquals("::1", IotOpcUaServerCertificateManager.toIpLiteral("[::1]"));
        assertEquals("fe80::1", IotOpcUaServerCertificateManager.toIpLiteral("fe80::1"));
        assertNull(IotOpcUaServerCertificateManager.toIpLiteral("localhost"));
        assertNull(IotOpcUaServerCertificateManager.toIpLiteral("opc.example.com"));
        assertNull(IotOpcUaServerCertificateManager.toIpLiteral("999.1.1.1"));
    }

    private X509Certificate generate(String hostname) {
        IotOpcUaServerConfig config = new IotOpcUaServerConfig();
        config.setHostname(hostname);
        config.setPkiDir(tempDir.resolve("pki").toString());
        config.setKeystorePassword("test-keystore");
        IotOpcUaServerCertificateManager manager = new IotOpcUaServerCertificateManager(config);
        try {
            manager.initialize();
            return manager.getCertificate();
        } finally {
            manager.close();
        }
    }

}
