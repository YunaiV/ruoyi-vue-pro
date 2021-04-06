package cn.iocoder.dashboard.modules.system.service.sms.impl;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.dashboard.common.enums.CommonStatusEnum;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.template.SysSmsTemplateCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.template.SysSmsTemplateExportReqVO;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.template.SysSmsTemplatePageReqVO;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.template.SysSmsTemplateUpdateReqVO;
import cn.iocoder.dashboard.modules.system.convert.sms.SysSmsTemplateConvert;
import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsChannelDO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsTemplateDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.sms.SysSmsTemplateMapper;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsChannelService;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsTemplateService;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import static cn.iocoder.dashboard.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.dashboard.modules.system.enums.SysErrorCodeConstants.*;

/**
 * 短信模板Service实现类
 *
 * @author zzf
 * @date 2021/1/25 9:25
 */
@Service
public class SysSmsTemplateServiceImpl implements SysSmsTemplateService {

    /**
     * 正则表达式，匹配 {} 中的变量
     */
    private static final Pattern PATTERN_PARAMS = Pattern.compile("\\{(.*?)}");

    @Resource
    private SysSmsTemplateMapper smsTemplateMapper;

    @Resource
    private SysSmsChannelService smsChannelService;

    @Override
    public SysSmsTemplateDO getSmsTemplateByCode(String code) {
        return smsTemplateMapper.selectByCode(code);
    }

    @Override
    public String formatSmsTemplateContent(String content, Map<String, Object> params) {
        return StrUtil.format(content, params);
    }

    @VisibleForTesting
    public List<String> parseTemplateContentParams(String content) {
        return ReUtil.findAllGroup1(PATTERN_PARAMS, content);
    }

    @Override
    public Long createSmsTemplate(SysSmsTemplateCreateReqVO createReqVO) {
        // 校验短信渠道
        SysSmsChannelDO channelDO = checkSmsChannel(createReqVO.getChannelId());
        // 校验短信编码是否重复
        checkSmsTemplateCodeDuplicate(null, createReqVO.getCode());

        // 插入
        SysSmsTemplateDO template = SysSmsTemplateConvert.INSTANCE.convert(createReqVO);
        template.setParams(parseTemplateContentParams(template.getContent()));
        template.setChannelCode(channelDO.getCode());
        smsTemplateMapper.insert(template);
        // 返回
        return template.getId();
    }

    @Override
    public void updateSmsTemplate(SysSmsTemplateUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateSmsTemplateExists(updateReqVO.getId());
        // 校验短信渠道
        SysSmsChannelDO channelDO = checkSmsChannel(updateReqVO.getChannelId());
        // 校验短信编码是否重复
        checkSmsTemplateCodeDuplicate(updateReqVO.getId(), updateReqVO.getCode());

        // 更新
        SysSmsTemplateDO updateObj = SysSmsTemplateConvert.INSTANCE.convert(updateReqVO);
        updateObj.setParams(parseTemplateContentParams(updateObj.getContent()));
        updateObj.setChannelCode(channelDO.getCode());
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

    @VisibleForTesting
    public SysSmsChannelDO checkSmsChannel(Long channelId) {
        SysSmsChannelDO channelDO = smsChannelService.getSmsChannel(channelId);
        if (channelDO == null) {
            throw exception(SMS_CHANNEL_NOT_EXISTS);
        }
        if (!Objects.equals(channelDO.getStatus(), CommonStatusEnum.ENABLE.getStatus())) {
            throw exception(SMS_CHANNEL_DISABLE);
        }
        return channelDO;
    }

    @VisibleForTesting
    public void checkSmsTemplateCodeDuplicate(Long id, String code) {
        SysSmsTemplateDO template = smsTemplateMapper.selectByCode(code);
        if (template == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的字典类型
        if (id == null) {
            throw exception(SMS_TEMPLATE_CODE_DUPLICATE);
        }
        if (!template.getId().equals(id)) {
            throw exception(SMS_TEMPLATE_CODE_DUPLICATE);
        }
    }

}
