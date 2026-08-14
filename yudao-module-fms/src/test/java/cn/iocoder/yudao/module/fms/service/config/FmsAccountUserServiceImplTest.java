package cn.iocoder.yudao.module.fms.service.config;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.accountuser.FmsAccountUserUpdateReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountUserDO;
import cn.iocoder.yudao.module.fms.dal.mysql.config.FmsAccountUserMapper;
import cn.iocoder.yudao.module.fms.enums.config.FmsAccountUserLevelEnum;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Import(FmsAccountUserServiceImpl.class)
public class FmsAccountUserServiceImplTest extends BaseDbUnitTest {

    @Resource
    private FmsAccountUserServiceImpl accountUserService;
    @Resource
    private FmsAccountUserMapper accountUserMapper;

    @MockBean
    private FmsAccountSetService accountSetService;
    @MockBean
    private AdminUserApi adminUserApi;

    @Test
    public void testCreateAccountOwner_firstAccountSet() {
        // 准备参数
        Long accountSetId = randomLongId();
        Long userId = randomLongId();

        // 调用
        accountUserService.createAccountOwner(accountSetId, userId);

        // 断言
        FmsAccountUserDO accountUser = accountUserMapper.selectByAccountSetIdAndUserId(accountSetId, userId);
        assertTrue(accountUser.getDefaultStatus());
        assertTrue(accountUser.getFounder());
        assertEquals(FmsAccountUserLevelEnum.OWNER.getLevel(), accountUser.getLevel());
    }

    @Test
    public void testGetAccountUserList_founderFirst() {
        // mock 数据
        Long accountSetId = randomLongId();
        Long userId = randomLongId();
        Long founderUserId = randomLongId();
        accountUserMapper.insert(new FmsAccountUserDO().setAccountSetId(accountSetId).setUserId(userId)
                .setDefaultStatus(false).setFounder(false)
                .setLevel(FmsAccountUserLevelEnum.READ.getLevel()));
        accountUserMapper.insert(new FmsAccountUserDO().setAccountSetId(accountSetId).setUserId(founderUserId)
                .setDefaultStatus(true).setFounder(true)
                .setLevel(FmsAccountUserLevelEnum.OWNER.getLevel()));

        // 调用
        List<FmsAccountUserDO> accountUsers = accountUserService.getAccountUserList(accountSetId, userId);

        // 断言
        assertEquals(2, accountUsers.size());
        assertEquals(founderUserId, accountUsers.get(0).getUserId());
        assertTrue(accountUsers.get(0).getFounder());
        verify(accountSetService).validateAccountSetReadPermission(accountSetId, userId);
    }

    @Test
    public void testGetAccountUser_notExists() {
        // 调用，并断言
        assertNull(accountUserService.getAccountUser(randomLongId(), randomLongId()));
    }

    @Test
    public void testUpdateAccountUserList_replaceAndRejoin() {
        // mock 数据
        Long accountSetId = randomLongId();
        Long founderUserId = randomLongId();
        Long oldMemberUserId = randomLongId();
        Long newMemberUserId = randomLongId();
        when(accountSetService.validateAccountSetOwnerPermission(accountSetId, founderUserId))
                .thenReturn(new FmsAccountSetDO().setId(accountSetId));
        accountUserMapper.insert(new FmsAccountUserDO().setAccountSetId(accountSetId)
                .setUserId(founderUserId).setDefaultStatus(true).setFounder(true)
                .setLevel(FmsAccountUserLevelEnum.OWNER.getLevel()));
        accountUserMapper.insert(new FmsAccountUserDO().setAccountSetId(accountSetId)
                .setUserId(oldMemberUserId).setDefaultStatus(false).setFounder(false)
                .setLevel(FmsAccountUserLevelEnum.READ.getLevel()));
        // 准备参数
        FmsAccountUserUpdateReqVO reqVO = new FmsAccountUserUpdateReqVO().setAccountSetId(accountSetId)
                .setMembers(Arrays.asList(
                        buildAccountUserMember(founderUserId, FmsAccountUserLevelEnum.OWNER),
                        buildAccountUserMember(oldMemberUserId, FmsAccountUserLevelEnum.WRITE),
                        buildAccountUserMember(newMemberUserId, FmsAccountUserLevelEnum.READ)));

        // 调用
        accountUserService.updateAccountUserList(reqVO, founderUserId);

        // 断言首次替换
        assertEquals(FmsAccountUserLevelEnum.WRITE.getLevel(), accountUserMapper
                .selectByAccountSetIdAndUserId(accountSetId, oldMemberUserId).getLevel());
        FmsAccountUserDO newMember = accountUserMapper.selectByAccountSetIdAndUserId(
                accountSetId, newMemberUserId);
        assertNotNull(newMember);
        assertFalse(newMember.getDefaultStatus());
        assertFalse(newMember.getFounder());
        assertEquals(FmsAccountUserLevelEnum.READ.getLevel(), newMember.getLevel());
        verify(accountSetService).validateAccountSetOwnerPermission(accountSetId, founderUserId);
        verify(adminUserApi).validateUserList(new HashSet<>(Arrays.asList(oldMemberUserId, newMemberUserId)));

        // 调用，并断言被移出的成员可以再次加入
        reqVO.setMembers(Arrays.asList(
                buildAccountUserMember(founderUserId, FmsAccountUserLevelEnum.OWNER),
                buildAccountUserMember(oldMemberUserId, FmsAccountUserLevelEnum.READ)));
        accountUserService.updateAccountUserList(reqVO, founderUserId);
        assertEquals(FmsAccountUserLevelEnum.READ.getLevel(), accountUserMapper
                .selectByAccountSetIdAndUserId(accountSetId, oldMemberUserId).getLevel());
        assertNull(accountUserMapper.selectByAccountSetIdAndUserId(accountSetId, newMemberUserId));

        // 调用，并断言逻辑删除的成员可以再次加入
        reqVO.setMembers(Arrays.asList(
                buildAccountUserMember(founderUserId, FmsAccountUserLevelEnum.OWNER),
                buildAccountUserMember(oldMemberUserId, FmsAccountUserLevelEnum.READ),
                buildAccountUserMember(newMemberUserId, FmsAccountUserLevelEnum.WRITE)));
        accountUserService.updateAccountUserList(reqVO, founderUserId);
        assertEquals(FmsAccountUserLevelEnum.WRITE.getLevel(), accountUserMapper
                .selectByAccountSetIdAndUserId(accountSetId, newMemberUserId).getLevel());
    }

    // ========== 随机对象 ==========

    private FmsAccountUserUpdateReqVO.Member buildAccountUserMember(
            Long userId, FmsAccountUserLevelEnum level) {
        return new FmsAccountUserUpdateReqVO.Member().setUserId(userId).setLevel(level.getLevel());
    }

}
