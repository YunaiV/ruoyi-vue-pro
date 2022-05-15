package cn.iocoder.yudao.module.product.convert.attrvalue;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.product.controller.admin.attrvalue.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.attrvalue.AttrValueDO;

/**
 * 规格值 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface AttrValueConvert {

    AttrValueConvert INSTANCE = Mappers.getMapper(AttrValueConvert.class);

    AttrValueDO convert(AttrValueCreateReqVO bean);

    AttrValueDO convert(AttrValueUpdateReqVO bean);

    AttrValueRespVO convert(AttrValueDO bean);

    List<AttrValueRespVO> convertList(List<AttrValueDO> list);

    PageResult<AttrValueRespVO> convertPage(PageResult<AttrValueDO> page);

    List<AttrValueExcelVO> convertList02(List<AttrValueDO> list);

}
