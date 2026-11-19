package cn.iocoder.yudao.module.system.service.oauth2;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbAndRedisUnitTest;
import cn.iocoder.yudao.module.system.controller.admin.oauth2.vo.token.OAuth2AccessTokenPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import cn.iocoder.yudao.module.system.dal.dataobject.oauth2.OAuth2ClientDO;
import cn.iocoder.yudao.module.system.dal.dataobject.oauth2.OAuth2CodeDO;
import cn.iocoder.yudao.module.system.dal.dataobject.oauth2.OAuth2RefreshTokenDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.oauth2.OAuth2AccessTokenMapper;
import cn.iocoder.yudao.module.system.dal.mysql.oauth2.OAuth2CodeMapper;
import cn.iocoder.yudao.module.system.dal.mysql.oauth2.OAuth2RefreshTokenMapper;
import cn.iocoder.yudao.module.system.dal.redis.oauth2.OAuth2AccessTokenRedisDAO;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import jakarta.annotation.Resource;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link OAuth2TokenServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import({OAuth2TokenServiceImpl.class, OAuth2AccessTokenRedisDAO.class})
public class OAuth2TokenServiceImplTest extends BaseDbAndRedisUnitTest {

    @Resource
    private OAuth2TokenServiceImpl oauth2TokenService;

    @Resource
    private OAuth2AccessTokenMapper oauth2AccessTokenMapper;
    @Resource
    private OAuth2RefreshTokenMapper oauth2RefreshTokenMapper;
    @Resource
    private OAuth2CodeMapper oauth2CodeMapper;

    @MockitoSpyBean
    private OAuth2AccessTokenRedisDAO oauth2AccessTokenRedisDAO;
    @Resource
    private PlatformTransactionManager transactionManager;

    @MockitoBean
    private OAuth2ClientService oauth2ClientService;
    @MockitoBean
    private AdminUserService adminUserService;

