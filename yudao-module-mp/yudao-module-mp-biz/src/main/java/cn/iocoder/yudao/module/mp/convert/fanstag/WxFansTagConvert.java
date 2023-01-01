package cn.iocoder.yudao.module.mp.convert.fanstag;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import me.chanjar.weixin.mp.bean.tag.WxUserTag;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.mp.controller.admin.fanstag.vo.*;

/**
 * 粉丝标签 Convert
 *
 * @author fengdan
 */
@Mapper
public interface WxFansTagConvert {

    WxFansTagConvert INSTANCE = Mappers.getMapper(WxFansTagConvert.class);

    WxUserTag convert(FansTagCreateReqVO bean);

    WxUserTag convert(FansTagUpdateReqVO bean);

    FansTagRespVO convert(WxUserTag bean);

    List<FansTagRespVO> convertList(List<WxUserTag> list);

    PageResult<FansTagRespVO> convertPage(PageResult<WxUserTag> page);

    List<FansTagExcelVO> convertList02(List<WxUserTag> list);

}
