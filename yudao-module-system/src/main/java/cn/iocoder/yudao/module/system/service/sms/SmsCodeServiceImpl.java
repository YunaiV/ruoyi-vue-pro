package cn.iocoder.yudao.module.system.service.sms;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.system.api.sms.dto.code.SmsCodeSendReqDTO;
import cn.iocoder.yudao.module.system.api.sms.dto.code.SmsCodeUseReqDTO;
import cn.iocoder.yudao.module.system.api.sms.dto.code.SmsCodeValidateReqDTO;
import cn.iocoder.yudao.module.system.dal.dataobject.sms.SmsCodeDO;
import cn.iocoder.yudao.module.system.dal.mysql.sms.SmsCodeMapper;
import cn.iocoder.yudao.module.system.dal.redis.sms.SmsCodeRedisDAO;
import cn.iocoder.yudao.module.system.enums.sms.SmsSceneEnum;
import cn.iocoder.yudao.module.system.framework.sms.config.SmsCodeProperties;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.randomInt;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.isToday;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;

/**
 * 短信验证码 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class SmsCodeServiceImpl implements SmsCodeService {

    @Resource
    private SmsCodeProperties smsCodeProperties;

    @Resource
    private SmsCodeMapper smsCodeMapper;
    @Resource
    private SmsCodeRedisDAO smsCodeRedisDAO;

    @Resource
    private SmsSendService smsSendService;

    @Override
    public void sendSmsCode(SmsCodeSendReqDTO reqDTO) {
        SmsSceneEnum sceneEnum = SmsSceneEnum.getCodeByScene(reqDTO.getScene());
        Assert.notNull(sceneEnum, "验证码场景({}) 查找不到配置", reqDTO.getScene());
        // 创建验证码
        String code = createSmsCode(reqDTO.getMobile(), reqDTO.getScene(), reqDTO.getCreateIp());
        // 发送验证码
        smsSendService.sendSingleSms(reqDTO.getMobile(), null, null,
                sceneEnum.getTemplateCode(), MapUtil.of("code", code));
    }

    private String createSmsCode(String mobile, Integer scene, String ip) {
        // 校验发送间隔：按手机号 + 场景，避免刚发过登录验证码时挡住注册、找回密码等其它场景
        SmsCodeDO lastSmsCode = smsCodeMapper.selectLastByMobile(mobile, null, null);
        SmsCodeDO lastSceneSmsCode = lastSmsCode;
        if (lastSmsCode != null && ObjUtil.notEqual(lastSmsCode.getScene(), scene)) { // 最后一条是其它场景，才需要单独查询本场景
            lastSceneSmsCode = smsCodeMapper.selectLastByMobile(mobile, null, scene);
        }
        if (lastSceneSmsCode != null
                && LocalDateTimeUtil.between(lastSceneSmsCode.getCreateTime(), LocalDateTime.now()).toMillis()
                        < smsCodeProperties.getSendFrequency().toMillis()) { // 发送过于频繁
            throw exception(SMS_CODE_SEND_TOO_FAST);
        }
        // 校验当天发送上限：按手机号计数，不区分场景，避免切换场景绕过
        if (lastSmsCode != null && isToday(lastSmsCode.getCreateTime()) // 必须是今天，才能计算超过当天的上限
                && lastSmsCode.getTodayIndex() >= smsCodeProperties.getSendMaximumQuantityPerDay()) { // 超过当天发送的上限。
            throw exception(SMS_CODE_EXCEED_SEND_MAXIMUM_QUANTITY_PER_DAY);
        }
        // 校验 IP 跨手机号的发送数量，手机号校验通过后才占用额度：先占当日额度，再占当前小时额度
        LocalDateTime now = LocalDateTime.now();
        if (!smsCodeRedisDAO.tryReserveSendIpDayCount(ip, now, smsCodeProperties.getSendMaximumQuantityPerIpPerDay())) {
            throw exception(SMS_CODE_EXCEED_SEND_MAXIMUM_QUANTITY_PER_IP_PER_DAY);
        }
        if (!smsCodeRedisDAO.tryReserveSendIpHourCount(ip, now, smsCodeProperties.getSendMaximumQuantityPerIpPerHour())) {
            // 小时超限时未发送，归还已占用的当日额度
            smsCodeRedisDAO.releaseSendIpDayCount(ip, now);
            throw exception(SMS_CODE_EXCEED_SEND_MAXIMUM_QUANTITY_PER_IP_PER_HOUR);
        }

        // 创建验证码记录
        String code = String.format("%0" + smsCodeProperties.getEndCode().toString().length() + "d",
                randomInt(smsCodeProperties.getBeginCode(), smsCodeProperties.getEndCode() + 1));
        SmsCodeDO newSmsCode = SmsCodeDO.builder().mobile(mobile).code(code).scene(scene)
                .todayIndex(lastSmsCode != null && isToday(lastSmsCode.getCreateTime()) ? lastSmsCode.getTodayIndex() + 1 : 1)
                .createIp(ip).used(false).build();
        smsCodeMapper.insert(newSmsCode);
        return code;
    }

    @Override
    public void useSmsCode(SmsCodeUseReqDTO reqDTO) {
        // 检测验证码是否有效
        SmsCodeDO lastSmsCode = validateSmsCode0(reqDTO.getMobile(), reqDTO.getCode(), reqDTO.getScene());
        // 使用验证码：按未使用条件原子认领，并发使用同一验证码时只有一个请求更新成功，其余按已使用处理
        int updateCount = smsCodeMapper.updateByIdAndUsed(lastSmsCode.getId(), false, SmsCodeDO.builder()
                .used(true).usedTime(LocalDateTime.now()).usedIp(reqDTO.getUsedIp()).build());
        if (updateCount == 0) {
            throw exception(SMS_CODE_USED);
        }
    }

    @Override
    public void validateSmsCode(SmsCodeValidateReqDTO reqDTO) {
        validateSmsCode0(reqDTO.getMobile(), reqDTO.getCode(), reqDTO.getScene());
    }

    private SmsCodeDO validateSmsCode0(String mobile, String code, Integer scene) {
        // 获得最后一个验证码，只有最后一个验证码有效，避免错误次数分散到历史验证码上
        SmsCodeDO lastSmsCode = smsCodeMapper.selectLastByMobile(mobile, null, scene);
        // 若验证码不存在，抛出异常
        if (lastSmsCode == null) {
            throw exception(SMS_CODE_NOT_FOUND);
        }
        // 超过时间
        if (LocalDateTimeUtil.between(lastSmsCode.getCreateTime(), LocalDateTime.now()).toMillis()
                >= smsCodeProperties.getExpireTimes().toMillis()) { // 验证码已过期
            throw exception(SMS_CODE_EXPIRED);
        }
        // 判断验证码是否已被使用
        if (Boolean.TRUE.equals(lastSmsCode.getUsed())) {
            throw exception(SMS_CODE_USED);
        }
        // 预占一次校验次数，达到错误上限后验证码作废
        if (!smsCodeRedisDAO.tryReserveValidateCount(lastSmsCode.getId(),
                smsCodeProperties.getValidateMaximumQuantity(), smsCodeProperties.getExpireTimes())) {
            throw exception(SMS_CODE_EXCEED_VALIDATE_MAXIMUM_QUANTITY);
        }
        // 校验验证码：错误时保留本次次数；正确时归还，正确校验不消耗次数
        // DONE @AI：改用 ObjUtil.notEqual 去掉取反；当前 Hutool 5 的 StrUtil 无 notEquals，两者对 String 的空值语义一致
        if (ObjUtil.notEqual(lastSmsCode.getCode(), code)) {
            throw exception(SMS_CODE_NOT_FOUND);
        }
        smsCodeRedisDAO.releaseValidateCount(lastSmsCode.getId());
        return lastSmsCode;
    }

}
