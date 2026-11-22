package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.security;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.opcua.IotOpcUaAuthTypeEnum;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.IotOpcUaClientConfig;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.eclipse.milo.opcua.stack.core.Stack;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.UserTokenType;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.UserTokenPolicy;
import org.eclipse.milo.opcua.stack.core.util.CertificateUtil;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateBuilder;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.OPC_UA_ENDPOINT_REJECTED;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.ubyte;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IotOpcUaClientSecurityFactory} 的单元测试
 *
 * @author 芋道源码
 */
public class IotOpcUaClientSecurityFactoryTest {

    private static final Integer AUTH_ANONYMOUS = IotOpcUaAuthTypeEnum.ANONYMOUS.getType();
    private static final Integer AUTH_USERNAME = IotOpcUaAuthTypeEnum.USERNAME.getType();

    private static final String ENDPOINT = "opc.tcp://192.168.1.10:4840/server";

    private static final UserTokenPolicy ANONYMOUS = new UserTokenPolicy("anonymous", UserTokenType.Anonymous,
            null, null, null);
    private static final UserTokenPolicy USERNAME_SECURE = new UserTokenPolicy("username", UserTokenType.UserName,
            null, null, SecurityPolicy.Basic256Sha256.getUri());
    private static final UserTokenPolicy USERNAME_PLAIN = new UserTokenPolicy("username-plain", UserTokenType.UserName,
            null, null, SecurityPolicy.None.getUri());

    @TempDir
    Path pkiDir;

    // ========== validateDevice ==========

    @Test
    public void testValidateDevice_anonymousRejected() {
        IotOpcUaClientSecurityFactory factory = new IotOpcUaClientSecurityFactory(newUsernameConfig());
        // 只支持用户名认证：匿名一律拒绝，不论安全策略、模式
        for (String[] security : new String[][]{{"Basic256Sha256", "SignAndEncrypt"},
                {"Aes256_Sha256_RsaPss", "Sign"}, {"None", "None"}}) {
            String reason = factory.validateDevice(newDevice(security[0], security[1], AUTH_ANONYMOUS, ""));
            assertNotNull(reason, security[0] + "/" + security[1]);
            assertTrue(reason.contains("只支持用户名认证"), reason);
        }
        // 历史数据中的匿名设备即使带了已配置的凭据别名，也拒绝
        assertNotNull(factory.validateDevice(newDevice("Basic256Sha256", "SignAndEncrypt", AUTH_ANONYMOUS, "reader")));
    }

    @Test
    public void testValidateDevice_securityRejected() {
        IotOpcUaClientSecurityFactory factory = new IotOpcUaClientSecurityFactory(newUsernameConfig());
        // 用户名认证的口令不能明文传输：None 策略、None / Sign 模式都拒绝
        for (String[] security : new String[][]{{"None", "None"}, {"None", "SignAndEncrypt"},
                {"Basic256Sha256", "Sign"}, {"Basic256Sha256", "None"}}) {
            assertNotNull(factory.validateDevice(newDevice(security[0], security[1], AUTH_USERNAME, "reader")),
                    security[0] + "/" + security[1]);
        }
        // 未知的安全策略、认证方式
        assertNotNull(factory.validateDevice(newDevice("Unknown", "SignAndEncrypt", AUTH_USERNAME, "reader")));
        assertNotNull(factory.validateDevice(newDevice("Basic256Sha256", "SignAndEncrypt", 99, "reader")));
    }

