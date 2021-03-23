package cn.iocoder.dashboard.modules.system.service.sms.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.sms.client.AbstractSmsClient;
import cn.iocoder.dashboard.framework.sms.core.SmsClientFactory;
import cn.iocoder.dashboard.framework.sms.core.enums.SmsChannelEnum;
import cn.iocoder.dashboard.framework.sms.core.property.SmsChannelProperty;
import cn.iocoder.dashboard.framework.sms.core.property.SmsTemplateProperty;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.SmsChannelAllVO;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.req.SmsChannelCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.req.SmsChannelPageReqVO;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.resp.SmsChannelEnumRespVO;
import cn.iocoder.dashboard.modules.system.convert.sms.SmsChannelConvert;
import cn.iocoder.dashboard.modules.system.convert.sms.SmsTemplateConvert;
import cn.iocoder.dashboard.modules.system.dal.mysql.dao.sms.SysSmsChannelMapper;
import cn.iocoder.dashboard.modules.system.dal.mysql.dao.sms.SysSmsTemplateMapper;
import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsChannelDO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsTemplateDO;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsChannelService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 短信渠道Service实现类
 *
 * @author zzf
 * @date 2021/1/25 9:25
 */
@Service
public class SysSmsChannelServiceImpl implements SysSmsChannelService {

    private final Map<String, Long> templateCode2ChannelIdMap = new ConcurrentHashMap<>(32);

    @Resource
    private SmsClientFactory clientFactory;

    @Resource
    private SysSmsChannelMapper channelMapper;

    @Resource
    private SysSmsTemplateMapper templateMapper;


    @PostConstruct
    @Override
    public void initSmsClientAndCacheSmsTemplate() {
        // 查询有效渠道信息
        List<SysSmsChannelDO> channelDOList = channelMapper.selectEnabledList();
        List<SmsChannelProperty> propertyList = SmsChannelConvert.INSTANCE.convertProperties(channelDOList);

        // 遍历渠道生成client、获取模板并缓存
        propertyList.forEach(channelProperty -> {
            List<SysSmsTemplateDO> templateDOList = templateMapper.selectListByChannelId(channelProperty.getId());
            if (ObjectUtil.isNotEmpty(templateDOList)) {
                Long clientId = clientFactory.createClient(channelProperty);
                templateDOList.forEach(template -> templateCode2ChannelIdMap.put(template.getCode(), clientId));

                List<SmsTemplateProperty> templatePropertyList = SmsTemplateConvert.INSTANCE.convertProperty(templateDOList);
                clientFactory.addOrUpdateTemplateCache(templatePropertyList);
            }
        });
    }

    @Override
    public PageResult<SysSmsChannelDO> pageSmsChannels(SmsChannelPageReqVO reqVO) {
        return SmsChannelConvert.INSTANCE.convertPage(channelMapper.selectChannelPage(reqVO));
    }

    @Override
    public Long createSmsChannel(SmsChannelCreateReqVO reqVO) {
        SysSmsChannelDO channelDO = SmsChannelConvert.INSTANCE.convert(reqVO);
        channelMapper.insert(channelDO);
        return channelDO.getId();
    }

    @Override
    public List<SmsChannelEnumRespVO> getSmsChannelEnums() {
        return SmsChannelConvert.INSTANCE.convertEnum(Arrays.asList(SmsChannelEnum.values()));
    }

    @Override
    public AbstractSmsClient getSmsClient(String templateCode) {
        return clientFactory.getClient(templateCode2ChannelIdMap.get(templateCode));
    }

    @Override
    public String getSmsTemplateApiIdByCode(String templateCode) {
        return clientFactory.getTemplateApiIdByCode(templateCode);
    }

    @Override
    public List<SmsChannelAllVO> listSmsChannelAllEnabledInfo() {
        List<SysSmsChannelDO> channelDOList = channelMapper.selectEnabledList();
        if (ObjectUtil.isNull(channelDOList)) {
            return null;
        }
        List<SmsChannelAllVO> channelAllVOList = SmsChannelConvert.INSTANCE.convert(channelDOList);
        channelAllVOList.forEach(smsChannelDO -> {
            List<SysSmsTemplateDO> templateDOList = templateMapper.selectListByChannelId(smsChannelDO.getId());
            if (ObjectUtil.isNull(templateDOList)) {
                templateDOList = new ArrayList<>();
            }
            smsChannelDO.setTemplateList(SmsTemplateConvert.INSTANCE.convert(templateDOList));
        });
        return channelAllVOList;
    }
}
