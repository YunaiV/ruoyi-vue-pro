package cn.iocoder.yudao.adminserver.modules.system.service.user;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.adminserver.BaseDbUnitTest;
import cn.iocoder.yudao.adminserver.modules.system.controller.user.vo.profile.SysUserProfileUpdatePasswordReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.user.vo.profile.SysUserProfileUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.user.vo.user.*;
import cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.dept.SysDeptDO;
import cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.dept.SysPostDO;
import cn.iocoder.yudao.adminserver.modules.system.dal.mysql.user.SysUserMapper;
import cn.iocoder.yudao.adminserver.modules.system.service.dept.SysDeptService;
import cn.iocoder.yudao.adminserver.modules.system.service.dept.SysPostService;
import cn.iocoder.yudao.adminserver.modules.system.service.permission.SysPermissionService;
import cn.iocoder.yudao.adminserver.modules.system.service.user.impl.SysUserServiceImpl;
import cn.iocoder.yudao.coreservice.modules.infra.service.file.InfFileCoreService;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.yudao.coreservice.modules.system.enums.common.SysSexEnum;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.function.Consumer;

import static cn.hutool.core.util.RandomUtil.randomBytes;
import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.adminserver.modules.system.enums.SysErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.common.util.collection.SetUtils.asSet;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.buildTime;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.util.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link SysUserService} 的单元测试类
 *
 * @author zxl
 */
@Import(SysUserServiceImpl.class)
public class SysUserServiceImplTest extends BaseDbUnitTest {

    @Resource
    private SysUserServiceImpl userService;

    @Resource
    private SysUserMapper userMapper;

    @MockBean
    private SysDeptService deptService;
    @MockBean
    private SysPostService postService;
    @MockBean
    private SysPermissionService permissionService;
    @MockBean
    private PasswordEncoder passwordEncoder;
    @MockBean
    private InfFileCoreService fileService;

    @Test
    public void testCreatUser_success() {
        // 准备参数
        SysUserCreateReqVO reqVO = randomPojo(SysUserCreateReqVO.class, o -> {
            o.setSex(RandomUtil.randomEle(SysSexEnum.values()).getSex());
            o.setMobile(randomString());
        });
        // mock deptService 的方法
        SysDeptDO dept = randomPojo(SysDeptDO.class, o -> {
            o.setId(reqVO.getDeptId());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        when(deptService.getDept(eq(dept.getId()))).thenReturn(dept);
        // mock postService 的方法
        List<SysPostDO> posts = CollectionUtils.convertList(reqVO.getPostIds(), postId ->
                randomPojo(SysPostDO.class, o -> {
                    o.setId(postId);
                    o.setStatus(CommonStatusEnum.ENABLE.getStatus());
                }));
        when(postService.getPosts(eq(reqVO.getPostIds()), isNull())).thenReturn(posts);
        // mock passwordEncoder 的方法
        when(passwordEncoder.encode(eq(reqVO.getPassword()))).thenReturn("yudaoyuanma");

        // 调用
        Long userId = userService.createUser(reqVO);
        // 断言
        SysUserDO user = userMapper.selectById(userId);
        assertPojoEquals(reqVO, user, "password");
        assertEquals("yudaoyuanma", user.getPassword());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), user.getStatus());
    }

