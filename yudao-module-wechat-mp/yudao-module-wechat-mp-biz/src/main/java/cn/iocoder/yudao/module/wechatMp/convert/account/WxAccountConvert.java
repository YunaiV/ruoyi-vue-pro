package cn.iocoder.yudao.module.wechatMp.convert.account;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.wechatMp.controller.admin.account.vo.*;
import cn.iocoder.yudao.module.wechatMp.dal.dataobject.account.WxAccountDO;

/**
 * 公众号账户 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface WxAccountConvert {

    WxAccountConvert INSTANCE = Mappers.getMapper(WxAccountConvert.class);

    WxAccountDO convert(WxAccountCreateReqVO bean);

    WxAccountDO convert(WxAccountUpdateReqVO bean);

    WxAccountRespVO convert(WxAccountDO bean);

    List<WxAccountRespVO> convertList(List<WxAccountDO> list);

    PageResult<WxAccountRespVO> convertPage(PageResult<WxAccountDO> page);

    List<WxAccountExcelVO> convertList02(List<WxAccountDO> list);

}
