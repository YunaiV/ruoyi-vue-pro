package cn.iocoder.yudao.module.mp.convert.receivetext;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.mp.controller.admin.receivetext.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.receivetext.WxReceiveTextDO;

/**
 * 回复关键字 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface WxReceiveTextConvert {

    WxReceiveTextConvert INSTANCE = Mappers.getMapper(WxReceiveTextConvert.class);

    WxReceiveTextDO convert(WxReceiveTextCreateReqVO bean);

    WxReceiveTextDO convert(WxReceiveTextUpdateReqVO bean);

    WxReceiveTextRespVO convert(WxReceiveTextDO bean);

    List<WxReceiveTextRespVO> convertList(List<WxReceiveTextDO> list);

    PageResult<WxReceiveTextRespVO> convertPage(PageResult<WxReceiveTextDO> page);

    List<WxReceiveTextExcelVO> convertList02(List<WxReceiveTextDO> list);

}
