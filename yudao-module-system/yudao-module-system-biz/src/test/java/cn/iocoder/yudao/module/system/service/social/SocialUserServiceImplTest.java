package cn.iocoder.yudao.module.system.service.social;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.system.api.social.dto.SocialUserBindReqDTO;
import cn.iocoder.yudao.module.system.api.social.dto.SocialUserRespDTO;
import cn.iocoder.yudao.module.system.controller.admin.socail.vo.user.SocialUserPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.social.SocialUserBindDO;
import cn.iocoder.yudao.module.system.dal.dataobject.social.SocialUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.social.SocialUserBindMapper;
import cn.iocoder.yudao.module.system.dal.mysql.social.SocialUserMapper;
import cn.iocoder.yudao.module.system.enums.social.SocialTypeEnum;
import com.xingyuv.jushauth.model.AuthUser;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.hutool.core.util.RandomUtil.randomLong;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.SOCIAL_USER_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

/**
 * {@link SocialUserServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(SocialUserServiceImpl.class)
public class SocialUserServiceImplTest extends BaseDbUnitTest {

    @Resource
    private SocialUserServiceImpl socialUserService;

    @Resource
    private SocialUserMapper socialUserMapper;
    @Resource
    private SocialUserBindMapper socialUserBindMapper;

    @MockBean
    private SocialClientService socialClientService;

    @Test
    public void testGetSocialUserList() {
        Long userId = 1L;
        Integer userType = UserTypeEnum.ADMIN.getValue();
        // mock 获得社交用户
        SocialUserDO socialUser = randomPojo(SocialUserDO.class).setType(SocialTypeEnum.GITEE.getType());
        socialUserMapper.insert(socialUser); // 可被查到
        socialUserMapper.insert(randomPojo(SocialUserDO.class)); // 不可被查到
        // mock 获得绑定
        socialUserBindMapper.insert(randomPojo(SocialUserBindDO.class) // 可被查询到
                .setUserId(userId).setUserType(userType).setSocialType(SocialTypeEnum.GITEE.getType())
                .setSocialUserId(socialUser.getId()));
        socialUserBindMapper.insert(randomPojo(SocialUserBindDO.class) // 不可被查询到
                .setUserId(2L).setUserType(userType).setSocialType(SocialTypeEnum.DINGTALK.getType()));

        // 调用
        List<SocialUserDO> result = socialUserService.getSocialUserList(userId, userType);
        // 断言
        assertEquals(1, result.size());
        assertPojoEquals(socialUser, result.get(0));
    }

    @Test
    public void testBindSocialUser() {
        // 准备参数
        SocialUserBindReqDTO reqDTO = new SocialUserBindReqDTO()
                .setUserId(1L).setUserType(UserTypeEnum.ADMIN.getValue())
                .setSocialType(SocialTypeEnum.GITEE.getType()).setCode("test_code").setState("test_state");
        // mock 数据：获得社交用户
        SocialUserDO socialUser = randomPojo(SocialUserDO.class).setType(reqDTO.getSocialType())
                .setCode(reqDTO.getCode()).setState(reqDTO.getState());
        socialUserMapper.insert(socialUser);
        // mock 数据：用户可能之前已经绑定过该社交类型
        socialUserBindMapper.insert(randomPojo(SocialUserBindDO.class).setUserId(1L).setUserType(UserTypeEnum.ADMIN.getValue())
                .setSocialType(SocialTypeEnum.GITEE.getType()).setSocialUserId(-1L));
        // mock 数据：社交用户可能之前绑定过别的用户
        socialUserBindMapper.insert(randomPojo(SocialUserBindDO.class).setUserType(UserTypeEnum.ADMIN.getValue())
                .setSocialType(SocialTypeEnum.GITEE.getType()).setSocialUserId(socialUser.getId()));

        // 调用
        String openid = socialUserService.bindSocialUser(reqDTO);
        // 断言
        List<SocialUserBindDO> socialUserBinds = socialUserBindMapper.selectList();
        assertEquals(1, socialUserBinds.size());
        assertEquals(socialUser.getOpenid(), openid);
    }

    @Test
    public void testUnbindSocialUser_success() {
        // 准备参数
        Long userId = 1L;
        Integer userType = UserTypeEnum.ADMIN.getValue();
        Integer type = SocialTypeEnum.GITEE.getType();
        String openid = "test_openid";
        // mock 数据：社交用户
        SocialUserDO socialUser = randomPojo(SocialUserDO.class).setType(type).setOpenid(openid);
        socialUserMapper.insert(socialUser);
        // mock 数据：社交绑定关系
        SocialUserBindDO socialUserBind = randomPojo(SocialUserBindDO.class).setUserType(userType)
                .setUserId(userId).setSocialType(type);
        socialUserBindMapper.insert(socialUserBind);

        // 调用
        socialUserService.unbindSocialUser(userId, userType, type, openid);
        // 断言
        assertEquals(0, socialUserBindMapper.selectCount(null).intValue());
    }

    @Test
    public void testUnbindSocialUser_notFound() {
        // 调用，并断言
        assertServiceException(
                () -> socialUserService.unbindSocialUser(randomLong(), UserTypeEnum.ADMIN.getValue(),
                        SocialTypeEnum.GITEE.getType(), "test_openid"),
                SOCIAL_USER_NOT_FOUND);
    }

    @Test
    public void testGetSocialUser() {
        // 准备参数
        Integer userType = UserTypeEnum.ADMIN.getValue();
        Integer type = SocialTypeEnum.GITEE.getType();
        String code = "tudou";
        String state = "yuanma";
        // mock 社交用户
        SocialUserDO socialUserDO = randomPojo(SocialUserDO.class).setType(type).setCode(code).setState(state);
        socialUserMapper.insert(socialUserDO);
        // mock 社交用户的绑定
        Long userId = randomLong();
        SocialUserBindDO socialUserBind = randomPojo(SocialUserBindDO.class).setUserType(userType).setUserId(userId)
                .setSocialType(type).setSocialUserId(socialUserDO.getId());
        socialUserBindMapper.insert(socialUserBind);

        // 调用
        SocialUserRespDTO socialUser = socialUserService.getSocialUserByCode(userType, type, code, state);
        // 断言
        assertEquals(userId, socialUser.getUserId());
        assertEquals(socialUserDO.getOpenid(), socialUser.getOpenid());
    }

    @Test
    public void testAuthSocialUser_exists() {
        // 准备参数
        Integer socialType = SocialTypeEnum.GITEE.getType();
        Integer userType = randomEle(SocialTypeEnum.values()).getType();
        String code = "tudou";
        String state = "yuanma";
        // mock 方法
        SocialUserDO socialUser = randomPojo(SocialUserDO.class).setType(socialType).setCode(code).setState(state);
        socialUserMapper.insert(socialUser);

        // 调用
        SocialUserDO result = socialUserService.authSocialUser(socialType, userType, code, state);
        // 断言
        assertPojoEquals(socialUser, result);
    }

    @Test
    public void testAuthSocialUser_notNull() {
        // mock 数据
        SocialUserDO socialUser = randomPojo(SocialUserDO.class,
                o -> o.setType(SocialTypeEnum.GITEE.getType()).setCode("tudou").setState("yuanma"));
        socialUserMapper.insert(socialUser);
        // 准备参数
        Integer socialType = SocialTypeEnum.GITEE.getType();
        Integer userType = randomEle(SocialTypeEnum.values()).getType();
        String code = "tudou";
        String state = "yuanma";

        // 调用
        SocialUserDO result = socialUserService.authSocialUser(socialType, userType, code, state);
        // 断言
        assertPojoEquals(socialUser, result);
    }

    @Test
    public void testAuthSocialUser_insert() {
        // 准备参数
        Integer socialType = SocialTypeEnum.GITEE.getType();
        Integer userType = randomEle(SocialTypeEnum.values()).getType();
        String code = "tudou";
        String state = "yuanma";
        // mock 方法
        AuthUser authUser = randomPojo(AuthUser.class);
        when(socialClientService.getAuthUser(eq(socialType), eq(userType), eq(code), eq(state))).thenReturn(authUser);

        // 调用
        SocialUserDO result = socialUserService.authSocialUser(socialType, userType, code, state);
        // 断言
        assertBindSocialUser(socialType, result, authUser);
        assertEquals(code, result.getCode());
        assertEquals(state, result.getState());
    }

    @Test
    public void testAuthSocialUser_update() {
        // 准备参数
        Integer socialType = SocialTypeEnum.GITEE.getType();
        Integer userType = randomEle(SocialTypeEnum.values()).getType();
        String code = "tudou";
        String state = "yuanma";
        // mock 数据
        socialUserMapper.insert(randomPojo(SocialUserDO.class).setType(socialType).setOpenid("test_openid"));
        // mock 方法
        AuthUser authUser = randomPojo(AuthUser.class);
        when(socialClientService.getAuthUser(eq(socialType), eq(userType), eq(code), eq(state))).thenReturn(authUser);

        // 调用
        SocialUserDO result = socialUserService.authSocialUser(socialType, userType, code, state);
        // 断言
        assertBindSocialUser(socialType, result, authUser);
        assertEquals(code, result.getCode());
        assertEquals(state, result.getState());
    }

    private void assertBindSocialUser(Integer type, SocialUserDO socialUser, AuthUser authUser) {
        assertEquals(authUser.getToken().getAccessToken(), socialUser.getToken());
        assertEquals(toJsonString(authUser.getToken()), socialUser.getRawTokenInfo());
        assertEquals(authUser.getNickname(), socialUser.getNickname());
        assertEquals(authUser.getAvatar(), socialUser.getAvatar());
        assertEquals(toJsonString(authUser.getRawUserInfo()), socialUser.getRawUserInfo());
        assertEquals(type, socialUser.getType());
        assertEquals(authUser.getUuid(), socialUser.getOpenid());
    }

    @Test
    public void testGetSocialUser_id() {
        // mock 数据
        SocialUserDO socialUserDO = randomPojo(SocialUserDO.class);
        socialUserMapper.insert(socialUserDO);
        // 参数准备
        Long id = socialUserDO.getId();

        // 调用
        SocialUserDO dbSocialUserDO = socialUserService.getSocialUser(id);
        // 断言
        assertPojoEquals(socialUserDO, dbSocialUserDO);
    }

    @Test
    public void testGetSocialUserPage() {
        // mock 数据
        SocialUserDO dbSocialUser = randomPojo(SocialUserDO.class, o -> { // 等会查询到
            o.setType(SocialTypeEnum.GITEE.getType());
            o.setNickname("芋艿");
            o.setOpenid("yudaoyuanma");
            o.setCreateTime(buildTime(2020, 1, 15));
        });
        socialUserMapper.insert(dbSocialUser);
        // 测试 type 不匹配
        socialUserMapper.insert(cloneIgnoreId(dbSocialUser, o -> o.setType(SocialTypeEnum.DINGTALK.getType())));
        // 测试 nickname 不匹配
        socialUserMapper.insert(cloneIgnoreId(dbSocialUser, o -> o.setNickname(randomString())));
        // 测试 openid 不匹配
        socialUserMapper.insert(cloneIgnoreId(dbSocialUser, o -> o.setOpenid("java")));
        // 测试 createTime 不匹配
        socialUserMapper.insert(cloneIgnoreId(dbSocialUser, o -> o.setCreateTime(buildTime(2020, 1, 21))));
        // 准备参数
        SocialUserPageReqVO reqVO = new SocialUserPageReqVO();
        reqVO.setType(SocialTypeEnum.GITEE.getType());
        reqVO.setNickname("芋");
        reqVO.setOpenid("yudao");
        reqVO.setCreateTime(buildBetweenTime(2020, 1, 10, 2020, 1, 20));

        // 调用
        PageResult<SocialUserDO> pageResult = socialUserService.getSocialUserPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbSocialUser, pageResult.getList().get(0));
    }

}
