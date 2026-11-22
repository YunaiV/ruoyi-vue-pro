package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.server.security;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.server.IotOpcUaServerConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.milo.opcua.stack.core.security.CertificateQuarantine;
import org.eclipse.milo.opcua.stack.core.security.DefaultApplicationGroup;
import org.eclipse.milo.opcua.stack.core.security.DefaultCertificateManager;
import org.eclipse.milo.opcua.stack.core.security.DefaultServerCertificateValidator;
import org.eclipse.milo.opcua.stack.core.security.FileBasedCertificateQuarantine;
import org.eclipse.milo.opcua.stack.core.security.FileBasedTrustListManager;
import org.eclipse.milo.opcua.stack.core.security.MemoryCertificateStore;
import org.eclipse.milo.opcua.stack.core.security.RsaSha256CertificateFactory;
import org.eclipse.milo.opcua.stack.core.util.CertificateUtil;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateBuilder;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateGenerator;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.OPC_UA_SERVER_SECURITY_INIT_FAILED;

/**
 * IoT OPC UA Server 证书管理：服务端证书（keystore）、客户端证书信任列表
 * <p>
 * 1. 服务端证书：pkiDir/own/server.pfx（PKCS12），不存在时生成自签名证书（文件权限仅属主可读写）；读取失败时协议启动失败；
 * 2. 信任列表：pkiDir/trusted/certs、pkiDir/issuer/certs（CRL 在同级 crl 目录）；未信任的客户端证书由 Milo 放进 pkiDir/rejected，人工移入 trusted/certs 后生效
 *
 * @author 芋道源码
 */
@Slf4j
public class IotOpcUaServerCertificateManager {

    private static final String KEYSTORE_TYPE = "PKCS12";
    /**
     * 产品 URI：固定不开放配置
     */
    public static final String PRODUCT_URI = "urn:yudao:iot:gateway:opc-ua-server";

    private final IotOpcUaServerConfig config;

    @Getter
    private DefaultCertificateManager certificateManager;
    @Getter
    private FileBasedTrustListManager trustListManager;
    @Getter
    private X509Certificate certificate;
    private X509Certificate[] certificateChain;
    /**
     * 应用 URI：与服务端证书 SAN 中的 URI 一致
     */
    @Getter
    private String applicationUri;

    public IotOpcUaServerCertificateManager(IotOpcUaServerConfig config) {
        this.config = config;
    }

    /**
     * 初始化：加载（或生成）服务端证书、信任列表；失败时抛出异常，协议启动失败
     */
    public void initialize() {
        if (StrUtil.isBlank(config.getKeystorePassword())) {
            throw exception(OPC_UA_SERVER_SECURITY_INIT_FAILED, "keystore-password 不能为空");
        }
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        try {
            Path pkiDir = Paths.get(config.getPkiDir()).toAbsolutePath();
            Files.createDirectories(pkiDir);
            KeyPair keyPair = loadOrCreateKeyStore(pkiDir.resolve("own").resolve("server.pfx"));
            this.trustListManager = FileBasedTrustListManager.createAndInitialize(pkiDir);
            this.certificateManager = buildCertificateManager(pkiDir, keyPair);
            log.info("[initialize][OPC UA Server 证书已加载，PKI 目录: {}，应用 URI: {}]", pkiDir, applicationUri);
        } catch (Exception e) {
            close();
            throw exception(OPC_UA_SERVER_SECURITY_INIT_FAILED, e.getMessage());
        }
    }

    /**
     * 关闭：停止信任列表的目录监听
     */
    public void close() {
        if (trustListManager != null) {
            try {
                trustListManager.close();
            } catch (Exception e) {
                log.warn("[close][关闭 OPC UA Server 信任列表失败]", e);
            }
            trustListManager = null;
        }
    }

