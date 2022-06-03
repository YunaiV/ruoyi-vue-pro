package cn.iocoder.yudao.module.mp.convert.menu;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.mp.controller.admin.menu.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.menu.WxMenuDO;

/**
 * 微信菜单 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface WxMenuConvert {

    WxMenuConvert INSTANCE = Mappers.getMapper(WxMenuConvert.class);

    WxMenuDO convert(WxMenuCreateReqVO bean);

    WxMenuDO convert(WxMenuUpdateReqVO bean);

    WxMenuRespVO convert(WxMenuDO bean);

    List<WxMenuRespVO> convertList(List<WxMenuDO> list);

    PageResult<WxMenuRespVO> convertPage(PageResult<WxMenuDO> page);

    List<WxMenuExcelVO> convertList02(List<WxMenuDO> list);

}
