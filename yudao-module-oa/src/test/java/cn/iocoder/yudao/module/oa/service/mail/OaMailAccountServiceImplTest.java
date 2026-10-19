package cn.iocoder.yudao.module.oa.service.mail;

import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.oa.controller.admin.mail.vo.account.OaMailAccountSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.mail.OaMailAccountDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.mail.OaMailProviderDO;
import cn.iocoder.yudao.module.oa.dal.mysql.mail.OaMailAccountMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link OaMailAccountServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(OaMailAccountServiceImpl.class)
public class OaMailAccountServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OaMailAccountServiceImpl mailAccountService;

    @Resource(name = "oaMailAccountMapper") // 区分 System 模块的同名 Mapper
    private OaMailAccountMapper mailAccountMapper;

    @MockBean
    private OaMailProviderService mailProviderService;
    @MockBean
    private OaMailMessageClient mailMessageClient;

    @AfterEach
    public void clearLogin() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testUpdateMailAccount_identityImmutable() {
        // mock 数据
        OaMailAccountDO account = randomMailAccountDO("1");
        mailAccountMapper.insert(account);
        // 准备参数：逐一验证三项账号身份均不可修改
        OaMailAccountSaveReqVO reqVO = new OaMailAccountSaveReqVO().setId(account.getId())
                .setProviderId(account.getProviderId()).setMail(account.getMail()).setUsername(account.getUsername())
                .setPassword("").setDefaultStatus(false).setStatus(0);

        // 调用，并断言异常：邮箱地址、登录账号、服务商均不可变更
        reqVO.setMail("changed@example.com");
        assertServiceException(() -> mailAccountService.updateMailAccount(reqVO, 1L), MAIL_ACCOUNT_IDENTITY_IMMUTABLE);
        reqVO.setMail(account.getMail()).setUsername("changed-login");
        assertServiceException(() -> mailAccountService.updateMailAccount(reqVO, 1L), MAIL_ACCOUNT_IDENTITY_IMMUTABLE);
        reqVO.setUsername(account.getUsername()).setProviderId(account.getProviderId() + 1);
        assertServiceException(() -> mailAccountService.updateMailAccount(reqVO, 1L), MAIL_ACCOUNT_IDENTITY_IMMUTABLE);
        assertEquals(account.getMail(), mailAccountMapper.selectById(account.getId()).getMail());
    }

    @Test
    public void testUpdateMailAccount_passwordAndStatus() {
        // mock 数据
        OaMailAccountDO account = randomMailAccountDO("1");
        mailAccountMapper.insert(account);
        // 准备参数
        OaMailAccountSaveReqVO reqVO = new OaMailAccountSaveReqVO().setId(account.getId())
                .setProviderId(account.getProviderId()).setMail(account.getMail()).setUsername(account.getUsername())
                .setPassword("new-test-password").setDefaultStatus(false).setStatus(1);
        when(mailProviderService.validateMailProviderExists(account.getProviderId()))
                .thenReturn(new OaMailProviderDO().setStatus(0));

        // 调用
        mailAccountService.updateMailAccount(reqVO, 1L);
        // 断言
        OaMailAccountDO result = mailAccountMapper.selectById(account.getId());
        assertEquals("new-test-password", result.getPassword());
        assertEquals(1, result.getStatus());
        assertEquals(account.getUsername(), result.getUsername());
    }

    @Test
    public void testGetMailAccountListByStatus() {
        // mock 数据：包含其他员工的邮箱、重复地址及停用账号
        mailAccountMapper.insert(randomMailAccountDO("1").setMail("a@example.com").setStatus(0));
        mailAccountMapper.insert(randomMailAccountDO("2").setMail("b@example.com").setStatus(0));
        mailAccountMapper.insert(randomMailAccountDO("3").setMail("a@example.com").setStatus(0));
        mailAccountMapper.insert(randomMailAccountDO("4").setMail("disabled@example.com").setStatus(1));

        // 调用
        List<OaMailAccountDO> accounts = mailAccountService.getMailAccountListByStatus(0);

        // 断言
        assertEquals(Arrays.asList("a@example.com", "a@example.com", "b@example.com"),
                convertList(accounts, OaMailAccountDO::getMail));
        assertTrue(convertList(accounts, OaMailAccountDO::getCreator).containsAll(Arrays.asList("1", "2", "3")));
        accounts.forEach(account -> {
            assertNull(account.getPassword());
            assertNull(account.getUsername());
            assertNull(account.getProviderId());
        });
    }

    @Test
    public void testGetMailAccountListByStatus_empty() {

        // 调用，并断言
        assertTrue(mailAccountService.getMailAccountListByStatus(0).isEmpty());
    }

    @Test
    public void testGetMailAccountListByUserIdAndStatus() {
        // mock 数据
        OaMailAccountDO own = randomMailAccountDO("1").setStatus(0);
        mailAccountMapper.insert(own);
        mailAccountMapper.insert(randomMailAccountDO("2"));
        mailAccountMapper.insert(randomMailAccountDO("1").setStatus(1));

        // 调用
        List<OaMailAccountDO> accounts = mailAccountService.getMailAccountListByUserIdAndStatus(1L, 0);
        // 断言
        assertEquals(1, accounts.size());
        assertEquals(Arrays.asList(own.getId()), convertList(accounts, OaMailAccountDO::getId));
    }

    @Test
    public void testGetMailAccountListByUserIdAndStatus_allStatuses() {
        // mock 数据
        mailAccountMapper.insert(randomMailAccountDO("1").setStatus(0));
        mailAccountMapper.insert(randomMailAccountDO("1").setStatus(1));
        mailAccountMapper.insert(randomMailAccountDO("2").setStatus(0));

        // 调用
        List<OaMailAccountDO> accounts = mailAccountService.getMailAccountListByUserIdAndStatus(1L, null);
        // 断言：管理列表保留本人停用账号，不包含他人账号
        assertEquals(2, accounts.size());
        assertTrue(accounts.stream().allMatch(account -> "1".equals(account.getCreator())));
    }

    @Test
    public void testGetMailAccount_notOwner() {
        // mock 数据
        OaMailAccountDO account = randomMailAccountDO("1");
        mailAccountMapper.insert(account);

        // 调用，并断言异常
        assertServiceException(() -> mailAccountService.validateMailAccount(account.getId(), 2L), MAIL_ACCOUNT_NOT_EXISTS);
    }

    @Test
    public void testUpdateMailAccount_emptyPassword() {
        // mock 数据
        OaMailAccountDO account = randomMailAccountDO("1");
        mailAccountMapper.insert(account);
        // 准备参数
        OaMailAccountSaveReqVO reqVO = new OaMailAccountSaveReqVO().setId(account.getId())
                .setProviderId(account.getProviderId()).setMail(account.getMail()).setUsername(account.getUsername())
                .setPassword("").setDefaultStatus(false).setStatus(0);
        // mock 方法
        when(mailProviderService.validateMailProviderExists(1L)).thenReturn(new OaMailProviderDO().setStatus(0));

        // 调用
        mailAccountService.updateMailAccount(reqVO, 1L);
        // 断言
        assertEquals(account.getPassword(), mailAccountMapper.selectById(account.getId()).getPassword());
        assertEquals("1", mailAccountMapper.selectById(account.getId()).getCreator());
    }

    @Test
    public void testUpdateMailAccountDefault_onlyOwner() {
        // mock 数据
        OaMailAccountDO first = randomMailAccountDO("1").setDefaultStatus(true);
        OaMailAccountDO second = randomMailAccountDO("1");
        OaMailAccountDO other = randomMailAccountDO("2").setDefaultStatus(true);
        mailAccountMapper.insert(first);
        mailAccountMapper.insert(second);
        mailAccountMapper.insert(other);
        // mock 方法
        when(mailProviderService.validateMailProviderExists(second.getProviderId()))
                .thenReturn(new OaMailProviderDO().setStatus(0));

        // 调用
        mailAccountService.updateMailAccountDefault(second.getId(), 1L);
        // 断言
        assertFalse(mailAccountMapper.selectById(first.getId()).getDefaultStatus());
        assertTrue(mailAccountMapper.selectById(second.getId()).getDefaultStatus());
        assertTrue(mailAccountMapper.selectById(other.getId()).getDefaultStatus());
    }

    @Test
    public void testCreateMailAccount_passwordAndAudit() {
        // 准备参数
        SecurityFrameworkUtils.setLoginUser(new LoginUser().setId(1L), new MockHttpServletRequest());
        OaMailAccountSaveReqVO reqVO = new OaMailAccountSaveReqVO().setProviderId(1L)
                .setMail("test@example.com").setUsername("test@example.com").setPassword("test-input")
                .setDefaultStatus(true).setStatus(0);
        // mock 方法
        when(mailProviderService.validateMailProviderExists(1L)).thenReturn(new OaMailProviderDO().setStatus(0));

        // 调用
        Long id = mailAccountService.createMailAccount(reqVO, 1L);
        // 断言
        OaMailAccountDO account = mailAccountMapper.selectById(id);
        assertEquals("1", account.getCreator());
        assertEquals("test-input", account.getPassword());
    }

    @Test
    public void testDeleteMailAccount_notOwner() {
        // mock 数据
        OaMailAccountDO account = randomMailAccountDO("1");
        mailAccountMapper.insert(account);

        // 调用，并断言异常
        assertServiceException(() -> mailAccountService.deleteMailAccount(account.getId(), 2L), MAIL_ACCOUNT_NOT_EXISTS);
        assertNotNull(mailAccountMapper.selectById(account.getId()));
    }

    @Test
    public void testConnection_disabledAccount() {
        // mock 数据
        OaMailAccountDO account = randomMailAccountDO("1").setStatus(1);
        mailAccountMapper.insert(account);

        // 调用，并断言异常
        assertServiceException(() -> mailAccountService.testMailAccountConnection(account.getId(), 1L), MAIL_ACCOUNT_DISABLED);
        verifyNoInteractions(mailMessageClient);
    }

    // ========== 随机对象 ==========
    /**
     * 构造已启用且非默认的测试邮箱账号。
     *
     * @param creator 创建人编号字符串
     * @return 未入库的测试对象
     */
    private static OaMailAccountDO randomMailAccountDO(String creator) {
        return randomPojo(OaMailAccountDO.class, account -> account.setId(null)
                .setMail("test@example.com").setUsername("test@example.com").setPassword("test-password")
                .setProviderId(1L).setStatus(0).setDefaultStatus(false).setCreator(creator));
    }
}
