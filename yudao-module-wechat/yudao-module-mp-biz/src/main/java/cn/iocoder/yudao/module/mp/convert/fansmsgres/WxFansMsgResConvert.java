package cn.iocoder.yudao.module.mp.convert.fansmsgres;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.mp.controller.admin.fansmsgres.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.fansmsgres.WxFansMsgResDO;

/**
 * 回复粉丝消息历史表  Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface WxFansMsgResConvert {

    WxFansMsgResConvert INSTANCE = Mappers.getMapper(WxFansMsgResConvert.class);

    WxFansMsgResDO convert(WxFansMsgResCreateReqVO bean);

    WxFansMsgResDO convert(WxFansMsgResUpdateReqVO bean);

    WxFansMsgResRespVO convert(WxFansMsgResDO bean);

    List<WxFansMsgResRespVO> convertList(List<WxFansMsgResDO> list);

    PageResult<WxFansMsgResRespVO> convertPage(PageResult<WxFansMsgResDO> page);

    List<WxFansMsgResExcelVO> convertList02(List<WxFansMsgResDO> list);

}
