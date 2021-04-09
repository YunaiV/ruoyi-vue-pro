package cn.iocoder.dashboard.modules.system.service.sms.impl;

import cn.iocoder.dashboard.common.enums.CommonStatusEnum;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.sms.core.client.SmsClientFactory;
import cn.iocoder.dashboard.framework.sms.core.property.SmsChannelProperties;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.channel.SysSmsChannelCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.channel.SysSmsChannelPageReqVO;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.channel.SysSmsChannelUpdateReqVO;
import cn.iocoder.dashboard.modules.system.convert.sms.SysSmsChannelConvert;
import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsChannelDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.sms.SysSmsChannelMapper;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsChannelService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.dashboard.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.dashboard.modules.system.enums.SysErrorCodeConstants.SMS_CHANNEL_NOT_EXISTS;

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
    private SysSmsChannelMapper smsChannelMapper;

    @Override
    @PostConstruct
    public void initSmsClients() {
        // 查询有效渠道信息
        List<SysSmsChannelDO> channelDOList = smsChannelMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
        // 创建渠道 Client
        List<SmsChannelProperties> propertiesList = SysSmsChannelConvert.INSTANCE.convertList02(channelDOList);
        propertiesList.forEach(properties -> smsClientFactory.createOrUpdateSmsClient(properties));
    }

    // TODO 芋艿：刷新缓存

    @Override
    public Long createSmsChannel(SysSmsChannelCreateReqVO createReqVO) {
        // 插入
        SysSmsChannelDO smsChannel = SysSmsChannelConvert.INSTANCE.convert(createReqVO);
        smsChannelMapper.insert(smsChannel);
        // 返回
        return smsChannel.getId();
    }

    @Override
    public void updateSmsChannel(SysSmsChannelUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateSmsChannelExists(updateReqVO.getId());
        // 更新
        SysSmsChannelDO updateObj = SysSmsChannelConvert.INSTANCE.convert(updateReqVO);
        smsChannelMapper.updateById(updateObj);
    }

    @Override
    public void deleteSmsChannel(Long id) {
        // 校验存在
        this.validateSmsChannelExists(id);
        // 更新
        smsChannelMapper.deleteById(id);
    }

    private void validateSmsChannelExists(Long id) {
        if (smsChannelMapper.selectById(id) == null) {
            throw exception(SMS_CHANNEL_NOT_EXISTS);
        }
    }

    @Override
    public SysSmsChannelDO getSmsChannel(Long id) {
        return smsChannelMapper.selectById(id);
    }

    @Override
    public List<SysSmsChannelDO> getSmsChannelList(Collection<Long> ids) {
        return smsChannelMapper.selectBatchIds(ids);
    }

    @Override
    public List<SysSmsChannelDO> getSmsChannelList() {
        return smsChannelMapper.selectList();
    }

    @Override
    public PageResult<SysSmsChannelDO> getSmsChannelPage(SysSmsChannelPageReqVO pageReqVO) {
        return smsChannelMapper.selectPage(pageReqVO);
    }

}
