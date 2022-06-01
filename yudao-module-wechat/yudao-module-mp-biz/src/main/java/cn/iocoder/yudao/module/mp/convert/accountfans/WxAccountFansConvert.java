package cn.iocoder.yudao.module.mp.convert.accountfans;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mp.admin.accountfans.vo.WxAccountFansCreateReqVO;
import cn.iocoder.yudao.module.mp.admin.accountfans.vo.WxAccountFansExcelVO;
import cn.iocoder.yudao.module.mp.admin.accountfans.vo.WxAccountFansRespVO;
import cn.iocoder.yudao.module.mp.admin.accountfans.vo.WxAccountFansUpdateReqVO;
import cn.iocoder.yudao.module.mp.dal.dataobject.accountfans.WxAccountFansDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

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
