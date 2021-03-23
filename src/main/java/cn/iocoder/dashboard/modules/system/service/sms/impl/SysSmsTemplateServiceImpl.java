package cn.iocoder.dashboard.modules.system.service.sms.impl;

import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsTemplateDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.sms.SysSmsTemplateMapper;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsTemplateService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 短信模板Service实现类
 *
 * @author zzf
 * @date 2021/1/25 9:25
 */
@Service
public class SysSmsTemplateServiceImpl implements SysSmsTemplateService {

    @Resource
    private SysSmsTemplateMapper smsTemplateMapper;

    @Override
    public SysSmsTemplateDO getSmsTemplateByCode(String code) {
        return smsTemplateMapper.selectOneByCode(code);
    }

}
