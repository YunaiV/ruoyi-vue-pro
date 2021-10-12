package cn.iocoder.yudao.coreservice.modules.system.service.sms;

import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.sms.SysSmsTemplateDO;

import java.util.Map;

/**
 * 短信模板 Core Service 接口
 *
 * @author 芋道源码
 */
public interface SysSmsTemplateCoreService {

    /**
     * 初始化短信模板的本地缓存
     */
    void initLocalCache();

    /**
     * 获得短信模板，从缓存中
     *
     * @param code 模板编码
     * @return 短信模板
     */
    SysSmsTemplateDO getSmsTemplateByCodeFromCache(String code);

    /**
     * 格式化短信内容
     *
     * @param content 短信模板的内容
     * @param params 内容的参数
     * @return 格式化后的内容
     */
    String formatSmsTemplateContent(String content, Map<String, Object> params);

}
