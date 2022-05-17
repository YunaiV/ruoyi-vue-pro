package cn.iocoder.yudao.module.product.convert.property;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.property.PropertyDO;

/**
 * 规格名称 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface PropertyConvert {

    PropertyConvert INSTANCE = Mappers.getMapper(PropertyConvert.class);

    PropertyDO convert(PropertyCreateReqVO bean);

    PropertyDO convert(PropertyUpdateReqVO bean);

    PropertyRespVO convert(PropertyDO bean);

    List<PropertyRespVO> convertList(List<PropertyDO> list);

    PageResult<PropertyRespVO> convertPage(PageResult<PropertyDO> page);

    List<PropertyExcelVO> convertList02(List<PropertyDO> list);

}
