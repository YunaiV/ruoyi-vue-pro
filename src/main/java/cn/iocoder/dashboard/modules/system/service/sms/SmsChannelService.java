package cn.iocoder.dashboard.modules.system.service.sms;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.sms.client.AbstractSmsClient;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.SmsChannelAllVO;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.req.SmsChannelCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.req.SmsChannelPageReqVO;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.resp.SmsChannelEnumRespVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.sms.SmsChannelDO;

import java.util.List;

/**
 * 短信渠道Service接口
 *
 * @author zzf
 * @date 2021/1/25 9:24
 */
public interface SmsChannelService {

    /**
     * 初始化短信渠道
     */
    void initSmsClient();

    /**
     * 分页查询短信渠道信息
     *
     * @param reqVO 参数对象
     * @return 短信渠道分页对象
     */
    PageResult<SmsChannelDO> pageChannels(SmsChannelPageReqVO reqVO);

    /**
     * 创建新的渠道信息
     *
     * @param reqVO 参数对象
     * @return 渠道id
     */
    Long createChannel(SmsChannelCreateReqVO reqVO);

    /**
     * 获取短信渠道枚举/渠道编码
     *
     * @return 短信渠道枚举/渠道编码
     */
    List<SmsChannelEnumRespVO> getChannelEnums();

    /**
     * 根据短信模板编码获取短信客户端
     *
     * @param templateCode 短信模板编码
     * @return 短信客户端
     */
    AbstractSmsClient<?> getClient(String templateCode);

    /**
     * 查询渠道(包含名下模块)信息集合
     *
     * @return 渠道(包含名下模块)信息集合
     */
    List<SmsChannelAllVO> listChannelAllEnabledInfo();
}
