package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.security;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.opcua.IotOpcUaAuthTypeEnum;
import cn.iocoder.yudao.module.iot.core.enums.opcua.IotOpcUaSecurityModeEnum;
import cn.iocoder.yudao.module.iot.core.enums.opcua.IotOpcUaSecurityPolicyEnum;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.IotOpcUaClientConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.OpcUaClientConfig;
import org.eclipse.milo.opcua.sdk.client.identity.IdentityProvider;
import org.eclipse.milo.opcua.sdk.client.identity.UsernameProvider;
import org.eclipse.milo.opcua.sdk.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.Stack;
import org.eclipse.milo.opcua.stack.core.security.CertificateValidator;
import org.eclipse.milo.opcua.stack.core.security.DefaultClientCertificateValidator;
import org.eclipse.milo.opcua.stack.core.security.FileBasedCertificateQuarantine;
import org.eclipse.milo.opcua.stack.core.security.FileBasedTrustListManager;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.UserTokenType;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.UserTokenPolicy;
import org.eclipse.milo.opcua.stack.core.util.CertificateUtil;
import org.eclipse.milo.opcua.stack.core.util.EndpointUtil;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateBuilder;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateGenerator;
import org.eclipse.milo.opcua.stack.transport.client.tcp.OpcTcpClientTransport;
import org.eclipse.milo.opcua.stack.transport.client.tcp.OpcTcpClientTransportConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.OPC_UA_ENDPOINT_REJECTED;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.OPC_UA_SECURITY_INIT_FAILED;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

/**
 * IoT OPC UA 客户端安全工厂：客户端证书、信任列表、设备配置校验、端点选择和创建 {@link OpcUaClient}
 * <p>
 * 1. 客户端证书：pkiDir/own/client.pfx（PKCS12，条目别名为 keystore-alias），不存在时生成自签名证书（文件权限仅属主可读写）；
 *    读取失败时协议启动失败，不回退匿名；运维放入的 CA 签发证书保留完整证书链；
 * 2. 信任列表：pkiDir/trusted、pkiDir/issuer，未信任的服务端证书放进 pkiDir/rejected，人工移入 trusted/certs 后生效；
 * 3. 端点选择严格匹配安全策略 URI、安全模式和 TCP 传输；主机、端口与配置不一致时默认拒绝；
 * 4. 已复用 Milo 的 FileBasedTrustListManager、DefaultClientCertificateValidator、SelfSignedCertificateBuilder、
 *    CertificateUtil、EndpointUtil、DiscoveryClient；keystore 的加载与 0600 权限、证书链校验、端点严格选择是安全约束，
 *    Hutool 没有等价实现，Milo 的 KeyStoreCertificateStore 面向服务端证书组（按证书类型命名别名），不能直接替换
 *
 * @author 芋道源码
 */
@Slf4j
public class IotOpcUaClientSecurityFactory {

    /**
     * keystore 类型：固定 PKCS12（JDK 9+ 默认类型，JKS 已不推荐）
     */
    private static final String KEYSTORE_TYPE = "PKCS12";
    /**
     * 产品 URI：OPC UA 规范中标识软件产品，与网关实例无关，固定不开放配置
     * <p>
     * 应用 URI 不单独配置：自签名证书为 PRODUCT_URI:主机名，运维放入的证书取 SAN 中的 URI（两者必须一致，否则服务端拒绝）
     */
    private static final String PRODUCT_URI = "urn:yudao:iot:gateway:opc-ua-client";
    /**
     * keystore 文件权限：只有属主可读写
     */
    private static final Set<PosixFilePermission> OWNER_ONLY_PERMISSIONS = PosixFilePermissions.fromString("rw-------");

    private final IotOpcUaClientConfig config;

    private FileBasedTrustListManager trustListManager;
    private CertificateValidator certificateValidator;
    private KeyPair keyPair;
    private X509Certificate certificate;
    /**
     * 客户端证书链：叶子证书在前，之后是中间 CA（自签名时只有叶子证书）
     */
    @Getter
    private X509Certificate[] certificateChain;
    /**
     * 应用 URI：与客户端证书 SAN 中的 URI 一致
     */
    @Getter
    private String applicationUri;

