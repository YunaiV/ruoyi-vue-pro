package cn.iocoder.yudao.adminserver.modules.activiti.convert.oa;

import java.util.*;

import cn.iocoder.yudao.adminserver.modules.activiti.dal.dataobject.oa.OALeaveDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.adminserver.modules.activiti.controller.oa.vo.*;

/**
 * 请假申请 Convert
 *
 * @author 芋艿
 */
@Mapper
public interface OALeaveConvert {

    OALeaveConvert INSTANCE = Mappers.getMapper(OALeaveConvert.class);

    OALeaveDO convert(OALeaveCreateReqVO bean);

    OALeaveDO convert(OALeaveUpdateReqVO bean);

    OALeaveRespVO convert(OALeaveDO bean);

    List<OALeaveRespVO> convertList(List<OALeaveDO> list);

    PageResult<OALeaveRespVO> convertPage(PageResult<OALeaveDO> page);

    List<OALeaveExcelVO> convertList02(List<OALeaveDO> list);

}
