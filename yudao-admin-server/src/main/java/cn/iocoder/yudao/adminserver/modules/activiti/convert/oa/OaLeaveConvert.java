package cn.iocoder.yudao.adminserver.modules.activiti.convert.oa;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.adminserver.modules.activiti.controller.oa.vo.*;
import cn.iocoder.yudao.adminserver.modules.activiti.dal.dataobject.oa.OaLeaveDO;

/**
 * 请假申请 Convert
 *
 * @author 芋艿
 */
@Mapper
public interface OaLeaveConvert {

    OaLeaveConvert INSTANCE = Mappers.getMapper(OaLeaveConvert.class);

    OaLeaveDO convert(OaLeaveCreateReqVO bean);

    OaLeaveDO convert(OaLeaveUpdateReqVO bean);

    OaLeaveRespVO convert(OaLeaveDO bean);

    List<OaLeaveRespVO> convertList(List<OaLeaveDO> list);

    PageResult<OaLeaveRespVO> convertPage(PageResult<OaLeaveDO> page);

    List<OaLeaveExcelVO> convertList02(List<OaLeaveDO> list);

}
