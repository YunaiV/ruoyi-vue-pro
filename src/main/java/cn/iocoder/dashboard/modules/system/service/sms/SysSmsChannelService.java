package cn.iocoder.dashboard.modules.system.service.sms;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.req.SmsChannelCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.req.SmsChannelPageReqVO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsChannelDO;

/**
 * 短信渠道Service接口
 *
 * @author zzf
 * @date 2021/1/25 9:24
 */
public interface SysSmsChannelService {

    /**
     * 初始化短信客户端
     */
    void initSmsClients();

    /**
     * 分页查询短信渠道信息
     *
     * @param reqVO 参数对象
     * @return 短信渠道分页对象
     */
    PageResult<SysSmsChannelDO> pageSmsChannels(SmsChannelPageReqVO reqVO);

    /**
     * 创建新的渠道信息
     *
     * @param reqVO 参数对象
     * @return 渠道id
     */
    Long createSmsChannel(SmsChannelCreateReqVO reqVO);

}