    @Test
    public void testCreateAccessToken() {
        TenantContextHolder.setTenantId(0L);
        // 准备参数
        Long userId = randomLongId();
        Integer userType = UserTypeEnum.ADMIN.getValue();
        String clientId = randomString();
        List<String> scopes = Lists.newArrayList("read", "write");
        LocalDateTime now = LocalDateTime.now().withNano(0);
        // mock 方法
        OAuth2ClientDO clientDO = randomPojo(OAuth2ClientDO.class).setClientId(clientId)
                .setAccessTokenValiditySeconds(30).setRefreshTokenValiditySeconds(60);
        when(oauth2ClientService.validOAuthClientFromCache(eq(clientId))).thenReturn(clientDO);
        // mock 数据（用户）
        AdminUserDO user = randomPojo(AdminUserDO.class);
        when(adminUserService.getUser(userId)).thenReturn(user);

        // 调用
        OAuth2AccessTokenDO accessTokenDO;
        try (MockedStatic<LocalDateTime> mockedTime = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {
            mockedTime.when(LocalDateTime::now).thenReturn(now);
            accessTokenDO = oauth2TokenService.createAccessToken(userId, userType, clientId, scopes);
        }
        // 断言访问令牌
        OAuth2AccessTokenDO dbAccessTokenDO = oauth2AccessTokenMapper.selectByAccessToken(accessTokenDO.getAccessToken());
        assertPojoEquals(accessTokenDO, dbAccessTokenDO, "createTime", "updateTime", "deleted");
        assertEquals(userId, accessTokenDO.getUserId());
        assertEquals(userType, accessTokenDO.getUserType());
        assertEquals(2, accessTokenDO.getUserInfo().size());
        assertEquals(user.getNickname(), accessTokenDO.getUserInfo().get("nickname"));
        assertEquals(user.getDeptId().toString(), accessTokenDO.getUserInfo().get("deptId"));
        assertEquals(clientId, accessTokenDO.getClientId());
        assertEquals(scopes, accessTokenDO.getScopes());
        assertEquals(now.plusSeconds(30), accessTokenDO.getExpiresTime());
        assertFalse(DateUtils.isExpired(accessTokenDO.getExpiresTime()));
        // 断言访问令牌的缓存
        OAuth2AccessTokenDO redisAccessTokenDO = oauth2AccessTokenRedisDAO.get(accessTokenDO.getAccessToken());
        assertPojoEquals(accessTokenDO, redisAccessTokenDO, "createTime", "updateTime", "deleted");
        // 断言刷新令牌
        OAuth2RefreshTokenDO refreshTokenDO = oauth2RefreshTokenMapper.selectList().get(0);
        assertPojoEquals(accessTokenDO, refreshTokenDO, "id", "expiresTime", "createTime", "updateTime", "deleted");
        assertEquals(now.plusSeconds(60), refreshTokenDO.getExpiresTime());
        assertFalse(DateUtils.isExpired(refreshTokenDO.getExpiresTime()));
    }

    @Test
    public void testRefreshAccessToken_null() {
        // 准备参数
        String refreshToken = randomString();
        String clientId = randomString();
        // mock 方法

        // 调用，并断言
        assertServiceException(() -> oauth2TokenService.refreshAccessToken(refreshToken, clientId),
                new ErrorCode(400, "无效的刷新令牌"));
    }

    @Test
    public void testRefreshAccessToken_clientIdError() {
        // 准备参数
        String refreshToken = randomString();
        String clientId = randomString();
        // mock 方法
        OAuth2ClientDO clientDO = randomPojo(OAuth2ClientDO.class).setClientId(clientId);
        when(oauth2ClientService.validOAuthClientFromCache(eq(clientId))).thenReturn(clientDO);
        // mock 数据（访问令牌）
        OAuth2RefreshTokenDO refreshTokenDO = randomPojo(OAuth2RefreshTokenDO.class)
                .setRefreshToken(refreshToken).setClientId("error");
        oauth2RefreshTokenMapper.insert(refreshTokenDO);

        // 调用，并断言
        assertServiceException(() -> oauth2TokenService.refreshAccessToken(refreshToken, clientId),
                new ErrorCode(400, "刷新令牌的客户端编号不正确"));
    }

    @Test
    public void testRefreshAccessToken_expired() {
        // 准备参数
        String refreshToken = randomString();
        String clientId = randomString();
        // mock 方法
        OAuth2ClientDO clientDO = randomPojo(OAuth2ClientDO.class).setClientId(clientId);
        when(oauth2ClientService.validOAuthClientFromCache(eq(clientId))).thenReturn(clientDO);
        // mock 数据（访问令牌）
        OAuth2RefreshTokenDO refreshTokenDO = randomPojo(OAuth2RefreshTokenDO.class)
                .setRefreshToken(refreshToken).setClientId(clientId)
                .setExpiresTime(LocalDateTime.now().minusDays(1));
        oauth2RefreshTokenMapper.insert(refreshTokenDO);
        // mock 数据（访问令牌）
        OAuth2AccessTokenDO accessTokenDO = randomPojo(OAuth2AccessTokenDO.class).setRefreshToken(refreshToken)
                .setExpiresTime(LocalDateTime.now().plusDays(1).withNano(0));
        oauth2AccessTokenMapper.insert(accessTokenDO);
        oauth2AccessTokenRedisDAO.set(accessTokenDO);

        // 调用，并断言
        assertServiceException(() -> oauth2TokenService.refreshAccessToken(refreshToken, clientId),
                new ErrorCode(401, "刷新令牌已过期"));
        // 断言，刷新令牌及其访问令牌被删除
        assertNull(oauth2RefreshTokenMapper.selectByRefreshToken(refreshToken));
        assertEquals(0, oauth2AccessTokenMapper.selectCount());
        assertNull(oauth2AccessTokenRedisDAO.get(accessTokenDO.getAccessToken()));
    }

    @Test
    public void testRefreshAccessToken_success() {
        TenantContextHolder.setTenantId(0L);
        // 准备参数
        String refreshToken = randomString();
        String clientId = randomString();
        LocalDateTime now = LocalDateTime.now().withNano(0);
        // mock 方法
        OAuth2ClientDO clientDO = randomPojo(OAuth2ClientDO.class).setClientId(clientId)
                .setAccessTokenValiditySeconds(30);
        when(oauth2ClientService.validOAuthClientFromCache(eq(clientId))).thenReturn(clientDO);
        // mock 数据（访问令牌）
        OAuth2RefreshTokenDO refreshTokenDO = randomPojo(OAuth2RefreshTokenDO.class, o ->
                o.setRefreshToken(refreshToken).setClientId(clientId)
                        .setExpiresTime(LocalDateTime.now().plusDays(1))
                        .setUserType(UserTypeEnum.ADMIN.getValue())
                        .setTenantId(TenantContextHolder.getTenantId()));
        oauth2RefreshTokenMapper.insert(refreshTokenDO);
        // mock 数据（访问令牌）
        OAuth2AccessTokenDO accessTokenDO = randomPojo(OAuth2AccessTokenDO.class).setRefreshToken(refreshToken)
                .setUserType(refreshTokenDO.getUserType())
                .setExpiresTime(LocalDateTime.now().plusDays(1).withNano(0));
        oauth2AccessTokenMapper.insert(accessTokenDO);
        oauth2AccessTokenRedisDAO.set(accessTokenDO);
        // mock 数据（用户）
        AdminUserDO user = randomPojo(AdminUserDO.class);
        when(adminUserService.getUser(refreshTokenDO.getUserId())).thenReturn(user);

        // 调用
        OAuth2AccessTokenDO newAccessTokenDO;
        try (MockedStatic<LocalDateTime> mockedTime = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {
            mockedTime.when(LocalDateTime::now).thenReturn(now);
            newAccessTokenDO = oauth2TokenService.refreshAccessToken(refreshToken, clientId);
        }
        // 断言，老的访问令牌保留，由其有效期自然结束（多标签页场景：https://github.com/YunaiV/ruoyi-vue-pro/issues/1332）
        assertNotNull(oauth2AccessTokenMapper.selectByAccessToken(accessTokenDO.getAccessToken()));
        assertNotNull(oauth2AccessTokenRedisDAO.get(accessTokenDO.getAccessToken()));
        // 断言，新的访问令牌
        OAuth2AccessTokenDO dbAccessTokenDO = oauth2AccessTokenMapper.selectByAccessToken(newAccessTokenDO.getAccessToken());
        assertPojoEquals(newAccessTokenDO, dbAccessTokenDO, "createTime", "updateTime", "deleted");
        assertPojoEquals(newAccessTokenDO, refreshTokenDO, "id", "expiresTime", "createTime", "updateTime", "deleted",
                "creator", "updater");
        assertEquals(now.plusSeconds(30), newAccessTokenDO.getExpiresTime());
        assertFalse(DateUtils.isExpired(newAccessTokenDO.getExpiresTime()));
        // 断言，新的访问令牌的缓存
        OAuth2AccessTokenDO redisAccessTokenDO = oauth2AccessTokenRedisDAO.get(newAccessTokenDO.getAccessToken());
        assertPojoEquals(newAccessTokenDO, redisAccessTokenDO, "createTime", "updateTime", "deleted");
    }

    @Test
    public void testGetAccessToken() {
        // mock 数据（访问令牌）
        OAuth2AccessTokenDO accessTokenDO = randomPojo(OAuth2AccessTokenDO.class)
                .setExpiresTime(LocalDateTime.now().plusDays(1).withNano(0));
        oauth2AccessTokenMapper.insert(accessTokenDO);
        // 准备参数
        String accessToken = accessTokenDO.getAccessToken();

        // 调用
        OAuth2AccessTokenDO result = oauth2TokenService.getAccessToken(accessToken);
        // 断言
        assertPojoEquals(accessTokenDO, result, "createTime", "updateTime", "deleted",
                "creator", "updater");
        assertPojoEquals(accessTokenDO, oauth2AccessTokenRedisDAO.get(accessToken), "createTime", "updateTime", "deleted",
                "creator", "updater");
    }

    @Test
    public void testCheckAccessToken_null() {
        // 调研，并断言
        assertServiceException(() -> oauth2TokenService.checkAccessToken(randomString()),
                new ErrorCode(401, "访问令牌不存在"));
    }

    @Test
    public void testCheckAccessToken_expired() {
        // mock 数据（访问令牌）
        OAuth2AccessTokenDO accessTokenDO = randomPojo(OAuth2AccessTokenDO.class)
                .setExpiresTime(LocalDateTime.now().minusDays(1));
        oauth2AccessTokenMapper.insert(accessTokenDO);
        // 准备参数
        String accessToken = accessTokenDO.getAccessToken();

        // 调研，并断言
        assertServiceException(() -> oauth2TokenService.checkAccessToken(accessToken),
                new ErrorCode(401, "访问令牌已过期"));
    }

    @Test
    public void testCheckAccessToken_refreshToken() {
        // mock 数据（访问令牌）
        OAuth2RefreshTokenDO refreshTokenDO = randomPojo(OAuth2RefreshTokenDO.class)
                .setUserId(0L)
                .setExpiresTime(LocalDateTime.now().plusDays(1));
        oauth2RefreshTokenMapper.insert(refreshTokenDO);
        // 准备参数
        String accessToken = refreshTokenDO.getRefreshToken();

        // 调研，并断言
        OAuth2AccessTokenDO result = oauth2TokenService.getAccessToken(accessToken);
        // 断言
        assertPojoEquals(refreshTokenDO, result, "expiresTime", "createTime", "updateTime", "deleted",
                "creator", "updater");
    }

    @Test
    public void testCheckAccessToken_success() {
        // mock 数据（访问令牌）
        OAuth2AccessTokenDO accessTokenDO = randomPojo(OAuth2AccessTokenDO.class)
                .setExpiresTime(LocalDateTime.now().plusDays(1).withNano(0));
        oauth2AccessTokenMapper.insert(accessTokenDO);
        // 准备参数
        String accessToken = accessTokenDO.getAccessToken();

        // 调研，并断言
        OAuth2AccessTokenDO result = oauth2TokenService.getAccessToken(accessToken);
        // 断言
        assertPojoEquals(accessTokenDO, result, "createTime", "updateTime", "deleted",
                "creator", "updater");
    }

    @Test
    public void testRemoveAccessToken_null() {
        // 调用，并断言
        assertNull(oauth2TokenService.removeAccessToken(randomString()));
    }

    @Test
    public void testRemoveAccessToken_success() {
        // mock 数据（访问令牌）
        OAuth2AccessTokenDO accessTokenDO = randomPojo(OAuth2AccessTokenDO.class)
                .setExpiresTime(LocalDateTime.now().plusDays(1).withNano(0));
        oauth2AccessTokenMapper.insert(accessTokenDO);
        // mock 数据（同一刷新令牌下的其它访问令牌，例如说其它标签页刷新所得）
        OAuth2AccessTokenDO sameRefreshAccessTokenDO = randomPojo(OAuth2AccessTokenDO.class)
                .setRefreshToken(accessTokenDO.getRefreshToken())
                .setExpiresTime(LocalDateTime.now().plusDays(1).withNano(0));
        oauth2AccessTokenMapper.insert(sameRefreshAccessTokenDO);
        oauth2AccessTokenRedisDAO.set(sameRefreshAccessTokenDO);
        // mock 数据（其它刷新令牌下的访问令牌）
        OAuth2AccessTokenDO otherAccessTokenDO = randomPojo(OAuth2AccessTokenDO.class)
                .setExpiresTime(LocalDateTime.now().plusDays(1).withNano(0));
        oauth2AccessTokenMapper.insert(otherAccessTokenDO);
        oauth2AccessTokenRedisDAO.set(otherAccessTokenDO);
        // mock 数据（刷新令牌）
        OAuth2RefreshTokenDO refreshTokenDO = randomPojo(OAuth2RefreshTokenDO.class)
                .setRefreshToken(accessTokenDO.getRefreshToken());
        oauth2RefreshTokenMapper.insert(refreshTokenDO);
        // 调用
        OAuth2AccessTokenDO result = oauth2TokenService.removeAccessToken(sameRefreshAccessTokenDO.getAccessToken());
        assertPojoEquals(sameRefreshAccessTokenDO, result, "createTime", "updateTime", "deleted",
                "creator", "updater");
        // 断言数据
        assertNull(oauth2AccessTokenMapper.selectByAccessToken(accessTokenDO.getAccessToken()));
        assertNull(oauth2RefreshTokenMapper.selectByRefreshToken(accessTokenDO.getRefreshToken()));
        assertNull(oauth2AccessTokenRedisDAO.get(accessTokenDO.getAccessToken()));
        // 断言，同一刷新令牌下的其它访问令牌被删除
        assertNull(oauth2AccessTokenMapper.selectByAccessToken(sameRefreshAccessTokenDO.getAccessToken()));
        assertNull(oauth2AccessTokenRedisDAO.get(sameRefreshAccessTokenDO.getAccessToken()));
        // 断言，其它刷新令牌下的访问令牌保留
        assertNotNull(oauth2AccessTokenMapper.selectByAccessToken(otherAccessTokenDO.getAccessToken()));
        assertNotNull(oauth2AccessTokenRedisDAO.get(otherAccessTokenDO.getAccessToken()));
    }


    @Test
    public void testRemoveAdminUserTokens_success() {
        // mock 数据（后台用户的访问令牌、独立刷新令牌和授权码）
        Long userId = randomLongId();
        Integer userType = UserTypeEnum.ADMIN.getValue();
        LocalDateTime expiresTime = LocalDateTime.now().plusMinutes(5).withNano(0);
        OAuth2AccessTokenDO accessToken = randomPojo(OAuth2AccessTokenDO.class)
                .setUserId(userId).setUserType(userType).setExpiresTime(expiresTime);
        oauth2AccessTokenMapper.insert(accessToken);
        oauth2AccessTokenRedisDAO.set(accessToken);
        OAuth2RefreshTokenDO refreshToken = randomPojo(OAuth2RefreshTokenDO.class)
                .setUserId(userId).setUserType(userType).setExpiresTime(expiresTime);
        oauth2RefreshTokenMapper.insert(refreshToken);
        oauth2AccessTokenRedisDAO.set(cloneIgnoreId(accessToken,
                o -> o.setAccessToken(refreshToken.getRefreshToken())));
        OAuth2CodeDO code = randomPojo(OAuth2CodeDO.class).setUserId(userId).setUserType(userType);
        oauth2CodeMapper.insert(code);
        // mock 数据（编号相同的会员令牌）
        OAuth2AccessTokenDO memberToken = cloneIgnoreId(accessToken, o -> o.setAccessToken(randomString())
                .setUserType(UserTypeEnum.MEMBER.getValue()));
        oauth2AccessTokenMapper.insert(memberToken);
        oauth2AccessTokenRedisDAO.set(memberToken);
        doThrow(new IllegalStateException()).doCallRealMethod().when(oauth2AccessTokenRedisDAO).deleteList(anyCollection());

        // 调用，并断言事务提交前仅删除数据库记录
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            oauth2TokenService.removeAdminUserTokens(List.of(userId));
            assertNull(oauth2AccessTokenMapper.selectById(accessToken.getId()));
            assertNull(oauth2RefreshTokenMapper.selectById(refreshToken.getId()));
            assertNull(oauth2CodeMapper.selectById(code.getId()));
            assertNotNull(oauth2AccessTokenRedisDAO.get(accessToken.getAccessToken()));
            assertNotNull(oauth2AccessTokenRedisDAO.get(refreshToken.getRefreshToken()));
        });
        // 断言提交后重试清理 Redis，并保留会员令牌
        verify(oauth2AccessTokenRedisDAO, times(2)).deleteList(anyCollection());
        assertNull(oauth2AccessTokenRedisDAO.get(accessToken.getAccessToken()));
        assertNull(oauth2AccessTokenRedisDAO.get(refreshToken.getRefreshToken()));
        assertNotNull(oauth2AccessTokenMapper.selectById(memberToken.getId()));
        assertNotNull(oauth2AccessTokenRedisDAO.get(memberToken.getAccessToken()));
        oauth2AccessTokenRedisDAO.delete(memberToken.getAccessToken());
    }

