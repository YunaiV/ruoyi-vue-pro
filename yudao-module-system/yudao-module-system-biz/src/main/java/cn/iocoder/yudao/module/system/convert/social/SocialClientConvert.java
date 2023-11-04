package cn.iocoder.yudao.module.system.convert.social;

import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.api.social.dto.SocialWxJsapiSignatureRespDTO;
import cn.iocoder.yudao.module.system.api.social.dto.SocialWxPhoneNumberInfoRespDTO;
import cn.iocoder.yudao.module.system.controller.admin.socail.vo.client.SocialClientCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.socail.vo.client.SocialClientRespVO;
import cn.iocoder.yudao.module.system.controller.admin.socail.vo.client.SocialClientUpdateReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.social.SocialClientDO;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SocialClientConvert {

    SocialClientConvert INSTANCE = Mappers.getMapper(SocialClientConvert.class);

    SocialWxJsapiSignatureRespDTO convert(WxJsapiSignature bean);

    SocialWxPhoneNumberInfoRespDTO convert(WxMaPhoneNumberInfo bean);

    SocialClientDO convert(SocialClientCreateReqVO bean);

    SocialClientDO convert(SocialClientUpdateReqVO bean);

    SocialClientRespVO convert(SocialClientDO bean);

    List<SocialClientRespVO> convertList(List<SocialClientDO> list);

    PageResult<SocialClientRespVO> convertPage(PageResult<SocialClientDO> page);



}