    private DefaultCertificateManager buildCertificateManager(Path pkiDir, KeyPair keyPair) throws Exception {
        CertificateQuarantine quarantine = FileBasedCertificateQuarantine.create(pkiDir.resolve("rejected"));
        X509Certificate[] chain = certificateChain;
        // 证书工厂只返回已加载的服务端证书，不在运行期重新生成
        RsaSha256CertificateFactory certificateFactory = new RsaSha256CertificateFactory() {

            @Override
            protected KeyPair createRsaSha256KeyPair() {
                return keyPair;
            }

            @Override
            protected X509Certificate[] createRsaSha256CertificateChain(KeyPair ignored) {
                return chain;
            }

        };
        DefaultApplicationGroup group = new DefaultApplicationGroup(trustListManager, new MemoryCertificateStore(),
                certificateFactory, new DefaultServerCertificateValidator(trustListManager, quarantine));
        group.initialize();
        return new DefaultCertificateManager(quarantine, group);
    }

    private KeyPair loadOrCreateKeyStore(Path file) throws Exception {
        char[] password = config.getKeystorePassword().toCharArray();
        String alias = config.getKeystoreAlias();
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
        // 1. 已存在：读取失败直接抛出（不覆盖）
        if (Files.exists(file)) {
            try (InputStream in = Files.newInputStream(file)) {
                keyStore.load(in, password);
            }
            Key key = keyStore.getKey(alias, password);
            if (!(key instanceof PrivateKey)) {
                throw new IllegalStateException("keystore 中没有别名为 " + alias + " 的私钥，请核对 keystore-alias");
            }
            Certificate[] chain = keyStore.getCertificateChain(alias);
            if (chain == null || chain.length == 0 || !Arrays.stream(chain).allMatch(X509Certificate.class::isInstance)) {
                throw new IllegalStateException("keystore 中没有服务端证书: " + alias);
            }
            X509Certificate[] certificates = Arrays.copyOf(chain, chain.length, X509Certificate[].class);
            this.certificate = certificates[0];
            this.certificateChain = certificates;
            this.applicationUri = CertificateUtil.getSanUri(certificate)
                    .orElseThrow(() -> new IllegalStateException("服务端证书缺少 SAN URI"));
            return new KeyPair(certificate.getPublicKey(), (PrivateKey) key);
        }

        // 2. 不存在：生成自签名证书（SAN 包含端点主机名，客户端校验主机名时需要：IP 字面量为 iPAddress，域名为 dNSName）
        String uri = PRODUCT_URI + ":" + config.getHostname();
        KeyPair keyPair = SelfSignedCertificateGenerator.generateRsaKeyPair(2048);
        SelfSignedCertificateBuilder builder = new SelfSignedCertificateBuilder(keyPair).setCountryCode("CN")
                .setCommonName(config.getApplicationName()).setOrganization("yudao")
                .setApplicationUri(uri);
        String ipAddress = toIpLiteral(config.getHostname());
        if (ipAddress != null) {
            builder.addIpAddress(ipAddress);
        } else {
            builder.addDnsName(config.getHostname());
        }
        X509Certificate cert = builder.build();
        keyStore.load(null, password);
        keyStore.setKeyEntry(alias, keyPair.getPrivate(), password, new X509Certificate[]{cert});
        Files.createDirectories(file.getParent());
        if (Files.getFileStore(file.getParent()).supportsFileAttributeView(PosixFileAttributeView.class)) {
            Files.createFile(file, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")));
        } else {
            Files.createFile(file);
        }
        try (OutputStream out = Files.newOutputStream(file)) {
            keyStore.store(out, password);
        }
        this.certificate = cert;
        this.certificateChain = new X509Certificate[]{cert};
        this.applicationUri = uri;
        log.info("[loadOrCreateKeyStore][已生成 OPC UA Server 证书: {}]", file);
        return keyPair;
    }

    /**
     * 主机名是否为 IP 字面量：只按字面判断，不做 DNS 解析
     *
     * @param hostname 主机名，IPv6 可以带方括号，例如 [::1]
     * @return IP 地址（去掉方括号）；不是 IP 字面量时返回 null
     */
    static String toIpLiteral(String hostname) {
        String host = StrUtil.trim(hostname);
        if (StrUtil.isWrap(host, '[', ']')) {
            host = StrUtil.unWrap(host, '[', ']');
        }
        return Validator.isIpv4(host) || Validator.isIpv6(host) ? host : null;
    }

}
