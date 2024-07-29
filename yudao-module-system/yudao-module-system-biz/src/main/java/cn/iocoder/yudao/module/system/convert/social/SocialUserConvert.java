package cn.iocoder.yudao.module.system.convert.social;

import cn.binarywang.wx.miniapp.bean.WxMaSubscribeMessage;
import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.api.social.dto.SocialUserBindReqDTO;
import cn.iocoder.yudao.module.system.api.social.dto.SocialWxSubscribeMessageSendReqDTO;
import cn.iocoder.yudao.module.system.api.social.dto.SocialWxSubscribeTemplateRespDTO;
import cn.iocoder.yudao.module.system.controller.admin.socail.vo.user.SocialUserBindReqVO;
import me.chanjar.weixin.common.bean.subscribemsg.TemplateInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;

@Mapper
public interface SocialUserConvert {

    SocialUserConvert INSTANCE = Mappers.getMapper(SocialUserConvert.class);

    @Mapping(source = "reqVO.type", target = "socialType")
    SocialUserBindReqDTO convert(Long userId, Integer userType, SocialUserBindReqVO reqVO);

    // TODO @puhui999：要不 convert 直接放到 service 里。
    default WxMaSubscribeMessage convert(SocialWxSubscribeMessageSendReqDTO reqDTO) {
        WxMaSubscribeMessage message = BeanUtils.toBean(reqDTO, WxMaSubscribeMessage.class);
        Map<String, String> messages = reqDTO.getMessages();
        if (CollUtil.isNotEmpty(messages)) {
            messages.keySet().forEach(key -> findAndThen(messages, key, value -> message.addData(new WxMaSubscribeMessage.MsgData(key, value))));
        }
        return message;
    }

    // TODO @puhui999：要不 convert 直接放到 service 里。其实可以 BeanUtils.toBean(reqDTO, WxMaSubscribeMessage.class) 来搞的呀。
    @Mapping(target = "id", source = "priTmplId")
    SocialWxSubscribeTemplateRespDTO convert(TemplateInfo templateInfo);

    // TODO @puhui999：是不是用 CollectionUtils.convertList 就 ok 啦。
    default List<SocialWxSubscribeTemplateRespDTO> convertList(List<TemplateInfo> subscribeTemplate) {
        List<SocialWxSubscribeTemplateRespDTO> list = new ArrayList<>();
        subscribeTemplate.forEach(templateInfo -> list.add(convert(templateInfo)));
        return list;
    }

}
