package cn.iocoder.yudao.module.point.convert.signinconfig;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.point.controller.admin.signinconfig.vo.*;
import cn.iocoder.yudao.module.point.dal.dataobject.signinconfig.SignInConfigDO;

/**
 * 积分签到规则 Convert
 *
 * @author QingX
 */
@Mapper
public interface SignInConfigConvert {

    SignInConfigConvert INSTANCE = Mappers.getMapper(SignInConfigConvert.class);

    SignInConfigDO convert(SignInConfigCreateReqVO bean);

    SignInConfigDO convert(SignInConfigUpdateReqVO bean);

    SignInConfigRespVO convert(SignInConfigDO bean);

    List<SignInConfigRespVO> convertList(List<SignInConfigDO> list);

    PageResult<SignInConfigRespVO> convertPage(PageResult<SignInConfigDO> page);

    List<SignInConfigExcelVO> convertList02(List<SignInConfigDO> list);

}
