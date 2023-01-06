package cn.iocoder.yudao.module.mp.service.message;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import cn.iocoder.yudao.module.mp.controller.admin.message.vo.MpMessagePageReqVO;
import cn.iocoder.yudao.module.mp.convert.message.MpMessageConvert;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.MpAccountDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.message.MpMessageDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.user.MpUserDO;
import cn.iocoder.yudao.module.mp.dal.mysql.message.MpMessageMapper;
import cn.iocoder.yudao.module.mp.enums.message.MpMessageSendFromEnum;
import cn.iocoder.yudao.module.mp.framework.mp.core.MpServiceFactory;
import cn.iocoder.yudao.module.mp.framework.mp.core.util.MpUtils;
import cn.iocoder.yudao.module.mp.service.account.MpAccountService;
import cn.iocoder.yudao.module.mp.service.message.bo.MpMessageSendOutReqBO;
import cn.iocoder.yudao.module.mp.service.user.MpUserService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Validator;
import java.io.File;

/**
 * 粉丝消息表 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class MpMessageServiceImpl implements MpMessageService {

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private MpAccountService mpAccountService;
    @Resource
    private MpUserService mpUserService;

    @Resource
    private MpMessageMapper mpMessageMapper;

    @Resource
    @Lazy // 延迟加载，解决循环依赖的问题
    private MpServiceFactory mpServiceFactory;

    @Resource
    private FileApi fileApi;

    @Resource
    private Validator validator;

    @Override
    public PageResult<MpMessageDO> getWxFansMsgPage(MpMessagePageReqVO pageReqVO) {
        return mpMessageMapper.selectPage(pageReqVO);
    }

    @Override
    public void receiveMessage(String appId, WxMpXmlMessage wxMessage) {
        WxMpService mpService = mpServiceFactory.getRequiredMpService(appId);
        // 获得关联信息
        MpAccountDO account = mpAccountService.getAccountFromCache(appId);
        Assert.notNull(account, "公众号账号({}) 不存在", appId);
        MpUserDO user = mpUserService.getUser(appId, wxMessage.getFromUser());
        Assert.notNull(user, "公众号粉丝({}/{}) 不存在", appId, wxMessage.getFromUser());

        // 记录消息
        MpMessageDO message = MpMessageConvert.INSTANCE.convert(wxMessage, account, user);
        message.setSendFrom(MpMessageSendFromEnum.USER_TO_MP.getFrom());
        if (StrUtil.isNotEmpty(message.getMediaId())) {
            message.setMediaUrl(mediaDownload(mpService, message.getMediaId()));
        }
        if (StrUtil.isNotEmpty(message.getThumbMediaId())) {
            message.setThumbMediaUrl(mediaDownload(mpService, message.getThumbMediaId()));
        }
        mpMessageMapper.insert(message);
    }

    @Override
    public WxMpXmlOutMessage sendOutMessage(MpMessageSendOutReqBO sendReqBO) {
        // 校验消息格式
        MpUtils.validateMessage(validator, sendReqBO.getType(), sendReqBO);

        // 获得关联信息
        MpAccountDO account = mpAccountService.getAccountFromCache(sendReqBO.getAppId());
        Assert.notNull(account, "公众号账号({}) 不存在", sendReqBO.getAppId());
        MpUserDO user = mpUserService.getUser(sendReqBO.getAppId(), sendReqBO.getOpenid());
        Assert.notNull(user, "公众号粉丝({}/{}) 不存在", sendReqBO.getAppId(), sendReqBO.getOpenid());

        // 记录消息
        MpMessageDO message = MpMessageConvert.INSTANCE.convert(sendReqBO, account, user);
        message.setSendFrom(MpMessageSendFromEnum.MP_TO_USER.getFrom());
        mpMessageMapper.insert(message);

        // 转换返回 WxMpXmlOutMessage 对象
        return MpMessageConvert.INSTANCE.convert02(message, account);
    }

    /**
     * 下载微信媒体文件的内容，并上传到文件服务
     *
     * 为什么要下载？媒体文件在微信后台保存时间为 3 天，即 3 天后 media_id 失效。
     *
     * @param mpService 微信公众号 Service
     * @param mediaId 媒体文件编号
     * @return 上传后的 URL
     */
    private String mediaDownload(WxMpService mpService, String mediaId) {
        try {
            // 第一步，从公众号下载媒体文件
            File file = mpService.getMaterialService().mediaDownload(mediaId);
            // 第二步，上传到文件服务
            String path = mediaId + "." + FileTypeUtil.getType(file);
            return fileApi.createFile(path, FileUtil.readBytes(file));
        } catch (WxErrorException e) {
            log.error("[mediaDownload][media({}) 下载失败]", mediaId);
        }
        return null;
    }

}