    @Test
    public void testUpdateUser_success() {
        // mock 数据
        SysUserDO dbUser = randomSysUserDO();
        userMapper.insert(dbUser);
        // 准备参数
        SysUserUpdateReqVO reqVO = randomPojo(SysUserUpdateReqVO.class, o -> {
            o.setId(dbUser.getId());
            o.setSex(RandomUtil.randomEle(SysSexEnum.values()).getSex());
            o.setMobile(randomString());
        });
        // mock deptService 的方法
        SysDeptDO dept = randomPojo(SysDeptDO.class, o -> {
            o.setId(reqVO.getDeptId());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        when(deptService.getDept(eq(dept.getId()))).thenReturn(dept);
        // mock postService 的方法
        List<SysPostDO> posts = CollectionUtils.convertList(reqVO.getPostIds(), postId ->
                randomPojo(SysPostDO.class, o -> {
                    o.setId(postId);
                    o.setStatus(CommonStatusEnum.ENABLE.getStatus());
                }));
        when(postService.getPosts(eq(reqVO.getPostIds()), isNull())).thenReturn(posts);

        // 调用
        userService.updateUser(reqVO);
        // 断言
        SysUserDO user = userMapper.selectById(reqVO.getId());
        assertPojoEquals(reqVO, user);
    }

    @Test
    public void testUpdateUserProfile_success() {
        // mock 数据
        SysUserDO dbUser = randomSysUserDO();
        userMapper.insert(dbUser);
        // 准备参数
        Long userId = dbUser.getId();
        SysUserProfileUpdateReqVO reqVO = randomPojo(SysUserProfileUpdateReqVO.class, o -> {
            o.setMobile(randomString());
            o.setSex(RandomUtil.randomEle(SysSexEnum.values()).getSex());
        });

        // 调用
        userService.updateUserProfile(userId, reqVO);
        // 断言
        SysUserDO user = userMapper.selectById(userId);
        assertPojoEquals(reqVO, user);
    }

    @Test
    public void testUpdateUserPassword_success() {
        // mock 数据
        SysUserDO dbUser = randomSysUserDO(o -> o.setPassword("encode:yudao"));
        userMapper.insert(dbUser);
        // 准备参数
        Long userId = dbUser.getId();
        SysUserProfileUpdatePasswordReqVO reqVO = randomPojo(SysUserProfileUpdatePasswordReqVO.class, o -> {
            o.setOldPassword("yudao");
            o.setNewPassword("yuanma");
        });
        // mock 方法
        when(passwordEncoder.encode(anyString())).then(
                (Answer<String>) invocationOnMock -> "encode:" + invocationOnMock.getArgument(0));
        when(passwordEncoder.matches(eq(reqVO.getOldPassword()), eq(dbUser.getPassword()))).thenReturn(true);

        // 调用
        userService.updateUserPassword(userId, reqVO);
        // 断言
        SysUserDO user = userMapper.selectById(userId);
        assertEquals("encode:yuanma", user.getPassword());
    }

    @Test
    public void testUpdateUserAvatar_success() {
        // mock 数据
        SysUserDO dbUser = randomSysUserDO();
        userMapper.insert(dbUser);
        // 准备参数
        Long userId = dbUser.getId();
        byte[] avatarFileBytes = randomBytes(10);
        ByteArrayInputStream avatarFile = new ByteArrayInputStream(avatarFileBytes);
        // mock 方法
        String avatar = randomString();
        when(fileService.createFile(anyString(), eq(avatarFileBytes))).thenReturn(avatar);

        // 调用
        userService.updateUserAvatar(userId, avatarFile);
        // 断言
        SysUserDO user = userMapper.selectById(userId);
        assertEquals(avatar, user.getAvatar());
    }

    @Test
    public void testUpdateUserPassword02_success() {
        // mock 数据
        SysUserDO dbUser = randomSysUserDO();
        userMapper.insert(dbUser);
        // 准备参数
        Long userId = dbUser.getId();
        String password = "yudao";
        // mock 方法
        when(passwordEncoder.encode(anyString())).then(
                (Answer<String>) invocationOnMock -> "encode:" + invocationOnMock.getArgument(0));

        // 调用
        userService.updateUserPassword(userId, password);
        // 断言
        SysUserDO user = userMapper.selectById(userId);
        assertEquals("encode:" + password, user.getPassword());
    }

    @Test
    public void testUpdateUserStatus() {
        // mock 数据
        SysUserDO dbUser = randomSysUserDO();
        userMapper.insert(dbUser);
        // 准备参数
        Long userId = dbUser.getId();
        Integer status = randomCommonStatus();

        // 调用
        userService.updateUserStatus(userId, status);
        // 断言
        SysUserDO user = userMapper.selectById(userId);
        assertEquals(status, user.getStatus());
    }

    @Test
    public void testDeleteUser_success(){
        // mock 数据
        SysUserDO dbUser = randomSysUserDO();
        userMapper.insert(dbUser);
        // 准备参数
        Long userId = dbUser.getId();

        // 调用数据
        userService.deleteUser(userId);
        // 校验结果
        assertNull(userMapper.selectById(userId));
        // 校验调用次数
        verify(permissionService, times(1)).processUserDeleted(eq(userId));
    }

    @Test
    public void testGetUserPage() {
        // mock 数据
        SysUserDO dbUser = initGetUserPageData();
        // 准备参数
        SysUserPageReqVO reqVO = new SysUserPageReqVO();
        reqVO.setUsername("yudao");
        reqVO.setMobile("1560");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setBeginTime(buildTime(2020, 12, 1));
        reqVO.setEndTime(buildTime(2020, 12, 24));
        reqVO.setDeptId(1L); // 其中，1L 是 2L 的父部门
        // mock 方法
        List<SysDeptDO> deptList = newArrayList(randomPojo(SysDeptDO.class, o -> o.setId(2L)));
        when(deptService.getDeptsByParentIdFromCache(eq(reqVO.getDeptId()), eq(true))).thenReturn(deptList);

        // 调用
        PageResult<SysUserDO> pageResult = userService.getUserPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbUser, pageResult.getList().get(0));
    }

    @Test
    public void testGetUsers() {
        // mock 数据
        SysUserDO dbUser = initGetUserPageData();
        // 准备参数
        SysUserExportReqVO reqVO = new SysUserExportReqVO();
        reqVO.setUsername("yudao");
        reqVO.setMobile("1560");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setBeginTime(buildTime(2020, 12, 1));
        reqVO.setEndTime(buildTime(2020, 12, 24));
        reqVO.setDeptId(1L); // 其中，1L 是 2L 的父部门
        // mock 方法
        List<SysDeptDO> deptList = newArrayList(randomPojo(SysDeptDO.class, o -> o.setId(2L)));
        when(deptService.getDeptsByParentIdFromCache(eq(reqVO.getDeptId()), eq(true))).thenReturn(deptList);

        // 调用
        List<SysUserDO> list = userService.getUsers(reqVO);
        // 断言
        assertEquals(1, list.size());
        assertPojoEquals(dbUser, list.get(0));
    }

    /**
     * 初始化 getUserPage 方法的测试数据
     */
    private SysUserDO initGetUserPageData() {
        // mock 数据
        SysUserDO dbUser = randomSysUserDO(o -> { // 等会查询到
            o.setUsername("yudaoyuanma");
            o.setMobile("15601691300");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setCreateTime(buildTime(2020, 12, 12));
            o.setDeptId(2L);
        });
        userMapper.insert(dbUser);
        // 测试 username 不匹配
        userMapper.insert(ObjectUtils.cloneIgnoreId(dbUser, o -> o.setUsername("yuanma")));
        // 测试 mobile 不匹配
        userMapper.insert(ObjectUtils.cloneIgnoreId(dbUser, o -> o.setMobile("18818260888")));
        // 测试 status 不匹配
        userMapper.insert(ObjectUtils.cloneIgnoreId(dbUser, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        // 测试 createTime 不匹配
        userMapper.insert(ObjectUtils.cloneIgnoreId(dbUser, o -> o.setCreateTime(buildTime(2020, 11, 11))));
        // 测试 dept 不匹配
        userMapper.insert(ObjectUtils.cloneIgnoreId(dbUser, o -> o.setDeptId(0L)));
        return dbUser;
    }

    /**
     * 情况一，校验不通过，导致插入失败
     */
    @Test
    public void testImportUsers_01() {
        // 准备参数
        SysUserImportExcelVO importUser = randomPojo(SysUserImportExcelVO.class);
        // mock 方法

        // 调用
        SysUserImportRespVO respVO = userService.importUsers(newArrayList(importUser), true);
        // 断言
        assertEquals(0, respVO.getCreateUsernames().size());
        assertEquals(0, respVO.getUpdateUsernames().size());
        assertEquals(1, respVO.getFailureUsernames().size());
        assertEquals(DEPT_NOT_FOUND.getMsg(), respVO.getFailureUsernames().get(importUser.getUsername()));
    }

    /**
     * 情况二，不存在，进行插入
     */
    @Test
    public void testImportUsers_02() {
        // 准备参数
        SysUserImportExcelVO importUser = randomPojo(SysUserImportExcelVO.class, o -> {
            o.setStatus(randomEle(CommonStatusEnum.values()).getStatus()); // 保证 status 的范围
            o.setSex(randomEle(SysSexEnum.values()).getSex()); // 保证 sex 的范围
        });
        // mock deptService 的方法
        SysDeptDO dept = randomPojo(SysDeptDO.class, o -> {
            o.setId(importUser.getDeptId());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        when(deptService.getDept(eq(dept.getId()))).thenReturn(dept);
        // mock passwordEncoder 的方法
        when(passwordEncoder.encode(eq("yudaoyuanma"))).thenReturn("java");

        // 调用
        SysUserImportRespVO respVO = userService.importUsers(newArrayList(importUser), true);
        // 断言
        assertEquals(1, respVO.getCreateUsernames().size());
        SysUserDO user = userMapper.selectByUsername(respVO.getCreateUsernames().get(0));
        assertPojoEquals(importUser, user);
        assertEquals("java", user.getPassword());
        assertEquals(0, respVO.getUpdateUsernames().size());
        assertEquals(0, respVO.getFailureUsernames().size());
    }

    /**
     * 情况三，存在，但是不强制更新
     */
    @Test
    public void testImportUsers_03() {
        // mock 数据
        SysUserDO dbUser = randomSysUserDO();
        userMapper.insert(dbUser);
        // 准备参数
        SysUserImportExcelVO importUser = randomPojo(SysUserImportExcelVO.class, o -> {
            o.setStatus(randomEle(CommonStatusEnum.values()).getStatus()); // 保证 status 的范围
            o.setSex(randomEle(SysSexEnum.values()).getSex()); // 保证 sex 的范围
            o.setUsername(dbUser.getUsername());
        });
        // mock deptService 的方法
        SysDeptDO dept = randomPojo(SysDeptDO.class, o -> {
            o.setId(importUser.getDeptId());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        when(deptService.getDept(eq(dept.getId()))).thenReturn(dept);

        // 调用
        SysUserImportRespVO respVO = userService.importUsers(newArrayList(importUser), false);
        // 断言
        assertEquals(0, respVO.getCreateUsernames().size());
        assertEquals(0, respVO.getUpdateUsernames().size());
        assertEquals(1, respVO.getFailureUsernames().size());
        assertEquals(USER_USERNAME_EXISTS.getMsg(), respVO.getFailureUsernames().get(importUser.getUsername()));
    }

    /**
     * 情况四，存在，强制更新
     */
    @Test
    public void testImportUsers_04() {
        // mock 数据
        SysUserDO dbUser = randomSysUserDO();
        userMapper.insert(dbUser);
        // 准备参数
        SysUserImportExcelVO importUser = randomPojo(SysUserImportExcelVO.class, o -> {
            o.setStatus(randomEle(CommonStatusEnum.values()).getStatus()); // 保证 status 的范围
            o.setSex(randomEle(SysSexEnum.values()).getSex()); // 保证 sex 的范围
            o.setUsername(dbUser.getUsername());
        });
        // mock deptService 的方法
        SysDeptDO dept = randomPojo(SysDeptDO.class, o -> {
            o.setId(importUser.getDeptId());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        when(deptService.getDept(eq(dept.getId()))).thenReturn(dept);

        // 调用
        SysUserImportRespVO respVO = userService.importUsers(newArrayList(importUser), true);
        // 断言
        assertEquals(0, respVO.getCreateUsernames().size());
        assertEquals(1, respVO.getUpdateUsernames().size());
        SysUserDO user = userMapper.selectByUsername(respVO.getUpdateUsernames().get(0));
        assertPojoEquals(importUser, user);
        assertEquals(0, respVO.getFailureUsernames().size());
    }

    @Test
    public void testCheckUserExists_notExists() {
        assertServiceException(() -> userService.checkUserExists(randomLongId()), USER_NOT_EXISTS);
    }

    @Test
    public void testCheckUsernameUnique_usernameExistsForCreate() {
        // 准备参数
        String username = randomString();
        // mock 数据
        userMapper.insert(randomSysUserDO(o -> o.setUsername(username)));

        // 调用，校验异常
        assertServiceException(() -> userService.checkUsernameUnique(null, username),
                USER_USERNAME_EXISTS);
    }

    @Test
    public void testCheckUsernameUnique_usernameExistsForUpdate() {
        // 准备参数
        Long id = randomLongId();
        String username = randomString();
        // mock 数据
        userMapper.insert(randomSysUserDO(o -> o.setUsername(username)));

        // 调用，校验异常
        assertServiceException(() -> userService.checkUsernameUnique(id, username),
                USER_USERNAME_EXISTS);
    }

    @Test
    public void testCheckEmailUnique_emailExistsForCreate() {
        // 准备参数
        String email = randomString();
        // mock 数据
        userMapper.insert(randomSysUserDO(o -> o.setEmail(email)));

        // 调用，校验异常
        assertServiceException(() -> userService.checkEmailUnique(null, email),
                USER_EMAIL_EXISTS);
    }

    @Test
    public void testCheckEmailUnique_emailExistsForUpdate() {
        // 准备参数
        Long id = randomLongId();
        String email = randomString();
        // mock 数据
        userMapper.insert(randomSysUserDO(o -> o.setEmail(email)));

        // 调用，校验异常
        assertServiceException(() -> userService.checkEmailUnique(id, email),
                USER_EMAIL_EXISTS);
    }

    @Test
    public void testCheckMobileUnique_mobileExistsForCreate() {
        // 准备参数
        String mobile = randomString();
        // mock 数据
        userMapper.insert(randomSysUserDO(o -> o.setMobile(mobile)));

        // 调用，校验异常
        assertServiceException(() -> userService.checkMobileUnique(null, mobile),
                USER_MOBILE_EXISTS);
    }

    @Test
    public void testCheckMobileUnique_mobileExistsForUpdate() {
        // 准备参数
        Long id = randomLongId();
        String mobile = randomString();
        // mock 数据
        userMapper.insert(randomSysUserDO(o -> o.setMobile(mobile)));

        // 调用，校验异常
        assertServiceException(() -> userService.checkMobileUnique(id, mobile),
                USER_MOBILE_EXISTS);
    }

    @Test
    public void testCheckDeptEnable_notFound() {
        assertServiceException(() -> userService.checkDeptEnable(randomLongId()),
                DEPT_NOT_FOUND);
    }

    @Test
    public void testCheckDeptEnable_notEnable() {
        // 准备参数
        Long deptId = randomLongId();
        // mock deptService 的方法
        SysDeptDO dept = randomPojo(SysDeptDO.class, o -> {
            o.setId(deptId);
            o.setStatus(CommonStatusEnum.DISABLE.getStatus());
        });
        when(deptService.getDept(eq(dept.getId()))).thenReturn(dept);

        // 调用，校验异常
        assertServiceException(() -> userService.checkDeptEnable(deptId),
                DEPT_NOT_ENABLE);
    }

    @Test
    public void testCheckPostEnable_notFound() {
        assertServiceException(() -> userService.checkPostEnable(randomSet(Long.class)),
                POST_NOT_FOUND);
    }

    @Test
    public void testCheckPostEnable_notEnable() {
        // 准备参数
        Set<Long> postIds = randomSet(Long.class);
        // mock postService 的方法
        List<SysPostDO> posts = CollectionUtils.convertList(postIds, postId ->
                randomPojo(SysPostDO.class, o -> {
                    o.setId(postId);
                    o.setStatus(CommonStatusEnum.DISABLE.getStatus());
                }));
        when(postService.getPosts(eq(postIds), isNull())).thenReturn(posts);

        // 调用，校验异常
        assertServiceException(() -> userService.checkPostEnable(postIds),
                POST_NOT_ENABLE, CollUtil.getFirst(posts).getName());
    }

    @Test
    public void testCheckOldPassword_notExists() {
        assertServiceException(() -> userService.checkOldPassword(randomLongId(), randomString()),
                USER_NOT_EXISTS);
    }

    @Test
    public void testCheckOldPassword_passwordFailed() {
        // mock 数据
        SysUserDO user = randomSysUserDO();
        userMapper.insert(user);
        // 准备参数
        Long id = user.getId();
        String oldPassword = user.getPassword();

        // 调用，校验异常
        assertServiceException(() -> userService.checkOldPassword(id, oldPassword),
                USER_PASSWORD_FAILED);
        // 校验调用
        verify(passwordEncoder, times(1)).matches(eq(oldPassword), eq(user.getPassword()));
    }

    @Test
    public void testUsersByPostIds() {
        // 准备参数
        Collection<Long> postIds = asSet(10L, 20L);
        // mock 方法
        SysUserDO user1 = randomSysUserDO(o -> o.setPostIds(asSet(10L, 30L)));
        userMapper.insert(user1);
        SysUserDO user2 = randomSysUserDO(o -> o.setPostIds(singleton(100L)));
        userMapper.insert(user2);

        // 调用
        List<SysUserDO> result = userService.getUsersByPostIds(postIds);
        // 断言
        assertEquals(1, result.size());
        assertEquals(user1, result.get(0));
    }

    // ========== 随机对象 ==========

    @SafeVarargs
    private static SysUserDO randomSysUserDO(Consumer<SysUserDO>... consumers) {
        Consumer<SysUserDO> consumer = (o) -> {
            o.setStatus(randomEle(CommonStatusEnum.values()).getStatus()); // 保证 status 的范围
            o.setSex(randomEle(SysSexEnum.values()).getSex()); // 保证 sex 的范围
        };
        return randomPojo(SysUserDO.class, ArrayUtils.append(consumer, consumers));
    }

}
