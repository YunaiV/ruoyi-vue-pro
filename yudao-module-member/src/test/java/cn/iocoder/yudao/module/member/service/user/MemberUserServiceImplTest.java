package cn.iocoder.yudao.module.member.service.user;

import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.redis.config.YudaoRedisAutoConfiguration;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbAndRedisUnitTest;
import cn.iocoder.yudao.module.member.controller.app.user.vo.AppMemberUserUpdateMobileReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import cn.iocoder.yudao.module.member.dal.mysql.user.MemberUserMapper;
import cn.iocoder.yudao.module.member.service.auth.MemberAuthServiceImpl;
import cn.iocoder.yudao.module.system.api.sms.SmsCodeApi;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;
import java.util.function.Consumer;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.hutool.core.util.RandomUtil.randomNumbers;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;

// TODO @芋艿：单测的 review，等逻辑都达成一致后
/**
 * {@link MemberUserServiceImpl} 的单元测试类
 *
 * @author 宋天
 */
@Disabled
@Import({MemberUserServiceImpl.class, YudaoRedisAutoConfiguration.class})
public class MemberUserServiceImplTest extends BaseDbAndRedisUnitTest {

    @Resource
    private MemberUserServiceImpl memberUserService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private MemberUserMapper userMapper;

    @MockBean
    private MemberAuthServiceImpl authService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private SmsCodeApi smsCodeApi;

    // TODO 芋艿：后续重构这个单测
//    @Test
//    public void testUpdateNickName_success(){
//        // mock 数据
//        MemberUserDO userDO = randomUserDO();
//        userMapper.insert(userDO);
//
//        // 随机昵称
//        String newNickName = randomString();
//
//        // 调用接口修改昵称
//        memberUserService.updateUser(userDO.getId(),newNickName);
//        // 查询新修改后的昵称
//        String nickname = memberUserService.getUser(userDO.getId()).getNickname();
//        // 断言
//        assertEquals(newNickName,nickname);
//    }

    @Test
    @Disabled // TODO 芋艿：后续再修复
    public void updateMobile_success(){
        // mock数据
        String oldMobile = randomNumbers(11);
        MemberUserDO userDO = randomUserDO();
        userDO.setMobile(oldMobile);
        userMapper.insert(userDO);

        // TODO 芋艿：需要修复该单元测试，重构多模块带来的
        // 旧手机和旧验证码
//        SmsCodeDO codeDO = new SmsCodeDO();
        String oldCode = RandomUtil.randomString(4);
//        codeDO.setMobile(userDO.getMobile());
//        codeDO.setCode(oldCode);
//        codeDO.setScene(SmsSceneEnum.MEMBER_UPDATE_MOBILE.getScene());
//        codeDO.setUsed(Boolean.FALSE);
//        when(smsCodeService.checkCodeIsExpired(codeDO.getMobile(),codeDO.getCode(),codeDO.getScene())).thenReturn(codeDO);

        // 更新手机号
        String newMobile = randomNumbers(11);
        String newCode = randomNumbers(4);
        AppMemberUserUpdateMobileReqVO reqVO = new AppMemberUserUpdateMobileReqVO();
        reqVO.setMobile(newMobile);
        reqVO.setCode(newCode);
        reqVO.setOldCode(oldCode);
        memberUserService.updateUserMobile(userDO.getId(),reqVO);

        assertEquals(memberUserService.getUser(userDO.getId()).getMobile(),newMobile);
    }

    // ========== 随机对象 ==========

    @SafeVarargs
    private static MemberUserDO randomUserDO(Consumer<MemberUserDO>... consumers) {
        Consumer<MemberUserDO> consumer = (o) -> {
            o.setStatus(randomEle(CommonStatusEnum.values()).getStatus()); // 保证 status 的范围
        };
        return randomPojo(MemberUserDO.class, ArrayUtils.append(consumer, consumers));
    }

}
