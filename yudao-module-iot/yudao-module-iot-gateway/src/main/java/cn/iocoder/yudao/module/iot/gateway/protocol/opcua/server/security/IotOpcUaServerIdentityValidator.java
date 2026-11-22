package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.server.security;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaServerConfigRespDTO;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.server.IotOpcUaServerConfig;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.milo.opcua.sdk.server.Session;
import org.eclipse.milo.opcua.sdk.server.SessionListener;
import org.eclipse.milo.opcua.sdk.server.identity.AbstractUsernameIdentityValidator;
import org.eclipse.milo.opcua.sdk.server.identity.DefaultUsernameIdentity;
import org.eclipse.milo.opcua.sdk.server.identity.Identity;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * IoT OPC UA Server 身份校验：只允许用户名登录，会话身份的 userData 为凭据别名
 * <ol>
 *     <li>匿名一律拒绝（只声明支持 UserName 令牌）；</li>
 *     <li>None 安全通道上激活会话：未开启 security-none-allowed 时拒绝；</li>
 *     <li>用户名、口令与网关本地凭据一致；凭据别名有启用的 OPC UA Server 配置，配置的租户在凭据的 tenant-ids 中；</li>
 *     <li>同一凭据别名的会话数不超过配置的 maxSessions：已激活会话按其当前身份上的凭据别名计数，
 *         激活中（校验通过、Milo 尚未设置身份）的会话按预留计数，在锁内原子判断，并发登录收敛到上限以内；
 *         会话重新激活、切换凭据别名或激活失败时，名额始终跟随会话实际生效的身份，不会记到未生效的新别名。</li>
 * </ol>
 *
 * @author 芋道源码
 */
@Slf4j
public class IotOpcUaServerIdentityValidator extends AbstractUsernameIdentityValidator implements SessionListener {

    /**
     * 激活中预留的有效期（毫秒）：覆盖校验通过到 Milo 设置会话身份之间的窗口；激活失败时预留到期失效
     */
    static final long RESERVATION_TIMEOUT_MILLIS = 10_000L;

    private final IotOpcUaServerConfig config;
    /**
     * 按凭据别名查找当前启用的配置
     */
    private final Function<String, IotOpcUaServerConfigRespDTO> configLookup;
    /**
     * 当前存活（已创建或已激活）的会话
     */
    private final Supplier<Collection<Session>> liveSessionsSupplier;
    /**
     * 激活中的名额预留：sessionId -> 预留；读写都在 this 锁内
     * <p>
     * 会话身份已是预留的别名（已按身份计数）、会话不再存活或预留到期后失效
     */
    private final Map<NodeId, Reservation> reservations = new HashMap<>();
    /**
     * 当前时间（毫秒）：测试可替换
     */
    private volatile LongSupplier clock = System::currentTimeMillis;

    public IotOpcUaServerIdentityValidator(IotOpcUaServerConfig config,
                                           Function<String, IotOpcUaServerConfigRespDTO> configLookup,
                                           Supplier<Collection<Session>> liveSessionsSupplier) {
        this.config = config;
        this.configLookup = configLookup;
        this.liveSessionsSupplier = liveSessionsSupplier;
    }

    void setClock(LongSupplier clock) {
        this.clock = clock;
    }

    /**
     * 获取会话的凭据别名
     *
     * @param session 会话
     * @return 凭据别名；未通过本校验器激活时返回 null
     */
    public static String getCredentialAlias(Session session) {
        Identity identity = session != null ? session.getIdentity() : null;
        Object userData = identity != null ? identity.getUserData() : null;
        return userData instanceof String ? (String) userData : null;
    }

    @Override
    protected Identity.UsernameIdentity authenticateUsernamePassword(Session session, String username, String password) {
        // 1. 校验口令和配置：只查一次配置，后续都使用这次结果，避免配置刷新导致前后不一致
        SecurityPolicy policy = session.getSecurityConfiguration().getSecurityPolicy();
        IotOpcUaServerConfigRespDTO serverConfig = authenticate(policy, username, password);
        if (serverConfig == null) {
            return null;
        }
        // 2. 占用会话名额
        String alias = serverConfig.getCredentialAlias();
        int maxSessions = serverConfig.getMaxSessions() != null && serverConfig.getMaxSessions() > 0
                ? serverConfig.getMaxSessions() : 1;
        if (!tryAcquireSession(alias, session, maxSessions)) {
            log.warn("[authenticateUsernamePassword][凭据别名 {} 的会话数已达上限 {}]", alias, maxSessions);
            return null;
        }
        // 3. 会话身份：userData 记录凭据别名，用于租户隔离
        DefaultUsernameIdentity identity = new DefaultUsernameIdentity(username);
        identity.setUserData(alias);
        return identity;
    }

