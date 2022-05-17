package cn.iocoder.yudao.module.product.convert.propertyvalue;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.product.controller.admin.propertyvalue.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.propertyvalue.PropertyValueDO;

/**
 * 规格值 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface PropertyValueConvert {

    PropertyValueConvert INSTANCE = Mappers.getMapper(PropertyValueConvert.class);

    PropertyValueDO convert(PropertyValueCreateReqVO bean);

    PropertyValueDO convert(PropertyValueUpdateReqVO bean);

    PropertyValueRespVO convert(PropertyValueDO bean);

    List<PropertyValueRespVO> convertList(List<PropertyValueDO> list);

    PageResult<PropertyValueRespVO> convertPage(PageResult<PropertyValueDO> page);

    List<PropertyValueExcelVO> convertList02(List<PropertyValueDO> list);

}
