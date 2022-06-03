package cn.iocoder.yudao.module.mp.convert.accountfanstag;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.mp.controller.admin.accountfanstag.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.accountfanstag.WxAccountFansTagDO;

/**
 * 粉丝标签关联 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface WxAccountFansTagConvert {

    WxAccountFansTagConvert INSTANCE = Mappers.getMapper(WxAccountFansTagConvert.class);

    WxAccountFansTagDO convert(WxAccountFansTagCreateReqVO bean);

    WxAccountFansTagDO convert(WxAccountFansTagUpdateReqVO bean);

    WxAccountFansTagRespVO convert(WxAccountFansTagDO bean);

    List<WxAccountFansTagRespVO> convertList(List<WxAccountFansTagDO> list);

    PageResult<WxAccountFansTagRespVO> convertPage(PageResult<WxAccountFansTagDO> page);

    List<WxAccountFansTagExcelVO> convertList02(List<WxAccountFansTagDO> list);

}
