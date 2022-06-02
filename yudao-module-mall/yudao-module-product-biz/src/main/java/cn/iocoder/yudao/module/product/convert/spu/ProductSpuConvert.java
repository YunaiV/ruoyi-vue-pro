package cn.iocoder.yudao.module.product.convert.spu;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;

/**
 * 商品spu Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface ProductSpuConvert {

    ProductSpuConvert INSTANCE = Mappers.getMapper(ProductSpuConvert.class);

    ProductSpuDO convert(ProductSpuCreateReqVO bean);

    ProductSpuDO convert(SpuUpdateReqVO bean);

    SpuRespVO convert(ProductSpuDO bean);

    List<SpuRespVO> convertList(List<ProductSpuDO> list);

    PageResult<SpuRespVO> convertPage(PageResult<ProductSpuDO> page);

    List<SpuExcelVO> convertList02(List<ProductSpuDO> list);

}
