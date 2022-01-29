package cn.iocoder.yudao.module.member.service.user;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.coreservice.modules.infra.service.file.InfFileCoreService;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.member.controller.app.user.vo.AppUserUpdateMobileReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.sms.SysSmsCodeDO;
import cn.iocoder.yudao.module.member.dal.dataobject.user.UserDO;
import cn.iocoder.yudao.module.member.dal.mysql.user.UserMapper;
import cn.iocoder.yudao.module.member.enums.SysErrorCodeConstants;
import cn.iocoder.yudao.module.member.enums.sms.SysSmsSceneEnum;
import cn.iocoder.yudao.module.member.service.sms.SysSmsCodeService;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.InputStream;
import java.util.Date;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;
import static cn.iocoder.yudao.module.member.enums.MemberErrorCodeConstants.USER_NOT_EXISTS;

/**
 * 会员 User Service 实现类
 *
 * @author 芋道源码
 */
@Service("memberUserService")
@Valid
@Slf4j
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper memberUserMapper;

    @Resource
    private InfFileCoreService fileCoreService;
    @Resource
    private SysSmsCodeService smsCodeService;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDO getUserByMobile(String mobile) {
        return memberUserMapper.selectByMobile(mobile);
    }

    @Override
    public UserDO createUserIfAbsent(String mobile, String registerIp) {
        // 用户已经存在
        UserDO user = memberUserMapper.selectByMobile(mobile);
        if (user != null) {
            return user;
        }
        // 用户不存在，则进行创建
        return this.createUser(mobile, registerIp);
    }

    private UserDO createUser(String mobile, String registerIp) {
        // 生成密码
        String password = IdUtil.fastSimpleUUID();
        // 插入用户
        UserDO user = new UserDO();
        user.setMobile(mobile);
        user.setStatus(CommonStatusEnum.ENABLE.getStatus()); // 默认开启
        user.setPassword(passwordEncoder.encode(password)); // 加密密码
        user.setRegisterIp(registerIp);
        memberUserMapper.insert(user);
        return user;
    }

    @Override
    public void updateUserLogin(Long id, String loginIp) {
        memberUserMapper.updateById(new UserDO().setId(id)
                .setLoginIp(loginIp).setLoginDate(new Date()));
    }

    @Override
    public UserDO getUser(Long id) {
        return memberUserMapper.selectById(id);
    }

    @Override
    public void updateUserNickname(Long userId, String nickname) {
        UserDO user = this.checkUserExists(userId);
        // 仅当新昵称不等于旧昵称时进行修改
        if (nickname.equals(user.getNickname())){
            return;
        }
        UserDO userDO = new UserDO();
        userDO.setId(user.getId());
        userDO.setNickname(nickname);
        memberUserMapper.updateById(userDO);
    }

    @Override
    public String updateUserAvatar(Long userId, InputStream avatarFile) {
        this.checkUserExists(userId);
        // 创建文件
        String avatar = fileCoreService.createFile(IdUtil.fastUUID(), IoUtil.readBytes(avatarFile));
        // 更新头像路径
        memberUserMapper.updateById(UserDO.builder().id(userId).avatar(avatar).build());
        return avatar;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateUserMobile(Long userId, AppUserUpdateMobileReqVO reqVO) {
        // 检测用户是否存在
        checkUserExists(userId);

        // 校验旧手机和旧验证码
        SysSmsCodeDO sysSmsCodeDO = smsCodeService.checkCodeIsExpired(reqVO.getOldMobile(), reqVO.getOldCode(),
                SysSmsSceneEnum.CHANGE_MOBILE_BY_SMS.getScene());
        // 判断旧 code 是否未被使用，如果是，抛出异常
        if (Boolean.FALSE.equals(sysSmsCodeDO.getUsed())){
            throw ServiceExceptionUtil.exception(SysErrorCodeConstants.USER_SMS_CODE_IS_UNUSED);
        }

        // 使用新验证码
        smsCodeService.useSmsCode(reqVO.getMobile(), SysSmsSceneEnum.CHANGE_MOBILE_BY_SMS.getScene(),
                reqVO.getCode(),getClientIP());

        // 更新用户手机
        memberUserMapper.updateById(UserDO.builder().id(userId).mobile(reqVO.getMobile()).build());
    }

    @VisibleForTesting
    public UserDO checkUserExists(Long id) {
        if (id == null) {
            return null;
        }
        UserDO user = memberUserMapper.selectById(id);
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }
        return user;
    }

}
