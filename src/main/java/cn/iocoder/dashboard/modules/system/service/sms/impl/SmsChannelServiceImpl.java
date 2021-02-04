package cn.iocoder.dashboard.modules.system.service.sms.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.dashboard.common.enums.SmsChannelEnum;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.sms.client.AbstractSmsClient;
import cn.iocoder.dashboard.framework.sms.core.SmsClientFactory;
import cn.iocoder.dashboard.framework.sms.core.property.SmsChannelProperty;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.SmsChannelAllVO;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.req.SmsChannelCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.req.SmsChannelPageReqVO;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.resp.SmsChannelEnumRespVO;
import cn.iocoder.dashboard.modules.system.convert.sms.SmsChannelConvert;
import cn.iocoder.dashboard.modules.system.convert.sms.SmsTemplateConvert;
import cn.iocoder.dashboard.modules.system.dal.mysql.dao.sms.SmsChannelMapper;
import cn.iocoder.dashboard.modules.system.dal.mysql.dao.sms.SmsTemplateMapper;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.sms.SmsChannelDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.sms.SmsTemplateDO;
import cn.iocoder.dashboard.modules.system.service.sms.SmsChannelService;
import org.springframework.beans.factory.annotation.Autowired;
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
public class SmsChannelServiceImpl implements SmsChannelService {

    private final Map<String, Long> templateCode2ChannelIdMap = new ConcurrentHashMap<>(32);

    @Autowired
    private SmsClientFactory smsClientFactory;

    /**
     * 初始化短信客户端
     */
    @PostConstruct
    @Override
    public void initSmsClient() {
        List<SmsChannelAllVO> smsChannelAllVOList = listChannelAllEnabledInfo();
        if (ObjectUtil.isEmpty(smsChannelAllVOList)) {
            return;
        }
        List<SmsChannelProperty> channelPropertyList = SmsChannelConvert.INSTANCE.convertProperty(smsChannelAllVOList);
        channelPropertyList.forEach(smsChannelProperty -> {
            Long clientId = smsClientFactory.createClient(smsChannelProperty);
            smsChannelProperty.getTemplateList().forEach(smsTemplateVO -> {
                templateCode2ChannelIdMap.put(smsTemplateVO.getCode(), clientId);
            });
        });
    }


    @Resource
    private SmsChannelMapper mapper;

    @Resource
    private SmsTemplateMapper templateMapper;

    @Override
    public PageResult<SmsChannelDO> pageChannels(SmsChannelPageReqVO reqVO) {
        return SmsChannelConvert.INSTANCE.convertPage(mapper.selectChannelPage(reqVO));
    }

    @Override
    public Long createChannel(SmsChannelCreateReqVO reqVO) {
        SmsChannelDO channelDO = SmsChannelConvert.INSTANCE.convert(reqVO);
        mapper.insert(channelDO);
        return channelDO.getId();
    }

    @Override
    public List<SmsChannelEnumRespVO> getChannelEnums() {
        return SmsChannelConvert.INSTANCE.convertEnum(Arrays.asList(SmsChannelEnum.values()));
    }

    @Override
    public AbstractSmsClient<?> getClient(String templateCode) {
        return smsClientFactory.getClient(templateCode2ChannelIdMap.get(templateCode));
    }

    @Override
    public List<SmsChannelAllVO> listChannelAllEnabledInfo() {
        List<SmsChannelDO> channelDOList = mapper.selectEnabledList();
        if (ObjectUtil.isNull(channelDOList)) {
            return null;
        }
        List<SmsChannelAllVO> channelAllVOList = SmsChannelConvert.INSTANCE.convert(channelDOList);

        channelAllVOList.forEach(smsChannelDO -> {

            List<SmsTemplateDO> templateDOList = templateMapper.selectListByChannelId(smsChannelDO.getId());
            if (ObjectUtil.isNull(templateDOList)) {
                templateDOList = new ArrayList<>();
            }
            smsChannelDO.setTemplateList(SmsTemplateConvert.INSTANCE.convert(templateDOList));
        });
        return channelAllVOList;
    }
}
