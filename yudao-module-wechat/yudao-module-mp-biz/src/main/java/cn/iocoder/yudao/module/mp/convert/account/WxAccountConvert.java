package cn.iocoder.yudao.module.mp.convert.account;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mp.admin.account.vo.WxAccountCreateReqVO;
import cn.iocoder.yudao.module.mp.admin.account.vo.WxAccountExcelVO;
import cn.iocoder.yudao.module.mp.admin.account.vo.WxAccountRespVO;
import cn.iocoder.yudao.module.mp.admin.account.vo.WxAccountUpdateReqVO;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.WxAccountDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

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
