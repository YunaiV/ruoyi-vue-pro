package cn.iocoder.yudao.module.system.convert.social;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.api.social.dto.SocialUserBindReqDTO;
import cn.iocoder.yudao.module.system.api.social.dto.SocialUserUnbindReqDTO;
import cn.iocoder.yudao.module.system.controller.admin.socail.vo.SocialUserBindReqVO;
import cn.iocoder.yudao.module.system.controller.admin.socail.vo.SocialUserUnbindReqVO;
import cn.iocoder.yudao.module.system.controller.admin.socail.vo.user.SocialUserRespVO;
import cn.iocoder.yudao.module.system.dal.dataobject.social.SocialUserDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SocialUserConvert {

    SocialUserConvert INSTANCE = Mappers.getMapper(SocialUserConvert.class);

    @Mapping(target = "socialType", source = "reqVO.type")
    SocialUserBindReqDTO convert(Long userId, Integer userType, SocialUserBindReqVO reqVO);

    SocialUserUnbindReqDTO convert(Long userId, Integer userType, SocialUserUnbindReqVO reqVO);

    SocialUserRespVO convert(SocialUserDO bean);

    List<SocialUserRespVO> convertList(List<SocialUserDO> list);

    PageResult<SocialUserRespVO> convertPage(PageResult<SocialUserDO> page);

}
