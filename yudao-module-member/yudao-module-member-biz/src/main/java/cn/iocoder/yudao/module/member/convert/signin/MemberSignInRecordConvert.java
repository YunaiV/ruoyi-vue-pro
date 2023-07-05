package cn.iocoder.yudao.module.member.convert.signin;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.controller.admin.signin.vo.MemberSignInRecordCreateReqVO;
import cn.iocoder.yudao.module.member.controller.admin.signin.vo.MemberSignInRecordRespVO;
import cn.iocoder.yudao.module.member.controller.admin.signin.vo.MemberSignInRecordUpdateReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.signin.MemberSignInRecordDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 用户签到积分 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface MemberSignInRecordConvert {

    MemberSignInRecordConvert INSTANCE = Mappers.getMapper(MemberSignInRecordConvert.class);

    MemberSignInRecordDO convert(MemberSignInRecordCreateReqVO bean);

    MemberSignInRecordDO convert(MemberSignInRecordUpdateReqVO bean);

    MemberSignInRecordRespVO convert(MemberSignInRecordDO bean);

    PageResult<MemberSignInRecordRespVO> convertPage(PageResult<MemberSignInRecordDO> page);

}