    @Test
    public void testValidateDevice_username() {
        IotOpcUaClientConfig config = new IotOpcUaClientConfig();
        IotOpcUaClientConfig.Credential credential = new IotOpcUaClientConfig.Credential();
        credential.setUsername("reader");
        credential.setPassword("secret");
        credential.setTenantIds(Collections.singletonList(1L));
        credential.setEndpoints(Collections.singletonList(ENDPOINT));
        config.getCredentials().put("reader", credential);
        IotOpcUaClientSecurityFactory factory = new IotOpcUaClientSecurityFactory(config);

        assertNull(factory.validateDevice(newDevice("Basic256Sha256", "SignAndEncrypt", AUTH_USERNAME, "reader")));
        // Sign 模式、别名不存在、租户不匹配、端点不匹配
        assertNotNull(factory.validateDevice(newDevice("Basic256Sha256", "Sign", AUTH_USERNAME, "reader")));
        assertNotNull(factory.validateDevice(newDevice("Basic256Sha256", "SignAndEncrypt", AUTH_USERNAME, "other")));
        IotOpcUaDeviceConfigRespDTO otherTenant = newDevice("Basic256Sha256", "SignAndEncrypt", AUTH_USERNAME, "reader");
        otherTenant.setTenantId(2L);
        assertNotNull(factory.validateDevice(otherTenant));
        IotOpcUaDeviceConfigRespDTO otherEndpoint = newDevice("Basic256Sha256", "SignAndEncrypt", AUTH_USERNAME, "reader");
        otherEndpoint.setEndpointUrl("opc.tcp://192.168.1.11:4840/server");
        assertNotNull(factory.validateDevice(otherEndpoint));
        // 凭据没有配置允许的租户时拒绝
        credential.setTenantIds(Collections.<Long>emptyList());
        assertNotNull(factory.validateDevice(newDevice("Basic256Sha256", "SignAndEncrypt", AUTH_USERNAME, "reader")));
    }

    @Test
    public void testValidateDevice_anyTenantAndEndpoint() {
        IotOpcUaClientConfig config = newUsernameConfig();
        config.getCredentials().get("reader").setTenantIds(Arrays.asList(1L, 2L));
        IotOpcUaClientSecurityFactory factory = new IotOpcUaClientSecurityFactory(config);
        // 网关不绑定租户：只受凭据级 tenant-ids 限制，凭据允许的租户都可以使用
        IotOpcUaDeviceConfigRespDTO otherTenant = newDevice("Basic256Sha256", "SignAndEncrypt", AUTH_USERNAME, "reader");
        otherTenant.setTenantId(2L);
        assertNull(factory.validateDevice(otherTenant));
        otherTenant.setTenantId(3L);
        assertNotNull(factory.validateDevice(otherTenant));

        // 端点地址不正确
        IotOpcUaDeviceConfigRespDTO device = newDevice("Basic256Sha256", "SignAndEncrypt", AUTH_USERNAME, "reader");
        device.setEndpointUrl("http://192.168.1.10:4840");
        assertNotNull(factory.validateDevice(device));
        device.setEndpointUrl("opc.tcp://user@192.168.1.10:4840");
        assertNotNull(factory.validateDevice(device));
    }

    @Test
    public void testValidateDevice_blankPassword() {
        IotOpcUaClientConfig config = new IotOpcUaClientConfig();
        IotOpcUaClientConfig.Credential credential = new IotOpcUaClientConfig.Credential();
        credential.setUsername("reader");
        credential.setPassword(""); // 例如 ${IOT_OPC_UA_PLC_READER_PASSWORD:} 未设置
        credential.setTenantIds(Collections.singletonList(1L));
        credential.setEndpoints(Collections.singletonList(ENDPOINT));
        config.getCredentials().put("reader", credential);
        IotOpcUaClientSecurityFactory factory = new IotOpcUaClientSecurityFactory(config);

        String reason = factory.validateDevice(newDevice("Basic256Sha256", "SignAndEncrypt", AUTH_USERNAME, "reader"));
        assertNotNull(reason);
        assertTrue(reason.contains("口令为空"));
        credential.setPassword(null);
        assertNotNull(factory.validateDevice(newDevice("Basic256Sha256", "SignAndEncrypt", AUTH_USERNAME, "reader")));
        // 只有空白的口令：按未配置拒绝
        credential.setPassword(" \t ");
        reason = factory.validateDevice(newDevice("Basic256Sha256", "SignAndEncrypt", AUTH_USERNAME, "reader"));
        assertNotNull(reason);
        assertTrue(reason.contains("口令为空"));
        // 只有空白的用户名：按未配置凭据拒绝
        credential.setPassword("secret");
        credential.setUsername("  ");
        reason = factory.validateDevice(newDevice("Basic256Sha256", "SignAndEncrypt", AUTH_USERNAME, "reader"));
        assertNotNull(reason);
        assertTrue(reason.contains("未配置凭据别名"));
    }

