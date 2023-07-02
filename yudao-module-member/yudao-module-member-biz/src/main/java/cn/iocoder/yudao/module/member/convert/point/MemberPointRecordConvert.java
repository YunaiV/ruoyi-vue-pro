package cn.iocoder.yudao.module.member.convert.point;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.member.controller.admin.point.vo.recrod.MemberPointRecordCreateReqVO;
import cn.iocoder.yudao.module.member.controller.admin.point.vo.recrod.MemberPointRecordExcelVO;
import cn.iocoder.yudao.module.member.controller.admin.point.vo.recrod.MemberPointRecordRespVO;
import cn.iocoder.yudao.module.member.controller.admin.point.vo.recrod.MemberPointRecordUpdateReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.point.MemberPointRecordDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 用户积分记录 Convert
 *
 * @author QingX
 */
@Mapper
public interface MemberPointRecordConvert {

    MemberPointRecordConvert INSTANCE = Mappers.getMapper(MemberPointRecordConvert.class);

    MemberPointRecordDO convert(MemberPointRecordCreateReqVO bean);

    MemberPointRecordDO convert(MemberPointRecordUpdateReqVO bean);

    MemberPointRecordRespVO convert(MemberPointRecordDO bean);

    List<MemberPointRecordRespVO> convertList(List<MemberPointRecordDO> list);

    PageResult<MemberPointRecordRespVO> convertPage(PageResult<MemberPointRecordDO> page);

    List<MemberPointRecordExcelVO> convertList02(List<MemberPointRecordDO> list);

}
