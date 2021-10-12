package cn.iocoder.yudao.userserver.modules.system.service.sms.impl;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.coreservice.modules.system.service.sms.SysSmsCoreService;
import cn.iocoder.yudao.userserver.modules.system.dal.dataobject.sms.SysSmsCodeDO;
import cn.iocoder.yudao.userserver.modules.system.dal.mysql.sms.SysSmsCodeMapper;
import cn.iocoder.yudao.userserver.modules.system.enums.sms.SysSmsTemplateCodeConstants;
import cn.iocoder.yudao.userserver.modules.system.framework.sms.SmsCodeProperties;
import cn.iocoder.yudao.userserver.modules.system.service.sms.SysSmsCodeService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Date;

import static cn.hutool.core.util.RandomUtil.randomInt;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
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
    private SysSmsCoreService smsCoreService;

    @Override
    public void sendSmsCode(String mobile, Integer scene, String createIp) {
        // 创建验证码
        String code = this.createSmsCode(mobile, scene, createIp);
        // 发送验证码
        smsCoreService.sendSingleSmsToMember(mobile, null, SysSmsTemplateCodeConstants.USER_SMS_LOGIN,
                MapUtil.of("code", code));
    }

    private String createSmsCode(String mobile, Integer scene, String ip) {
        // 校验是否可以发送验证码，不用筛选场景
        SysSmsCodeDO lastSmsCode = smsCodeMapper.selectLastByMobile(mobile, null);
        if (lastSmsCode != null) {
            if (lastSmsCode.getTodayIndex() >= smsCodeProperties.getSendMaximumQuantityPerDay()) { // 超过当天发送的上限。
                throw exception(USER_SMS_CODE_EXCEED_SEND_MAXIMUM_QUANTITY_PER_DAY);
            }
            if (System.currentTimeMillis() - lastSmsCode.getCreateTime().getTime()
                    < smsCodeProperties.getSendFrequency().toMillis()) { // 发送过于频繁
                throw exception(USER_SMS_CODE_SEND_TOO_FAST);
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
        // 校验验证码
        SysSmsCodeDO lastSmsCode = smsCodeMapper.selectLastByMobile(mobile, scene);
        if (lastSmsCode == null) { // 若验证码不存在，抛出异常
            throw exception(USER_SMS_CODE_NOT_FOUND);
        }
        if (System.currentTimeMillis() - lastSmsCode.getCreateTime().getTime()
                >= smsCodeProperties.getExpireTimes().toMillis()) { // 验证码已过期
            throw exception(USER_SMS_CODE_EXPIRED);
        }
        if (lastSmsCode.getUsed()) { // 验证码已使用
            throw exception(USER_SMS_CODE_USED);
        }
        if (!lastSmsCode.getCode().equals(code)) {
            throw exception(USER_SMS_CODE_NOT_CORRECT);
        }

        // 使用验证码
        smsCodeMapper.updateById(SysSmsCodeDO.builder().id(lastSmsCode.getId())
                .used(true).usedTime(new Date()).usedIp(usedIp).build());
    }

}
