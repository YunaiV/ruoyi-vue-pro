package cn.iocoder.yudao.module.mp.convert.message;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mp.controller.admin.message.vo.MpMessageRespVO;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.MpAccountDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.message.MpMessageDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.user.MpUserDO;
import cn.iocoder.yudao.module.mp.service.message.bo.MpMessageSendOutReqBO;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutNewsMessage;
import me.chanjar.weixin.mp.builder.outxml.BaseBuilder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

@Mapper
public interface MpMessageConvert {

    MpMessageConvert INSTANCE = Mappers.getMapper(MpMessageConvert.class);

    MpMessageRespVO convert(MpMessageDO bean);

    List<MpMessageRespVO> convertList(List<MpMessageDO> list);

    PageResult<MpMessageRespVO> convertPage(PageResult<MpMessageDO> page);

    @Mappings(value = {
            @Mapping(source = "msgType", target = "type"),
            @Mapping(target = "createTime", ignore = true),
    })
    MpMessageDO convert(WxMpXmlMessage wxMessage);

    default MpMessageDO convert(WxMpXmlMessage wxMessage, MpAccountDO account, MpUserDO user) {
        MpMessageDO message = convert(wxMessage);
        if (account != null) {
            message.setAccountId(account.getId()).setAppId(account.getAppId());
        }
        if (user != null) {
            message.setUserId(user.getId()).setOpenid(user.getOpenid());
        }
        return message;
    }

    default MpMessageDO convert(MpMessageSendOutReqBO sendReqBO, MpAccountDO account, MpUserDO user) {
        // 构建消息
        MpMessageDO message = new MpMessageDO();
        message.setType(sendReqBO.getType());
        switch (sendReqBO.getType()) {
            case WxConsts.XmlMsgType.TEXT: // 1. 文本
                message.setContent(sendReqBO.getContent());
                break;
            case WxConsts.XmlMsgType.IMAGE: // 2. 图片
            case WxConsts.XmlMsgType.VOICE: // 3. 语音
                message.setMediaId(sendReqBO.getMediaId()).setMediaUrl(sendReqBO.getMediaUrl());
                break;
            case WxConsts.XmlMsgType.VIDEO: // 4. 视频
                message.setMediaId(sendReqBO.getMediaId()).setMediaUrl(sendReqBO.getMediaUrl())
                        .setTitle(sendReqBO.getTitle()).setDescription(sendReqBO.getDescription());
                break;
            case WxConsts.XmlMsgType.NEWS: // 5. 图文
                message.setArticles(Collections.singletonList(sendReqBO.getArticle()));
                break;
            default:
                throw new IllegalArgumentException("不支持的消息类型：" + message.getType());
        }

        // 其它字段
        if (account != null) {
            message.setAccountId(account.getId()).setAppId(account.getAppId());
        }
        if (user != null) {
            message.setUserId(user.getId()).setOpenid(user.getOpenid());
        }
        return message;
    }

    default WxMpXmlOutMessage convert02(MpMessageDO message, MpAccountDO account) {
        BaseBuilder<?, ? extends WxMpXmlOutMessage> messageBuilder;
        // 个性化字段
        switch (message.getType()) {
            case WxConsts.XmlMsgType.TEXT:
                messageBuilder = WxMpXmlOutMessage.TEXT().content(message.getContent());
                break;
            case WxConsts.XmlMsgType.IMAGE:
                messageBuilder = WxMpXmlOutMessage.IMAGE().mediaId(message.getMediaId());
                break;
            case WxConsts.XmlMsgType.VOICE:
                messageBuilder = WxMpXmlOutMessage.VOICE().mediaId(message.getMediaId());
                break;
            case WxConsts.XmlMsgType.VIDEO:
                messageBuilder = WxMpXmlOutMessage.VIDEO().mediaId(message.getMediaId())
                        .title(message.getTitle()).description(message.getDescription());
                break;
            case WxConsts.XmlMsgType.NEWS:
                messageBuilder = WxMpXmlOutMessage.NEWS().articles(convertList02(message.getArticles()));
                break;
            default:
                throw new IllegalArgumentException("不支持的消息类型：" + message.getType());
        }
        // 通用字段
        messageBuilder.fromUser(account.getAccount());
        messageBuilder.toUser(message.getOpenid());
        return messageBuilder.build();
    }
    List<WxMpXmlOutNewsMessage.Item> convertList02(List<MpMessageDO.Article> list);

}