    @Test
    public void testParseEndpoint() {
        assertNotNull(IotOpcUaClientSecurityFactory.parseEndpoint(ENDPOINT));
        assertNotNull(IotOpcUaClientSecurityFactory.parseEndpoint("opc.tcp://[::1]:4840"));
        // 查询参数、片段、用户信息、非 opc.tcp
        assertNull(IotOpcUaClientSecurityFactory.parseEndpoint(ENDPOINT + "?a=1"));
        assertNull(IotOpcUaClientSecurityFactory.parseEndpoint(ENDPOINT + "#a"));
        assertNull(IotOpcUaClientSecurityFactory.parseEndpoint("opc.tcp://user@192.168.1.10:4840"));
        assertNull(IotOpcUaClientSecurityFactory.parseEndpoint("http://192.168.1.10:4840"));
        assertNull(IotOpcUaClientSecurityFactory.parseEndpoint(" "));
    }

    @Test
    public void testBuildSessionKey() {
        IotOpcUaDeviceConfigRespDTO device = newDevice("Basic256Sha256", "SignAndEncrypt", AUTH_USERNAME, "reader");
        device.setTimeout(5000);
        String key = IotOpcUaClientSecurityFactory.buildSessionKey(device);
        // 同一租户、端点、安全配置、超时时间：同一会话
        IotOpcUaDeviceConfigRespDTO same = newDevice("Basic256Sha256", "SignAndEncrypt", AUTH_USERNAME, "reader");
        same.setDeviceId(2L);
        same.setTimeout(5000);
        assertEquals(key, IotOpcUaClientSecurityFactory.buildSessionKey(same));
        // 不同租户：不共用会话
        same.setTenantId(2L);
        assertNotEquals(key, IotOpcUaClientSecurityFactory.buildSessionKey(same));
        // 超时时间变化：换到新的会话（新建客户端后生效）
        device.setTimeout(8000);
        assertNotEquals(key, IotOpcUaClientSecurityFactory.buildSessionKey(device));
        // 重试间隔不同：不共用会话（会话级重试只有一个间隔，共用时取值不确定）
        device.setTimeout(5000);
        device.setRetryInterval(30000);
        assertNotEquals(key, IotOpcUaClientSecurityFactory.buildSessionKey(device));
    }

    // ========== selectEndpoint ==========

    @Test
    public void testSelectEndpoint_strictPolicyAndMode() {
        IotOpcUaClientSecurityFactory factory = new IotOpcUaClientSecurityFactory(new IotOpcUaClientConfig());
        List<EndpointDescription> endpoints = Arrays.asList(
                newEndpoint(ENDPOINT, SecurityPolicy.Basic256Sha256, MessageSecurityMode.Sign, USERNAME_SECURE),
                newEndpoint(ENDPOINT, SecurityPolicy.Basic256Sha256, MessageSecurityMode.SignAndEncrypt, USERNAME_SECURE));

        EndpointDescription endpoint = factory.selectEndpoint(endpoints,
                newDevice("Basic256Sha256", "SignAndEncrypt", AUTH_USERNAME, "reader"));

        assertEquals(MessageSecurityMode.SignAndEncrypt, endpoint.getSecurityMode());
        // 服务端没有该策略：拒绝（不降级）
        ServiceException exception = assertThrows(ServiceException.class, () -> factory.selectEndpoint(endpoints,
                newDevice("Aes128_Sha256_RsaOaep", "SignAndEncrypt", AUTH_USERNAME, "reader")));
        assertEquals(OPC_UA_ENDPOINT_REJECTED.getCode(), exception.getCode());
    }

    @Test
    public void testSelectEndpoint_anonymousTokenOnly() {
        IotOpcUaClientSecurityFactory factory = new IotOpcUaClientSecurityFactory(new IotOpcUaClientConfig());
        IotOpcUaDeviceConfigRespDTO device = newDevice("Basic256Sha256", "SignAndEncrypt", AUTH_USERNAME, "reader");
        // 端点只提供 Anonymous 令牌：拒绝，不回退匿名
        List<EndpointDescription> anonymousOnly = Collections.singletonList(newEndpoint(ENDPOINT,
                SecurityPolicy.Basic256Sha256, MessageSecurityMode.SignAndEncrypt, ANONYMOUS));
        ServiceException exception = assertThrows(ServiceException.class,
                () -> factory.selectEndpoint(anonymousOnly, device));
        assertEquals(OPC_UA_ENDPOINT_REJECTED.getCode(), exception.getCode());
    }

