package cn.iocoder.yudao.module.mp.convert.menu;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.mp.service.message.bo.MpMessageSendOutReqBO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.mp.controller.admin.menu.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.menu.MpMenuDO;

@Mapper
public interface MpMenuConvert {

    MpMenuConvert INSTANCE = Mappers.getMapper(MpMenuConvert.class);

    MpMenuDO convert(MpMenuSaveReqVO bean);

    MpMenuRespVO convert(MpMenuDO bean);

    @Mappings({
            @Mapping(source = "menu.appId", target = "appId"),
            @Mapping(source = "menu.replyMessageType", target = "type"),
            @Mapping(source = "menu.replyContent", target = "content"),
            @Mapping(source = "menu.replyMediaId", target = "mediaId"),
            @Mapping(source = "menu.replyMediaUrl", target = "mediaUrl"),
            @Mapping(source = "menu.replyTitle", target = "title"),
            @Mapping(source = "menu.replyDescription", target = "description"),
            @Mapping(source = "menu.replyArticles", target = "articles"),
    })
    MpMessageSendOutReqBO convert(String openid, MpMenuDO menu);

}
