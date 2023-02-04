package cn.iocoder.yudao.module.system.service.sms;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.sms.core.client.SmsClient;
import cn.iocoder.yudao.framework.sms.core.client.SmsClientFactory;
import cn.iocoder.yudao.framework.sms.core.client.SmsCommonResult;
import cn.iocoder.yudao.framework.sms.core.client.dto.SmsTemplateRespDTO;
import cn.iocoder.yudao.module.system.controller.admin.sms.vo.template.SmsTemplateCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.sms.vo.template.SmsTemplateExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.sms.vo.template.SmsTemplatePageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.sms.vo.template.SmsTemplateUpdateReqVO;
import cn.iocoder.yudao.module.system.convert.sms.SmsTemplateConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.sms.SmsChannelDO;
import cn.iocoder.yudao.module.system.dal.dataobject.sms.SmsTemplateDO;
import cn.iocoder.yudao.module.system.dal.mysql.sms.SmsTemplateMapper;
import cn.iocoder.yudao.module.system.mq.producer.sms.SmsProducer;
import com.google.common.annotations.VisibleForTesting;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;

/**
 * 短信模板 Service 实现类
 *
 * @author zzf
 * @since 2021/1/25 9:25
 */
@Service
@Slf4j
public class SmsTemplateServiceImpl implements SmsTemplateService {

    /**
     * 正则表达式，匹配 {} 中的变量
     */
    private static final Pattern PATTERN_PARAMS = Pattern.compile("\\{(.*?)}");

    @Resource
    private SmsTemplateMapper smsTemplateMapper;

    @Resource
    private SmsChannelService smsChannelService;

    @Resource
    private SmsClientFactory smsClientFactory;

    @Resource
    private SmsProducer smsProducer;