    @Test
    public void testSelectEndpoint_usernameTokenPolicy() {
        IotOpcUaClientSecurityFactory factory = new IotOpcUaClientSecurityFactory(new IotOpcUaClientConfig());
        IotOpcUaDeviceConfigRespDTO device = newDevice("Basic256Sha256", "SignAndEncrypt", AUTH_USERNAME, "reader");
        // 同一路径只有明文令牌：拒绝，不回退到其它路径
        List<EndpointDescription> plainOnly = Arrays.asList(
                newEndpoint(ENDPOINT, SecurityPolicy.Basic256Sha256, MessageSecurityMode.SignAndEncrypt, USERNAME_PLAIN),
                newEndpoint("opc.tcp://192.168.1.10:4840/other", SecurityPolicy.Basic256Sha256,
                        MessageSecurityMode.SignAndEncrypt, USERNAME_SECURE));
        assertThrows(ServiceException.class, () -> factory.selectEndpoint(plainOnly, device));
        // 有安全的用户名令牌
        List<EndpointDescription> secure = Collections.singletonList(newEndpoint(ENDPOINT, SecurityPolicy.Basic256Sha256,
                MessageSecurityMode.SignAndEncrypt, USERNAME_PLAIN, USERNAME_SECURE));
        assertNotNull(factory.selectEndpoint(secure, device));
    }

    @Test
    public void testSelectEndpoint_emptyTransportProfile() {
        IotOpcUaClientSecurityFactory factory = new IotOpcUaClientSecurityFactory(new IotOpcUaClientConfig());
        IotOpcUaDeviceConfigRespDTO device = newDevice("Basic256Sha256", "SignAndEncrypt", AUTH_USERNAME, "reader");
        EndpointDescription https = newEndpoint("https://192.168.1.10:4840/server", SecurityPolicy.Basic256Sha256,
                MessageSecurityMode.SignAndEncrypt, (String) null, USERNAME_SECURE);
        EndpointDescription tcp = newEndpoint(ENDPOINT, SecurityPolicy.Basic256Sha256,
                MessageSecurityMode.SignAndEncrypt, "", USERNAME_SECURE);
        EndpointDescription websocket = newEndpoint(ENDPOINT, SecurityPolicy.Basic256Sha256,
                MessageSecurityMode.SignAndEncrypt, "http://opcfoundation.org/UA-Profile/Transport/wss-uasc-uabinary",
                USERNAME_SECURE);

        // 未返回传输协议 URI：兼容 opc.tcp:// 端点
        assertSame(tcp, factory.selectEndpoint(Arrays.asList(https, tcp), device));
        // 未返回传输协议 URI 的其它传输、明确的其它传输：拒绝
        assertThrows(ServiceException.class, () -> factory.selectEndpoint(Collections.singletonList(https), device));
        assertThrows(ServiceException.class, () -> factory.selectEndpoint(Collections.singletonList(websocket), device));
    }

    @Test
    public void testChooseUsernameTokenPolicy() {
        // 安全的 Anonymous 令牌排在前面：只能选中 UserName 令牌，否则用户名认证握手失败
        UserTokenPolicy anonymousSecure = new UserTokenPolicy("anonymous-secure", UserTokenType.Anonymous,
                null, null, SecurityPolicy.Basic256Sha256.getUri());
        String endpointPolicyUri = SecurityPolicy.Basic256Sha256.getUri();
        assertSame(USERNAME_SECURE, IotOpcUaClientSecurityFactory.chooseUsernameTokenPolicy(
                Stream.of(anonymousSecure, USERNAME_PLAIN, USERNAME_SECURE), endpointPolicyUri).orElse(null));
        // 没有安全的 UserName 令牌：不选（握手时抛出异常，不明文发送口令）
        assertFalse(IotOpcUaClientSecurityFactory.chooseUsernameTokenPolicy(
                Stream.of(anonymousSecure, USERNAME_PLAIN), endpointPolicyUri).isPresent());
        // 令牌未指定安全策略：按端点的安全策略判断
        UserTokenPolicy usernameInherit = new UserTokenPolicy("username-inherit", UserTokenType.UserName,
                null, null, null);
        assertSame(usernameInherit, IotOpcUaClientSecurityFactory.chooseUsernameTokenPolicy(
                Stream.of(anonymousSecure, usernameInherit), endpointPolicyUri).orElse(null));
        assertFalse(IotOpcUaClientSecurityFactory.chooseUsernameTokenPolicy(
                Stream.of(usernameInherit), SecurityPolicy.None.getUri()).isPresent());
    }

