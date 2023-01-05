package cn.iocoder.yudao.module.mp.service.message;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.MpAccountDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.message.MpAutoReplyDO;
import cn.iocoder.yudao.module.mp.dal.mysql.message.MpAutoReplyMapper;
import cn.iocoder.yudao.module.mp.enums.message.MpAutoReplyTypeEnum;
import cn.iocoder.yudao.module.mp.service.account.MpAccountService;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

/**
 * 公众号的自动回复 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MpAutoReplyServiceImpl implements MpAutoReplyService {

    @Resource
    private MpMessageService mpMessageService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private MpAccountService mpAccountService;

    @Resource
    private MpAutoReplyMapper mpAutoReplyMapper;

    @Override
    public WxMpXmlOutMessage replyForMessage(String appId, WxMpXmlMessage wxMessage) {
        // 第一步，匹配自动回复
        List<MpAutoReplyDO> replies = null;
        // 1.1 关键字
        if (wxMessage.getMsgType().equals(WxConsts.XmlMsgType.TEXT)) {
            // 完全匹配
            replies = mpAutoReplyMapper.selectListByAppIdAndKeywordAll(appId, wxMessage.getContent());
            if (CollUtil.isEmpty(replies)) {
                // 模糊匹配
                replies = mpAutoReplyMapper.selectListByAppIdAndKeywordLike(appId, wxMessage.getContent());
            }
        }
        // 1.2 消息类型
        if (CollUtil.isEmpty(replies)) {
            replies = mpAutoReplyMapper.selectListByAppIdAndMessage(appId, wxMessage.getMsgType());
        }
        if (CollUtil.isEmpty(replies)) {
            return null;
        }
        MpAutoReplyDO reply = CollUtil.getFirst(replies);

        // 第二步，基于自动回复，创建消息
        return mpMessageService.createFromAutoReply(wxMessage.getFromUser(), reply);
    }

    @Override
    public WxMpXmlOutMessage replyForSubscribe(String appId, WxMpXmlMessage wxMessage) {
        // 第一步，匹配自动回复
        List<MpAutoReplyDO> replies = mpAutoReplyMapper.selectListByAppIdAndSubscribe(appId);
        MpAutoReplyDO reply = CollUtil.isNotEmpty(replies) ? CollUtil.getFirst(replies)
                : buildDefaultSubscribeAutoReply(appId); // 如果不存在，提供一个默认末班

        // 第二步，基于自动回复，创建消息
        return mpMessageService.createFromAutoReply(wxMessage.getFromUser(), reply);
    }

    private MpAutoReplyDO buildDefaultSubscribeAutoReply(String appId) {
        MpAccountDO account = mpAccountService.getAccountFromCache(appId);
        Assert.notNull(account, "公众号账号({}) 不存在", appId);
        // 构建默认的【关注】自动回复
        return new MpAutoReplyDO().setAppId(appId).setAccountId(account.getId())
                .setType(MpAutoReplyTypeEnum.SUBSCRIBE.getType())
                .setResponseMessageType(WxConsts.XmlMsgType.TEXT).setResponseContent("感谢关注");
    }

}
