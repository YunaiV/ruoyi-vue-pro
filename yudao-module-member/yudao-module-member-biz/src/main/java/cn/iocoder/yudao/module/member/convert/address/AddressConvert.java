package cn.iocoder.yudao.module.member.convert.address;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.api.address.dto.AddressRespDTO;
import cn.iocoder.yudao.module.member.controller.app.address.vo.AppAddressCreateReqVO;
import cn.iocoder.yudao.module.member.controller.app.address.vo.AppAddressRespVO;
import cn.iocoder.yudao.module.member.controller.app.address.vo.AppAddressUpdateReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.address.AddressDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 用户收件地址 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface AddressConvert {

    AddressConvert INSTANCE = Mappers.getMapper(AddressConvert.class);

    AddressDO convert(AppAddressCreateReqVO bean);

    AddressDO convert(AppAddressUpdateReqVO bean);

    AppAddressRespVO convert(AddressDO bean);

    List<AppAddressRespVO> convertList(List<AddressDO> list);

    PageResult<AppAddressRespVO> convertPage(PageResult<AddressDO> page);

    AddressRespDTO convert02(AddressDO bean);

}
