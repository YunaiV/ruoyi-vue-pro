package cn.iocoder.dashboard.modules.system.service.sms.impl;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.template.SysSmsTemplateCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.template.SysSmsTemplateExportReqVO;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.template.SysSmsTemplatePageReqVO;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.template.SysSmsTemplateUpdateReqVO;
import cn.iocoder.dashboard.modules.system.convert.sms.SysSmsTemplateConvert;
import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsTemplateDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.sms.SysSmsTemplateMapper;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsTemplateService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.dashboard.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.dashboard.modules.system.enums.SysErrorCodeConstants.SMS_TEMPLATE_NOT_EXISTS;

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

    @Override
    public String formatSmsTemplateContent(String content, Map<String, Object> params) {
        return StrUtil.format(content, params);
    }

    @Override
    public Long createSmsTemplate(SysSmsTemplateCreateReqVO createReqVO) {
        // 插入
        SysSmsTemplateDO smsTemplate = SysSmsTemplateConvert.INSTANCE.convert(createReqVO);
        smsTemplateMapper.insert(smsTemplate);
        // 返回
        return smsTemplate.getId();
    }

    @Override
    public void updateSmsTemplate(SysSmsTemplateUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateSmsTemplateExists(updateReqVO.getId());
        // 更新
        SysSmsTemplateDO updateObj = SysSmsTemplateConvert.INSTANCE.convert(updateReqVO);
        smsTemplateMapper.updateById(updateObj);
    }

    @Override
    public void deleteSmsTemplate(Long id) {
        // 校验存在
        this.validateSmsTemplateExists(id);
        // 更新
        smsTemplateMapper.deleteById(id);
    }

    private void validateSmsTemplateExists(Long id) {
        if (smsTemplateMapper.selectById(id) == null) {
            throw exception(SMS_TEMPLATE_NOT_EXISTS);
        }
    }

    @Override
    public SysSmsTemplateDO getSmsTemplate(Long id) {
        return smsTemplateMapper.selectById(id);
    }

    @Override
    public List<SysSmsTemplateDO> getSmsTemplateList(Collection<Long> ids) {
        return smsTemplateMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<SysSmsTemplateDO> getSmsTemplatePage(SysSmsTemplatePageReqVO pageReqVO) {
        return smsTemplateMapper.selectPage(pageReqVO);
    }

    @Override
    public List<SysSmsTemplateDO> getSmsTemplateList(SysSmsTemplateExportReqVO exportReqVO) {
        return smsTemplateMapper.selectList(exportReqVO);
    }

}
