package cn.iocoder.dashboard.modules.system.service.sms;

import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsTemplateDO;

import java.util.Map;

/**
 * 短信模板 Service 接口
 *
 * @author zzf
 * @date 2021/1/25 9:24
 */
public interface SysSmsTemplateService {

    /**
     * 获得短信模板
     *
     * @param code 模板编码
     * @return 短信模板
     */
    SysSmsTemplateDO getSmsTemplateByCode(String code);

    /**
     * 格式化短信内容
     *
     * @param content 短信模板的内容
     * @param params 内容的参数
     * @return 格式化后的内容
     */
    String formatSmsTemplateContent(String content, Map<String, Object> params);

}
