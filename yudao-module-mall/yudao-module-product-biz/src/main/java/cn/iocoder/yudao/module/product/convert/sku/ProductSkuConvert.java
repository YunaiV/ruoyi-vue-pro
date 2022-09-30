package cn.iocoder.yudao.module.product.convert.sku;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.api.sku.dto.SkuInfoRespDTO;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuCreateOrUpdateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuRespVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuDetailRespVO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import org.mapstruct.Mapper;
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

    ProductSkuRespVO convert(ProductSkuDO bean);

    List<ProductSkuRespVO> convertList(List<ProductSkuDO> list);

    List<ProductSkuDO> convertSkuDOList(List<ProductSkuCreateOrUpdateReqVO> list);

    ProductSkuRespDTO convert02(ProductSkuDO bean);

    List<ProductSkuRespDTO> convertList02(List<ProductSkuDO> list);

    List<ProductSpuDetailRespVO.Sku> convertList03(List<ProductSkuDO> list);

    List<SkuInfoRespDTO> convertList03(List<ProductSkuDO> list);

}
