package cn.iocoder.yudao.module.member.convert.user;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.member.controller.admin.user.vo.MemberUserRespVO;
import cn.iocoder.yudao.module.member.controller.admin.user.vo.MemberUserUpdateReqVO;
import cn.iocoder.yudao.module.member.controller.app.user.vo.AppMemberUserInfoRespVO;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Mapper
public interface MemberUserConvert {

    MemberUserConvert INSTANCE = Mappers.getMapper(MemberUserConvert.class);

    AppMemberUserInfoRespVO convert(MemberUserDO bean);

    MemberUserRespDTO convert2(MemberUserDO bean);

    List<MemberUserRespDTO> convertList2(List<MemberUserDO> list);

    MemberUserDO convert(MemberUserUpdateReqVO bean);

    PageResult<MemberUserRespVO> convertPage(PageResult<MemberUserDO> page);

    MemberUserRespVO convert03(MemberUserDO bean);

    default PageResult<MemberUserRespVO> convertPage(PageResult<MemberUserDO> pageResult,
                                                     Map<Long, String> tagMap) {
        PageResult<MemberUserRespVO> result = convertPage(pageResult);
        for (MemberUserRespVO vo : result.getList()) {
            vo.setTagNames(convertList(vo.getTagIds(), tagMap::get));
        }
        return result;
    }
}
