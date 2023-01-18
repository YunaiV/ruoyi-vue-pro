package cn.iocoder.yudao.module.mp.convert.menu;

import cn.iocoder.yudao.module.mp.controller.admin.menu.vo.MpMenuRespVO;
import cn.iocoder.yudao.module.mp.controller.admin.menu.vo.MpMenuSaveReqVO;
import cn.iocoder.yudao.module.mp.dal.dataobject.menu.MpMenuDO;
import cn.iocoder.yudao.module.mp.service.message.bo.MpMessageSendOutReqBO;
import me.chanjar.weixin.common.bean.menu.WxMenuButton;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface MpMenuConvert {

    MpMenuConvert INSTANCE = Mappers.getMapper(MpMenuConvert.class);

    MpMenuRespVO convert(MpMenuDO bean);

    List<MpMenuRespVO> convertList(List<MpMenuDO> list);

    @Mappings({
            @Mapping(source = "menu.appId", target = "appId"),
            @Mapping(source = "menu.replyMessageType", target = "type"),
            @Mapping(source = "menu.replyContent", target = "content"),
            @Mapping(source = "menu.replyMediaId", target = "mediaId"),
            @Mapping(source = "menu.replyThumbMediaId", target = "thumbMediaId"),
            @Mapping(source = "menu.replyTitle", target = "title"),
            @Mapping(source = "menu.replyDescription", target = "description"),
            @Mapping(source = "menu.replyArticles", target = "articles"),
            @Mapping(source = "menu.replyMusicUrl", target = "musicUrl"),
            @Mapping(source = "menu.replyHqMusicUrl", target = "hqMusicUrl"),
    })
    MpMessageSendOutReqBO convert(String openid, MpMenuDO menu);

    List<WxMenuButton> convert(List<MpMenuSaveReqVO.Menu> list);

    @Mappings({
            @Mapping(source = "menuKey", target = "key"),
            @Mapping(source = "children", target = "subButtons"),
    })
    WxMenuButton convert(MpMenuSaveReqVO.Menu bean);

    MpMenuDO convert02(MpMenuSaveReqVO.Menu menu);

}