    /**
     * 短信模板缓存
     * key：短信模板编码 {@link SmsTemplateDO#getCode()}
     *
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    @Getter // 为了方便测试，这里提供 getter 方法
    private volatile Map<String, SmsTemplateDO> smsTemplateCache;

    @Override
    @PostConstruct
    public void initLocalCache() {
        // 第一步：查询数据
        List<SmsTemplateDO> smsTemplateList = smsTemplateMapper.selectList();
        log.info("[initLocalCache][缓存短信模版，数量为:{}]", smsTemplateList.size());

        // 第二步：构建缓存
        smsTemplateCache = CollectionUtils.convertMap(smsTemplateList, SmsTemplateDO::getCode);
    }

    @Override
    public SmsTemplateDO getSmsTemplateByCodeFromCache(String code) {
        return smsTemplateCache.get(code);
    }

    @Override
    public String formatSmsTemplateContent(String content, Map<String, Object> params) {
        return StrUtil.format(content, params);
    }

    @Override
    public SmsTemplateDO getSmsTemplateByCode(String code) {
        return smsTemplateMapper.selectByCode(code);
    }

    @VisibleForTesting
    public List<String> parseTemplateContentParams(String content) {
        return ReUtil.findAllGroup1(PATTERN_PARAMS, content);
    }

    @Override
    public Long createSmsTemplate(SmsTemplateCreateReqVO createReqVO) {
        // 校验短信渠道
        SmsChannelDO channelDO = validateSmsChannel(createReqVO.getChannelId());
        // 校验短信编码是否重复
        validateSmsTemplateCodeDuplicate(null, createReqVO.getCode());
        // 校验短信模板
        validateApiTemplate(createReqVO.getChannelId(), createReqVO.getApiTemplateId());

        // 插入
        SmsTemplateDO template = SmsTemplateConvert.INSTANCE.convert(createReqVO);
        template.setParams(parseTemplateContentParams(template.getContent()));
        template.setChannelCode(channelDO.getCode());
        smsTemplateMapper.insert(template);
        // 发送刷新消息
        smsProducer.sendSmsTemplateRefreshMessage();
        // 返回
        return template.getId();
    }

    @Override
    public void updateSmsTemplate(SmsTemplateUpdateReqVO updateReqVO) {
        // 校验存在
        validateSmsTemplateExists(updateReqVO.getId());
        // 校验短信渠道
        SmsChannelDO channelDO = validateSmsChannel(updateReqVO.getChannelId());
        // 校验短信编码是否重复
        validateSmsTemplateCodeDuplicate(updateReqVO.getId(), updateReqVO.getCode());
        // 校验短信模板
        validateApiTemplate(updateReqVO.getChannelId(), updateReqVO.getApiTemplateId());

        // 更新
        SmsTemplateDO updateObj = SmsTemplateConvert.INSTANCE.convert(updateReqVO);
        updateObj.setParams(parseTemplateContentParams(updateObj.getContent()));
        updateObj.setChannelCode(channelDO.getCode());
        smsTemplateMapper.updateById(updateObj);
        // 发送刷新消息
        smsProducer.sendSmsTemplateRefreshMessage();
    }

    @Override
    public void deleteSmsTemplate(Long id) {
        // 校验存在
        validateSmsTemplateExists(id);
        // 更新
        smsTemplateMapper.deleteById(id);
        // 发送刷新消息
        smsProducer.sendSmsTemplateRefreshMessage();
    }

    private void validateSmsTemplateExists(Long id) {
        if (smsTemplateMapper.selectById(id) == null) {
            throw exception(SMS_TEMPLATE_NOT_EXISTS);
        }
    }

    @Override
    public SmsTemplateDO getSmsTemplate(Long id) {
        return smsTemplateMapper.selectById(id);
    }

    @Override
    public List<SmsTemplateDO> getSmsTemplateList(Collection<Long> ids) {
        return smsTemplateMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<SmsTemplateDO> getSmsTemplatePage(SmsTemplatePageReqVO pageReqVO) {
        return smsTemplateMapper.selectPage(pageReqVO);
    }

    @Override
    public List<SmsTemplateDO> getSmsTemplateList(SmsTemplateExportReqVO exportReqVO) {
        return smsTemplateMapper.selectList(exportReqVO);
    }

    @Override
    public Long countByChannelId(Long channelId) {
        return smsTemplateMapper.selectCountByChannelId(channelId);
    }

    @VisibleForTesting
    public SmsChannelDO validateSmsChannel(Long channelId) {
        SmsChannelDO channelDO = smsChannelService.getSmsChannel(channelId);
        if (channelDO == null) {
            throw exception(SMS_CHANNEL_NOT_EXISTS);
        }
        if (!Objects.equals(channelDO.getStatus(), CommonStatusEnum.ENABLE.getStatus())) {
            throw exception(SMS_CHANNEL_DISABLE);
        }
        return channelDO;
    }

    @VisibleForTesting
    public void validateSmsTemplateCodeDuplicate(Long id, String code) {
        SmsTemplateDO template = smsTemplateMapper.selectByCode(code);
        if (template == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的字典类型
        if (id == null) {
            throw exception(SMS_TEMPLATE_CODE_DUPLICATE, code);
        }
        if (!template.getId().equals(id)) {
            throw exception(SMS_TEMPLATE_CODE_DUPLICATE, code);
        }
    }

    /**
     * 校验 API 短信平台的模板是否有效
     *
     * @param channelId 渠道编号
     * @param apiTemplateId API 模板编号
     */
    @VisibleForTesting
    public void validateApiTemplate(Long channelId, String apiTemplateId) {
        // 获得短信模板
        SmsClient smsClient = smsClientFactory.getSmsClient(channelId);
        Assert.notNull(smsClient, String.format("短信客户端(%d) 不存在", channelId));
        SmsCommonResult<SmsTemplateRespDTO> templateResult = smsClient.getSmsTemplate(apiTemplateId);
        // 校验短信模板是否正确
        templateResult.checkError();
    }

}
