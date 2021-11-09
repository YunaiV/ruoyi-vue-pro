package cn.iocoder.yudao.adminserver.modules.pay.service.channel;

import java.util.*;
import javax.validation.*;

import cn.iocoder.yudao.adminserver.modules.pay.controller.channel.vo.*;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayChannelDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * 支付渠道
 * Service 接口
 *
 * @author 芋艿
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
     * 获得支付渠道
     * 列表
     *
     * @param ids 编号
     * @return 支付渠道
     * 列表
     */
    List<PayChannelDO> getChannelList(Collection<Long> ids);

    /**
     * 获得支付渠道
     * 分页
     *
     * @param pageReqVO 分页查询
     * @return 支付渠道
     * 分页
     */
    PageResult<PayChannelDO> getChannelPage(PayChannelPageReqVO pageReqVO);

    /**
     * 获得支付渠道
     * 列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 支付渠道
     * 列表
     */
    List<PayChannelDO> getChannelList(PayChannelExportReqVO exportReqVO);

    /**
     * 根据支付应用ID集合获取所有的支付渠道
     *
     * @param payIds 支付应用编号集合
     * @return 支付渠道
     */
    List<PayChannelDO> getSimpleChannels(Collection<Long> payIds);

    /**
     * 解析pem文件获取公钥私钥字符串
     *
     * @param file pem公私钥文件
     * @return 解析后的字符串
     */
    String parsingPemFile(MultipartFile file);

    /**
     * 创建微信的渠道配置
     *
     * @param reqVO 创建信息
     * @return 创建结果
     */
    Long createWechatChannel(PayWechatChannelCreateReqVO reqVO);

    /**
     * 根据条件获取通道数量
     *
     * @param merchantId 商户编号
     * @param appid      应用编号
     * @param code       通道编码
     * @return 数量
     */
    Integer getChannelCountByConditions(Long merchantId, Long appid, String code);

    /**
     * 根据条件获取通道
     *
     * @param merchantId 商户编号
     * @param appid      应用编号
     * @param code       通道编码
     * @return 数量
     */
    PayChannelDO getChannelByConditions(Long merchantId, Long appid, String code);

    /**
     * 更新微信支付渠道
     *
     * @param updateReqVO 更新信息
     */
    void updateWechatChannel(PayWechatChannelUpdateReqVO updateReqVO);
}