    @Test
    public void testSelectEndpoint_hostMismatch() {
        IotOpcUaClientConfig config = new IotOpcUaClientConfig();
        IotOpcUaClientSecurityFactory factory = new IotOpcUaClientSecurityFactory(config);
        List<EndpointDescription> endpoints = Collections.singletonList(newEndpoint("opc.tcp://plc-host:4840/server",
                SecurityPolicy.Basic256Sha256, MessageSecurityMode.SignAndEncrypt, USERNAME_SECURE));
        IotOpcUaDeviceConfigRespDTO device = newDevice("Basic256Sha256", "SignAndEncrypt", AUTH_USERNAME, "reader");

        // 默认拒绝
        assertThrows(ServiceException.class, () -> factory.selectEndpoint(endpoints, device));
        // 开启改写：改为配置的主机、端口
        config.setEndpointHostRewrite(true);
        assertEquals("opc.tcp://192.168.1.10:4840/server", factory.selectEndpoint(endpoints, device).getEndpointUrl());
    }

    @Test
    public void testSelectEndpoint_nonRootPathStrict() {
        IotOpcUaClientSecurityFactory factory = new IotOpcUaClientSecurityFactory(new IotOpcUaClientConfig());
        // 配置的路径 /server 没有该策略的端点，只有 /other 有：拒绝，不换到其它路径
        List<EndpointDescription> otherPathOnly = Arrays.asList(
                newEndpoint(ENDPOINT, SecurityPolicy.Basic256Sha256, MessageSecurityMode.Sign, USERNAME_SECURE),
                newEndpoint("opc.tcp://192.168.1.10:4840/other", SecurityPolicy.Basic256Sha256,
                        MessageSecurityMode.SignAndEncrypt, USERNAME_SECURE));
        IotOpcUaDeviceConfigRespDTO device = newDevice("Basic256Sha256", "SignAndEncrypt", AUTH_USERNAME, "reader");
        ServiceException exception = assertThrows(ServiceException.class, () -> factory.selectEndpoint(otherPathOnly, device));
        assertEquals(OPC_UA_ENDPOINT_REJECTED.getCode(), exception.getCode());

        // 末尾的 / 不影响路径匹配
        device.setEndpointUrl(ENDPOINT + "/");
        List<EndpointDescription> samePath = Collections.singletonList(newEndpoint(ENDPOINT, SecurityPolicy.Basic256Sha256,
                MessageSecurityMode.SignAndEncrypt, USERNAME_SECURE));
        assertEquals(ENDPOINT, factory.selectEndpoint(samePath, device).getEndpointUrl());

        // 根路径：优先根路径的端点；没有时可以选择其它路径
        device.setEndpointUrl("opc.tcp://192.168.1.10:4840");
        assertEquals("opc.tcp://192.168.1.10:4840/other", factory.selectEndpoint(otherPathOnly, device).getEndpointUrl());
        List<EndpointDescription> withRoot = Arrays.asList(
                newEndpoint("opc.tcp://192.168.1.10:4840/other", SecurityPolicy.Basic256Sha256,
                        MessageSecurityMode.SignAndEncrypt, USERNAME_SECURE),
                newEndpoint("opc.tcp://192.168.1.10:4840/", SecurityPolicy.Basic256Sha256,
                        MessageSecurityMode.SignAndEncrypt, USERNAME_SECURE));
        assertEquals("opc.tcp://192.168.1.10:4840/", factory.selectEndpoint(withRoot, device).getEndpointUrl());
    }

    @Test
    public void testSelectEndpoint_preferConfiguredHost() {
        IotOpcUaClientSecurityFactory factory = new IotOpcUaClientSecurityFactory(new IotOpcUaClientConfig());
        // 服务端同时返回主机名、IP 两个地址：选择与配置一致的，不需要开启改写
        List<EndpointDescription> endpoints = Arrays.asList(
                newEndpoint("opc.tcp://plc-host:4840/server", SecurityPolicy.Basic256Sha256,
                        MessageSecurityMode.SignAndEncrypt, USERNAME_SECURE),
                newEndpoint(ENDPOINT, SecurityPolicy.Basic256Sha256, MessageSecurityMode.SignAndEncrypt, USERNAME_SECURE));

        EndpointDescription endpoint = factory.selectEndpoint(endpoints,
                newDevice("Basic256Sha256", "SignAndEncrypt", AUTH_USERNAME, "reader"));

        assertEquals(ENDPOINT, endpoint.getEndpointUrl());
    }

