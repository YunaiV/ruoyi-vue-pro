package cn.iocoder.dashboard.modules.system.service.sms;

import cn.iocoder.dashboard.framework.sms.core.enums.SmsSendFailureTypeEnum;
import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsTemplateDO;

import java.util.Map;

/**
 * 短信发送日志服务接口
 *
 * @author zzf
 * @date 13:48 2021/3/2
 */
public interface SysSmsSendLogService {

    /**
     * 创建发送日志
     *
     * @param mobile 手机号
     * @param userId 用户编号
     * @param userType 用户类型
     * @param template 短信模板
     * @param templateContent 短信内容
     * @param templateParams 短信参数
     * @return
     */
    Long createSmsSendLog(String mobile, Long userId, Integer userType,
                          SysSmsTemplateDO template, String templateContent, Map<String, Object> templateParams);

    /**
     * 更新发送日志的结果
     *
     * @param id 日志编号
     * @param success 是否成功
     * @param sendFailureType 发送失败的类型
     * @param sendFailureMsg 发送失败的提示
     * @param apiSendFailureType 短信 API 发送失败的类型
     * @param apiSendFailureMsg 短信 API 发送失败的提示
     * @param apiRequestId 短信 API 发送返回的唯一请求 ID
     * @param apiSerialNo 短信 API 发送返回的序号
     */
    void updateSmsSendLogResult(Long id, Boolean success, Integer sendFailureType, String sendFailureMsg,
                                String apiSendFailureType, String apiSendFailureMsg, String apiRequestId, String apiSerialNo);

    default void updateSmsSendLogFailure(Long id, SmsSendFailureTypeEnum sendFailureType) {
        updateSmsSendLogResult(id, false, sendFailureType.getType(), sendFailureType.getMsg(),
                null, null, null, null);
    }

}
