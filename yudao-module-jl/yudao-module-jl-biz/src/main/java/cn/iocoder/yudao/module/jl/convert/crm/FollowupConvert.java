package cn.iocoder.yudao.module.jl.convert.crm;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.FollowupDO;

/**
 * 销售线索跟进，可以是跟进客户，也可以是跟进线索 Convert
 *
 * @author 惟象科技
 */
@Mapper
public interface FollowupConvert {

    FollowupConvert INSTANCE = Mappers.getMapper(FollowupConvert.class);

    FollowupDO convert(FollowupCreateReqVO bean);

    FollowupDO convert(FollowupUpdateReqVO bean);

    FollowupRespVO convert(FollowupDO bean);

    List<FollowupRespVO> convertList(List<FollowupDO> list);

    PageResult<FollowupRespVO> convertPage(PageResult<FollowupDO> page);

    List<FollowupExcelVO> convertList02(List<FollowupDO> list);

}
