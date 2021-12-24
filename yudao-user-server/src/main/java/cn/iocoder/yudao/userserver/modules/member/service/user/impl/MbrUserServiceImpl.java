package cn.iocoder.yudao.userserver.modules.member.service.user.impl;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.coreservice.modules.infra.service.file.InfFileCoreService;
import cn.iocoder.yudao.coreservice.modules.member.dal.dataobject.user.MbrUserDO;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.userserver.modules.member.controller.user.vo.MbrUserInfoRespVO;
import cn.iocoder.yudao.userserver.modules.member.controller.user.vo.MbrUserUpdateMobileReqVO;
import cn.iocoder.yudao.userserver.modules.member.convert.user.UserProfileConvert;
import cn.iocoder.yudao.userserver.modules.member.dal.mysql.user.MbrUserMapper;
import cn.iocoder.yudao.userserver.modules.member.service.user.MbrUserService;
import cn.iocoder.yudao.userserver.modules.system.dal.dataobject.sms.SysSmsCodeDO;
import cn.iocoder.yudao.userserver.modules.system.enums.sms.SysSmsSceneEnum;
import cn.iocoder.yudao.userserver.modules.system.service.sms.SysSmsCodeService;
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
import static cn.iocoder.yudao.userserver.modules.member.enums.MbrErrorCodeConstants.USER_NOT_EXISTS;
import static cn.iocoder.yudao.userserver.modules.system.enums.SysErrorCodeConstants.USER_SMS_CODE_IS_UNUSED;

/**
 * User Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Valid
@Slf4j
public class MbrUserServiceImpl implements MbrUserService {

    @Resource
    private MbrUserMapper userMapper;

    @Resource
    private InfFileCoreService fileCoreService;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private SysSmsCodeService smsCodeService;


    @Override
    public MbrUserDO getUserByMobile(String mobile) {
        return userMapper.selectByMobile(mobile);
    }

    @Override
    public MbrUserDO createUserIfAbsent(String mobile, String registerIp) {
        // 用户已经存在
        MbrUserDO user = userMapper.selectByMobile(mobile);
        if (user != null) {
            return user;
        }
        // 用户不存在，则进行创建
        return this.createUser(mobile, registerIp);
    }

    private MbrUserDO createUser(String mobile, String registerIp) {
        // 生成密码
        String password = IdUtil.fastSimpleUUID();
        // 插入用户
        MbrUserDO user = new MbrUserDO();
        user.setMobile(mobile);
        user.setStatus(CommonStatusEnum.ENABLE.getStatus()); // 默认开启
        user.setPassword(passwordEncoder.encode(password)); // 加密密码
        user.setRegisterIp(registerIp);
        userMapper.insert(user);
        return user;
    }

    @Override
    public void updateUserLogin(Long id, String loginIp) {
        userMapper.updateById(new MbrUserDO().setId(id).setLoginIp(loginIp).setLoginDate(new Date()));
    }

    @Override
    public MbrUserDO getUser(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public void updateNickname(Long userId, String nickname) {
        MbrUserDO user = this.checkUserExists(userId);
        // 仅当新昵称不等于旧昵称时进行修改
        if (nickname.equals(user.getNickname())){
            return;
        }
        MbrUserDO userDO = new MbrUserDO();
        userDO.setId(user.getId());
        userDO.setNickname(nickname);
        userMapper.updateById(userDO);
    }

    @Override
    public String updateAvatar(Long userId, InputStream avatarFile) {
        this.checkUserExists(userId);
        // 创建文件
        String avatar = fileCoreService.createFile(IdUtil.fastUUID(), IoUtil.readBytes(avatarFile));
        // 更新头像路径
        MbrUserDO userDO = MbrUserDO.builder()
                .id(userId)
                .avatar(avatar)
                .build();
        userMapper.updateById(userDO);
        return avatar;
    }

    @Override
    public MbrUserInfoRespVO getUserInfo(Long userId) {
        MbrUserDO user = this.checkUserExists(userId);
        // 拼接返回结果
        return UserProfileConvert.INSTANCE.convert(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMobile(Long userId, MbrUserUpdateMobileReqVO reqVO) {
        // 检测用户是否存在
        checkUserExists(userId);

        // 校验旧手机和旧验证码
        SysSmsCodeDO sysSmsCodeDO = smsCodeService.checkCodeIsExpired(reqVO.getOldMobile(), reqVO.getOldCode(), SysSmsSceneEnum.CHANGE_MOBILE_BY_SMS.getScene());
        // 判断旧code是否未被使用，如果是，抛出异常
        if (Boolean.FALSE.equals(sysSmsCodeDO.getUsed())){
            throw exception(USER_SMS_CODE_IS_UNUSED);
        }

        // 使用新验证码
        smsCodeService.useSmsCode(reqVO.getMobile(), SysSmsSceneEnum.CHANGE_MOBILE_BY_SMS.getScene(), reqVO.getCode(),getClientIP());

        // 更新用户手机
        MbrUserDO userDO = MbrUserDO.builder().id(userId).mobile(reqVO.getMobile()).build();
        userMapper.updateById(userDO);
    }

    @VisibleForTesting
    public MbrUserDO checkUserExists(Long id) {
        if (id == null) {
            return null;
        }
        MbrUserDO user = userMapper.selectById(id);
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }else{
            return user;
        }
    }
}
