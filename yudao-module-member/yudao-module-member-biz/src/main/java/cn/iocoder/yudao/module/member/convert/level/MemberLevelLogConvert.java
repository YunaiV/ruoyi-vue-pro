package cn.iocoder.yudao.module.member.convert.level;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.log.MemberLevelLogExcelVO;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.log.MemberLevelLogRespVO;
import cn.iocoder.yudao.module.member.dal.dataobject.level.MemberLevelLogDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 会员等级记录 Convert
 *
 * @author owen
 */
@Mapper
public interface MemberLevelLogConvert {

    MemberLevelLogConvert INSTANCE = Mappers.getMapper(MemberLevelLogConvert.class);

    MemberLevelLogRespVO convert(MemberLevelLogDO bean);

    List<MemberLevelLogRespVO> convertList(List<MemberLevelLogDO> list);

    PageResult<MemberLevelLogRespVO> convertPage(PageResult<MemberLevelLogDO> page);

    List<MemberLevelLogExcelVO> convertList02(List<MemberLevelLogDO> list);

}
