package cn.iocoder.dashboard.modules.msg.service.sms;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.msg.controller.sms.vo.SmsChannelAllVO;
import cn.iocoder.dashboard.modules.msg.controller.sms.vo.req.SmsChannelCreateReqVO;
import cn.iocoder.dashboard.modules.msg.controller.sms.vo.req.SmsChannelPageReqVO;
import cn.iocoder.dashboard.modules.msg.controller.sms.vo.resp.SmsChannelEnumRespVO;
import cn.iocoder.dashboard.modules.msg.dal.mysql.daoobject.sms.SmsChannelDO;

import java.util.List;

/**
 * 短信渠道Service接口
 *
 * @author zzf
 * @date 2021/1/25 9:24
 */
public interface SmsChannelService {

    PageResult<SmsChannelDO> pageChannels(SmsChannelPageReqVO reqVO);

    Long createChannel(SmsChannelCreateReqVO reqVO);

    List<SmsChannelEnumRespVO> getChannelEnums();

    /**
     * 查询渠道(包含名下模块)信息集合
     *
     * @return 渠道(包含名下模块)信息集合
     */
    List<SmsChannelAllVO> listChannelAllEnabledInfo();

    boolean flushChannel();
}
