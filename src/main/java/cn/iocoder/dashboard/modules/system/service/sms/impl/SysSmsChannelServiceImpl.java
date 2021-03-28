package cn.iocoder.dashboard.modules.system.service.sms.impl;

import cn.iocoder.dashboard.common.enums.CommonStatusEnum;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.sms.core.client.SmsClientFactory;
import cn.iocoder.dashboard.framework.sms.core.enums.SmsChannelEnum;
import cn.iocoder.dashboard.framework.sms.core.property.SmsChannelProperties;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.req.SmsChannelCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.req.SmsChannelPageReqVO;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.resp.SmsChannelEnumRespVO;
import cn.iocoder.dashboard.modules.system.convert.sms.SmsChannelConvert;
import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsChannelDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.sms.SysSmsChannelMapper;
import cn.iocoder.dashboard.modules.system.dal.mysql.sms.SysSmsTemplateMapper;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsChannelService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * 短信渠道Service实现类
 *
 * @author zzf
 * @date 2021/1/25 9:25
 */
@Service
public class SysSmsChannelServiceImpl implements SysSmsChannelService {

    @Resource
    private SmsClientFactory smsClientFactory;

    @Resource
    private SysSmsChannelMapper channelMapper;

    @Resource
    private SysSmsTemplateMapper templateMapper;

    @Override
    @PostConstruct
    public void initSmsClientAndCacheSmsTemplate() {
        // 查询有效渠道信息
        List<SysSmsChannelDO> channelDOList = channelMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
        // 创建渠道 Client
        List<SmsChannelProperties> propertiesList = SmsChannelConvert.INSTANCE.convertList(channelDOList);
        propertiesList.forEach(properties -> smsClientFactory.createOrUpdateSmsClient(properties));
    }

    @Override
    public PageResult<SysSmsChannelDO> pageSmsChannels(SmsChannelPageReqVO reqVO) {
        return channelMapper.selectChannelPage(reqVO);
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

//    @Override
//    public List<SmsChannelAllVO> listSmsChannelAllEnabledInfo() {
//        List<SysSmsChannelDO> channelDOList = channelMapper.selectListByStatus();
//        if (ObjectUtil.isNull(channelDOList)) {
//            return null;
//        }
//        List<SmsChannelAllVO> channelAllVOList = SmsChannelConvert.INSTANCE.convert(channelDOList);
//        channelAllVOList.forEach(smsChannelDO -> {
//            List<SysSmsTemplateDO> templateDOList = templateMapper.selectListByChannelId(smsChannelDO.getId());
//            if (ObjectUtil.isNull(templateDOList)) {
//                templateDOList = new ArrayList<>();
//            }
//            smsChannelDO.setTemplateList(SmsTemplateConvert.INSTANCE.convert(templateDOList));
//        });
//        return channelAllVOList;
//    }
}
