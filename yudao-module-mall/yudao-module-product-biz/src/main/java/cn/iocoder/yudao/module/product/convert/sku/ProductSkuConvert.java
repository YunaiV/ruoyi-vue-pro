package cn.iocoder.yudao.module.product.convert.sku;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;

/**
 * 商品sku Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface ProductSkuConvert {

    ProductSkuConvert INSTANCE = Mappers.getMapper(ProductSkuConvert.class);

    ProductSkuDO convert(ProductSkuCreateReqVO bean);

    ProductSkuDO convert(ProductSkuUpdateReqVO bean);

    ProductSkuRespVO convert(ProductSkuDO bean);

    List<ProductSkuRespVO> convertList(List<ProductSkuDO> list);

    PageResult<ProductSkuRespVO> convertPage(PageResult<ProductSkuDO> page);

    List<ProductSkuExcelVO> convertList02(List<ProductSkuDO> list);

}
