package cn.iocoder.dashboard.modules.system.service.user;

import cn.hutool.core.util.RandomUtil;
import cn.iocoder.dashboard.BaseDbUnitTest;
import cn.iocoder.dashboard.common.enums.CommonStatusEnum;
import cn.iocoder.dashboard.modules.infra.service.file.InfFileService;
import cn.iocoder.dashboard.modules.system.controller.user.vo.profile.SysUserProfileUpdatePasswordReqVO;
import cn.iocoder.dashboard.modules.system.controller.user.vo.profile.SysUserProfileUpdateReqVO;
import cn.iocoder.dashboard.modules.system.controller.user.vo.user.SysUserCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.user.vo.user.SysUserImportExcelVO;
import cn.iocoder.dashboard.modules.system.controller.user.vo.user.SysUserImportRespVO;
import cn.iocoder.dashboard.modules.system.controller.user.vo.user.SysUserUpdateReqVO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.dept.SysDeptDO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.dept.SysPostDO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.user.SysUserMapper;
import cn.iocoder.dashboard.modules.system.enums.common.SysSexEnum;
import cn.iocoder.dashboard.modules.system.service.dept.SysDeptService;
import cn.iocoder.dashboard.modules.system.service.dept.SysPostService;
import cn.iocoder.dashboard.modules.system.service.permission.SysPermissionService;
import cn.iocoder.dashboard.util.collection.ArrayUtils;
import cn.iocoder.dashboard.util.collection.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static cn.hutool.core.util.RandomUtil.randomBytes;
import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.dashboard.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.dashboard.util.RandomUtils.*;
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
    private InfFileService fileService;

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
    public void testDeleteUser(){
        // mock 数据
        SysUserDO dbUser = randomSysUserDO();
        userMapper.insert(dbUser);
        // 准备参数
        Long userId = dbUser.getId();

        // 调用数据
        userService.deleteUser(userId);
        // 校验结果
        assertNull(userService.getUser(userId));
        // 校验调用次数
        verify(permissionService, times(1)).processUserDeleted(eq(userId));
    }



    @Test
    public void test_importUsers(){
        SysDeptDO dept = randomPojo(SysDeptDO.class, o -> { // 等会查询到
            o.setName("开发部");
            o.setSort(1);
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
//        int depId = deptMapper.insert(dept);
        int depId = 0;
        // 准备参数
        List<SysUserImportExcelVO> list = new ArrayList<>();
        list.add(randomPojo(SysUserImportExcelVO.class, o->{
            o.setDeptId(dept.getId());
            o.setSex(1);
            o.setStatus(1);
        }));
        list.add(randomPojo(SysUserImportExcelVO.class, o->{
            o.setDeptId(dept.getId());
            o.setSex(1);
            o.setStatus(1);
        }));
        list.add(randomPojo(SysUserImportExcelVO.class, o->{
            o.setDeptId(dept.getId());
            o.setSex(1);
            o.setStatus(1);
        }));
        // 批量插入
        SysUserImportRespVO respVO = userService.importUsers(list,false);
        System.out.println(respVO.getCreateUsernames().size());
        // 校验结果
        assertEquals(respVO.getCreateUsernames().size(),3);
        // 批量更新
        list.get(0).setSex(0);
        SysUserImportRespVO respVOUpdate = userService.importUsers(list,true);
        System.out.println(respVOUpdate.getUpdateUsernames().size());
        // 校验结果
        assertEquals(respVOUpdate.getUpdateUsernames().size(),3);
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
