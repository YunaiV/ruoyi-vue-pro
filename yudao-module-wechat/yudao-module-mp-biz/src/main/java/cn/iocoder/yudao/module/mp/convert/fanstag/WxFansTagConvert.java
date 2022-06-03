package cn.iocoder.yudao.module.mp.convert.fanstag;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.mp.controller.admin.fanstag.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.fanstag.WxFansTagDO;

/**
 * 粉丝标签 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface WxFansTagConvert {

    WxFansTagConvert INSTANCE = Mappers.getMapper(WxFansTagConvert.class);

    WxFansTagDO convert(WxFansTagCreateReqVO bean);

    WxFansTagDO convert(WxFansTagUpdateReqVO bean);

    WxFansTagRespVO convert(WxFansTagDO bean);

    List<WxFansTagRespVO> convertList(List<WxFansTagDO> list);

    PageResult<WxFansTagRespVO> convertPage(PageResult<WxFansTagDO> page);

    List<WxFansTagExcelVO> convertList02(List<WxFansTagDO> list);

}
