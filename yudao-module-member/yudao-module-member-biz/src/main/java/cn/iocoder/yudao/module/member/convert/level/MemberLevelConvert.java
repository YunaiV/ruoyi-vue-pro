package cn.iocoder.yudao.module.member.convert.level;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.level.MemberLevelCreateReqVO;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.level.MemberLevelRespVO;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.level.MemberLevelSimpleRespVO;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.level.MemberLevelUpdateReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.level.MemberLevelDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 会员等级 Convert
 *
 * @author owen
 */
@Mapper
public interface MemberLevelConvert {

    MemberLevelConvert INSTANCE = Mappers.getMapper(MemberLevelConvert.class);

    MemberLevelDO convert(MemberLevelCreateReqVO bean);

    MemberLevelDO convert(MemberLevelUpdateReqVO bean);

    MemberLevelRespVO convert(MemberLevelDO bean);

    List<MemberLevelRespVO> convertList(List<MemberLevelDO> list);

    PageResult<MemberLevelRespVO> convertPage(PageResult<MemberLevelDO> page);

    List<MemberLevelSimpleRespVO> convertSimpleList(List<MemberLevelDO> list);
}
