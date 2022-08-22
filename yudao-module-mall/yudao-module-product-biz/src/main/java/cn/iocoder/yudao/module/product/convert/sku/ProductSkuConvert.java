package cn.iocoder.yudao.module.product.convert.sku;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuCreateOrUpdateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuExcelVO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuRespVO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuUpdateReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 商品sku Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface ProductSkuConvert {

    ProductSkuConvert INSTANCE = Mappers.getMapper(ProductSkuConvert.class);

    ProductSkuDO convert(ProductSkuCreateOrUpdateReqVO bean);

    ProductSkuDO convert(ProductSkuUpdateReqVO bean);

    @Mapping(source = "properties", target = "properties")
    ProductSkuRespVO convert(ProductSkuDO bean);

    List<ProductSkuRespVO> convertList(List<ProductSkuDO> list);

    List<ProductSkuDO> convertSkuDOList(List<ProductSkuCreateOrUpdateReqVO> list);

    PageResult<ProductSkuRespVO> convertPage(PageResult<ProductSkuDO> page);

    List<ProductSkuExcelVO> convertList02(List<ProductSkuDO> list);

}
