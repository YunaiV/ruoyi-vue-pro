package cn.iocoder.dashboard.modules.system.service.sms;

import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsTemplateDO;

import java.util.Map;

/**
 * 短信发送日志服务接口
 *
 * @author zzf
 * @date 13:48 2021/3/2
 */
public interface SysSmsSendLogService {

    Long createSmsSendLog(String mobile, Long userId, Integer userType,
                          SysSmsTemplateDO template, String templateContent, Map<String, Object> templateParams);

    void getAndSaveSmsSendLog();

}