    public IotOpcUaClientSecurityFactory(IotOpcUaClientConfig config) {
        this.config = config;
    }

    // ========== 生命周期 ==========

    /**
     * 初始化：加载（或生成）客户端证书、信任列表；失败时抛出异常，协议启动失败
     */
    public void initialize() {
        if (StrUtil.isBlank(config.getKeystorePassword())) {
            throw exception(OPC_UA_SECURITY_INIT_FAILED, "keystore-password 不能为空");
        }
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        try {
            Path pkiDir = Paths.get(config.getPkiDir()).toAbsolutePath();
            Files.createDirectories(pkiDir);
            loadOrCreateKeyStore(pkiDir.resolve("own").resolve("client.pfx"));
            this.trustListManager = FileBasedTrustListManager.createAndInitialize(pkiDir);
            this.certificateValidator = new DefaultClientCertificateValidator(trustListManager,
                    FileBasedCertificateQuarantine.create(pkiDir.resolve("rejected")));
            log.info("[initialize][OPC UA 客户端证书已加载，PKI 目录: {}，应用 URI: {}]", pkiDir, applicationUri);
        } catch (Exception e) {
            close();
            throw exception(OPC_UA_SECURITY_INIT_FAILED, e.getMessage());
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
                log.warn("[close][关闭 OPC UA 信任列表失败]", e);
            }
            trustListManager = null;
        }
    }

    private void loadOrCreateKeyStore(Path file) throws Exception {
        char[] password = config.getKeystorePassword().toCharArray();
        String alias = config.getKeystoreAlias();
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
        // 1. 已存在：读取失败直接抛出（不覆盖、不回退匿名）
        if (Files.exists(file)) {
            try (InputStream in = Files.newInputStream(file)) {
                keyStore.load(in, password);
            }
            Key key = keyStore.getKey(alias, password);
            if (!(key instanceof PrivateKey)) {
                // 不重新生成：别名与 keystore 不一致时启动失败，由运维核对 keystore-alias
                throw new IllegalStateException("keystore 中没有别名为 " + alias + " 的私钥，请核对 keystore-alias");
            }
            PrivateKey privateKey = (PrivateKey) key;
            // 证书链：叶子证书在前，保留中间 CA，握手时一并发送给服务端
            Certificate[] chain = keyStore.getCertificateChain(alias);
            if (chain == null || chain.length == 0 || !(chain[0] instanceof X509Certificate)) {
                throw new IllegalStateException("keystore 中没有客户端证书: " + alias);
            }
            if (!Arrays.stream(chain).allMatch(X509Certificate.class::isInstance)) {
                throw new IllegalStateException("客户端证书链中有非 X.509 证书: " + alias);
            }
            X509Certificate[] certificates = Arrays.copyOf(chain, chain.length, X509Certificate[].class);
            X509Certificate cert = certificates[0];
            this.keyPair = new KeyPair(cert.getPublicKey(), privateKey);
            this.certificate = cert;
            this.certificateChain = certificates;
            this.applicationUri = CertificateUtil.getSanUri(cert)
                    .orElseThrow(() -> new IllegalStateException("客户端证书缺少 SAN URI"));
            warnIfPermissive(file);
            return;
        }

        // 2. 不存在：生成自签名证书
        String hostname = getLocalHostname();
        String uri = PRODUCT_URI + ":" + hostname;
        KeyPair newKeyPair = SelfSignedCertificateGenerator.generateRsaKeyPair(2048);
        X509Certificate cert = new SelfSignedCertificateBuilder(newKeyPair)
                .setCommonName(config.getApplicationName()).setOrganization("yudao")
                .setApplicationUri(uri).addDnsName(hostname)
                .build();
        keyStore.load(null, password);
        keyStore.setKeyEntry(alias, newKeyPair.getPrivate(), password, new X509Certificate[]{cert});
        Files.createDirectories(file.getParent());
        createOwnerOnlyFile(file);
        try (OutputStream out = Files.newOutputStream(file, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            keyStore.store(out, password);
        }
        this.keyPair = newKeyPair;
        this.certificate = cert;
        this.certificateChain = new X509Certificate[]{cert};
        this.applicationUri = uri;
        log.info("[loadOrCreateKeyStore][已生成 OPC UA 客户端证书: {}]", file);
    }

    /**
     * 创建只有属主可读写（0600）的空文件：keystore 中有客户端私钥；不支持 POSIX 权限的文件系统直接创建
     */
    private static void createOwnerOnlyFile(Path file) throws IOException {
        if (Files.getFileStore(file.getParent()).supportsFileAttributeView(PosixFileAttributeView.class)) {
            Files.createFile(file, PosixFilePermissions.asFileAttribute(OWNER_ONLY_PERMISSIONS));
        } else {
            Files.createFile(file);
        }
    }

    /**
     * 已有的 keystore 对属组、其它用户可读写时告警（不修改运维放入的文件）
     */
    private static void warnIfPermissive(Path file) {
        try {
            if (!Files.getFileStore(file).supportsFileAttributeView(PosixFileAttributeView.class)) {
                return;
            }
            Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(file);
            if (!OWNER_ONLY_PERMISSIONS.containsAll(permissions)) {
                log.warn("[warnIfPermissive][OPC UA 客户端 keystore {} 的权限为 {}，包含客户端私钥，建议改为 600]",
                        file, PosixFilePermissions.toString(permissions));
            }
        } catch (IOException e) {
            log.debug("[warnIfPermissive][读取 keystore 权限失败: {}]", file, e);
        }
    }

    private static String getLocalHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "localhost";
        }
    }

    // ========== 设备配置校验 ==========

    /**
     * 校验设备配置能否在本网关使用
     *
     * @param device 设备配置
     * @return 不能使用的原因；可以使用时返回 null
     */
    public String validateDevice(IotOpcUaDeviceConfigRespDTO device) {
        // 1. 端点地址
        if (parseEndpoint(device.getEndpointUrl()) == null) {
            return "端点地址不正确: " + device.getEndpointUrl();
        }
        // 2. 认证方式：只支持用户名认证，匿名一律拒绝（包括历史保存的匿名配置）
        IotOpcUaSecurityPolicyEnum policy = IotOpcUaSecurityPolicyEnum.of(device.getSecurityPolicy());
        IotOpcUaSecurityModeEnum mode = IotOpcUaSecurityModeEnum.of(device.getSecurityMode());
        IotOpcUaAuthTypeEnum authType = IotOpcUaAuthTypeEnum.of(device.getAuthType());
        if (policy == null || mode == null || authType == null) {
            return "安全策略、安全模式或认证方式不支持";
        }
        if (authType != IotOpcUaAuthTypeEnum.USERNAME) {
            return "OPC UA Client 只支持用户名认证，不支持" + authType.getName() + "认证";
        }
        // 3. 安全策略、模式：口令不能明文传输，必须是非 None 安全策略 + SignAndEncrypt
        if (policy == IotOpcUaSecurityPolicyEnum.NONE || mode != IotOpcUaSecurityModeEnum.SIGN_AND_ENCRYPT) {
            return "用户名认证必须使用非 None 安全策略和 SignAndEncrypt 安全模式";
        }
        // 4. 网关本地凭据：凭据的 tenant-ids、endpoints 都必须命中
        IotOpcUaClientConfig.Credential credential = config.getCredentials() != null
                ? config.getCredentials().get(device.getCredentialAlias()) : null;
        // 空白用户名、口令按未配置处理（与管理后台对凭据别名的非空校验一致）
        if (credential == null || StrUtil.isBlank(credential.getUsername())) {
            return "网关未配置凭据别名 " + device.getCredentialAlias();
        }
        if (StrUtil.isBlank(credential.getPassword())) {
            // 例如环境变量未设置时，${IOT_OPC_UA_XXX_PASSWORD:} 解析为空字符串
            return "凭据别名 " + device.getCredentialAlias() + " 的口令为空";
        }
        if (CollUtil.isEmpty(credential.getTenantIds()) || !credential.getTenantIds().contains(device.getTenantId())) {
            return "凭据别名 " + device.getCredentialAlias() + " 不允许租户 " + device.getTenantId() + " 使用";
        }
        if (CollUtil.isEmpty(credential.getEndpoints()) || !credential.getEndpoints().contains(device.getEndpointUrl())) {
            return "凭据别名 " + device.getCredentialAlias() + " 不允许端点 " + device.getEndpointUrl() + " 使用";
        }
        return null;
    }

    /**
     * 构建会话 key：租户、端点、安全策略、模式、认证方式、凭据别名、超时时间、重试间隔都相同的设备共用一个会话
     * <p>
     * 1. 带上租户：不同租户的设备不共用连接、worker 队列和订阅，互不影响；
     * 2. 带上超时时间：超时时间在创建客户端时生效，修改后设备换到新的会话（新建客户端），旧会话没有设备时关闭；
     * 3. 带上重试间隔：创建客户端、重建订阅失败的重试是会话级的，同一会话的设备重试间隔必须一致，行为才确定
     *
     * @param device 设备配置
     * @return 会话 key
     */
    public static String buildSessionKey(IotOpcUaDeviceConfigRespDTO device) {
        return String.join("|", String.valueOf(device.getTenantId()), device.getEndpointUrl(),
                device.getSecurityPolicy(), device.getSecurityMode(), String.valueOf(device.getAuthType()),
                StrUtil.nullToEmpty(device.getCredentialAlias()), String.valueOf(device.getTimeout()),
                String.valueOf(device.getRetryInterval()));
    }

    // ========== 创建客户端 ==========

    /**
     * 创建客户端：GetEndpoints 发现端点、严格选择端点，再创建 {@link OpcUaClient}（未连接）
     * <p>
     * 阻塞方法，在 worker 线程上执行
     *
     * @param device 设备配置（会话内首个设备）
     * @return 客户端
     * @throws Exception 发现端点失败、没有符合要求的端点
     */
    public OpcUaClient createClient(IotOpcUaDeviceConfigRespDTO device) throws Exception {
        int timeout = ObjUtil.defaultIfNull(device.getTimeout(), 5000);
        // 1. 发现端点（与 Milo 相同：失败时再尝试 <endpoint>/discovery）
        List<EndpointDescription> endpoints = getEndpoints(device.getEndpointUrl(), timeout);
        // 2. 选择端点
        EndpointDescription endpoint = selectEndpoint(endpoints, device);

        // 3. 创建客户端
        OpcUaClientConfig clientConfig = OpcUaClientConfig.builder()
                .setApplicationName(LocalizedText.english(config.getApplicationName()))
                .setApplicationUri(applicationUri)
                .setProductUri(PRODUCT_URI)
                .setKeyPair(keyPair)
                .setCertificate(certificate)
                .setCertificateChain(certificateChain)
                .setCertificateValidator(certificateValidator)
                .setIdentityProvider(buildIdentityProvider(device))
                .setRequestTimeout(uint(timeout))
                .setEndpoint(endpoint)
                .setDiscoveryEndpoints(endpoints)
                .build();
        OpcTcpClientTransportConfig transportConfig = OpcTcpClientTransportConfig.newBuilder()
                .setConnectTimeout(uint(timeout)).build();
        return new OpcUaClient(clientConfig, new OpcTcpClientTransport(transportConfig));
    }

    private static List<EndpointDescription> getEndpoints(String endpointUrl, int timeout) throws Exception {
        try {
            return DiscoveryClient.getEndpoints(endpointUrl, b -> b.setConnectTimeout(uint(timeout)))
                    .get(timeout * 2L, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            if (endpointUrl.endsWith("/discovery")) {
                throw e;
            }
            String discoveryUrl = StrUtil.removeSuffix(endpointUrl, "/") + "/discovery";
            return DiscoveryClient.getEndpoints(discoveryUrl, b -> b.setConnectTimeout(uint(timeout)))
                    .get(timeout * 2L, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 严格选择端点
     * <ol>
     *     <li>安全策略 URI、安全模式与配置完全一致，传输为 opc.tcp 二进制（未返回传输协议 URI 时，端点地址必须是 opc.tcp://）；</li>
     *     <li>端点提供 UserName 令牌，且令牌的生效安全策略不是 None（只支持用户名认证）；</li>
     *     <li>配置了非根路径时只选择路径一致的端点，没有则拒绝（凭据按端点授权，不能换到其它路径）；
     *         配置为根路径时优先选择根路径的端点，没有再从其它路径中选择；</li>
     *     <li>优先选择主机、端口与配置一致的端点（服务端可能同时返回主机名、IP 等多个地址）；
     *         都不一致时拒绝，除非开启 endpoint-host-rewrite（改写为配置的主机、端口）。</li>
     * </ol>
     *
     * @param endpoints 服务端返回的端点
     * @param device    设备配置
     * @return 选中的端点
     */
    public EndpointDescription selectEndpoint(List<EndpointDescription> endpoints, IotOpcUaDeviceConfigRespDTO device) {
        IotOpcUaSecurityPolicyEnum policy = IotOpcUaSecurityPolicyEnum.of(device.getSecurityPolicy());
        MessageSecurityMode mode = MessageSecurityMode.valueOf(IotOpcUaSecurityModeEnum.of(device.getSecurityMode()).getMode());
        URI configured = parseEndpoint(device.getEndpointUrl());
        if (configured == null) {
            throw exception(OPC_UA_ENDPOINT_REJECTED, "端点地址不正确: " + device.getEndpointUrl());
        }
        String configuredPath = normalizePath(configured.getPath());
        int configuredPort = configured.getPort() > 0 ? configured.getPort() : 4840;

        // 1.1 安全策略、模式、传输一致的端点
        List<EndpointDescription> candidates = endpoints.stream()
                .filter(e -> policy.getUri().equals(e.getSecurityPolicyUri()) && e.getSecurityMode() == mode)
                .filter(IotOpcUaClientSecurityFactory::isTcpBinaryTransport)
                .collect(Collectors.toList());
        // 1.2 路径：非根路径严格一致；根路径优先一致，没有时不限制
        List<EndpointDescription> samePath = candidates.stream()
                .filter(e -> configuredPath.equals(normalizePath(EndpointUtil.getPath(e.getEndpointUrl()))))
                .collect(Collectors.toList());
        if (!samePath.isEmpty() || !"/".equals(configuredPath)) {
            candidates = samePath;
        }
        // 1.3 身份令牌：安全的 UserName 令牌
        candidates = candidates.stream()
                .filter(e -> selectUsernameTokenPolicy(e).isPresent())
                .collect(Collectors.toList());
        if (candidates.isEmpty()) {
            throw exception(OPC_UA_ENDPOINT_REJECTED, String.format("服务端没有 %s / %s / %s 的端点",
                    device.getSecurityPolicy(), device.getSecurityMode(), device.getAuthType()));
        }

        // 2. 主机、端口：优先选择与配置一致的端点；都不一致时拒绝或改写
        Optional<EndpointDescription> sameHost = candidates.stream()
                .filter(e -> isSameHostAndPort(e.getEndpointUrl(), configured.getHost(), configuredPort))
                .findFirst();
        if (sameHost.isPresent()) {
            return sameHost.get();
        }
        EndpointDescription endpoint = candidates.get(0);
        if (!Boolean.TRUE.equals(config.getEndpointHostRewrite())) {
            throw exception(OPC_UA_ENDPOINT_REJECTED, String.format("服务端返回的端点 %s 与配置的主机、端口不一致"
                    + "（可开启 endpoint-host-rewrite）", endpoint.getEndpointUrl()));
        }
        return EndpointUtil.updateUrl(endpoint, configured.getHost(), configuredPort);
    }

    /**
     * 是否为 opc.tcp 二进制传输：传输协议 URI 为 UA-TCP UA-SC UA-Binary；
     * 部分老旧服务端不返回传输协议 URI，此时按端点地址兼容，必须是 opc.tcp://（排除 https、opc.wss 等其它传输）
     */
    private static boolean isTcpBinaryTransport(EndpointDescription endpoint) {
        if (StrUtil.isNotEmpty(endpoint.getTransportProfileUri())) {
            return Stack.TCP_UASC_UABINARY_TRANSPORT_URI.equals(endpoint.getTransportProfileUri());
        }
        return StrUtil.startWithIgnoreCase(endpoint.getEndpointUrl(), "opc.tcp://");
    }

    private static boolean isSameHostAndPort(String endpointUrl, String host, int port) {
        return StrUtil.equalsIgnoreCase(EndpointUtil.getHost(endpointUrl), host)
                && EndpointUtil.getPort(endpointUrl) == port;
    }

    /**
     * 规范化路径：空路径视为根路径 /，去掉末尾的 /
     */
    private static String normalizePath(String path) {
        if (StrUtil.isEmpty(path) || "/".equals(path)) {
            return "/";
        }
        return StrUtil.removeSuffix(path, "/");
    }

    /**
     * 选择 UserName 令牌策略：令牌的生效安全策略（令牌未指定时取端点的）必须是已知的非 None 策略，口令不会明文传输
     */
    private static Optional<UserTokenPolicy> selectUsernameTokenPolicy(EndpointDescription endpoint) {
        return chooseUsernameTokenPolicy(getTokenPolicies(endpoint), endpoint.getSecurityPolicyUri());
    }

    /**
     * 从令牌策略中选择 UserName 令牌：先按令牌类型过滤，再要求生效安全策略不是 None
     * <p>
     * 端点选择、握手时的令牌选择共用，避免排在前面的安全 Anonymous 等其它类型令牌被选中，导致用户名认证握手失败
     *
     * @param policies          端点的令牌策略
     * @param endpointPolicyUri 端点的安全策略 URI（令牌未指定安全策略时使用）
     * @return UserName 令牌策略
     */
    static Optional<UserTokenPolicy> chooseUsernameTokenPolicy(Stream<UserTokenPolicy> policies,
                                                               String endpointPolicyUri) {
        return policies.filter(Objects::nonNull)
                .filter(p -> p.getTokenType() == UserTokenType.UserName)
                .filter(p -> isSecureTokenPolicy(p, endpointPolicyUri))
                .findFirst();
    }

    private static boolean isSecureTokenPolicy(UserTokenPolicy tokenPolicy, String endpointPolicyUri) {
        String uri = StrUtil.isEmpty(tokenPolicy.getSecurityPolicyUri()) ? endpointPolicyUri : tokenPolicy.getSecurityPolicyUri();
        return SecurityPolicy.fromUriSafe(uri).filter(s -> s != SecurityPolicy.None).isPresent();
    }

    private static Stream<UserTokenPolicy> getTokenPolicies(EndpointDescription endpoint) {
        UserTokenPolicy[] policies = endpoint.getUserIdentityTokens();
        return policies != null ? Arrays.stream(policies).filter(Objects::nonNull) : Stream.empty();
    }

    private IdentityProvider buildIdentityProvider(IotOpcUaDeviceConfigRespDTO device) {
        // 只支持用户名认证：validateDevice 已拒绝匿名，这里兜底，不回退匿名
        if (!IotOpcUaAuthTypeEnum.USERNAME.getType().equals(device.getAuthType())) {
            throw new IllegalStateException("OPC UA Client 只支持用户名认证");
        }
        IotOpcUaClientConfig.Credential credential = config.getCredentials().get(device.getCredentialAlias());
        String password = credential.getPassword();
        String endpointPolicyUri = IotOpcUaSecurityPolicyEnum.of(device.getSecurityPolicy()).getUri();
        return new UsernameProvider(credential.getUsername(), () -> password.getBytes(StandardCharsets.UTF_8),
                certificateValidator, policies -> chooseUsernameTokenPolicy(policies.stream(), endpointPolicyUri)
                        .orElseThrow(() -> new IllegalStateException("端点没有安全的 UserName 令牌策略")));
    }

    /**
     * 解析端点地址：必须是 opc.tcp，有主机，端口（可省略，默认 4840）在 1 ~ 65535，不带用户信息、查询参数和片段
     *
     * @param endpointUrl 端点地址
     * @return URI；不合法时返回 null
     */
    public static URI parseEndpoint(String endpointUrl) {
        if (StrUtil.isBlank(endpointUrl)) {
            return null;
        }
        try {
            URI uri = new URI(endpointUrl);
            if (!"opc.tcp".equalsIgnoreCase(uri.getScheme()) || StrUtil.isEmpty(uri.getHost())
                    || uri.getUserInfo() != null || uri.getPort() == 0 || uri.getPort() > 65535
                    || uri.getRawQuery() != null || uri.getRawFragment() != null) {
                return null;
            }
            return uri;
        } catch (Exception e) {
            return null;
        }
    }

}
