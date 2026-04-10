package cn.iocoder.yudao.module.mp.convert.message;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mp.controller.admin.message.vo.message.MpMessageRespVO;
import cn.iocoder.yudao.module.mp.controller.admin.message.vo.message.MpMessageSendReqVO;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.MpAccountDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.message.MpMessageDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.user.MpUserDO;
import cn.iocoder.yudao.module.mp.service.message.bo.MpMessageSendOutReqBO;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.mp.bean.kefu.WxMpKefuMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutNewsMessage;
import me.chanjar.weixin.mp.builder.outxml.BaseBuilder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface MpMessageConvert {

    MpMessageConvert INSTANCE = Mappers.getMapper(MpMessageConvert.class);

    MpMessageRespVO convert(MpMessageDO bean);

    List<MpMessageRespVO> convertList(List<MpMessageDO> list);

    PageResult<MpMessageRespVO> convertPage(PageResult<MpMessageDO> page);

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
    @Mappings(value = {
            @Mapping(source = "msgType", target = "type"),
            @Mapping(target = "createTime", ignore = true),
    })
    MpMessageDO convert(WxMpXmlMessage bean);

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
                message.setMediaId(sendReqBO.getMediaId());
                break;
            case WxConsts.XmlMsgType.VIDEO: // 4. 视频
                message.setMediaId(sendReqBO.getMediaId())
                        .setTitle(sendReqBO.getTitle()).setDescription(sendReqBO.getDescription());
                break;
            case WxConsts.XmlMsgType.NEWS: // 5. 图文
                message.setArticles(sendReqBO.getArticles());
            case WxConsts.XmlMsgType.MUSIC: // 6. 音乐
                message.setTitle(sendReqBO.getTitle()).setDescription(sendReqBO.getDescription())
                        .setMusicUrl(sendReqBO.getMusicUrl()).setHqMusicUrl(sendReqBO.getHqMusicUrl())
                        .setThumbMediaId(sendReqBO.getThumbMediaId());
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
        BaseBuilder<?, ? extends WxMpXmlOutMessage> builder;
        // 个性化字段
        switch (message.getType()) {
            case WxConsts.XmlMsgType.TEXT:
                builder = WxMpXmlOutMessage.TEXT().content(message.getContent());
                break;
            case WxConsts.XmlMsgType.IMAGE:
                builder = WxMpXmlOutMessage.IMAGE().mediaId(message.getMediaId());
                break;
            case WxConsts.XmlMsgType.VOICE:
                builder = WxMpXmlOutMessage.VOICE().mediaId(message.getMediaId());
                break;
            case WxConsts.XmlMsgType.VIDEO:
                builder = WxMpXmlOutMessage.VIDEO().mediaId(message.getMediaId())
                        .title(message.getTitle()).description(message.getDescription());
                break;
            case WxConsts.XmlMsgType.NEWS:
                builder = WxMpXmlOutMessage.NEWS().articles(convertList02(message.getArticles()));
                break;
            case WxConsts.XmlMsgType.MUSIC:
                builder = WxMpXmlOutMessage.MUSIC().title(message.getTitle()).description(message.getDescription())
                        .musicUrl(message.getMusicUrl()).hqMusicUrl(message.getHqMusicUrl())
                        .thumbMediaId(message.getThumbMediaId());
                break;
            default:
                throw new IllegalArgumentException("不支持的消息类型：" + message.getType());
        }
        // 通用字段
        builder.fromUser(account.getAccount());
        builder.toUser(message.getOpenid());
        return builder.build();
    }
    List<WxMpXmlOutNewsMessage.Item> convertList02(List<MpMessageDO.Article> list);

    default WxMpKefuMessage convert(MpMessageSendReqVO sendReqVO, MpUserDO user) {
        me.chanjar.weixin.mp.builder.kefu.BaseBuilder<?> builder;
        // 个性化字段
        switch (sendReqVO.getType()) {
            case WxConsts.KefuMsgType.TEXT:
                builder = WxMpKefuMessage.TEXT().content(sendReqVO.getContent());
                break;
            case WxConsts.KefuMsgType.IMAGE:
                builder = WxMpKefuMessage.IMAGE().mediaId(sendReqVO.getMediaId());
                break;
            case WxConsts.KefuMsgType.VOICE:
                builder = WxMpKefuMessage.VOICE().mediaId(sendReqVO.getMediaId());
                break;
            case WxConsts.KefuMsgType.VIDEO:
                builder = WxMpKefuMessage.VIDEO().mediaId(sendReqVO.getMediaId())
                        .title(sendReqVO.getTitle()).description(sendReqVO.getDescription());
                break;
            case WxConsts.KefuMsgType.NEWS:
                builder = WxMpKefuMessage.NEWS().articles(convertList03(sendReqVO.getArticles()));
                break;
            case WxConsts.KefuMsgType.MUSIC:
                builder = WxMpKefuMessage.MUSIC().title(sendReqVO.getTitle()).description(sendReqVO.getDescription())
                        .thumbMediaId(sendReqVO.getThumbMediaId())
                        .musicUrl(sendReqVO.getMusicUrl()).hqMusicUrl(sendReqVO.getHqMusicUrl());
                break;
            default:
                throw new IllegalArgumentException("不支持的消息类型：" + sendReqVO.getType());
        }
        // 通用字段
        builder.toUser(user.getOpenid());
        return builder.build();
    }
    List<WxMpKefuMessage.WxArticle> convertList03(List<MpMessageDO.Article> list);

    default MpMessageDO convert(WxMpKefuMessage wxMessage, MpAccountDO account, MpUserDO user) {
        MpMessageDO message = convert(wxMessage);
        if (account != null) {
            message.setAccountId(account.getId()).setAppId(account.getAppId());
        }
        if (user != null) {
            message.setUserId(user.getId()).setOpenid(user.getOpenid());
        }
        return message;
    }
    @Mappings(value = {
            @Mapping(source = "msgType", target = "type"),
            @Mapping(target = "createTime", ignore = true),
    })
    MpMessageDO convert(WxMpKefuMessage bean);

}
