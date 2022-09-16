package cn.iocoder.yudao.module.product.convert.property;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.property.ProductPropertyAndValueRespVO;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.property.ProductPropertyCreateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.property.ProductPropertyUpdateReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.property.ProductPropertyDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 规格名称 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface ProductPropertyConvert {

    ProductPropertyConvert INSTANCE = Mappers.getMapper(ProductPropertyConvert.class);

    ProductPropertyDO convert(ProductPropertyCreateReqVO bean);

    ProductPropertyDO convert(ProductPropertyUpdateReqVO bean);

    ProductPropertyAndValueRespVO convert(ProductPropertyDO bean);

    List<ProductPropertyAndValueRespVO> convertList(List<ProductPropertyDO> list);

    PageResult<ProductPropertyAndValueRespVO> convertPage(PageResult<ProductPropertyDO> page);

}
