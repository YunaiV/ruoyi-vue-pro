package cn.iocoder.yudao.module.mp.convert.message;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.mp.dal.dataobject.account.MpAccountDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.message.MpMessageDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.user.MpUserDO;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.mp.controller.admin.message.vo.*;

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
}
