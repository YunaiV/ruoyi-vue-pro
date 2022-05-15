package cn.iocoder.yudao.module.product.convert.attrkey;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.product.controller.admin.attrkey.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.attrkey.AttrKeyDO;

/**
 * 规格名称 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface AttrKeyConvert {

    AttrKeyConvert INSTANCE = Mappers.getMapper(AttrKeyConvert.class);

    AttrKeyDO convert(AttrKeyCreateReqVO bean);

    AttrKeyDO convert(AttrKeyUpdateReqVO bean);

    AttrKeyRespVO convert(AttrKeyDO bean);

    List<AttrKeyRespVO> convertList(List<AttrKeyDO> list);

    PageResult<AttrKeyRespVO> convertPage(PageResult<AttrKeyDO> page);

    List<AttrKeyExcelVO> convertList02(List<AttrKeyDO> list);

}