    /**
     * 校验安全策略和用户名口令
     *
     * @param policy   安全通道的安全策略
     * @param username 用户名
     * @param password 口令
     * @return 凭据别名对应的启用配置；校验失败时返回 null
     */
    public IotOpcUaServerConfigRespDTO authenticate(SecurityPolicy policy, String username, String password) {
        // 1. None 安全通道：默认拒绝
        if (policy == SecurityPolicy.None && !Boolean.TRUE.equals(config.getSecurityNoneAllowed())) {
            log.warn("[authenticate][未开启 security-none-allowed，拒绝在 None 安全通道上登录，用户 {}]", username);
            return null;
        }
        // 2. 用户名、口令：空白口令视为未配置
        String alias = null;
        for (Map.Entry<String, IotOpcUaServerConfig.Credential> entry : MapUtil.emptyIfNull(
                config.getCredentials()).entrySet()) {
            IotOpcUaServerConfig.Credential credential = entry.getValue();
            if (credential != null && StrUtil.isNotBlank(credential.getUsername())
                    && StrUtil.isNotBlank(credential.getPassword())
                    && Objects.equals(credential.getUsername(), username)
                    && MessageDigest.isEqual(credential.getPassword().getBytes(StandardCharsets.UTF_8),
                    StrUtil.nullToEmpty(password).getBytes(StandardCharsets.UTF_8))) {
                alias = entry.getKey();
                break;
            }
        }
        if (alias == null) {
            log.warn("[authenticate][用户 {} 的用户名或口令不正确]", username);
            return null;
        }
        // 3. 配置与租户
        IotOpcUaServerConfigRespDTO serverConfig = configLookup.apply(alias);
        if (serverConfig == null) {
            log.warn("[authenticate][凭据别名 {} 没有启用的 OPC UA Server 配置]", alias);
            return null;
        }
        if (!Objects.equals(alias, serverConfig.getCredentialAlias())
                || !isTenantAllowed(serverConfig.getTenantId(), alias)) {
            return null;
        }
        return serverConfig;
    }

    /**
     * 配置的租户是否被凭据允许：网关本地凭据（账号、口令）只授权给 tenant-ids 中的租户，防止配置引用其它租户的凭据别名
     *
     * @param tenantId 配置的租户编号
     * @param alias    凭据别名
     * @return 是否允许
     */
    public boolean isTenantAllowed(Long tenantId, String alias) {
        IotOpcUaServerConfig.Credential credential = config.getCredentials() != null
                ? config.getCredentials().get(alias) : null;
        if (credential == null || CollUtil.isEmpty(credential.getTenantIds())
                || !credential.getTenantIds().contains(tenantId)) {
            log.warn("[isTenantAllowed][凭据别名 {} 不允许租户 {} 使用]", alias, tenantId);
            return false;
        }
        return true;
    }

    // ========== 会话名额 ==========

    /**
     * 占用凭据别名的会话名额
     * <ol>
     *     <li>会话已以该别名激活（重新激活、换安全通道）：不重复占用；</li>
     *     <li>其它会话的占用 = 当前身份为该别名的存活会话 + 预留该别名且未失效的会话；未满时为本会话预留名额。</li>
     * </ol>
     * 预留不会释放会话原有身份的名额：切换别名成功后按新身份计数，失败（例如换安全通道时身份不一致）时原名额保持不变，
     * 新别名的预留到期失效
     *
     * @param alias       凭据别名
     * @param session     激活中的会话
     * @param maxSessions 最大会话数
     * @return 是否占用成功
     */
    synchronized boolean tryAcquireSession(String alias, Session session, int maxSessions) {
        // 1. 已以该别名激活：之前未生效的其它别名预留一并失效
        if (alias.equals(getCredentialAlias(session))) {
            reservations.remove(session.getSessionId());
            return true;
        }
        // 2. 其它会话已占满名额
        Set<NodeId> occupied = collectOccupied(alias);
        occupied.remove(session.getSessionId());
        if (occupied.size() >= maxSessions) {
            return false;
        }
        // 3. 预留名额
        reservations.put(session.getSessionId(), new Reservation(alias, clock.getAsLong() + RESERVATION_TIMEOUT_MILLIS));
        return true;
    }

    /**
     * 收集占用凭据别名名额的会话，并清理已失效的预留
     *
     * @param alias 凭据别名
     * @return 会话编号
     */
    private Set<NodeId> collectOccupied(String alias) {
        // 1. 已激活：当前身份为该别名的存活会话
        Map<NodeId, Session> liveSessions = new HashMap<>();
        Set<NodeId> occupied = new HashSet<>();
        for (Session live : liveSessionsSupplier.get()) {
            liveSessions.put(live.getSessionId(), live);
            if (alias.equals(getCredentialAlias(live))) {
                occupied.add(live.getSessionId());
            }
        }
        // 2. 激活中：未失效的预留
        long now = clock.getAsLong();
        Iterator<Map.Entry<NodeId, Reservation>> iterator = reservations.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<NodeId, Reservation> entry = iterator.next();
            Reservation reservation = entry.getValue();
            Session session = liveSessions.get(entry.getKey());
            if (session == null || reservation.expireAt <= now
                    || reservation.alias.equals(getCredentialAlias(session))) {
                iterator.remove();
                continue;
            }
            if (reservation.alias.equals(alias)) {
                occupied.add(entry.getKey());
            }
        }
        return occupied;
    }

    /**
     * 清空全部预留：Server 启动、停止时调用
     */
    public synchronized void clearSessions() {
        reservations.clear();
    }

    /**
     * 获取凭据别名已占用的会话名额数（测试用）
     *
     * @param alias 凭据别名
     * @return 名额数
     */
    synchronized int getSessionCount(String alias) {
        return collectOccupied(alias).size();
    }

    @Override
    public synchronized void onSessionClosed(Session session) {
        reservations.remove(session.getSessionId());
    }

    /**
     * 激活中的名额预留
     */
    private static final class Reservation {

        private final String alias;
        private final long expireAt;

        private Reservation(String alias, long expireAt) {
            this.alias = alias;
            this.expireAt = expireAt;
        }

    }

}
