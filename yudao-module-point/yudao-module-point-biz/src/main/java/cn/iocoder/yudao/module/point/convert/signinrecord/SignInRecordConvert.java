package cn.iocoder.yudao.module.point.convert.signinrecord;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.point.controller.admin.signinrecord.vo.*;
import cn.iocoder.yudao.module.point.dal.dataobject.signinrecord.SignInRecordDO;

/**
 * 用户签到积分 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface SignInRecordConvert {

    SignInRecordConvert INSTANCE = Mappers.getMapper(SignInRecordConvert.class);

    SignInRecordDO convert(SignInRecordCreateReqVO bean);

    SignInRecordDO convert(SignInRecordUpdateReqVO bean);

    SignInRecordRespVO convert(SignInRecordDO bean);

    List<SignInRecordRespVO> convertList(List<SignInRecordDO> list);

    PageResult<SignInRecordRespVO> convertPage(PageResult<SignInRecordDO> page);

    List<SignInRecordExcelVO> convertList02(List<SignInRecordDO> list);

}
