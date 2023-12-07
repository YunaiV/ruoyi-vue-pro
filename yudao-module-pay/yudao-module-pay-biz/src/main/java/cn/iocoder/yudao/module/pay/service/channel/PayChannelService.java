package cn.iocoder.yudao.module.pay.service.channel;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.pay.core.client.PayClient;
import cn.iocoder.yudao.module.pay.controller.admin.channel.vo.PayChannelCreateReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.channel.vo.PayChannelUpdateReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.channel.PayChannelDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 支付渠道 Service 接口
 *
 * @author aquan
 */
public interface PayChannelService {

    /**
     * 创建支付渠道
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createChannel(@Valid PayChannelCreateReqVO createReqVO);

    /**
     * 更新支付渠道
     *
     * @param updateReqVO 更新信息
     */
    void updateChannel(@Valid PayChannelUpdateReqVO updateReqVO);

    /**
     * 删除支付渠道
     *
     * @param id 编号
     */
    void deleteChannel(Long id);

    /**
     * 获得支付渠道
     *
     * @param id 编号
     * @return 支付渠道
     */
    PayChannelDO getChannel(Long id);

    /**
     * 根据支付应用 ID 集合，获得支付渠道列表
     *
     * @param appIds 应用编号集合
     * @return 支付渠道列表
     */
    List<PayChannelDO> getChannelListByAppIds(Collection<Long> appIds);

    /**
     * 根据条件获取渠道
     *
     * @param appId      应用编号
     * @param code       渠道编码
     * @return 数量
     */
    PayChannelDO getChannelByAppIdAndCode(Long appId, String code);

    /**
     * 支付渠道的合法性
     *
     * 如果不合法，抛出 {@link ServiceException} 业务异常
     *
     * @param id 渠道编号
     * @return 渠道信息
     */
    PayChannelDO validPayChannel(Long id);

    /**
     * 支付渠道的合法性
     *
     * 如果不合法，抛出 {@link ServiceException} 业务异常
     *
     * @param appId 应用编号
     * @param code 支付渠道
     * @return 渠道信息
     */
    PayChannelDO validPayChannel(Long appId, String code);

    /**
     * 获得指定应用的开启的渠道列表
     *
     * @param appId 应用编号
     * @return 渠道列表
     */
    List<PayChannelDO> getEnableChannelList(Long appId);

    /**
     * 获得指定编号的支付客户端
     *
     * @param id 编号
     * @return 支付客户端
     */
    PayClient getPayClient(Long id);

}
