package cn.iocoder.yudao.module.member.service.sms;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.coreservice.modules.member.dal.dataobject.user.MbrUserDO;
import cn.iocoder.yudao.coreservice.modules.system.service.sms.SysSmsCoreService;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.userserver.modules.member.service.user.MbrUserService;
import cn.iocoder.yudao.userserver.modules.system.dal.dataobject.sms.SysSmsCodeDO;
import cn.iocoder.yudao.userserver.modules.system.dal.mysql.sms.SysSmsCodeMapper;
import cn.iocoder.yudao.userserver.modules.system.enums.sms.SysSmsSceneEnum;
import cn.iocoder.yudao.userserver.modules.system.framework.sms.SmsCodeProperties;
import cn.iocoder.yudao.userserver.modules.system.service.sms.SysSmsCodeService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Date;

import static cn.hutool.core.util.RandomUtil.randomInt;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;
import static cn.iocoder.yudao.userserver.modules.system.enums.SysErrorCodeConstants.*;

/**
 * 短信验证码 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class SysSmsCodeServiceImpl implements SysSmsCodeService {

    @Resource
    private SmsCodeProperties smsCodeProperties;

    @Resource
    private SysSmsCodeMapper smsCodeMapper;

    @Resource
    private MbrUserService mbrUserService;

    @Resource
    private SysSmsCoreService smsCoreService;

    @Override
    public void sendSmsCode(String mobile, Integer scene, String createIp) {
        // 创建验证码
        String code = this.createSmsCode(mobile, scene, createIp);

        // 获取发送模板
        String codeTemplate = SysSmsSceneEnum.getCodeByScene(scene);

        // 如果是更换手机号发送验证码，则需要检测手机号是否被注册
        if (SysSmsSceneEnum.CHANGE_MOBILE_BY_SMS.getScene().equals(scene)){
            this.checkMobileIsRegister(mobile,scene);
        }

        // 发送验证码
        smsCoreService.sendSingleSmsToMember(mobile, null, codeTemplate,
                MapUtil.of("code", code));
    }

    public void checkMobileIsRegister(String mobile, Integer scene) {
        // 检测手机号是否已被使用
        MbrUserDO userByMobile = mbrUserService.getUserByMobile(mobile);
        if (userByMobile != null){
            throw ServiceExceptionUtil.exception(SysErrorCodeConstants.USER_SMS_CODE_IS_EXISTS);
        }

        // 发送短信
        this.sendSmsCode(mobile,scene,getClientIP());
    }

    private String createSmsCode(String mobile, Integer scene, String ip) {
        // 校验是否可以发送验证码，不用筛选场景
        SysSmsCodeDO lastSmsCode = smsCodeMapper.selectLastByMobile(mobile, null,null);
        if (lastSmsCode != null) {
            if (lastSmsCode.getTodayIndex() >= smsCodeProperties.getSendMaximumQuantityPerDay()) { // 超过当天发送的上限。
                throw ServiceExceptionUtil.exception(SysErrorCodeConstants.USER_SMS_CODE_EXCEED_SEND_MAXIMUM_QUANTITY_PER_DAY);
            }
            if (System.currentTimeMillis() - lastSmsCode.getCreateTime().getTime()
                    < smsCodeProperties.getSendFrequency().toMillis()) { // 发送过于频繁
                throw ServiceExceptionUtil.exception(SysErrorCodeConstants.USER_SMS_CODE_SEND_TOO_FAST);
            }
            // TODO 芋艿：提升，每个 IP 每天可发送数量
            // TODO 芋艿：提升，每个 IP 每小时可发送数量
        }

        // 创建验证码记录
        String code = String.valueOf(randomInt(smsCodeProperties.getBeginCode(), smsCodeProperties.getEndCode() + 1));
        SysSmsCodeDO newSmsCode = SysSmsCodeDO.builder().mobile(mobile).code(code)
                .scene(scene).todayIndex(lastSmsCode != null ? lastSmsCode.getTodayIndex() + 1 : 1)
                .createIp(ip).used(false).build();
        smsCodeMapper.insert(newSmsCode);
        return code;
    }

    @Override
    public void useSmsCode(String mobile, Integer scene, String code, String usedIp) {

        // 检测验证码是否有效
        SysSmsCodeDO lastSmsCode = this.checkCodeIsExpired(mobile, code, scene);

        // 判断验证码是否已被使用
        if (Boolean.TRUE.equals(lastSmsCode.getUsed())) {
            throw ServiceExceptionUtil.exception(SysErrorCodeConstants.USER_SMS_CODE_USED);
        }

        // 使用验证码
        smsCodeMapper.updateById(SysSmsCodeDO.builder().id(lastSmsCode.getId())
                .used(true).usedTime(new Date()).usedIp(usedIp).build());
    }

    @Override
    public void sendSmsCodeLogin(Long userId) {
        MbrUserDO user = mbrUserService.getUser(userId);
        if (user == null){
            throw ServiceExceptionUtil.exception(SysErrorCodeConstants.USER_NOT_EXISTS);
        }
        // 发送验证码
        this.sendSmsCode(user.getMobile(),SysSmsSceneEnum.CHANGE_MOBILE_BY_SMS.getScene(), getClientIP());
    }

    @Override
    public SysSmsCodeDO checkCodeIsExpired(String mobile, String code, Integer scene) {
        // 校验验证码
        SysSmsCodeDO lastSmsCode = smsCodeMapper.selectLastByMobile(mobile,code,scene);

        // 若验证码不存在，抛出异常
        if (lastSmsCode == null) {
            throw ServiceExceptionUtil.exception(SysErrorCodeConstants.USER_SMS_CODE_NOT_FOUND);
        }
        if (System.currentTimeMillis() - lastSmsCode.getCreateTime().getTime()
                >= smsCodeProperties.getExpireTimes().toMillis()) { // 验证码已过期
            throw ServiceExceptionUtil.exception(SysErrorCodeConstants.USER_SMS_CODE_EXPIRED);
        }

        return lastSmsCode;

    }

}
