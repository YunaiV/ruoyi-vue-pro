package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.server.security;

import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaServerConfigRespDTO;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.server.IotOpcUaServerConfig;
import org.eclipse.milo.opcua.sdk.server.SecurityConfiguration;
import org.eclipse.milo.opcua.sdk.server.Session;
import org.eclipse.milo.opcua.sdk.server.identity.DefaultUsernameIdentity;
import org.eclipse.milo.opcua.sdk.server.identity.Identity;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link IotOpcUaServerIdentityValidator} 的单元测试：None 默认禁止、用户名口令、租户、配置刷新竞态、
 * 会话名额（并发、重新激活、凭据别名切换、激活失败）
 *
 * @author 芋道源码
 */
public class IotOpcUaServerIdentityValidatorTest {

    private IotOpcUaServerConfig config;
    private final Map<String, IotOpcUaServerConfigRespDTO> configs = new HashMap<>();
    private IotOpcUaServerIdentityValidator validator;
    /**
     * 存活的会话：模拟 Milo SessionManager 的 created + active 会话
     */
    private final List<Session> liveSessions = new CopyOnWriteArrayList<>();
    private final AtomicLong now = new AtomicLong(1_000_000L);

    @BeforeEach
    public void setUp() {
        config = new IotOpcUaServerConfig();
        config.getCredentials().put("scada-a", newCredential("scada", "secret", 1L));
        config.getCredentials().put("scada-b", newCredential("other", "secret-b", 2L));
        configs.put("scada-a", newConfig(1L, 1L, "scada-a"));
        configs.put("scada-b", newConfig(2L, 3L, "scada-b")); // 配置的租户 3 不在凭据允许的租户中
        validator = new IotOpcUaServerIdentityValidator(config, configs::get, () -> liveSessions);
        validator.setClock(now::get);
    }

    @Test
    public void testDefaults() {
        // 断言：默认禁止 None，默认没有凭据
        IotOpcUaServerConfig defaults = new IotOpcUaServerConfig();
        assertFalse(defaults.getSecurityNoneAllowed());
        assertTrue(defaults.getCredentials().isEmpty());
    }

    @Test
    public void testAuthenticate_success() {
        assertEquals("scada-a", validator.authenticate(SecurityPolicy.Basic256Sha256, "scada", "secret")
                .getCredentialAlias());
    }

    @Test
    public void testAuthenticate_noneDeniedByDefault() {
        // 断言：默认在 None 安全通道上拒绝
        assertNull(validator.authenticate(SecurityPolicy.None, "scada", "secret"));
        // 断言：显式开启后允许（仍需用户名口令）
        config.setSecurityNoneAllowed(true);
        assertEquals("scada-a", validator.authenticate(SecurityPolicy.None, "scada", "secret").getCredentialAlias());
        assertNull(validator.authenticate(SecurityPolicy.None, "scada", "wrong"));
    }

    @Test
    public void testAuthenticate_wrongPassword() {
        assertNull(validator.authenticate(SecurityPolicy.Basic256Sha256, "scada", "wrong"));
        assertNull(validator.authenticate(SecurityPolicy.Basic256Sha256, "nobody", "secret"));
        assertNull(validator.authenticate(SecurityPolicy.Basic256Sha256, "scada", null));
    }

    @Test
    public void testAuthenticate_blankPasswordCredentialIgnored() {
        // 准备参数：口令为空（例如环境变量未设置）
        config.getCredentials().put("blank", newCredential("blank", "", 1L));
        configs.put("blank", newConfig(9L, 1L, "blank"));
        // 断言：空口令的凭据不能登录
        assertNull(validator.authenticate(SecurityPolicy.Basic256Sha256, "blank", ""));
    }

    @Test
    public void testAuthenticate_tenantDenied() {
        // 断言：配置的租户不在凭据允许的租户中
        assertNull(validator.authenticate(SecurityPolicy.Basic256Sha256, "other", "secret-b"));
        // 断言：凭据没有配置允许的租户时拒绝（不因网关未绑定租户而放行）
        config.getCredentials().get("scada-a").setTenantIds(Collections.<Long>emptyList());
        assertNull(validator.authenticate(SecurityPolicy.Basic256Sha256, "scada", "secret"));
    }

    @Test
    public void testAuthenticate_noConfig() {
        // 断言：凭据别名没有启用的配置
        configs.remove("scada-a");
        assertNull(validator.authenticate(SecurityPolicy.Basic256Sha256, "scada", "secret"));
    }

