package cn.iocoder.dashboard.modules.system.service.sms;

import javax.servlet.ServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 短信Service接口
 * 只支持异步，因此没有返回值
 *
 * @author zzf
 * @date 2021/1/25 9:24
 */
public interface SysSmsService {

    void sendSingleSms(String mobile, Long userId, Integer userType,
                       String templateCode, Map<String, Object> templateParams);

    void sendBatchSms(List<String> mobiles, List<Long> userIds, Integer userType,
                      String templateCode, Map<String, Object> templateParams);

    /**
     * 处理短信发送回调函数
     *
     * @param request        请求
     * @return 响应数据
     */
    Object smsSendCallbackHandle(ServletRequest request);

}
