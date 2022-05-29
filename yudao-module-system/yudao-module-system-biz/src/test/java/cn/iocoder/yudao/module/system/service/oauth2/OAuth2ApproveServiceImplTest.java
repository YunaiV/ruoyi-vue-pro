package cn.iocoder.yudao.module.system.service.oauth2;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.system.dal.dataobject.oauth2.OAuth2ApproveDO;
import cn.iocoder.yudao.module.system.dal.dataobject.oauth2.OAuth2ClientDO;
import cn.iocoder.yudao.module.system.dal.mysql.oauth2.OAuth2ApproveMapper;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.*;

import static cn.hutool.core.util.RandomUtil.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.addTime;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link OAuth2ApproveServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(OAuth2ApproveServiceImpl.class)
public class OAuth2ApproveServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OAuth2ApproveServiceImpl oauth2ApproveService;

    @Resource
    private OAuth2ApproveMapper oauth2ApproveMapper;

    @MockBean
    private OAuth2ClientService oauth2ClientService;

    @Test
    public void checkForPreApproval_clientAutoApprove() {
        // 准备参数
        Long userId = randomLongId();
        Integer userType = randomEle(UserTypeEnum.values()).getValue();
        String clientId = randomString();
        List<String> requestedScopes = Lists.newArrayList("read");
        // mock 方法
        when(oauth2ClientService.validOAuthClientFromCache(eq(clientId)))
                .thenReturn(randomPojo(OAuth2ClientDO.class).setAutoApproveScopes(requestedScopes));

        // 调用
        boolean success = oauth2ApproveService.checkForPreApproval(userId, userType,
                clientId, requestedScopes);
        // 断言
        assertTrue(success);
        List<OAuth2ApproveDO> result = oauth2ApproveMapper.selectList();
        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).getUserId());
        assertEquals(userType, result.get(0).getUserType());
        assertEquals(clientId, result.get(0).getClientId());
        assertEquals("read", result.get(0).getScope());
        assertTrue(result.get(0).getApproved());
        assertFalse(DateUtils.isExpired(result.get(0).getExpiresTime()));
    }

    @Test
    public void checkForPreApproval_approve() {
        // 准备参数
        Long userId = randomLongId();
        Integer userType = randomEle(UserTypeEnum.values()).getValue();
        String clientId = randomString();
        List<String> requestedScopes = Lists.newArrayList("read");
        // mock 方法
        when(oauth2ClientService.validOAuthClientFromCache(eq(clientId)))
                .thenReturn(randomPojo(OAuth2ClientDO.class).setAutoApproveScopes(null));
        // mock 数据
        OAuth2ApproveDO approve = randomPojo(OAuth2ApproveDO.class).setUserId(userId)
                .setUserType(userType).setClientId(clientId).setScope("read")
                .setExpiresTime(addTime(Duration.ofDays(1))).setApproved(true); // 同意
        oauth2ApproveMapper.insert(approve);

        // 调用
        boolean success = oauth2ApproveService.checkForPreApproval(userId, userType,
                clientId, requestedScopes);
        // 断言
        assertTrue(success);
    }

    @Test
    public void checkForPreApproval_reject() {
        // 准备参数
        Long userId = randomLongId();
        Integer userType = randomEle(UserTypeEnum.values()).getValue();
        String clientId = randomString();
        List<String> requestedScopes = Lists.newArrayList("read");
        // mock 方法
        when(oauth2ClientService.validOAuthClientFromCache(eq(clientId)))
                .thenReturn(randomPojo(OAuth2ClientDO.class).setAutoApproveScopes(null));
        // mock 数据
        OAuth2ApproveDO approve = randomPojo(OAuth2ApproveDO.class).setUserId(userId)
                .setUserType(userType).setClientId(clientId).setScope("read")
                .setExpiresTime(addTime(Duration.ofDays(1))).setApproved(false); // 拒绝
        oauth2ApproveMapper.insert(approve);

        // 调用
        boolean success = oauth2ApproveService.checkForPreApproval(userId, userType,
                clientId, requestedScopes);
        // 断言
        assertFalse(success);
    }

    @Test
    public void testUpdateAfterApproval_none() {
        // 准备参数
        Long userId = randomLongId();
        Integer userType = randomEle(UserTypeEnum.values()).getValue();
        String clientId = randomString();

        // 调用
        boolean success = oauth2ApproveService.updateAfterApproval(userId, userType, clientId,
                null);
        // 断言
        assertTrue(success);
        List<OAuth2ApproveDO> result = oauth2ApproveMapper.selectList();
        assertEquals(0, result.size());
    }

    @Test
    public void testUpdateAfterApproval_approved() {
        // 准备参数
        Long userId = randomLongId();
        Integer userType = randomEle(UserTypeEnum.values()).getValue();
        String clientId = randomString();
        Map<String, Boolean> requestedScopes = new LinkedHashMap<>(); // 有序，方便判断
        requestedScopes.put("read", true);
        requestedScopes.put("write", false);
        // mock 方法

        // 调用
        boolean success = oauth2ApproveService.updateAfterApproval(userId, userType, clientId,
                requestedScopes);
        // 断言
        assertTrue(success);
        List<OAuth2ApproveDO> result = oauth2ApproveMapper.selectList();
        assertEquals(2, result.size());
        // read
        assertEquals(userId, result.get(0).getUserId());
        assertEquals(userType, result.get(0).getUserType());
        assertEquals(clientId, result.get(0).getClientId());
        assertEquals("read", result.get(0).getScope());
        assertTrue(result.get(0).getApproved());
        assertFalse(DateUtils.isExpired(result.get(0).getExpiresTime()));
        // write
        assertEquals(userId, result.get(1).getUserId());
        assertEquals(userType, result.get(1).getUserType());
        assertEquals(clientId, result.get(1).getClientId());
        assertEquals("write", result.get(1).getScope());
        assertFalse(result.get(1).getApproved());
        assertFalse(DateUtils.isExpired(result.get(1).getExpiresTime()));
    }

    @Test
    public void testUpdateAfterApproval_reject() {
        // 准备参数
        Long userId = randomLongId();
        Integer userType = randomEle(UserTypeEnum.values()).getValue();
        String clientId = randomString();
        Map<String, Boolean> requestedScopes = new LinkedHashMap<>();
        requestedScopes.put("write", false);
        // mock 方法

        // 调用
        boolean success = oauth2ApproveService.updateAfterApproval(userId, userType, clientId,
                requestedScopes);
        // 断言
        assertFalse(success);
        List<OAuth2ApproveDO> result = oauth2ApproveMapper.selectList();
        assertEquals(1, result.size());
        // write
        assertEquals(userId, result.get(0).getUserId());
        assertEquals(userType, result.get(0).getUserType());
        assertEquals(clientId, result.get(0).getClientId());
        assertEquals("write", result.get(0).getScope());
        assertFalse(result.get(0).getApproved());
        assertFalse(DateUtils.isExpired(result.get(0).getExpiresTime()));
    }

    @Test
    public void testGetApproveList() {
        // 准备参数
        Long userId = 10L;
        Integer userType = UserTypeEnum.ADMIN.getValue();
        String clientId = randomString();
        // mock 数据
        OAuth2ApproveDO approve = randomPojo(OAuth2ApproveDO.class).setUserId(userId)
                .setUserType(userType).setClientId(clientId).setExpiresTime(addTime(Duration.ofDays(1L)));
        oauth2ApproveMapper.insert(approve); // 未过期
        oauth2ApproveMapper.insert(ObjectUtil.clone(approve).setId(null)
                .setExpiresTime(addTime(Duration.ofDays(-1L)))); // 已过期

        // 调用
        List<OAuth2ApproveDO> result = oauth2ApproveService.getApproveList(userId, userType, clientId);
        // 断言
        assertEquals(1, result.size());
        assertPojoEquals(approve, result.get(0));
    }

    @Test
    public void testSaveApprove_insert() {
        // 准备参数
        Long userId = randomLongId();
        Integer userType = randomEle(UserTypeEnum.values()).getValue();
        String clientId = randomString();
        String scope = randomString();
        Boolean approved = randomBoolean();
        Date expireTime = randomDay(1, 30);
        // mock 方法

        // 调用
        oauth2ApproveService.saveApprove(userId, userType, clientId,
                scope, approved, expireTime);
        // 断言
        List<OAuth2ApproveDO> result = oauth2ApproveMapper.selectList();
        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).getUserId());
        assertEquals(userType, result.get(0).getUserType());
        assertEquals(clientId, result.get(0).getClientId());
        assertEquals(scope, result.get(0).getScope());
        assertEquals(approved, result.get(0).getApproved());
        assertEquals(expireTime, result.get(0).getExpiresTime());
    }

    @Test
    public void testSaveApprove_update() {
        // mock 数据
        OAuth2ApproveDO approve = randomPojo(OAuth2ApproveDO.class);
        oauth2ApproveMapper.insert(approve);
        // 准备参数
        Long userId = approve.getUserId();
        Integer userType = approve.getUserType();
        String clientId = approve.getClientId();
        String scope = approve.getScope();
        Boolean approved = randomBoolean();
        Date expireTime = randomDay(1, 30);
        // mock 方法

        // 调用
        oauth2ApproveService.saveApprove(userId, userType, clientId,
                scope, approved, expireTime);
        // 断言
        List<OAuth2ApproveDO> result = oauth2ApproveMapper.selectList();
        assertEquals(1, result.size());
        assertEquals(approve.getId(), result.get(0).getId());
        assertEquals(userId, result.get(0).getUserId());
        assertEquals(userType, result.get(0).getUserType());
        assertEquals(clientId, result.get(0).getClientId());
        assertEquals(scope, result.get(0).getScope());
        assertEquals(approved, result.get(0).getApproved());
        assertEquals(expireTime, result.get(0).getExpiresTime());
    }

}
