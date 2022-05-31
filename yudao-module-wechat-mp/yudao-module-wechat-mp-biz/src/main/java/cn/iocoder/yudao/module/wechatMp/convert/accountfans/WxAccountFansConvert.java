package cn.iocoder.yudao.module.wechatMp.convert.accountfans;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.wechatMp.controller.admin.accountfans.vo.*;
import cn.iocoder.yudao.module.wechatMp.dal.dataobject.accountfans.WxAccountFansDO;

/**
 * 微信公众号粉丝 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface WxAccountFansConvert {

    WxAccountFansConvert INSTANCE = Mappers.getMapper(WxAccountFansConvert.class);

    WxAccountFansDO convert(WxAccountFansCreateReqVO bean);

    WxAccountFansDO convert(WxAccountFansUpdateReqVO bean);

    WxAccountFansRespVO convert(WxAccountFansDO bean);

    List<WxAccountFansRespVO> convertList(List<WxAccountFansDO> list);

    PageResult<WxAccountFansRespVO> convertPage(PageResult<WxAccountFansDO> page);

    List<WxAccountFansExcelVO> convertList02(List<WxAccountFansDO> list);

}
