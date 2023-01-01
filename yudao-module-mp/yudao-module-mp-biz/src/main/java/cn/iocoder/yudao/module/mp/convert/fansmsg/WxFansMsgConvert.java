package cn.iocoder.yudao.module.mp.convert.fansmsg;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.mp.controller.admin.fansmsg.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.fansmsg.WxFansMsgDO;

/**
 * 粉丝消息表  Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface WxFansMsgConvert {

    WxFansMsgConvert INSTANCE = Mappers.getMapper(WxFansMsgConvert.class);

    WxFansMsgDO convert(WxFansMsgCreateReqVO bean);

    WxFansMsgDO convert(WxFansMsgUpdateReqVO bean);

    WxFansMsgRespVO convert(WxFansMsgDO bean);

    List<WxFansMsgRespVO> convertList(List<WxFansMsgDO> list);

    PageResult<WxFansMsgRespVO> convertPage(PageResult<WxFansMsgDO> page);

    List<WxFansMsgExcelVO> convertList02(List<WxFansMsgDO> list);

}
