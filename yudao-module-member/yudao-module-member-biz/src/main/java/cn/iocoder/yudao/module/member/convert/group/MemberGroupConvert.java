package cn.iocoder.yudao.module.member.convert.group;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.controller.admin.group.vo.MemberGroupCreateReqVO;
import cn.iocoder.yudao.module.member.controller.admin.group.vo.MemberGroupRespVO;
import cn.iocoder.yudao.module.member.controller.admin.group.vo.MemberGroupSimpleRespVO;
import cn.iocoder.yudao.module.member.controller.admin.group.vo.MemberGroupUpdateReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.group.MemberGroupDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 用户分组 Convert
 *
 * @author owen
 */
@Mapper
public interface MemberGroupConvert {

    MemberGroupConvert INSTANCE = Mappers.getMapper(MemberGroupConvert.class);

    MemberGroupDO convert(MemberGroupCreateReqVO bean);

    MemberGroupDO convert(MemberGroupUpdateReqVO bean);

    MemberGroupRespVO convert(MemberGroupDO bean);

    List<MemberGroupRespVO> convertList(List<MemberGroupDO> list);

    PageResult<MemberGroupRespVO> convertPage(PageResult<MemberGroupDO> page);

    List<MemberGroupSimpleRespVO> convertSimpleList(List<MemberGroupDO> list);
}
