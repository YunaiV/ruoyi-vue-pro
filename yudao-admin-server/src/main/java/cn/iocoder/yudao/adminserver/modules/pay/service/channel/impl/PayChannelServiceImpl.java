package cn.iocoder.yudao.adminserver.modules.pay.service.channel.impl;

import cn.hutool.core.io.IoUtil;
import cn.iocoder.yudao.adminserver.modules.pay.controller.channel.vo.*;
import cn.iocoder.yudao.adminserver.modules.pay.convert.channel.PayChannelConvert;
import cn.iocoder.yudao.adminserver.modules.pay.dal.mysql.channel.PayChannelMapper;
import cn.iocoder.yudao.adminserver.modules.pay.service.channel.PayChannelService;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayChannelDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.pay.core.client.impl.wx.WXPayClientConfig;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.coreservice.modules.pay.enums.PayErrorCodeCoreConstants.*;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 支付渠道 Service 实现类
 *
 * @author 芋艿 // TODO aquan：作者写自己哈
 */
@Service
@Slf4j
@Validated
public class PayChannelServiceImpl implements PayChannelService {

    @Resource
    private PayChannelMapper channelMapper;

    @Override
    public Long createChannel(PayChannelCreateReqVO createReqVO) {
        // 插入
        PayChannelDO channel = PayChannelConvert.INSTANCE.convert(createReqVO);
        channelMapper.insert(channel);
        // 返回
        return channel.getId();
    }

    @Override
    public void updateChannel(PayChannelUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateChannelExists(updateReqVO.getId());
        // 更新
        PayChannelDO updateObj = PayChannelConvert.INSTANCE.convert(updateReqVO);
        channelMapper.updateById(updateObj);
    }

    @Override
    public void deleteChannel(Long id) {
        // 校验存在
        this.validateChannelExists(id);
        // 删除
        channelMapper.deleteById(id);
    }

    private void validateChannelExists(Long id) {
        if (channelMapper.selectById(id) == null) {
            throw exception(CHANNEL_NOT_EXISTS);
        }
    }

    @Override
    public PayChannelDO getChannel(Long id) {
        return channelMapper.selectById(id);
    }

    @Override
    public List<PayChannelDO> getChannelList(Collection<Long> ids) {
        return channelMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<PayChannelDO> getChannelPage(PayChannelPageReqVO pageReqVO) {
        return channelMapper.selectPage(pageReqVO);
    }

    @Override
    public List<PayChannelDO> getChannelList(PayChannelExportReqVO exportReqVO) {
        return channelMapper.selectList(exportReqVO);
    }

    /**
     * 根据支付应用ID集合获取所有的支付渠道
     *
     * @param payIds 支付应用编号集合
     * @return 支付渠道
     */
    @Override
    public List<PayChannelDO> getSimpleChannels(Collection<Long> payIds) {
        return channelMapper.selectList(new QueryWrapper<PayChannelDO>().lambda().in(PayChannelDO::getAppId, payIds));
    }

    /**
     * 解析pem文件获取公钥私钥字符串
     *
     * @param file pem公私钥文件
     * @return 解析后的字符串
     */
    @Override
    public String parsingPemFile(MultipartFile file) {
        try {
            return IoUtil.readUtf8(file.getInputStream());
        } catch (IOException e) {
            log.error("[parsingPemToString]读取pem[{}]文件错误", file.getOriginalFilename());
            throw exception(CHANNEL_KEY_READ_ERROR);
        }
    }

    /**
     * 创建微信的渠道配置
     *
     * @param reqVO 创建信息
     * @return 创建结果
     */
    @Override
    public Long createWechatChannel(PayWechatChannelCreateReqVO reqVO) {

        // 判断是否有重复的有责无法新增
        Integer channelCount = this.getChannelCountByConditions(reqVO.getMerchantId(), reqVO.getAppId(), reqVO.getCode());
        if (channelCount > 0) {
            throw exception(EXIST_SAME_CHANNEL_ERROR);
        }

        PayChannelDO channel = PayChannelConvert.INSTANCE.convert(reqVO);
        WXPayClientConfig config = PayChannelConvert.INSTANCE.configConvert(reqVO.getWeChatConfig());
        channel.setConfig(config);
        channelMapper.insert(channel);
        return channel.getId();
    }

    /**
     * 根据条件获取通道数量
     *
     * @param merchantId 商户编号
     * @param appid      应用编号
     * @param code       通道编码
     * @return 数量
     */
    @Override
    public Integer getChannelCountByConditions(Long merchantId, Long appid, String code) {
        return this.channelMapper.selectCount(new QueryWrapper<PayChannelDO>().lambda()
                .eq(PayChannelDO::getMerchantId, merchantId)
                .eq(PayChannelDO::getAppId, appid)
                .eq(PayChannelDO::getCode, code)
        );
    }

    // TODO @aquan：service 不出现 mybatis plus 哈
    /**
     * 根据条件获取通道
     *
     * @param merchantId 商户编号
     * @param appid      应用编号
     * @param code       通道编码
     * @return 数量
     */
    @Override
    public PayChannelDO getChannelByConditions(Long merchantId, Long appid, String code) {
        return this.channelMapper.selectOne((new QueryWrapper<PayChannelDO>().lambda()
                .eq(PayChannelDO::getMerchantId, merchantId)
                .eq(PayChannelDO::getAppId, appid)
                .eq(PayChannelDO::getCode, code)
        ));
    }

    /**
     * 更新微信支付渠道
     *
     * @param updateReqVO 更新信息
     */
    @Override
    public void updateWechatChannel(PayWechatChannelUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateChannelExists(updateReqVO.getId());
        PayChannelDO channel = PayChannelConvert.INSTANCE.convert(updateReqVO);
        WXPayClientConfig config = PayChannelConvert.INSTANCE.configConvert(updateReqVO.getWeChatConfig());
        channel.setConfig(config);
        this.channelMapper.updateById(channel);
    }
}