    @Test
    public void testAuthenticateUsernamePassword_configRemovedDuringActivate() {
        // 准备参数：第一次查找返回配置，之后配置被刷新掉（返回 null）
        AtomicInteger lookups = new AtomicInteger();
        IotOpcUaServerIdentityValidator racyValidator = new IotOpcUaServerIdentityValidator(config,
                alias -> lookups.getAndIncrement() == 0 ? configs.get(alias) : null, Collections::emptyList);

        // 调用：不会因为二次查找返回 null 而 NPE
        Identity.UsernameIdentity identity = racyValidator.authenticateUsernamePassword(
                mockSession(SecurityPolicy.Basic256Sha256, "s-1"), "scada", "secret");
        // 断言：只查找一次，身份上记录凭据别名
        assertNotNull(identity);
        assertEquals(1, lookups.get());
        assertEquals("scada-a", identity.getUserData());
    }

    @Test
    public void testAuthenticateUsernamePassword_failedReactivationKeepsSlot() {
        // 准备参数：会话已以 scada-a 激活
        Session session = newLiveSession("s-1");
        activate(session, "scada-a");
        // 调用：同一会话用错误口令重新激活
        assertNull(validator.authenticateUsernamePassword(session, "scada", "wrong"));
        // 断言：名额仍记在 scada-a，没有新的预留
        assertEquals(1, validator.getSessionCount("scada-a"));
        assertEquals(0, validator.getSessionCount("scada-b"));
    }

    @Test
    public void testTryAcquireSession_concurrentConverges() throws Exception {
        int threads = 16;
        List<Session> sessions = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            sessions.add(newLiveSession("s-" + i));
        }
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            // 调用：16 个激活中的会话同时占用 maxSessions = 2 的名额
            CountDownLatch start = new CountDownLatch(1);
            List<Future<Boolean>> futures = new ArrayList<>();
            for (Session session : sessions) {
                futures.add(executor.submit(() -> {
                    start.await();
                    return validator.tryAcquireSession("scada-a", session, 2);
                }));
            }
            start.countDown();
            int acquired = 0;
            for (Future<Boolean> future : futures) {
                if (future.get(5, TimeUnit.SECONDS)) {
                    acquired++;
                }
            }
            // 断言：恰好 2 个成功
            assertEquals(2, acquired);
            assertEquals(2, validator.getSessionCount("scada-a"));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void testTryAcquireSession_reactivateSameAlias() {
        Session s1 = newLiveSession("s-1");
        Session s2 = newLiveSession("s-2");
        Session s3 = newLiveSession("s-3");
        assertTrue(validator.tryAcquireSession("scada-a", s1, 2));
        activate(s1, "scada-a");
        assertTrue(validator.tryAcquireSession("scada-a", s2, 2));
        activate(s2, "scada-a");
        // 断言：同一会话重新激活（换安全通道等）不重复占用，名额已满时仍可重新激活
        assertTrue(validator.tryAcquireSession("scada-a", s1, 2));
        assertEquals(2, validator.getSessionCount("scada-a"));
        // 断言：名额已满时拒绝新会话
        assertFalse(validator.tryAcquireSession("scada-a", s3, 2));
    }

    @Test
    public void testTryAcquireSession_releaseOnClose() {
        Session s1 = newLiveSession("s-1");
        Session s2 = newLiveSession("s-2");
        assertTrue(validator.tryAcquireSession("scada-a", s1, 1));
        activate(s1, "scada-a");
        assertFalse(validator.tryAcquireSession("scada-a", s2, 1));
        // 调用：会话关闭（Milo 先移出会话，再通知监听器）；断言：名额释放后可以占用
        liveSessions.remove(s1);
        validator.onSessionClosed(s1);
        assertTrue(validator.tryAcquireSession("scada-a", s2, 1));
        assertEquals(1, validator.getSessionCount("scada-a"));
    }

    @Test
    public void testTryAcquireSession_aliasSwitchSucceeded() {
        // 准备参数：会话已以 scada-a 激活
        Session session = newLiveSession("s-1");
        activate(session, "scada-a");
        // 调用：同一安全通道上切换为 scada-b，Milo 随后设置新身份
        assertTrue(validator.tryAcquireSession("scada-b", session, 2));
        activate(session, "scada-b");
        // 断言：名额跟随生效的身份
        assertEquals(0, validator.getSessionCount("scada-a"));
        assertEquals(1, validator.getSessionCount("scada-b"));
    }

