package cn.iocoder.yudao.module.mp.convert.subscribetext;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.mp.controller.admin.subscribetext.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.subscribetext.WxSubscribeTextDO;

/**
 * 关注欢迎语 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface WxSubscribeTextConvert {

    WxSubscribeTextConvert INSTANCE = Mappers.getMapper(WxSubscribeTextConvert.class);

    WxSubscribeTextDO convert(WxSubscribeTextCreateReqVO bean);

    WxSubscribeTextDO convert(WxSubscribeTextUpdateReqVO bean);

    WxSubscribeTextRespVO convert(WxSubscribeTextDO bean);

    List<WxSubscribeTextRespVO> convertList(List<WxSubscribeTextDO> list);

    PageResult<WxSubscribeTextRespVO> convertPage(PageResult<WxSubscribeTextDO> page);

    List<WxSubscribeTextExcelVO> convertList02(List<WxSubscribeTextDO> list);

}