    // ========== initialize ==========

    @Test
    public void testInitialize_keystorePermissionAndCertificateChain() throws Exception {
        IotOpcUaClientConfig config = new IotOpcUaClientConfig();
        config.setPkiDir(pkiDir.toString());
        config.setKeystorePassword("p1");
        // 生成的 client.pfx 只有属主可读写
        IotOpcUaClientSecurityFactory factory = new IotOpcUaClientSecurityFactory(config);
        factory.initialize();
        factory.close();
        Path keystoreFile = pkiDir.resolve("own").resolve("client.pfx");
        if (Files.getFileStore(keystoreFile).supportsFileAttributeView(PosixFileAttributeView.class)) {
            assertEquals("rw-------", PosixFilePermissions.toString(Files.getPosixFilePermissions(keystoreFile)));
        }
        assertEquals(1, factory.getCertificateChain().length);

        // 运维放入带中间 CA 的证书链（叶子证书由中间 CA 签发）：完整保留
        KeyPair caKeyPair = SelfSignedCertificateGenerator.generateRsaKeyPair(2048);
        X509Certificate intermediate = new SelfSignedCertificateBuilder(caKeyPair)
                .setCommonName("intermediate").setApplicationUri("urn:test:ca").build();
        KeyPair keyPair = SelfSignedCertificateGenerator.generateRsaKeyPair(2048);
        X509Certificate leaf = newSignedCertificate(intermediate, caKeyPair, keyPair, "urn:test:leaf");
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, "p1".toCharArray());
        keyStore.setKeyEntry("yudao-opc-ua-client", keyPair.getPrivate(), "p1".toCharArray(),
                new X509Certificate[]{leaf, intermediate});
        try (OutputStream out = Files.newOutputStream(keystoreFile)) {
            keyStore.store(out, "p1".toCharArray());
        }
        IotOpcUaClientSecurityFactory reloaded = new IotOpcUaClientSecurityFactory(config);
        reloaded.initialize();
        reloaded.close();
        assertEquals(2, reloaded.getCertificateChain().length);
        assertEquals(leaf, reloaded.getCertificateChain()[0]);
        assertEquals(intermediate, reloaded.getCertificateChain()[1]);
        assertEquals("urn:test:leaf", reloaded.getApplicationUri());
    }

    @Test
    public void testInitialize_customAliasAndApplicationName() throws Exception {
        IotOpcUaClientConfig config = new IotOpcUaClientConfig();
        config.setPkiDir(pkiDir.toString());
        config.setKeystorePassword("p1");
        config.setKeystoreAlias("gateway-1");
        config.setApplicationName("Gateway 1");
        // 首次生成：证书放在配置的别名下，CN 为配置的应用名称；应用 URI 与证书 SAN 一致
        IotOpcUaClientSecurityFactory factory = new IotOpcUaClientSecurityFactory(config);
        factory.initialize();
        factory.close();
        Path keystoreFile = pkiDir.resolve("own").resolve("client.pfx");
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream in = Files.newInputStream(keystoreFile)) {
            keyStore.load(in, "p1".toCharArray());
        }
        assertTrue(keyStore.isKeyEntry("gateway-1"));
        X509Certificate cert = factory.getCertificateChain()[0];
        assertTrue(cert.getSubjectX500Principal().getName().contains("CN=Gateway 1"));
        assertEquals(CertificateUtil.getSanUri(cert).get(), factory.getApplicationUri());
        assertTrue(factory.getApplicationUri().startsWith("urn:yudao:iot:gateway:opc-ua-client:"));

        // 修改别名：不重新生成、不覆盖原有证书，启动失败
        byte[] keystoreBytes = Files.readAllBytes(keystoreFile);
        config.setKeystoreAlias("gateway-2");
        ServiceException exception = assertThrows(ServiceException.class,
                () -> new IotOpcUaClientSecurityFactory(config).initialize());
        assertTrue(exception.getMessage().contains("gateway-2"));
        assertArrayEquals(keystoreBytes, Files.readAllBytes(keystoreFile));

        // 修改应用名称：照常加载原有证书，证书、应用 URI 不变
        config.setKeystoreAlias("gateway-1");
        config.setApplicationName("Gateway 1 renamed");
        IotOpcUaClientSecurityFactory reloaded = new IotOpcUaClientSecurityFactory(config);
        reloaded.initialize();
        reloaded.close();
        assertEquals(cert, reloaded.getCertificateChain()[0]);
        assertEquals(factory.getApplicationUri(), reloaded.getApplicationUri());
    }

    @Test
    public void testInitialize() throws Exception {
        IotOpcUaClientConfig config = new IotOpcUaClientConfig();
        config.setPkiDir(pkiDir.toString());
        // 口令为空：启动失败
        assertThrows(ServiceException.class, () -> new IotOpcUaClientSecurityFactory(config).initialize());

        // 首次生成证书，再次启动读取同一证书
        config.setKeystorePassword("p1");
        IotOpcUaClientSecurityFactory factory = new IotOpcUaClientSecurityFactory(config);
        factory.initialize();
        factory.close();
        String applicationUri = factory.getApplicationUri();
        assertTrue(Files.exists(pkiDir.resolve("own").resolve("client.pfx")));
        assertTrue(Files.isDirectory(pkiDir.resolve("trusted").resolve("certs")));
        IotOpcUaClientSecurityFactory reloaded = new IotOpcUaClientSecurityFactory(config);
        reloaded.initialize();
        reloaded.close();
        assertEquals(applicationUri, reloaded.getApplicationUri());

        // 口令错误：启动失败，不覆盖原有证书
        config.setKeystorePassword("wrong");
        assertThrows(ServiceException.class, () -> new IotOpcUaClientSecurityFactory(config).initialize());
    }

    // ========== 工具方法 ==========

    private static X509Certificate newSignedCertificate(X509Certificate issuer, KeyPair issuerKeyPair, KeyPair keyPair,
                                                        String applicationUri) throws Exception {
        long now = System.currentTimeMillis();
        JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
                X500Name.getInstance(issuer.getSubjectX500Principal().getEncoded()), BigInteger.valueOf(now),
                new Date(now - 60_000), new Date(now + 86_400_000L), new X500Name("CN=leaf"), keyPair.getPublic());
        builder.addExtension(Extension.subjectAlternativeName, false,
                new GeneralNames(new GeneralName(GeneralName.uniformResourceIdentifier, applicationUri)));
        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(issuerKeyPair.getPrivate());
        return new JcaX509CertificateConverter().getCertificate(builder.build(signer));
    }

    /**
     * 配置凭据 reader：允许租户 1、端点 {@link #ENDPOINT}
     */
    private static IotOpcUaClientConfig newUsernameConfig() {
        IotOpcUaClientConfig config = new IotOpcUaClientConfig();
        IotOpcUaClientConfig.Credential credential = new IotOpcUaClientConfig.Credential();
        credential.setUsername("reader");
        credential.setPassword("secret");
        credential.setTenantIds(Collections.singletonList(1L));
        credential.setEndpoints(Collections.singletonList(ENDPOINT));
        config.getCredentials().put("reader", credential);
        return config;
    }

    private static IotOpcUaDeviceConfigRespDTO newDevice(String policy, String mode, Integer authType, String alias) {
        IotOpcUaDeviceConfigRespDTO device = new IotOpcUaDeviceConfigRespDTO();
        device.setDeviceId(1L);
        device.setTenantId(1L);
        device.setEndpointUrl(ENDPOINT);
        device.setSecurityPolicy(policy);
        device.setSecurityMode(mode);
        device.setAuthType(authType);
        device.setCredentialAlias(alias);
        return device;
    }

    private static EndpointDescription newEndpoint(String url, SecurityPolicy policy, MessageSecurityMode mode,
                                                   UserTokenPolicy... tokens) {
        return newEndpoint(url, policy, mode, Stack.TCP_UASC_UABINARY_TRANSPORT_URI, tokens);
    }

    private static EndpointDescription newEndpoint(String url, SecurityPolicy policy, MessageSecurityMode mode,
                                                   String transportProfileUri, UserTokenPolicy... tokens) {
        return new EndpointDescription(url, null, ByteString.NULL_VALUE, mode, policy.getUri(), tokens,
                transportProfileUri, ubyte(0));
    }

}