    @Test
    public void testRemoveAdminUserTokens_rollback() {
        // mock 数据（没有关联访问令牌的刷新令牌）
        OAuth2RefreshTokenDO refreshToken = randomPojo(OAuth2RefreshTokenDO.class)
                .setUserType(UserTypeEnum.ADMIN.getValue()).setExpiresTime(LocalDateTime.now().plusMinutes(5));
        oauth2RefreshTokenMapper.insert(refreshToken);
        oauth2AccessTokenRedisDAO.set(randomPojo(OAuth2AccessTokenDO.class)
                .setAccessToken(refreshToken.getRefreshToken()).setExpiresTime(refreshToken.getExpiresTime()));

        // 调用，并回滚
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            oauth2TokenService.removeAdminUserTokens(List.of(refreshToken.getUserId()));
            assertNull(oauth2RefreshTokenMapper.selectById(refreshToken.getId()));
            status.setRollbackOnly();
        });
        // 断言数据库和缓存都保留原令牌
        assertNotNull(oauth2RefreshTokenMapper.selectById(refreshToken.getId()));
        assertNotNull(oauth2AccessTokenRedisDAO.get(refreshToken.getRefreshToken()));
        oauth2AccessTokenRedisDAO.delete(refreshToken.getRefreshToken());
    }

    @Test
    public void testRemoveAdminUserTokens_cacheCleanupFails() {
        // mock 数据（没有关联访问令牌的刷新令牌）
        OAuth2RefreshTokenDO refreshToken = randomPojo(OAuth2RefreshTokenDO.class)
                .setUserType(UserTypeEnum.ADMIN.getValue());
        oauth2RefreshTokenMapper.insert(refreshToken);
        doThrow(new IllegalStateException()).when(oauth2AccessTokenRedisDAO).deleteList(anyCollection());

        AtomicBoolean otherCallbackCalled = new AtomicBoolean();

        // 调用，并断言数据库提交后，缓存异常不影响其他提交回调
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            oauth2TokenService.removeAdminUserTokens(List.of(refreshToken.getUserId()));
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    otherCallbackCalled.set(true);
                }
            });
        });
        assertTrue(otherCallbackCalled.get());
        assertNull(oauth2RefreshTokenMapper.selectById(refreshToken.getId()));
        verify(oauth2AccessTokenRedisDAO, times(2)).deleteList(anyCollection());
    }

    @Test
    public void testRemoveAdminUserTokens_otherCommitCallbackFails() {
        // mock 数据（缓存中的刷新令牌）
        OAuth2RefreshTokenDO refreshToken = randomPojo(OAuth2RefreshTokenDO.class)
                .setUserType(UserTypeEnum.ADMIN.getValue()).setExpiresTime(LocalDateTime.now().plusMinutes(5));
        oauth2RefreshTokenMapper.insert(refreshToken);
        oauth2AccessTokenRedisDAO.set(randomPojo(OAuth2AccessTokenDO.class)
                .setAccessToken(refreshToken.getRefreshToken()).setExpiresTime(refreshToken.getExpiresTime()));

        // 调用，并断言其他提交回调抛错时，令牌缓存仍能清理
        assertThrows(IllegalStateException.class, () -> new TransactionTemplate(transactionManager)
                .executeWithoutResult(status -> {
                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            throw new IllegalStateException();
                        }
                    });
                    oauth2TokenService.removeAdminUserTokens(List.of(refreshToken.getUserId()));
                }));
        assertNull(oauth2RefreshTokenMapper.selectById(refreshToken.getId()));
        assertNull(oauth2AccessTokenRedisDAO.get(refreshToken.getRefreshToken()));
    }

    @Test
    public void testGetAccessTokenPage() {
        // mock 数据
        OAuth2AccessTokenDO dbAccessToken = randomPojo(OAuth2AccessTokenDO.class, o -> { // 等会查询到
            o.setUserId(10L);
            o.setUserType(1);
            o.setClientId("test_client");
            o.setExpiresTime(LocalDateTime.now().plusDays(1).withNano(0));
        });
        oauth2AccessTokenMapper.insert(dbAccessToken);
        // 测试 userId 不匹配
        oauth2AccessTokenMapper.insert(cloneIgnoreId(dbAccessToken, o -> o.setUserId(20L)));
        // 测试 userType 不匹配
        oauth2AccessTokenMapper.insert(cloneIgnoreId(dbAccessToken, o -> o.setUserType(2)));
        // 测试 userType 不匹配
        oauth2AccessTokenMapper.insert(cloneIgnoreId(dbAccessToken, o -> o.setClientId("it_client")));
        // 测试 expireTime 不匹配
        oauth2AccessTokenMapper.insert(cloneIgnoreId(dbAccessToken, o -> o.setExpiresTime(LocalDateTimeUtil.now().minusDays(1))));
        // 准备参数
        OAuth2AccessTokenPageReqVO reqVO = new OAuth2AccessTokenPageReqVO();
        reqVO.setUserId(10L);
        reqVO.setUserType(1);
        reqVO.setClientId("test");

        // 调用
        PageResult<OAuth2AccessTokenDO> pageResult = oauth2TokenService.getAccessTokenPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbAccessToken, pageResult.getList().get(0));
    }

}
