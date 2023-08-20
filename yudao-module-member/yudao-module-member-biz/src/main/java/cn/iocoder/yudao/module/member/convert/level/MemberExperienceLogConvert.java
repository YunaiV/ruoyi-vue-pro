package cn.iocoder.yudao.module.member.convert.level;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.experience.MemberExperienceLogExcelVO;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.experience.MemberExperienceLogRespVO;
import cn.iocoder.yudao.module.member.dal.dataobject.level.MemberExperienceLogDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 会员经验记录 Convert
 *
 * @author owen
 */
@Mapper
public interface MemberExperienceLogConvert {

    MemberExperienceLogConvert INSTANCE = Mappers.getMapper(MemberExperienceLogConvert.class);

    MemberExperienceLogRespVO convert(MemberExperienceLogDO bean);

    List<MemberExperienceLogRespVO> convertList(List<MemberExperienceLogDO> list);

    PageResult<MemberExperienceLogRespVO> convertPage(PageResult<MemberExperienceLogDO> page);

    List<MemberExperienceLogExcelVO> convertList02(List<MemberExperienceLogDO> list);

}
