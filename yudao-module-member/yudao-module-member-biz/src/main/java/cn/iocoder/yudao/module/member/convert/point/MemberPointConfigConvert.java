package cn.iocoder.yudao.module.member.convert.point;

import cn.iocoder.yudao.module.member.controller.admin.point.vo.config.MemberPointConfigRespVO;
import cn.iocoder.yudao.module.member.controller.admin.point.vo.config.MemberPointConfigSaveReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.point.MemberPointConfigDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 会员积分配置 Convert
 *
 * @author QingX
 */
@Mapper
public interface MemberPointConfigConvert {

    MemberPointConfigConvert INSTANCE = Mappers.getMapper(MemberPointConfigConvert.class);

    MemberPointConfigRespVO convert(MemberPointConfigDO bean);

    MemberPointConfigDO convert(MemberPointConfigSaveReqVO bean);

}
