package cn.iocoder.yudao.adminserver.modules.bpm.convert.oa;

import java.util.*;

import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.leave.OALeaveDO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.oa.vo.OALeaveCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.oa.vo.OALeaveExcelVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.oa.vo.OALeaveRespVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.oa.vo.OALeaveUpdateReqVO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

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
