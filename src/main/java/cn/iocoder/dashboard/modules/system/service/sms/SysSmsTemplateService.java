package cn.iocoder.dashboard.modules.system.service.sms;

import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsTemplateDO;

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

}