    @Test
    public void testTryAcquireSession_aliasSwitchFailed() {
        // 准备参数：会话已以 scada-a 激活
        Session session = newLiveSession("s-1");
        Session other = newLiveSession("s-2");
        activate(session, "scada-a");
        // 调用：换安全通道时切换为 scada-b，校验通过但 Milo 因身份不一致拒绝激活（身份仍是 scada-a）
        assertTrue(validator.tryAcquireSession("scada-b", session, 1));
        // 断言：scada-a 的名额没有被移走；scada-b 只在激活窗口内被预留
        assertEquals(1, validator.getSessionCount("scada-a"));
        assertEquals(1, validator.getSessionCount("scada-b"));
        assertFalse(validator.tryAcquireSession("scada-b", other, 1));
        // 调用：预留到期；断言：scada-b 的名额释放，scada-a 不变
        now.addAndGet(IotOpcUaServerIdentityValidator.RESERVATION_TIMEOUT_MILLIS);
        assertEquals(0, validator.getSessionCount("scada-b"));
        assertEquals(1, validator.getSessionCount("scada-a"));
        assertTrue(validator.tryAcquireSession("scada-b", other, 1));
    }

    @Test
    public void testTryAcquireSession_reactivateOriginalAliasAfterFailedSwitch() {
        Session session = newLiveSession("s-1");
        activate(session, "scada-a");
        // 调用：切换 scada-b 未生效，随后以 scada-a 重新激活
        assertTrue(validator.tryAcquireSession("scada-b", session, 1));
        assertTrue(validator.tryAcquireSession("scada-a", session, 1));
        // 断言：未生效的 scada-b 预留立即失效
        assertEquals(0, validator.getSessionCount("scada-b"));
        assertEquals(1, validator.getSessionCount("scada-a"));
    }

    @Test
    public void testTryAcquireSession_aliasSwitchDeniedWhenFull() {
        Session s1 = newLiveSession("s-1");
        Session s2 = newLiveSession("s-2");
        activate(s1, "scada-a");
        activate(s2, "scada-b");
        // 调用：s-1 切换到已满的 scada-b；断言：拒绝，s-1 仍占用 scada-a
        assertFalse(validator.tryAcquireSession("scada-b", s1, 1));
        assertEquals(1, validator.getSessionCount("scada-a"));
        assertEquals(1, validator.getSessionCount("scada-b"));
    }

    @Test
    public void testTryAcquireSession_activationFailed() {
        Session s1 = newLiveSession("s-1");
        Session s2 = newLiveSession("s-2");
        assertTrue(validator.tryAcquireSession("scada-a", s1, 1));
        // 断言：激活中的预留占用名额
        assertFalse(validator.tryAcquireSession("scada-a", s2, 1));
        // 调用：s-1 激活失败、会话已不存在（未收到关闭通知）；断言：预留失效，s-2 可以占用
        liveSessions.remove(s1);
        assertTrue(validator.tryAcquireSession("scada-a", s2, 1));
        assertEquals(1, validator.getSessionCount("scada-a"));
    }

    private Session newLiveSession(String sessionId) {
        Session session = mockSession(SecurityPolicy.Basic256Sha256, sessionId);
        liveSessions.add(session);
        return session;
    }

    /**
     * 模拟 Milo 激活成功后设置会话身份
     */
    private static void activate(Session session, String alias) {
        DefaultUsernameIdentity identity = new DefaultUsernameIdentity(alias);
        identity.setUserData(alias);
        when(session.getIdentity()).thenReturn(identity);
    }

    private static Session mockSession(SecurityPolicy policy, String sessionId) {
        SecurityConfiguration securityConfiguration = mock(SecurityConfiguration.class);
        when(securityConfiguration.getSecurityPolicy()).thenReturn(policy);
        Session session = mock(Session.class);
        when(session.getSecurityConfiguration()).thenReturn(securityConfiguration);
        when(session.getSessionId()).thenReturn(new NodeId(1, sessionId));
        return session;
    }

    private static IotOpcUaServerConfig.Credential newCredential(String username, String password, Long tenantId) {
        IotOpcUaServerConfig.Credential credential = new IotOpcUaServerConfig.Credential();
        credential.setUsername(username);
        credential.setPassword(password);
        credential.setTenantIds(Collections.singletonList(tenantId));
        return credential;
    }

    private static IotOpcUaServerConfigRespDTO newConfig(Long id, Long tenantId, String alias) {
        IotOpcUaServerConfigRespDTO config = new IotOpcUaServerConfigRespDTO();
        config.setId(id);
        config.setTenantId(tenantId);
        config.setName("config-" + id);
        config.setCredentialAlias(alias);
        config.setMaxSessions(2);
        return config;
    }

}
