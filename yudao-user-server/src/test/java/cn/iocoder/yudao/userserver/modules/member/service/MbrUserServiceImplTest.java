package cn.iocoder.yudao.userserver.modules.member.service;

import cn.iocoder.yudao.coreservice.modules.infra.service.file.InfFileCoreService;
import cn.iocoder.yudao.coreservice.modules.member.dal.dataobject.user.MbrUserDO;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.userserver.BaseDbUnitTest;
import cn.iocoder.yudao.userserver.modules.member.controller.user.vo.MbrUserInfoRespVO;
import cn.iocoder.yudao.userserver.modules.member.dal.mysql.user.MbrUserMapper;
import cn.iocoder.yudao.userserver.modules.member.service.user.impl.MbrUserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.io.*;
import java.util.function.Consumer;

import static cn.hutool.core.util.RandomUtil.randomBytes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static org.mockito.Mockito.*;
/**
 * {@link MbrUserServiceImpl} 的单元测试类
 *
 * @author 宋天
 */
@Import(MbrUserServiceImpl.class)
public class MbrUserServiceImplTest extends BaseDbUnitTest {

    @Resource
    private MbrUserServiceImpl mbrUserService;

    @Resource
    private MbrUserMapper userMapper;

    @MockBean
    private InfFileCoreService fileCoreService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    public void testUpdateNickName_success(){
        // mock 数据
        MbrUserDO userDO = randomMbrUserDO();
        userMapper.insert(userDO);

        // 随机昵称
        String newNickName = randomString();

        // 调用接口修改昵称
        mbrUserService.updateNickname(userDO.getId(),newNickName);
        // 查询新修改后的昵称
        String nickname = mbrUserService.getUser(userDO.getId()).getNickname();
        // 断言
        assertEquals(newNickName,nickname);
    }

    @Test
    public void testGetUserInfo_success(){
        // mock 数据
        MbrUserDO userDO = randomMbrUserDO();
        userMapper.insert(userDO);

        // 查询用户昵称与头像
        MbrUserInfoRespVO userInfo = mbrUserService.getUserInfo(userDO.getId());
        System.out.println(userInfo);
    }

    @Test
    public void testUpdateAvatar_success(){
        // mock 数据
        MbrUserDO dbUser = randomMbrUserDO();
        userMapper.insert(dbUser);

        // 准备参数
        Long userId = dbUser.getId();
        byte[] avatarFileBytes = randomBytes(10);
        ByteArrayInputStream avatarFile = new ByteArrayInputStream(avatarFileBytes);
        // mock 方法
        String avatar = randomString();
        when(fileCoreService.createFile(anyString(), eq(avatarFileBytes))).thenReturn(avatar);
        // 调用
        String str = mbrUserService.updateAvatar(userId, avatarFile);
        // 断言
        assertEquals(avatar, str);
    }

    // ========== 随机对象 ==========

    @SafeVarargs
    private static MbrUserDO randomMbrUserDO(Consumer<MbrUserDO>... consumers) {
        Consumer<MbrUserDO> consumer = (o) -> {
            o.setStatus(randomEle(CommonStatusEnum.values()).getStatus()); // 保证 status 的范围
        };
        return randomPojo(MbrUserDO.class, ArrayUtils.append(consumer, consumers));
    }

}
